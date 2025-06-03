# FAQ

## How do I have to setup my Google API Client?

This recipe is mostly equivalent to the setup described in the [Java Quickstart Guide for the Google Sheets API](https://developers.google.com/sheets/api/quickstart/java).

* Create a new project `parti-time` in [Google Cloud Console](https://console.cloud.google.com/welcome?project=parti-time).
* Enable the Google Sheets API ("Enabled APIs & services")
* Configure the "OAuth Consent Screen" (only the minimum config needed)
* Authorize "Credentials"
  * Create "OAuth Client ID"
  * Type: Desktop App
  * Name: "parti-time CLI"
* Download `credentials.json` to `~/.config/parti-time/credentials.json`

## How can I diff my local timesheets vs. Google?

```
diff -u <(pt download --google-sheet-id '1abcdefgHIJKLmnoPQrsTUVWxyz09123456789qwerty') <(pt cat --include-private=no TimeTracker.2024-??.tl) | colordiff
```
