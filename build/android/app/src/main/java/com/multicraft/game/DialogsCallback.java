package com.multicraft.game;

public interface DialogsCallback {
    void onPositive(String source);

    void onNegative(String source);

    void onNeutral(String source);
}
