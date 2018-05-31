package mobi.MultiCraft;


public interface CallBackListener {
    void updateViews(int text, int textVisibility, int progressVisibility);

    void onEvent(String source, String param);
}
