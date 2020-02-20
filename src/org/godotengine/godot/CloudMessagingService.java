/**
 * Copyright 2016 Google Inc. All Rights Reserved.
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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.godot.game.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class CloudMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        /**
         * There are two types of messages:
         * Data Messages and Notification Messages
         *
         * - Data messages
         * are handled here in onMessageReceived whether the app is in the foreground or background.
         * Data messages are the type traditionally used with GCM.
         *
         * - Notification messages
         * are only received here in onMessageReceived when the app is in the foreground. When the app
         * is in the background an automatically generated notification is displayed.
         * When the user taps on the notification they are returned to the app.
         *
         * Messages containing both notification and data payloads are treated as notification messages.
         * The Firebase console always sends notification messages.
         *
         * For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
         */

        boolean dataPayloadHandled = false;
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            scheduleJob(remoteMessage.getData());
            dataPayloadHandled = true;
        }

        if (!dataPayloadHandled) {
            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                //sendNotification(remoteMessage.getNotification().getBody(), this);
                Utils.callScriptFunc("CloudMessaging", "Notification", remoteMessage.getNotification().getBody());
            }
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob(Map<String, String> remoteMessage) {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CloudMessagingWorker.class).build();
        WorkManager.getInstance().beginWith(work).enqueue();

        Utils.callScriptFunc("CloudMessaging", "Data", remoteMessage);
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        // Left empty for now.
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody, Context context) {
        Intent intent = new Intent(context, Godot.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
                        .setContentTitle(context.getString(R.string.fcm_message))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}