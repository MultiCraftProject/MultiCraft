#ifndef ioswrap_h
#define ioswrap_h

#ifdef __cplusplus
extern "C" {
#endif

enum {
    PATH_DOCUMENTS,
    PATH_LIBRARY_SUPPORT,
    PATH_LIBRARY_CACHE,
};

void ioswrap_log(const char *message);
void ioswrap_paths(int type, char *dest, size_t destlen);
void ioswrap_assets(void); // extracts assets.zip to PATH_LIBRARY_SUPPORT
void ioswrap_size(unsigned int *dest);
void ioswrap_show_dialog(void *uiviewcontroller, const char *accept, const char *hint, const char *current, int type);
int ioswrap_get_dialog(const char **text);

#ifdef __cplusplus
}
#endif

#endif /* ioswrap_h */
