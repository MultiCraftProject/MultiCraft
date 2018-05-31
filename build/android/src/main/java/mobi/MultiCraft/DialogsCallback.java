package mobi.MultiCraft;

public interface DialogsCallback {
    void onPositive(String source);

    void onNegative(String source);

    void onCancelled(String source);
}
