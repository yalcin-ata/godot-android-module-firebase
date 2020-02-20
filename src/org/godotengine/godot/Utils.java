/**
 * Copyright 2017 FrogSquare. All Rights Reserved.
 * Changes: 2020 Yalcin Ata
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
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static final String TAG = "Firebase";

    public static int scriptInstanceId = -1;


    public static String readFromFile(String path, Context context) {
        StringBuilder returnString = new StringBuilder();

        String fileName = path;
        if (path.startsWith("res://")) {
            fileName = fileName.replaceFirst("res://", "");
        }

        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;

        try {
            fIn = context.getResources().getAssets().open(fileName);

            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);

            String line = "";

            int i = 6;
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
            logDebug("Reading failed: " + e.toString());
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (fIn != null) {
                    fIn.close();
                }
                if (input != null) {
                    input.close();
                }

            } catch (Exception e2) {
                e2.getMessage();
            }
        }

        return returnString.toString();
    }

    public static void setScriptInstance(int instanceId) {
        scriptInstanceId = instanceId;
    }

    public static void callScriptFunc(int script_id, String from, Object key, Object value) {
        GodotLib.calldeferred(script_id, "_on_firebase_receive_message", new Object[]{TAG, from, key, value});
    }

    public static void callScriptFunc(String from, Object key, Object value) {
        if (scriptInstanceId == -1) {
            logDebug("scriptInstanceId not set");
            return;
        }

        GodotLib.calldeferred(scriptInstanceId, "_on_firebase_receive_message", new Object[]{TAG, from, key, value});
    }

    public static String getDeviceId(Activity activity) {
        String androidId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

        String deviceId = md5(androidId).toUpperCase();

        return androidId;
    }

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());

            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);

                while (h.length() < 2) {
                    h = "0" + h;
                }

                hexString.append(h);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logWarn("Firebase Utils exception: " + e.toString());
        }

        return "";
    }

    public static void putAllInDict(Bundle bundle, Dictionary keyValues) {
        String[] keys = keyValues.get_keys();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            putGodotValue(bundle, key, keyValues.get(key));
        }
    }

    public static void putGodotValue(Bundle bundle, String key, Object value) {

        if (value instanceof Boolean) {
            bundle.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            bundle.putInt(key, (Integer) value);
        } else if (value instanceof Double) {
            bundle.putDouble(key, (Double) value);
        } else if (value instanceof String) {
            bundle.putString(key, (String) value);
        } else {
            if (value != null) {
                bundle.putString(key, value.toString());
            }
        }
    }

    public static void logDebug(final String message) {
        Log.d(TAG, message);
    }

    public static void logWarn(final String message) {
        Log.w(TAG, message);
    }

}
