#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <SSZipArchive/SSZipArchive.h>
#include "ioswrap.h"

// returns the app version as an integer
static uint32_t parse_version()
{
	struct {
		uint8_t major, minor, patch, revision;
	} version;
	NSString *fullver = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
	NSString *revstr = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"];

	if([fullver length] != 3 + 2 + 1)
		goto err;
	version.major = [fullver characterAtIndex:0] - '0';
	if([fullver characterAtIndex:1] != '.')
		goto err;
	version.minor = [fullver characterAtIndex:2] - '0';
	if([fullver characterAtIndex:3] != '.')
		goto err;
	version.patch = [fullver characterAtIndex:4] - '0';
	version.revision = [revstr intValue];

	uint32_t ret = version.revision | (version.patch << 8) | (version.minor << 16) | (version.major << 24);
	NSLog(@"App version %@-%@  ->  %u", fullver, revstr, ret);
	return ret;

err:
	NSLog(@"VERSION PARSING ERROR: Only versions in the format x.x.x can be used");
	exit(1);
}

static uint32_t read_version(NSString *path)
{
	NSString *filename = [path stringByAppendingPathComponent:@"_version"];
	NSError *error;
	NSString *content = [NSString stringWithContentsOfFile:filename encoding:NSASCIIStringEncoding error:&error];
	if(error)
		return 0;
	return [content intValue];
}

static inline void write_version(NSString *path, uint32_t ver)
{
	NSString *filename = [path stringByAppendingPathComponent:@"_version"];
	NSString *content = [NSString stringWithFormat:@"%d", ver];
	[content writeToFile:filename atomically:NO encoding:NSASCIIStringEncoding error:nil];
}

static void recursive_delete(NSString *path)
{
	NSFileManager *fm = [NSFileManager defaultManager];
	for(NSString* file in [fm contentsOfDirectoryAtPath:path error:nil])
		[fm removeItemAtPath:[path stringByAppendingPathComponent:file] error:nil];
}

/**************/

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
	// versioned: update and DELETE previous files with each App update
	const struct { const char *name; int path; BOOL versioned; } assets[] = {
		{ .name = "assets", .path = PATH_LIBRARY_SUPPORT, .versioned = YES },
		{ .name = "worlds", .path = PATH_DOCUMENTS, .versioned = NO },
		{ NULL, 0 },
	};
	char buf[256];
	uint32_t v_runtime = parse_version();

	for(int i = 0; assets[i].name != NULL; i++) {
		ioswrap_paths(assets[i].path, buf, sizeof(buf));
		NSString *destpath = [NSString stringWithUTF8String:buf];
		NSString *zippath = [[NSBundle mainBundle] pathForResource:[NSString stringWithUTF8String:assets[i].name] ofType:@"zip"];

#ifdef DEBUG
		// always replace assets in debug mode
		recursive_delete(destpath);
		goto extract;
#else
		if(!assets[i].versioned)
			goto extract;
#endif
		uint32_t v_disk = read_version(destpath);
		if(v_runtime <= v_disk) {
			NSLog(@"%s: skipping update (%d)", assets[i].name, v_disk);
			continue;
		}
		NSLog(@"%s: updating from %d to %d", assets[i].name, v_disk, v_runtime);
		recursive_delete(destpath); // delete assets before updating them

extract:
		NSLog(@"%s: extract %@ to %@", assets[i].name, zippath, destpath);
		[SSZipArchive unzipFileAtPath:zippath toDestination:destpath];
		write_version(destpath, v_runtime);
	}
}

void ioswrap_size(unsigned int *dest)
{
    CGSize bounds = [[UIScreen mainScreen] bounds].size;
    CGFloat scale = [[UIScreen mainScreen] scale];
    dest[0] = bounds.width * scale;
    dest[1] = bounds.height * scale;
}

/********/

static int dialog_state;
static char dialog_text[512];

#define DIALOG_MULTILINE  1
#define DIALOG_SINGLELINE 2
#define DIALOG_PASSWORD   3

void ioswrap_show_dialog(void *uiviewcontroller, const char *accept, const char *hint, const char *current, int type)
{
	UIViewController *viewc = (__bridge UIViewController*) uiviewcontroller;
	NSString *accept_ = [NSString stringWithUTF8String:accept];
	(void) hint; // unused

	UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"Text Input" message:nil preferredStyle:UIAlertControllerStyleAlert];
	[alert addTextFieldWithConfigurationHandler:^(UITextField *textField) {
		textField.text = [NSString stringWithUTF8String:current];
		if(type == DIALOG_PASSWORD)
			textField.secureTextEntry = YES;
	}];
	[alert addAction:[UIAlertAction actionWithTitle:accept_ style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
		dialog_state = 0;
		strncpy(dialog_text, alert.textFields[0].text.UTF8String, sizeof(dialog_text));
	}]];

	dialog_state = -1;
	dialog_text[0] = 0;
	[viewc presentViewController:alert animated:YES completion:nil];
}

int ioswrap_get_dialog(const char **text)
{
	int ret = dialog_state;
	if(text) {
		*text = dialog_text;
		dialog_state = -1; // reset
	}

	return ret;
}
