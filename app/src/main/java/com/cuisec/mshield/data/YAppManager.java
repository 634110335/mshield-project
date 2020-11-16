package com.cuisec.mshield.data;

public class YAppManager {

    private volatile static YAppManager appManager = null;

    private boolean signBusyStatus; // 签名繁忙状态
    private boolean sealStatus; // 手写签名强制状态

    public static YAppManager getInstance() {
        if (appManager == null) {
            synchronized (YAppManager.class) {
                if (appManager == null) {
                    appManager = new YAppManager();
                }
            }
        }
        return appManager;
    }

    private YAppManager() {
    }

    public boolean isSignBusyStatus() {
        return signBusyStatus;
    }

    public void setSignBusyStatus(boolean signBusyStatus) {
        this.signBusyStatus = signBusyStatus;
    }

    public boolean isSealStatus() {
        return sealStatus;
    }

    public void setSealStatus(boolean sealStatus) {
        this.sealStatus = sealStatus;
    }
}
