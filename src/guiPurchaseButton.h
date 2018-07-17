/*
MultiCraft

Copyright (C) 2014-2018 Maksim Gamarnik [MoNTE48] MoNTE48@mail.ua

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

#pragma once

#include "irrlichttypes_extrabloated.h"
#include "porting.h"

class GUIPurchaseButton : public gui::IGUIElement
{
public:
	GUIPurchaseButton(gui::IGUIEnvironment *env, gui::IGUIElement *parent,
			ISimpleTextureSource *tsrc) :
		gui::IGUIElement(gui::EGUIET_ELEMENT, env, parent, -1,
				core::rect<s32>(0, 0, 0, 0))
	{
		core::dimension2du dim = env->getVideoDriver()->getScreenSize();
		s32 w = dim.Width / 6;
		s32 h = w / 4;
		setRelativePosition(core::rect<s32>(dim.Width - w, 0, dim.Width, h));
		gui::IGUIButton *btn = env->addButton(core::rect<s32>(0, 0, w, h), this,
			-1, L"Purchase");

		//TODO figure out why Irrlicht fails to find the texture
		//video::ITexture *texture = tsrc->getTexture("multicraft_local_buy_btn.png");
		//if (texture) {
		//	btn->setUseAlphaChannel(true);
		//	btn->setDrawBorder(false);
		//	btn->setImage(texture);
		//	btn->setPressedImage(texture);
		//	btn->setScaleImage(true);
		//	btn->setText(L"");
		//}
		btn->setText(L"Purchase");
	}
	bool OnEvent(const irr::SEvent &event)
	{
		if (event.EventType == irr::EET_GUI_EVENT &&
				event.GUIEvent.EventType == gui::EGET_BUTTON_CLICKED) {
			if (porting::getPurchaseState() == 0) {
				porting::showPurchaseMenu();
			} else {
				setVisible(false);
			}
			//setVisible(false);
			return true;
		}
		return gui::IGUIElement::OnEvent(event);
	}
};
