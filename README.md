# godot-android-module-firebase
Godot Android module for Firebase, written from scratch. This project replaces https://github.com/yalcin-ata/godot-plugin-firebase.

This works for [Godot Engine](https://godotengine.org/)'s stable version 3.2 (not beta).

Follow the instructions [below](#instructions).

[API documentation can be found here.](INSTRUCTIONS.md)

## Instructions
### Preparing project
1. Download and start Godot 3.2. No need to build it on your own (compile, ...).
2. Install **Export Templates**: select menu *Editor > Manage Export Templates...* and download for Current Version (3.2.stable)
3. Install **Android Build Template** for your project: select menu *Project > Install Android Build Template...*, and then click *Install*. This will install the files in your project's directory (by adding `[PROJECT]/android/build/`).
4. Select menu *Project > Export*, and *Add...* Android. After setting your *Unique Name*, keystore stuff etc, don't forget to turn on ***Use Custom Build***. Then click *Close*.
5. Run in `[PROJECT]/android/`:
   <pre>git clone https://github.com/yalcin-ata/godot-android-module-firebase</pre>
6. From [Firebase console](http://console.firebase.google.com/) download your project's **google-services.json** and copy/move it to `[PROJECT]/android/build/`.

   **Notice:**<br/>Remember to always download a new version of google-services.json whenever you make changes at the Firebase console!

### Preparing Firebase Android Module
7. Add following two lines at the bottom of `[PROJECT]/android/build/gradle.properties`:
   <pre>
   android.useAndroidX=true
   android.enableJetifier=true
   </pre>

   
8. Change `minSdk` from 18 to 21 in `[PROJECT]/android/build/config.gradle`:

   <pre>minSdk : 21</pre>
   
9. Change gradle version to 6.1.1 in `[PROJECT]/android/build/gradle/wrapper/gradle-wrapper.properties`:

   <pre>distributionUrl=https\://services.gradle.org/distributions/gradle-6.1.1-all.zip</pre>
   
10. Edit `[PROJECT]/android/godot-android-module-firebase/assets/godot-firebase-config.json` to your needs.

    **Notice:**<br />
    If `TestAds` for AdMob is set to `true` all your Ad Unit IDs will be ignored, and the official [AdMob Test IDs](https://developers.google.com/admob/android/test-ads) will be used instead.

    How to completely remove unneeded features is explained [below](#removing-unneeded-features).
   
11. Edit `[PROJECT]/android/godot-android-modules-firebase/gradle.conf` to match your `applicationId`:

    <pre>applicationId 'your.package.name'</pre>
   
12. In `[PROJECT]/android/godot-android-modules-firebase/AndroidManifest.conf` edit the following section to match your needs:

    <pre>
    &lt;!-- AdMob -->
    &lt;meta-data
    &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;android:name="com.google.android.gms.ads.APPLICATION_ID"
    &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;android:value="ca-app-pub-ADMOB_APP_ID"/>
    &lt;!-- AdMob -->
    </pre>
   
13. In Godot select menu *Project > Project Settings* and go to *Android: Modules* to add the following line:

    <pre>org/godotengine/godot/Firebase</pre>
    
    - Alternative:<br />edit `[PROJECT]/project.godot` and add somewhere the following lines:<br /><br />    
    <pre>
    [android]
    modules="org/godotengine/godot/Firebase"
    </pre>
      
  Setup is done, now you can take a look at the [instructions here (API).](INSTRUCTIONS.md)
   
### Removing Unneeded Features
**Notice:**<br />
Never remove
<pre>implementation 'com.google.firebase:firebase-analytics:VERSION'</pre>
from `gradle.conf` as this is needed for Firebase.</div>

If you want to remove some features completely (i.e. to reduce the app size, not interested in a feature, ...) follow these steps:

Let's assume you don't need **Cloud Messaging**:

- in `[PROJECT]/android/godot-android-modules-firebase/gradle.conf` remove following lines:
  <pre>
  // Firebase Cloud Messaging
  implementation 'com.google.firebase:firebase-messaging:20.1.0'
  implementation 'androidx.work:work-runtime:2.3.1'
  </pre>

- in `[PROJECT]/android/godot-android-modules-firebase/AndroidManifest.conf` remove following lines:
  <pre>
  &lt;!-- Firebase Cloud Messaging -->
  &lt;service
  &emsp;&emsp;&emsp;android:name="org.godotengine.godot.CloudMessagingService"
  &emsp;&emsp;&emsp;android:exported="false">
  &emsp;&emsp;&emsp;&lt;intent-filter>
  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&lt;action android:name="com.google.firebase.MESSAGING_EVENT" />
  &emsp;&emsp;&emsp;&lt;/intent-filter>
  &lt;/service>
  &lt;!-- Set custom default icon. This is used when no icon is set for incoming notification messages.
       See README(https://goo.gl/l4GJaQ) for more. -->
  &lt;meta-data
  &emsp;&emsp;&emsp;android:name="com.google.firebase.messaging.default_notification_icon"
  &emsp;&emsp;&emsp;android:resource="@drawable/ic_stat_ic_notification" />
  &lt;!-- Set color used with incoming notification messages. This is used when no color is set for the incoming
       notification message. See README(https://goo.gl/6BKBk7) for more. -->
  &lt;meta-data
  &emsp;&emsp;&emsp;android:name="com.google.firebase.messaging.default_notification_color"
  &emsp;&emsp;&emsp;android:resource="@color/colorAccent" />
  &lt;meta-data
  &emsp;&emsp;&emsp;android:name="com.google.firebase.messaging.default_notification_channel_id"
  &emsp;&emsp;&emsp;android:value="@string/default_notification_channel_id" />
  </pre>

- in `[PROJECT]/android/godot-android-modules-firebase/src/org.godotengine.godot.Firebase.java` remove everything related to **Cloud Messaging**:
  <pre>
  // ===== Cloud Messaging
  "cloudmessaging_subscribe_to_topic", "cloudmessaging_unsubscribe_from_topic"
  </pre>
  
  **Notice:**<br />
  Remove the last comma at the last method name in the `registerClass()` method call, i.e. change
  - <pre>
    // ===== Storage
    "storage_upload", "storage_download",<- this one
    </pre>
     to
  - <pre>
    // ===== Storage
    "storage_upload", "storage_download"
    </pre>

  ---

  <pre>
  // ===== Cloud Messaging
  if (config.optBoolean("CloudMessaging", false)) {
      Utils.logDebug("CloudMessaging initializing");
      CloudMessaging.getInstance(activity).init(firebaseApp);
  }
  </pre>

  ---

  <pre>
  // ===== Cloud Messaging
  public void cloudmessaging_subscribe_to_topic(final String topicName) {
  &emsp;&emsp;&emsp;activity.runOnUiThread(new Runnable() {
  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;@Override
  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;public void run() {
  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;CloudMessaging.getInstance(activity).subscribeToTopic(topicName);
  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;}
  &emsp;&emsp;&emsp;});
  }

  public void cloudmessaging_unsubscribe_from_topic(final String topicName) {
  &emsp;&emsp;&emsp;activity.runOnUiThread(new Runnable() {
  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;@Override
  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;public void run() {
  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;CloudMessaging.getInstance(activity).unsubscribeFromTopic(topicName);
  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;}
  &emsp;&emsp;&emsp;});
  }
  // ===== Cloud Messaging ======================================================
  </pre>

- in `[PROJECT]/android/godot-android-modules-firebase/src/org/godotengine/godot/` remove every class with names starting with **CloudMessaging**.

Done!

### Authentication
1. Go to project's *Firebase Console > Authentication > Sign-in method > Google: **enable***.
2. Generate SHA-1:
   * For **release**
     * Run in shell:
     
       <pre>keytool -list -v -alias <YOUR-ALIAS> -keystore release.keystore</pre>
       (type afterwards your password)
     
     * Copy calculated SHA-1.
     
     * Go to project's *Firebase Console > Project Settings* (click on gear wheel icon):
     
       * Scroll down to *Your apps* and click on *Add fingerprint*,
     
       * Paste the copied SHA-1 and save.
       
   * For **debug**
     * Run in shell:
       
       <pre>keytool -list -v -alias <YOUR-ALIAS> -keystore debug.keystore</pre>
       (type afterwards your password)
     * Copy calculated SHA-1.
     
     * Go to project's *Firebase Console > Project Settings* (click on gear wheel icon):
     
       * Scroll down to *Your apps* and click on *Add fingerprint*,
     
       * Paste the copied SHA-1 and save.
       
    * At project's *Firebase Console > Project Settings* (click on gear wheel icon):
      * Under *Public settings* is *public-facing name*, beginning with `project-...`: copy `project-...`.
      
      * Edit `[PROJECT]/android/godot-android-modules-firebase/res/values/strings.xml` and edit the following line:
      
        <pre>&lt;string name="server_client_id">project-.....</string></pre>
        
3. From [Firebase console](http://console.firebase.google.com/) download **google-services.json** and copy/move it to `[PROJECT]/android/build/`.

   **Again:**<br />
   Remember to always download a new version of google-services.json whenever you make changes at the Firebase console!</div>
   
### In-App Messaging
Follow instructions at [Firebase: Send a test message](https://firebase.google.com/docs/in-app-messaging/get-started?authuser=0&platform=android#send_a_test_message).

### Cloud Messaging
For advanced users:

Optional: Edit `[PROJECT]/android/godot-android-module-firebase/res/values/strings.xml` and edit following line:

<pre>&lt;string name="default_notification_channel_id">TO BE DONE</string></pre>

Links: [Firebase Cloud Messaging client](https://firebase.google.com/docs/cloud-messaging/android/client), [Firebase Cloud Messaging receive](https://firebase.google.com/docs/cloud-messaging/android/receive)

---

## ADB Logging
Run in shell:

<pre>clear</pre>
(clear screen)
<pre>adb logcat -b all -c</pre>
(clear buffer cache)
<pre>adb -d logcat godot:V GoogleService:V Firebase:V StorageException:V StorageTask:V UploadTask:V FIAM.Headless:V DEBUG:V AndroidRuntime:V ValidateServiceOp:V *:S</pre>
