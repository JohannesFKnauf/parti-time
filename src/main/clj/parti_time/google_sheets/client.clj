(ns parti-time.google-sheets.client
  (:import (com.google.api.client.extensions.java6.auth.oauth2 AuthorizationCodeInstalledApp)
           (com.google.api.client.extensions.jetty.auth.oauth2 LocalServerReceiver$Builder)
           (com.google.api.client.auth.oauth2 Credential)
           (com.google.api.client.googleapis.auth.oauth2 GoogleAuthorizationCodeFlow$Builder
                                                         GoogleClientSecrets)
           (com.google.api.client.googleapis.javanet GoogleNetHttpTransport)
           (com.google.api.client.json.gson GsonFactory)
           (com.google.api.client.util.store FileDataStoreFactory)
           (com.google.api.services.sheets.v4 Sheets
                                              SheetsScopes
                                              Sheets$Builder)
           (com.google.api.services.sheets.v4.model AppendValuesResponse
                                                    ValueRange))
  (:require [clojure.java.io :as io]))

(defn default-http-transport []
  (GoogleNetHttpTransport/newTrustedTransport))

(defn default-json-factory []
  (GsonFactory/getDefaultInstance))

(def default-data-store-factory
  (->> "/.cache/parti-time/tokens"
       (str (System/getProperty "user.home"))
       (io/file)
       (FileDataStoreFactory.)))

(defn get-client-secrets []
  (->> "/.config/parti-time/credentials.json"
       (str (System/getProperty "user.home"))
       (io/input-stream)
       (io/reader)
       (GoogleClientSecrets/load (default-json-factory))))

(defn get-credentials
  "Get OAuth credentials from Google by performing the Authorization Flow.

  The user will have to confirm access in a browser window. parti-time
  will start a server listening on localhost and the browser window
  will redirect the user to this local server, to return the
  authorization result.

  Requires the app credentials JSON file. The app credentials are not
  secret, since they are only used for rate limiting, not for access
  control. They can be persisted safely."
  []
  (let [flow (.. (GoogleAuthorizationCodeFlow$Builder. (default-http-transport)
                                                       (default-json-factory)
                                                       (get-client-secrets)
                                                       [(SheetsScopes/SPREADSHEETS)])
                 (setDataStoreFactory ^FileDataStoreFactory default-data-store-factory)
                 (setAccessType "offline")
                 (build))
        receiver (.. (LocalServerReceiver$Builder.)
                     (setPort -1)  ; find unused port
                     (build))
        app (AuthorizationCodeInstalledApp. flow receiver)]
    (.authorize app "user")))

(defn sheets-service ^Sheets [^Credential credentials]
  (.. (Sheets$Builder. (default-http-transport)
                       (default-json-factory)
                       credentials)
      (setApplicationName "parti-time CLI")
      (build)))

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
  [credentials sheet-id range]
  (let [service (sheets-service credentials)
        request (.. service
                    (spreadsheets)
                    (values)
                    (get sheet-id
                         range))
        ^ValueRange response (. request execute)]
    {:values (.. response
                 getValues)
     :range (.. response
                getRange)}))

(defn get-last-row
  "get-last-row returns the last row in a sheet.

  It does so by an API trick: When appending to a sheet, the Google
  Sheets API returns the range that got updated. When trying an empty
  append, this will reveal exactly the range that we desire."
  ([credentials sheet-id]
   (get-last-row credentials sheet-id "A:Z"))
  ([credentials sheet-id search-range]
   (let [service (sheets-service credentials)
         empty-value-range (ValueRange.)
         request (.. service
                     (spreadsheets)
                     (values)
                     (append sheet-id
                             search-range
                             empty-value-range)
                     (setValueInputOption "USER_ENTERED"))
         ^AppendValuesResponse response (. request execute)
         first-empty-cell (.. response
                              getUpdates
                              getUpdatedRange)
         [_ _ _ first-empty-row-number] (re-find #"(?<sheet>[^!]*)!(?<column>[A-Z]*)(?<row>[0-9]*)" first-empty-cell) ;; TODO replace by range functions
         first-empty-row (Integer/parseInt first-empty-row-number)
         last-row (- first-empty-row 1)
         last-row-range (str "A" last-row ":F" last-row)] ;; TODO make generic depend on search-range
     (get-cells credentials sheet-id last-row-range))))

(defn append-rows
  "append rows to a Google sheet, identified by sheet-id.

  The rows is given as a nested List, e.g. [[1 2 3] [4 5 6]]."
  ([credentials sheet-id rows]
   (append-rows credentials sheet-id rows "A:Z"))
  ([credentials sheet-id rows input-range]
   (let [service (sheets-service credentials)
         value-range (.. (ValueRange.)
                         (setValues rows))
         request (.. service
                     (spreadsheets)
                     (values)
                     (append sheet-id
                             input-range
                             value-range)
                    (setValueInputOption "USER_ENTERED"))
         ^AppendValuesResponse response (. request execute)]
     (get-in response ["updates" "updatedRange"]))))

;; Known limitation: No explicit login/logout commands yet
;;  check if logged in: (.. default-data-store-factory (getDataStore "StoredCredential"))
;;  logoff: remove credentials (.. default-data-store-factory (getDataStore "StoredCredential") (clear))
;;  login: just get and store credentials
;;  login --force: logout+login
;;  login --ttl: not possible for Google's API limitations
;;  -> running a command without being logged in could use an in-memory data store option, i.e. forcing auth on every command
;;  -> https://cloud.google.com/java/docs/reference/google-http-client/latest/com.google.api.client.util.store.MemoryDataStoreFactory?hl=en#com_google_api_client_util_store_MemoryDataStoreFactory_getDefaultInstance__
