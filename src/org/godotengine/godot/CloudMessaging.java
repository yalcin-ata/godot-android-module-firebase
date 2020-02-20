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
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class CloudMessaging {

    private static Activity activity = null;
    private static CloudMessaging instance = null;
    private FirebaseApp firebaseApp = null;
    private String token = "";

    public CloudMessaging(Activity activity) {
        this.activity = activity;
    }

    public static CloudMessaging getInstance(Activity activity) {
        if (instance == null) {
            instance = new CloudMessaging(activity);
        }

        return instance;
    }

    public void init(FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    return;
                }

                token = task.getResult().getToken();
            }
        });

        Utils.logDebug("CloudMessaging initialized");
    }

    public void subscribeToTopic(final String topicName) {
        FirebaseMessaging.getInstance().subscribeToTopic(topicName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Utils.callScriptFunc("CloudMessaging", "subscribe", false);
                    Utils.logDebug("CloudMessaging subscribeToTopic() Failed to subscribe to " + topicName);
                    return;
                }
                Utils.callScriptFunc("CloudMessaging", "subscribe", true);
                Utils.logDebug("CloudMessaging subscribeToTopic() " + topicName);
            }
        });
    }

    public void unsubscribeFromTopic(final String topicName) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName);
        Utils.callScriptFunc("CloudMessaging", "unsubscribe", true);
        Utils.logDebug("CloudMessaging unsubscribeFromTopic() " + topicName);
    }

    public String getToken() {
        return token;
    }
}
