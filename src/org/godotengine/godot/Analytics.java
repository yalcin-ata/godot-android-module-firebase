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
import android.os.Bundle;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {

    private static Activity activity = null;
    private static Analytics instance = null;

    private FirebaseApp firebaseApp = null;
    private FirebaseAnalytics firebaseAnalytics = null;

    public Analytics(Activity activity) {
        this.activity = activity;
    }

    public static Analytics getInstance(Activity activity) {
        if (instance == null) {
            synchronized (Analytics.class) {
                instance = new Analytics(activity);
            }
        }

        return instance;
    }

    public void init(FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;
        firebaseAnalytics = FirebaseAnalytics.getInstance(activity);

        Utils.logDebug("Analytics initialized");
    }

    public void sendEvents(String eventName, Dictionary keyValues) {
        Bundle bundle = new Bundle();
        Utils.putAllInDict(bundle, keyValues);

        // firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
        firebaseAnalytics.logEvent(eventName, bundle);
    }

    public void sendCustom(final String key, final String value) {
        Bundle bundle = new Bundle();
        bundle.putString(key, value);

        // firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
        firebaseAnalytics.logEvent("appEvent", bundle);
    }
}