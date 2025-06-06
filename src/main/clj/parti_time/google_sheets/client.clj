(ns parti-time.google-sheets.client
  (:require [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [happygapi.sheets.spreadsheets :as gsheets]
            [happy.oauth2-credentials :as credentials]))

(defn google-client-secret-path []
  (str (System/getProperty "user.home") "/.config/parti-time/credentials.json"))

(defn credentials-cache-path []
  (str (System/getProperty "user.home") "/.cache/parti-time/tokens"))

(def *credentials-cache
  (atom nil))

(defn fetch-credentials [user]
  (or (get @*credentials-cache user)
      (let [credentials-file (io/file (credentials-cache-path) (str user ".edn"))]
        (when (.exists credentials-file)
          (edn/read-string (slurp credentials-file))))))

(defn save-credentials [user new-credentials]
  (when (not= @*credentials-cache new-credentials)
    (swap! *credentials-cache assoc user new-credentials)
    (spit (io/file (doto (io/file (credentials-cache-path)) (.mkdirs))
                   (str user ".edn"))
          new-credentials)))

(defn get-client-secret []
  (-> (google-client-secret-path)
      slurp
      (json/parse-string true)
      (get :installed)
      (update-in [:redirect_uris 0] #(clojure.string/replace % #"^(http://localhost)(:\d+)?" "$1"))))

(defn init! []
  (credentials/init! (get-client-secret)
                     ["https://www.googleapis.com/auth/spreadsheets"]
                     fetch-credentials
                     save-credentials))

(defn get-cells
  "get-cells returns cells in a sheet as plain data.

  Trailing empty rows will be omitted. Trailing empty columns will
  also be omitted in each row, i.e. rows can have different length.
  Empty rows will be empty vectors. Empty columns will be empty
  Strings.

  The range returns all cells with the range end included. E.g. 3:3
  will return the third row. 1:3 and 3:1 are equivalent and both
  return the first until including the third row. A1:B3 returns the
  first 3 rows containing the first 2 columns."
  [sheet-id range]
  (gsheets/values-get$ (credentials/auth!)
                       {:spreadsheetId sheet-id
                        :range range}))

(defn get-last-row
  "get-last-row returns the last row in a sheet.

  It does so by an API trick: When appending to a sheet, the Google
  Sheets API returns the range that got updated. When trying an empty
  append, this will reveal exactly the range that we desire."
  ([sheet-id]
   (get-last-row sheet-id "A:Z"))
  ([sheet-id search-range]
   (let [response (gsheets/values-append$ (credentials/auth!)
                           {:spreadsheetId sheet-id
                            :range search-range
                            :valueInputOption "USER_ENTERED"}
                           {:values [[]]})
         first-empty-cell (get-in response [:updates :updatedRange])
         [_ _ _ first-empty-row-number] (re-find #"(?<sheet>[^!]*)!(?<column>[A-Z]*)(?<row>[0-9]*)" first-empty-cell)
         ;; TODO replace by range functions
         first-empty-row (Integer/parseInt first-empty-row-number)
         last-row (- first-empty-row 1)
         last-row-range (str "A" last-row ":F" last-row)] ;; TODO make generic depend on search-range
     (get-cells sheet-id last-row-range))))

(defn append-rows
  "append rows to a Google sheet, identified by sheet-id.

  The rows is given as a nested List, e.g. [[1 2 3] [4 5 6]]."
  ([sheet-id rows]
   (append-rows sheet-id rows "A:Z"))
  ([sheet-id rows input-range]
   (let [response (gsheets/values-append$ (credentials/auth!)
                           {:spreadsheetId sheet-id
                            :range input-range
                            :valueInputOption "USER_ENTERED"}
                           {:values rows})]
     (get-in response [:updates :updatedRange]))))

(defn range->google-grid-range
  "Google sheets's RepeatCellRequest requires a GridRange JSON structure.

  In contrast to parti-time.google-sheets.ranges, Google's GridRange
   - starts counting from 0
   - uses [inclusive-start, exclusive-end) ranges
   - uses internal ids to identity sheets (tabs) instead of sheet names"
  [sheet-id {:keys [sheet-name
                    start-col
                    start-row
                    end-col
                    end-row]}]
  (let [spreadsheet (gsheets/get$ (credentials/auth!)
                                  {:spreadsheetId sheet-id})
        sub-sheet-id (if (nil? sheet-name)
                       0
                       (->> spreadsheet
                            :sheets
                            (filter #(= sheet-name (get-in %1 [:properties :title]) ))
                            first
                            :properties
                            :sheetId))]
    {:sheetId sub-sheet-id
     :startRowIndex (dec start-row) ; correct for starting at 0 vs. 1
     :startColumnIndex (dec start-col)
     :endRowIndex end-row ; start-correcture (-1) vs. exclusive-range (+1) are cancelling each other out
     :endColumnIndex end-col}))

(defn set-formula
  "Fill a range with a given formula.

  The formula is applied to the upper left cell and cell references
  are incremented just like for user-entered formulas. You can use
  absolute references (prefixed by $) as usual.
  
  https://developers.google.com/workspace/sheets/api/reference/rest/v4/spreadsheets/request#repeatcellrequest

  HappyGAPI will throw an exception, when the API response contains an error."
  [sheet-id range formula]
  (gsheets/batchUpdate$ (credentials/auth!)
                        {:spreadsheetId sheet-id}
                        {:requests
                         [{:repeatCell
                           {:range (parti-time.google-sheets.client/range->google-grid-range sheet-id range)
                            :cell {:userEnteredValue
                                   {:formulaValue formula}}
                            :fields "userEnteredValue.formulaValue"}}]}))

(defn set-number-format
  "update the given range to a custom number format

  You can find a list of format-types in
  https://developers.google.com/workspace/sheets/api/reference/rest/v4/spreadsheets/cells#numberformattype

  HappyGAPI will throw an exception, when the API response contains an error."
  [sheet-id range format-type format-pattern]
  (gsheets/batchUpdate$ (credentials/auth!)
                        {:spreadsheetId sheet-id}
                        {:requests
                         [{:repeatCell
                           {:range (parti-time.google-sheets.client/range->google-grid-range sheet-id range)
                            :cell {:userEnteredFormat
                                   {:numberFormat
                                    {:type format-type
                                     :pattern format-pattern}}}
                            :fields "userEnteredFormat.numberFormat"}}]}))

;; Known limitation: No explicit login/logout commands yet
;;  check if logged in: (.. default-data-store-factory (getDataStore "StoredCredential"))
;;  logoff: remove credentials (.. default-data-store-factory (getDataStore "StoredCredential") (clear))
;;  login: just get and store credentials
;;  login --force: logout+login
;;  login --ttl: not possible for Google's API limitations
;;  -> running a command without being logged in could use an in-memory data store option, i.e. forcing auth on every command
;;  -> https://cloud.google.com/java/docs/reference/google-http-client/latest/com.google.api.client.util.store.MemoryDataStoreFactory?hl=en#com_google_api_client_util_store_MemoryDataStoreFactory_getDefaultInstance__
;; Known workaround: To logout, delete ~/.cache/parti-time/tokens/user.edn


;; Known limitation: We are still stuck on https://github.com/timothypratley/happygapi -- last time we checked, https://github.com/timothypratley/happyapi wasn't ready for our use case, because the credential store/retrieve could not easily be overridden
