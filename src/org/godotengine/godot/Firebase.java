/**
 * Copyright 2020 Yalcin Ata. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import com.google.firebase.FirebaseApp;
import org.godotengine.godot.Dictionary;
import org.json.JSONException;
import org.json.JSONObject;

import javax.microedition.khronos.opengles.GL10;

public class Firebase extends Godot.SingletonBase {

    private static Activity activity = null;
    private static Context context = null;
    private static JSONObject firebaseConfig = new JSONObject();
    private FirebaseApp firebaseApp = null;
    private FrameLayout layout = null;

    public Firebase(Activity p_activity) {
        // Register class name and functions to bind.
        registerClass("Firebase", new String[]
                {
                        "init",

                        // ===== AdMob
                        "admob_banner_is_loaded", "admob_banner_show", "admob_banner_get_size",
                        "admob_interstitial_show", "admob_rewarded_video_show", "admob_rewarded_video_request_status",

                        // ===== Analytics
                        "analytics_send_custom", "analytics_send_events",

                        // ===== Authentication
                        "authentication_get_id_token",
                        // ----- Google
                        "authentication_google_sign_in", "authentication_google_sign_out", "authentication_google_is_connected",
                        "authentication_google_get_user",

                        // ===== Firestore
                        "firestore_load_document", "firestore_add_document", "firestore_set_document_data",

                        // ===== Storage
                        "storage_upload", "storage_download",

                        // ===== InAppMessaging
                        // Nothing to implement, just adding implementation 'com.google.firebase:firebase-inappmessaging-display:19.0.3' to gradle.conf enables it, done!

                        // ===== Cloud Messaging
                        "cloudmessaging_subscribe_to_topic", "cloudmessaging_unsubscribe_from_topic"
                });
        activity = p_activity;
        context = activity.getApplicationContext();
    }

    static public Godot.SingletonBase initialize(Activity p_activity) {
        return new Firebase(p_activity);
    }

    public static JSONObject getConfig() {
        return firebaseConfig;
    }

    public void init(final int script_id) {

        activity.runOnUiThread(new Runnable() {
            public void run() {
                String fileName = "res://godot-firebase-config.json";
                String data = Utils.readFromFile(fileName, activity);
                data = data.replaceAll("\\s+", "");

                Utils.setScriptInstance(script_id);
                initFirebase(data);
            }
        });
    }

    private void initFirebase(final String data) {
        Utils.logDebug("Firebase initializing");

        JSONObject config = null;
        firebaseApp = FirebaseApp.initializeApp(activity);

        if (data.length() <= 0) {
            Utils.logDebug("Firebase initialized.");
            return;
        }

        try {
            config = new JSONObject(data);
            firebaseConfig = config;
        } catch (JSONException e) {
            Utils.logDebug("JSON Parse error: " + e.toString());
        }

        // ===== AdMob
        if (config.optBoolean("AdMob", false)) {
            Utils.logDebug("AdMob initializing");
            AdMob.getInstance(activity).init(firebaseApp, layout);
        }

        // ===== Analytics
        if (config.optBoolean("Analytics", false)) {
            Utils.logDebug("Analytics initializing");
            Analytics.getInstance(activity).init(firebaseApp);
        }

        // ===== Authentication
        if (config.optBoolean("Authentication", false)) {
            Utils.logDebug("Authentication initializing");
            Authentication.getInstance(activity).init(firebaseApp);
        }

        // ===== Firestore
        if (config.optBoolean("Firestore", false)) {
            Utils.logDebug("Firestore initializing");
            Firestore.getInstance(activity).init(firebaseApp);
        }

        // ===== Storage
        if (config.optBoolean("Storage", false)) {
            Utils.logDebug("Storage initializing");
            Storage.getInstance(activity).init(firebaseApp);
        }

        // ===== InAppMessaging
        // Just adding implementation 'com.google.firebase:firebase-inappmessaging-display:19.0.3' to gradle.conf enables it, done!
        {
            Utils.logDebug("In-App Messaging initialized");
        }

        // ===== Cloud Messaging
        if (config.optBoolean("CloudMessaging", false)) {
            Utils.logDebug("CloudMessaging initializing");
            CloudMessaging.getInstance(activity).init(firebaseApp);
        }

        Utils.logDebug("Firebase initialized");
    }

    // ===== AdMob
    public boolean admob_banner_is_loaded() {
        return AdMob.getInstance(activity).bannerIsLoaded();
    }

    public void admob_banner_show(final boolean show) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).bannerShow(show);
            }
        });
    }

    public Dictionary admob_banner_get_size() {
        return AdMob.getInstance(activity).bannerGetSize();
    }

    public void admob_interstitial_show() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).interstitialShow();
            }
        });
    }

    public void admob_rewarded_video_show() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).rewardedVideoShow();
            }
        });
    }

    public void admob_rewarded_video_request_status() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).rewardedVideoRequestStatus();
            }
        });
    }
    // ===== AdMob ====================================================================================================

    // ===== Analytics
    public void analytics_send_custom(final String key, final String value) {
        if (key.length() <= 0 || value.length() <= 0) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).sendCustom(key, value);
            }
        });
    }

    public void analytics_send_events(final String key, final Dictionary data) {
        if (key.length() <= 0 || data.size() <= 0) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).sendEvents(key, data);
            }
        });
    }
    // ===== Analytics ================================================================================================

    // ===== Authentication
    public void authentication_get_id_token() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Authentication.getInstance(activity).getIdToken();
            }
        });
    }

    // ----- Google
    public void authentication_google_sign_in() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Authentication.getInstance(activity).signIn();
            }
        });
    }

    public void authentication_google_sign_out() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Authentication.getInstance(activity).signOut();
            }
        });
    }

    public boolean authentication_google_is_connected() {
        return Authentication.getInstance(activity).isConnected();
    }

    public String authentication_google_get_user() {
        return Authentication.getInstance(activity).getUserDetails();
    }
    // ===== Authentication ===========================================================================================

    // ===== Firestore
    public void firestore_add_document(final String name, final Dictionary data) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Firestore.getInstance(activity).addDocument(name, data);
            }
        });
    }

    public void firestore_load_document(final String name) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Firestore.getInstance(activity).loadDocuments(name, -1);
            }
        });
    }

    public void firestore_set_document_data(final String colName, final String docName, final Dictionary data) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Firestore.getInstance(activity).setDocumentData(colName, docName, data);
            }
        });
    }
    // ===== Firestore ================================================================================================

    // ===== Storage
    public void storage_upload(final String fileName) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Storage.getInstance(activity).upload(fileName);
            }
        });
    }

    public void storage_download(final String fileName) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Storage.getInstance(activity).download(fileName);
            }
        });
    }
    // ===== Storage ==================================================================================================

    // ===== Cloud Messaging
    public void cloudmessaging_subscribe_to_topic(final String topicName) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CloudMessaging.getInstance(activity).subscribeToTopic(topicName);
            }
        });
    }

    public void cloudmessaging_unsubscribe_from_topic(final String topicName) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CloudMessaging.getInstance(activity).unsubscribeFromTopic(topicName);
            }
        });
    }
    // ===== Cloud Messaging ==========================================================================================

    // Forwarded callbacks you can reimplement, as SDKs often need them.
    protected void onMainActivityResult(int requestCode, int resultCode, Intent data) {
        Authentication.getInstance(activity).onActivityResult(requestCode, resultCode, data);
    }

    protected void onMainRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    }

    protected void onMainPause() {
        AdMob.getInstance(activity).onPause();
        Authentication.getInstance(activity).onPause();
    }

    protected void onMainResume() {
        AdMob.getInstance(activity).onResume();
        Authentication.getInstance(activity).onResume();
    }

    protected void onMainDestroy() {
        AdMob.getInstance(activity).onStop();
        Authentication.getInstance(activity).onStop();
    }

    @Override
    public View onMainCreateView(Activity activity) {
        layout = new FrameLayout(activity);
        return layout;
    }

    protected void onGLDrawFrame(GL10 gl) {
    }

    protected void onGLSurfaceChanged(GL10 gl, int width, int height) {
    } // Singletons will always miss first 'onGLSurfaceChanged' call.

}
