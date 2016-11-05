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

#ifdef __cplusplus
}
#endif

#endif /* ioswrap_h */
