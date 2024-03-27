# Where can I learn about the Google Sheets API?

Start with the [Java Quickstart Guide for the Google Sheets API](https://developers.google.com/sheets/api/quickstart/java).

Learn more about some Google's Java API packages we use:
* https://cloud.google.com/java/docs/reference/google-oauth-client/latest/com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
* https://cloud.google.com/java/docs/reference/google-oauth-client/latest/com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver.Builder
* https://cloud.google.com/java/docs/reference/google-oauth-client/latest/com.google.api.client.auth.oauth2.Credential
* https://cloud.google.com/java/docs/reference/google-api-client/latest/com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder
* https://cloud.google.com/java/docs/reference/google-api-client/latest/com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
* https://cloud.google.com/java/docs/reference/google-api-client/latest/com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
* https://cloud.google.com/java/docs/reference/google-http-client/latest/com.google.api.client.json.gson.GsonFactory
* https://cloud.google.com/java/docs/reference/google-http-client/latest/com.google.api.client.util.store.FileDataStoreFactory
* https://googleapis.dev/java/google-api-services-sheets/latest/com/google/api/services/sheets/v4/model/AppendValuesResponse.html
* https://googleapis.dev/java/google-api-services-sheets/latest/com/google/api/services/sheets/v4/model/ValueRange.html
* https://googleapis.dev/java/google-api-services-sheets/latest/com/google/api/services/sheets/v4/Sheets.html
* https://googleapis.dev/java/google-api-services-sheets/latest/com/google/api/services/sheets/v4/SheetsScopes.html
* https://googleapis.dev/java/google-api-services-sheets/latest/com/google/api/services/sheets/v4/Sheets.Builder.html

All Java APIs are just auto-generated wrappers of the underlying [Google Sheets REST API](https://developers.google.com/sheets/api/reference/rest).

# How do I have to setup my Google API Client?

This recipe is mostly equivalent to the setup described in the [Java Quickstart Guide for the Google Sheets API](https://developers.google.com/sheets/api/quickstart/java).

* Create a new project `parti-time` in [Google Cloud Console](https://console.cloud.google.com/welcome?project=parti-time).
* Enable the Google Sheets API ("Enabled APIs & services")
* Configure the "OAuth Consent Screen" (only the minimum config needed)
* Authorize "Credentials"
  * Create "OAuth Client ID"
  * Type: Desktop App
  * Name: "parti-time CLI"
* Download `credentials.json` to `~/.config/parti-time/credentials.json`
