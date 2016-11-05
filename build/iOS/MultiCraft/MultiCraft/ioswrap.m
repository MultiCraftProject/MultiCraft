#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <SSZipArchive/SSZipArchive.h>
#include "ioswrap.h"

void ioswrap_log(const char *message)
{
    NSLog(@"%s", message);
}

void ioswrap_paths(int type, char *dest, size_t destlen)
{
    NSArray *paths;

    if (type == PATH_DOCUMENTS)
        paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    else if (type == PATH_LIBRARY_SUPPORT || type == PATH_LIBRARY_CACHE)
        paths = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES);
    else
        return;

    NSString *path = paths.firstObject;
    const char *path_c = path.UTF8String;

    if (type == PATH_DOCUMENTS)
        snprintf(dest, destlen, "%s", path_c);
    else if (type == PATH_LIBRARY_SUPPORT)
        snprintf(dest, destlen, "%s/Application Support", path_c);
    else // type == PATH_LIBRARY_CACHE
        snprintf(dest, destlen, "%s/Caches", path_c);
}

void ioswrap_assets()
{
	const struct { const char *name; int path; } assets[] = {
		{ .name = "assets", .path = PATH_LIBRARY_SUPPORT },
		{ .name = "worlds", .path = PATH_DOCUMENTS },
		{ NULL, 0 },
	};
	char buf[256];

	for(int i = 0; assets[i].name != NULL; i++) {
		ioswrap_paths(assets[i].path, buf, sizeof(buf));
		NSString *destpath = [NSString stringWithUTF8String:buf];
		NSString *zippath = [[NSBundle mainBundle] pathForResource:[NSString stringWithUTF8String:assets[i].name] ofType:@"zip"];

		NSLog(@"%s: extract %@ to %@", assets[i].name, zippath, destpath);
		[SSZipArchive unzipFileAtPath:zippath toDestination:destpath];
	}
}

void ioswrap_size(unsigned int *dest)
{
    CGSize bounds = [[UIScreen mainScreen] bounds].size;
    CGFloat scale = [[UIScreen mainScreen] scale];
    dest[0] = bounds.width * scale;
    dest[1] = bounds.height * scale;
}
