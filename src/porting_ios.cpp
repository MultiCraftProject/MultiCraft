#include <string>

#include "porting.h"
#include "config.h"
#include "ioswrap.h"

static void *uiviewcontroller;

namespace porting {
    void initializePathsiOS() {
        char buf[128];

        ioswrap_paths(PATH_DOCUMENTS, buf, sizeof(buf));
        path_user = std::string(buf);
		ioswrap_paths(PATH_LIBRARY_SUPPORT, buf, sizeof(buf));
		path_share = std::string(buf);
		path_locale = std::string(buf) + "/locale";
        ioswrap_paths(PATH_LIBRARY_CACHE, buf, sizeof(buf));
        path_cache = std::string(buf);
    }

    void copyAssets() {
		ioswrap_assets();
    }

    float getDisplayDensity() {
        return 1.0;
    }

    v2u32 getDisplaySize() {
        static bool firstrun = true;
        static v2u32 retval;

        if(firstrun) {
            unsigned int values[2];
            ioswrap_size(values);
            retval.X = values[0];
            retval.Y = values[1];
            firstrun = false;
        }

        return retval;
    }

	void setViewController(void *v) {
		uiviewcontroller = v;
	}

	void showInputDialog(const std::string &acceptButton, const std::string &hint,
						 const std::string &current, int editType)
	{
		ioswrap_show_dialog(uiviewcontroller, acceptButton.c_str(), hint.c_str(), current.c_str(), editType);
	}

	int getInputDialogState() {
		return ioswrap_get_dialog(NULL);
	}

	std::string getInputDialogValue() {
		const char *str;
		ioswrap_get_dialog(&str);
		return std::string(str);
	}

	void showPurchaseMenu() {
#ifdef ADS
		ads_show_purchase();
#endif
	}

	int getPurchaseState() {
#ifdef ADS
		return ads_purchase_state();
#else
		return 1;
#endif
	}
}


extern int real_main(int argc, char *argv[]);

void irrlicht_main() {
	static const char *args[] = {
		PROJECT_NAME,
	};
	real_main(1, (char**) args);
}
