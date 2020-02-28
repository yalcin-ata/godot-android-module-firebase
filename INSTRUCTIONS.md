# Instructions
## Initialization 
In any of your Godot Script (I prefer a singleton called `Global.gd`), initialize the Firebase Module:
   
   <pre>
var firebase = null
   
func _ready() -> void:
&emsp;&emsp;&emsp;if Engine.has_singleton("Firebase"):
&emsp;&emsp;&emsp;&emsp;&emsp;firebase = Engine.get_singleton("Firebase")
&emsp;&emsp;&emsp;&emsp;&emsp;if firebase:
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;firebase.init(get_instance_id())
</pre>
   
And implement the callback receiver:
<pre>
func _on_firebase_receive_message(tag, from, key, data) -> void:
&emsp;&emsp;&emsp;if tag == "Firebase":
&emsp;&emsp;&emsp;&emsp;&emsp;if from == ...
...
</pre>

## AdMob
<pre>firebase.admob_banner_is_loaded() -> bool</pre>
Returns *true* if banner is loaded, *false* otherwise.

<pre>firebase.admob_banner_show(bool) -> void</pre>
Shows (true) or hides (false) the banner.

<pre>firebase.admob_banner_get_size() -> Dictionary</pre>
Returns a *Dictionary* containing the "width" and "height" of the banner ({"width" : 0, "height" : 0}).

<pre>firebase.admob_interstitial_show() -> void</pre>
Shows the interstitial video.

<pre>firebase.admob_rewarded_video_request_status() -> void</pre>
Requests rewarded video status.<br />
**Callback** is `tag=Firebase from=AdMob key=RewardedVideo data={status:loaded, unit_id:ca-app-pub-AD_UNIT_ID}` or `...status:not_loaded,...`.

<pre>firebase.admob_rewarded_video_show() -> void</pre>
Shows the rewarded video.

### Callbacks fired when AdMob is initialized:
<pre>
tag=Firebase from=AdMob key=Banner data=on_ad_loaded
tag=Firebase from=AdMob key=Banner data=on_ad_failed_to_load:errorCode

tag=Firebase from=AdMob key=Interstitial data=on_ad_loaded
tag=Firebase from=AdMob key=Interstitial data=on_ad_failed_to_load:errorCode

tag=Firebase from=AdMob key=RewardedVideo data={status:on_rewarded_ad_loaded, unit_id:ca-app-pub-AD_UNIT_ID}
tag=Firebase from=AdMob key=RewardedVideo data={status:on_rewarded_ad_failed_to_load:errorCode, unit_id:ca-app-pub-AD_UNIT_ID}
</pre>

`on_ad_failed_to_load:errorCode` shows the error code which comes from AdMob. [See here the error codes](https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest#constants_1).
Usually they are:

| code | error |
|:---:|---|
| 0 | `ERROR_CODE_INTERNAL_ERROR` |
| 1 | `ERROR_CODE_INVALID_REQUEST` |
| 2 | `ERROR_CODE_NETWORK_ERROR` |
| 3 | `ERROR_CODE_NO_FILL` |

### Other AdMob callbacks
<pre>
tag=Firebase from=AdMob key=Interstitial data=on_ad_closed

tag=Firebase from=AdMob key=RewardedVideo data={status:earned, rewardType, rewardAmount, unit_id:ca-app-pub-AD_UNIT_ID}
tag=Firebase from=AdMob key=RewardedVideo data={status:closed, unit_id:ca-app-pub-AD_UNIT_ID}
</pre>

If RewardedVideo ad<br />
- was closed without watching to the end, one callback is called: `status:closed`,
- is watched completely, two callbacks are called: `status:earned` and `status:closed`. 

## Analytics
<pre>firebase.analytics_send_events("eventName", Dictionary) -> void</pre>
Logs "eventName" with the provided Dictionary to Firebase Analytics.

<pre>firebase.analytics_send_custom("someKey", "someValue" -> void</pre>
Logs a custom event of {"someKey", "someValue"}.

## Authentication
<pre>firebase.authentication_get_id_token() -> void</pre>
Requests the currently logged-in user's ID token.<br />
**Callback** is `tag=Firebase from=Authentication key=idToken data='the data itself'` if user is logged in, no callback otherwise.

### Authentication Google
<pre>firebase.authentication_google_sign_in() -> void</pre>
Requests to sign-in a Google user.<br />
**Callback** is `tag=Firebase from=Authentication key=Google data=true` if login was successful, `data=false` otherwise.

<pre>firebase.authentication_google_sign_out() -> void</pre>
Requests to sign-out the logged in Google user.<br />
**Callback** is `tag=Firebase from=Authentication key=Google data=false`.

<pre>firebase.authentication_google_is_connected() -> bool</pre>
Returns *true* if a Google user is logged in, *false* otherwise.

<pre>firebase.authentication_google_get_user() -> String</pre>
Requests logged in Google user data. *"null"* if no Google user is logged-in. If a Google user is logged-in the returned *String* is in JSON format:<br />
<pre>{
&emsp;&emsp;&emsp;"uid" : "the uid",
&emsp;&emsp;&emsp;"name" : "the display name",
&emsp;&emsp;&emsp;"email" : "user's email address",
&emsp;&emsp;&emsp;"photo_uri" : "URL of user's profile picture"
}</pre>

Example usage of user data:

<pre>
func some_method() -> void:
&emsp;&emsp;&emsp;var user_details : String = firebase.authentication_google_get_user()
&emsp;&emsp;&emsp;var parsed_json = JSON.parse(user_details)
&emsp;&emsp;&emsp;var data = parsed_json.result
&emsp;&emsp;&emsp;if data == null:
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;return
&emsp;&emsp;&emsp;$UsernameLabel.set_text(data.name)
&emsp;&emsp;&emsp;$EmailLabel.set_text(data.email)

&emsp;&emsp;&emsp;# Download texture
&emsp;&emsp;&emsp;var http_request = HTTPRequest.new()
&emsp;&emsp;&emsp;add_child(http_request)
&emsp;&emsp;&emsp;http_request.connect("request_completed", self, "http_request_completed")
&emsp;&emsp;&emsp;var http_error = http_request.request(data.photo_uri)
&emsp;&emsp;&emsp;if http_error != OK:
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;print("An error occured in the HTTP request")

func http_request_completed(result, response_code, headers, body):
&emsp;&emsp;&emsp;var image = Image.new()
&emsp;&emsp;&emsp;# var image_error = image.load_png_from_buffer(body) # <- for PNG
&emsp;&emsp;&emsp;var image_error = image.load_jpg_from_buffer(body) # <- for JPG
&emsp;&emsp;&emsp;if image_error != OK:
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;print("An error occured while trying to display the image")
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;return
&emsp;&emsp;&emsp;var texture = ImageTexture.new()
&emsp;&emsp;&emsp;texture.create_from_image(image)
&emsp;&emsp;&emsp;$UserTexture.texture = texture

...

# To delete/remove displayed user info when logged out:
$Username.Label.set_text("")
$EmailLabel.set_text("")
$UserTexture.texture = null
</pre>

## Firestore
<pre>firebase.firestore_add_document("collectionName", Dictionary) -> void</pre>
A new Dictionary is created under "collectionName".<br />
**Callback** is `tag=Firebase from=Firestore key=AddDocument data=true` on success, `data=false` otherwise.

<pre>firebase.firestore_load_document("collectionName") -> void</pre>
Loads "collectionName" from Firebase.<br />
**Callback** is `tag=Firebase from=Firestore key=Documents data=data in JSON format`.

<pre>firebase.firestore_set_document_data("collectionName", "documentName", Dictionary) -> void</pre>
"documentName"'s Dictionary is merged/updated (under "collectionName").<br />
**Callback** is `tag=Firebase from=Firestore key=SetDocument data=true` on success, `data=false` otherwise.

## Storage
<pre>firebase.storage_download("fileName") -> void</pre>
Download "fileName" from Firebase. *(i.e. `firebase.storage_download("test.txt")`*<br />
**Callback** is `tag=Firebase from=Storage key=download data=true` on success, `data=false` otherwise, or `data=userNotSignedIn` if user is not logged in.

<pre>firebase.storage_upload("fileName") -> void</pre>
Upload "fileName" to Firebase. *(i.e. `firebase.storage_upload("user://savegame.json"))`*<br />
**Callback** is `tag=Firebase from=Storage key=upload data=true` on success, `data=false` otherwise, or `data=userNotSignedIn` if user is not logged in.

## In-App Messaging
No methods to call from the app. Follow instructions at [Firebase: Send a test message](https://firebase.google.com/docs/in-app-messaging/get-started?authuser=0&platform=android#send_a_test_message).

## Cloud Messaging
<pre>firebase.cloudmessaging_subscribe_to_topic("topicName") -> void</pre>
Subscribes to "topicName".<br />
**Callback** is `tag=Firebase from=CloudMessaging key=subscribe data=true` on success, `data=false` otherwise.

<pre>firebase.cloudmessaging_ubsubscribe_from_topic("topicName") -> void</pre>
Unsubscribes from "topicName".<br />
**Callback** is `tag=Firebase from=CloudMessaging key=unsubscribe data=true"` (data is always *true*).

### Callbacks on message received
#### Message is of type Data Message
`tag=Firebase from=CloudMessaging key=Data data=message data map`
#### Message is of type Notification Message
`tag=Firebase from=CloudMessaging key=Notification data=message text`
