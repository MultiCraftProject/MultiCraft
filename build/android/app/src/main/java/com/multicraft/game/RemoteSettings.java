package com.multicraft.game;

import java.util.List;

public class RemoteSettings {
    private int versionCode = 0;
    private List<Integer> badVersionCodes;
    private String packageName, content;
    private int adsDelay = -1;
    private int adsRepeat = -1;
    private boolean adsEnabled = true;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public List<Integer> getBadVersionCodes() {
        return badVersionCodes;
    }

    public void setBadVersionCodes(List<Integer> badVersionCodes) {
        this.badVersionCodes = badVersionCodes;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getAdsDelay() {
        return adsDelay;
    }

    public void setAdsDelay(int adsDelay) {
        this.adsDelay = adsDelay;
    }

    public int getAdsRepeat() {
        return adsRepeat;
    }

    public void setAdsRepeat(int adsRepeat) {
        this.adsRepeat = adsRepeat;
    }

    public boolean isAdsEnabled() {
        return adsEnabled;
    }

    public void setAdsEnabled(boolean adsEnabled) {
        this.adsEnabled = adsEnabled;
    }
}
