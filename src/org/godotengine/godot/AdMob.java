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
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.godot.game.BuildConfig;
import com.godot.game.R;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.FirebaseApp;
import org.godotengine.godot.Dictionary;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AdMob {

    private static Activity activity = null;
    private static AdMob instance = null;
    private static Dictionary rewardedMetaData = null;
    FrameLayout layout = null;
    private boolean rewardedVideoLoaded = false;
    private boolean bannerLoaded = false;
    private HashMap<String, RewardedAd> rewardedVideoAds = null;
    private AdView adView = null;
    private InterstitialAd interstitialAd = null;
    private Dictionary adSize = null;
    private FirebaseApp firebaseApp = null;
    private JSONObject adMobConfig = null;

    private AdMob(Activity activity) {
        this.activity = activity;
    }

    public static AdMob getInstance(Activity p_activity) {
        if (instance == null) {
            instance = new AdMob(p_activity);
        }

        return instance;
    }

    public void init(FirebaseApp firebaseApp, FrameLayout layout) {

        this.layout = layout;

        this.firebaseApp = firebaseApp;

        adMobConfig = Firebase.getConfig().optJSONObject("AdMobAdUnits");
        MobileAds.initialize(activity, adMobConfig.optString("AppId"));

        if (adMobConfig.optBoolean("Banner", false)) {
            bannerCreate();
        }
        if (adMobConfig.optBoolean("Interstitial", false)) {
            interstitialCreate();
        }
        if (adMobConfig.optBoolean("RewardedVideo", false)) {
            String ad_unit_id = adMobConfig.optString("RewardedVideoId", "");
            List<String> ad_units = new ArrayList<String>();

            if (ad_unit_id.length() <= 0 || adMobConfig.optBoolean("TestAds", false)) {
                ad_unit_id = activity.getString(R.string.test_rewarded_video_ad_unit_id);
            }

            rewardedVideoAds = new HashMap<String, RewardedAd>();
            ad_units = Arrays.asList(ad_unit_id.split(","));
            for (String unit_id : ad_units) {
                rewardedVideoAds.put(unit_id, rewardedVideoRequestNew(unit_id));
            }
        }

        adSize = new Dictionary();
        adSize.put("width", 0);
        adSize.put("height", 0);

        onStart();
    }

    private boolean isInitialized() {
        if (firebaseApp == null) {
            return false;
        }
        return true;
    }

    public Dictionary buildStatus(String unitid, String status) {
        Dictionary dict = new Dictionary();
        dict.put("unit_id", unitid);
        dict.put("status", status);

        return dict;
    }

    public void bannerCreate() {
        if (adMobConfig == null) {
            return;
        }

        String ad_unit_id = adMobConfig.optString("BannerId", "");

        if (ad_unit_id.length() <= 0 || adMobConfig.optBoolean("TestAds", false)) {
            ad_unit_id = activity.getString(R.string.test_banner_ad_unit_id);
        }

        bannerCreate(ad_unit_id);
    }

    public void bannerCreate(final String ad_unit_id) {
        bannerLoaded = false;

        // see https://github.com/godotengine/godot/issues/32827 for changes
        // FrameLayout layout = ((Godot)activity).layout; // Getting Godots framelayout
        FrameLayout.LayoutParams AdParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        if (adView != null) {
            layout.removeView(adView);
        }

        if (adMobConfig.optString("BannerGravity", "BOTTOM").equals("BOTTOM")) {
            AdParams.gravity = Gravity.BOTTOM;
        } else {
            AdParams.gravity = Gravity.TOP;
        }

        AdRequest.Builder adRequestB = new AdRequest.Builder();
        adRequestB.tagForChildDirectedTreatment(true);

        AdRequest adRequest = adRequestB.build();

        adView = new AdView(activity);
        adView.setBackgroundColor(Color.TRANSPARENT);
        adView.setAdUnitId(ad_unit_id);
        adView.setAdSize(AdSize.SMART_BANNER);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                AdSize adSize = adView.getAdSize();
                bannerLoaded = true;

                AdMob.this.adSize.put("width", adSize.getWidthInPixels(activity));
                AdMob.this.adSize.put("height", adSize.getHeightInPixels(activity));

                Utils.callScriptFunc("AdMob", "Banner", "on_ad_loaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                bannerLoaded = false;
                Utils.callScriptFunc("AdMob", "Banner", "on_ad_failed_to_load:" + errorCode);
            }
        });

        adView.setVisibility(View.INVISIBLE);
        adView.loadAd(adRequest);

        layout.addView(adView, AdParams);
    }

    public Dictionary bannerGetSize() {

        return adSize;
    }

    public boolean bannerIsLoaded() {
        return bannerLoaded;
    }

    public void bannerShow(final boolean show) {
        if (!isInitialized() || adView == null) {
            return;
        }

        if (show) {
            if (adView.isEnabled()) {
                adView.setEnabled(true);
            }
            if (adView.getVisibility() == View.INVISIBLE) {
                adView.setVisibility(View.VISIBLE);
            }
        } else {
            if (adView.isEnabled()) {
                adView.setEnabled(false);
            }
            if (adView.getVisibility() != View.INVISIBLE) {
                adView.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void bannerSetUnitId(final String id) {
        bannerCreate(id);
    }

    public void interstitialCreate() {
        if (adMobConfig == null) {
            return;
        }

        String ad_unit_id = adMobConfig.optString("InterstitialId", "");

        if (ad_unit_id.length() <= 0 || adMobConfig.optBoolean("TestAds", false)) {
            ad_unit_id = activity.getString(R.string.test_interstitial_ad_unit_id);
        }

        interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdUnitId(ad_unit_id);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Utils.callScriptFunc("AdMob", "Interstitial", "on_ad_loaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Utils.callScriptFunc("AdMob", "Interstitial", "on_ad_failed_to_load:" + errorCode);
            }

            @Override
            public void onAdClosed() {
                Utils.callScriptFunc("AdMob", "Interstitial", "on_ad_closed");
                interstitialRequestNew();
            }
        });

        interstitialRequestNew();
    }

    public void interstitialShow() {
        if (!isInitialized() || interstitialAd == null) {
            return;
        }

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            // Left empty for now.
        }
    }

    private void interstitialRequestNew() {
        AdRequest.Builder adRB = new AdRequest.Builder();

        // Covered with the test ad ID
        if (BuildConfig.DEBUG || adMobConfig.optBoolean("TestAds", false)) {
            adRB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            adRB.addTestDevice(adMobConfig.optString("TestDevice", Utils.getDeviceId(activity)));
        }

        AdRequest adRequest = adRB.build();

        interstitialAd.loadAd(adRequest);
    }

    public void rewardedVideoEmitStatus() {
        for (String unit_id : rewardedVideoAds.keySet()) {
            Utils.callScriptFunc("AdMob", "RewardedVideo", buildStatus(unit_id, rewardedVideoAds.get(unit_id).isLoaded() ? "loaded" : "not_loaded"));
        }
    }

    public boolean rewardedVideoIsLoaded(final String unit_id) {
        if (!isInitialized() || rewardedVideoAds == null) {
            return false;
        }

        return rewardedVideoAds.get(unit_id).isLoaded();
    }

    public void rewardedVideoReload(final String unitid) {
        if (rewardedVideoAds == null) {
            return;
        }

        rewardedVideoAds.put(unitid, rewardedVideoRequestNew(unitid));
    }

    public void rewardedVideoRequestStatus() {
        rewardedVideoEmitStatus();
    }

    private RewardedAd rewardedVideoRequestNew(final String unitid) {
        rewardedVideoLoaded = false;
        RewardedAd rewardedAd = new RewardedAd(activity, unitid);
        AdRequest.Builder adRB = new AdRequest.Builder();

        // Covered with the test ad ID
        if (BuildConfig.DEBUG || adMobConfig.optBoolean("TestAds", false)) {
            adRB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            adRB.addTestDevice(adMobConfig.optString("TestDevice", Utils.getDeviceId(activity)));
        }

        rewardedAd.loadAd(adRB.build(), new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                rewardedVideoLoaded = true;
                Utils.callScriptFunc("AdMob", "RewardedVideo", buildStatus(unitid, "on_rewarded_ad_loaded"));
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                Utils.callScriptFunc("AdMob", "RewardedVideo", buildStatus(unitid, "on_rewarded_ad_failed_to_load:" + errorCode));
                //reloadRewardedVideo(unitid);
            }
        });
        return rewardedAd;
    }

    public void rewardedVideoShow() {
        if (!isInitialized() || rewardedVideoAds == null) {
            return;
        }

        rewardedVideoShow((String) rewardedVideoAds.keySet().toArray()[0]);
    }

    public void rewardedVideoShow(final String unit_id) {
        if (!isInitialized() || rewardedVideoAds == null) {
            return;
        }

        RewardedAdCallback adCallback = new RewardedAdCallback() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem reward) {
                Dictionary ret = new Dictionary();
                ret.put("status", "earned");
                ret.put("RewardType", reward.getType());
                ret.put("RewardAmount", reward.getAmount());
                ret.put("unit_id", unit_id);

                Utils.callScriptFunc("AdMob", "RewardedVideo", ret);
            }

            @Override
            public void onRewardedAdClosed() {
                Utils.callScriptFunc("AdMob", "RewardedVideo", buildStatus(unit_id, "closed"));
                rewardedVideoReload(adMobConfig.optBoolean("TestAds", false) ? activity.getString(R.string.test_rewarded_video_ad_unit_id) : unit_id);
            }

            @Override
            public void onRewardedAdOpened() {
                // Left empty for now.
            }

            @Override
            public void onRewardedAdFailedToShow(int errorCode) {
                // Left empty for now.
            }
        };

        RewardedAd reward_ad;

        //If it is a test, call a test ads, but pass the actual ad id called to the callback.
        if (adMobConfig.optBoolean("TestAds", false)) {
            String test_unit_id = activity.getString(R.string.test_rewarded_video_ad_unit_id);
            rewardedMetaData.put("unit_id", test_unit_id);
            reward_ad = rewardedVideoAds.get(test_unit_id);
        } else {
            rewardedMetaData.put("unit_id", unit_id);
            reward_ad = rewardedVideoAds.get(unit_id);
        }

        if (reward_ad.isLoaded()) {
            reward_ad.show(activity, adCallback);
        } else {
            // Left empty for now.
        }
    }

    public void onStart() {
        rewardedMetaData = new Dictionary();
    }

    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
    }

    public void onResume() {
        if (adView != null) {
            adView.resume();
        }
    }

    public void onStop() {
        if (adView != null) {
            adView.destroy();
        }
    }
}
