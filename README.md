# Reel Tracker

Counts Instagram Reels / YouTube Shorts you scroll, shows a live counter overlay, charts your habits,
and blocks the apps once you hit your daily limit.

## Get the APK (no Android Studio needed)
1. Create a new GitHub repo and upload everything in this folder (keep the `.github` folder).
2. Open the **Actions** tab -> **Build APK** runs automatically (or press *Run workflow*).
3. When it finishes (~3 min) download the APK from **Releases -> Latest build -> ReelTracker.apk**
   (open that page on your phone) or from the run's *Artifacts*.

Or build locally: `./gradlew assembleDebug` -> `app/build/outputs/apk/debug/app-debug.apk`
(needs JDK 17 + Android SDK 34).

## First-run setup on the phone
1. Install the APK (allow "Install unknown apps" for your browser/file manager).
2. Open Reel Tracker -> **Open Accessibility settings** -> Installed apps -> **Reel Tracker** -> turn on.
   - Android 13+: if the toggle is greyed out ("Restricted setting"), go to
     Settings -> Apps -> Reel Tracker -> ⋮ menu -> **Allow restricted settings**, then retry.
3. Set your daily limit and pick the apps to track.

## If counting stops working
Instagram/YouTube rename internal view IDs in updates. Edit the `reelIds` map in
`ReelService.kt` and rebuild.
