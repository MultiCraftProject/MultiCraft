package com.multicraft.game.callbacks;

public interface DialogsCallback {
    void onPositive(String source);

    void onNegative(String source);

    void onNeutral(String source);
}
