/*
MultiCraft

Copyright (C) 2014-2018 Maksim Gamarnik [MoNTE48] MoNTE48@mail.ua

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 3.0 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

#pragma once

#include "irrlichttypes.h"
#include "porting.h"
#include "util/string.h"

class GUIPurchaseButton : public gui::IGUIElement
{
public:
	GUIPurchaseButton(gui::IGUIEnvironment *env, gui::IGUIElement *parent,
			ISimpleTextureSource *tsrc) :
		gui::IGUIElement(gui::EGUIET_ELEMENT, env, parent, -1,
				core::rect<s32>(0, 0, 0, 0)),
		m_texture(NULL)
	{
		std::string texture = porting::path_share + DIR_DELIM + "textures" +
			DIR_DELIM + "base" + DIR_DELIM + "local_buy_btn.png";
		m_texture = tsrc->getTexture(texture);
	}

	void draw()
	{
		if (!IsVisible)
			return;

		v2u32 screensize = Environment->getVideoDriver()->getScreenSize();
		if (screensize != m_screensize)
		{
			m_screensize = screensize;
			regenerateGui(screensize);
		}
		gui::IGUIElement::draw();
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

private:
	void regenerateGui(v2u32 size)
	{
		s32 w = size.X / 6; // button width = screen width / 6
		s32 h = w / 4; // button height = button width / 4
		setRelativePosition(core::rect<s32>(size.X - w, 0, size.X, h));
		gui::IGUIButton *btn = Environment->addButton(
				core::rect<s32>(0, 0, w, h), this, -1, L"Purchase");
		if (m_texture) {
			btn->setUseAlphaChannel(true);
			btn->setDrawBorder(false);
			btn->setImage(m_texture);
			btn->setPressedImage(m_texture);
			btn->setScaleImage(true);
			btn->setText(L"");
		}
	}

	video::ITexture *m_texture;
	v2u32 m_screensize;
};
