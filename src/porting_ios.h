#ifndef __PORTING__IOS_H__
#define __PORTING__IOS_H__

#ifndef __IOS__
#error This file should only be included on iOS
#endif

namespace porting {
    void initializePathsiOS();
    void copyAssets();
	void setViewController(void *v);

	void showInputDialog(const std::string &acceptButton, const std::string &hint,
						 const std::string &current, int editType);
	int getInputDialogState();
	std::string getInputDialogValue();
}

#endif
