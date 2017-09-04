--Minetest
--Copyright (C) 2014 sapier
--
--This program is free software; you can redistribute it and/or modify
--it under the terms of the GNU Lesser General Public License as published by
--the Free Software Foundation; either version 3.0 of the License, or
--(at your option) any later version.
--
--This program is distributed in the hope that it will be useful,
--but WITHOUT ANY WARRANTY; without even the implied warranty of
--MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--GNU Lesser General Public License for more details.
--
--You should have received a copy of the GNU Lesser General Public License along
--with this program; if not, write to the Free Software Foundation, Inc.,
--51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

local function current_game()
	local last_game_id = core.settings:get("menu_last_game")
	local game, index = gamemgr.find_by_gameid(last_game_id)

	return game
end


local function get_formspec(tabview, name, tabdata)
	local retval = ""

	local index = filterlist.get_current_index(menudata.worldlist,
				tonumber(core.settings:get("mainmenu_last_selected_world"))
				)

		retval = retval ..
			"image_button[0.45,4.9;2.9,0.8;" ..
				core.formspec_escape(defaulttexturedir ..
					"multicraft_local_delete_btn.png") .. ";world_delete;;true;false]" ..
			"image_button[3.14,4.9;2.9,0.8;" ..
				core.formspec_escape(defaulttexturedir ..
					"multicraft_local_new_btn.png") .. ";world_create;;true;false]"

	local creative_mode = core.settings:get_bool("creative_mode")

	retval = retval ..
			"image_button[7,1.5;4.5,1.27;" ..
				core.formspec_escape(defaulttexturedir ..
					"multicraft_local_play_btn.png") .. ";play;;true;false]" ..
			"image_button[7.25,3.15;4.05,0.8;" ..
				core.formspec_escape(defaulttexturedir ..
					"multicraft_local_creative_" ..
					tostring(creative_mode) .. "_btn.png") ..
					";cb_creative_mode;;true;false]" ..
			"textlist[0,0;6.25,4.63;sp_worlds;" ..
			menu_render_worldlist() ..
			";" .. index .. ";true]"
	return retval
end

local function main_button_handler(this, fields, name, tabdata)
	assert(name == "local")

	local world_doubleclick = false

	if fields["sp_worlds"] ~= nil then
		local event = core.explode_textlist_event(fields["sp_worlds"])
		local selected = core.get_textlist_index("sp_worlds")

		menu_worldmt_legacy(selected)

		if event.type == "DCL" then
			world_doubleclick = true
		end

		if event.type == "CHG" and selected ~= nil then
			core.settings:set("mainmenu_last_selected_world",
				menudata.worldlist:get_raw_index(selected))
			return true
		end
	end

	if menu_handle_key_up_down(fields,"sp_worlds","mainmenu_last_selected_world") then
		return true
	end

	if fields.cb_creative_mode then
		local creative_mode = core.settings:get_bool("creative_mode")
		core.settings:set("creative_mode", tostring((not creative_mode)))
		core.settings:set("enable_damage", tostring(creative_mode))

		return true
	end

	if fields["cb_server"] then
		core.settings:set("enable_server", fields["cb_server"])

		return true
	end

	if fields["cb_server_announce"] then
		core.settings:set("server_announce", fields["cb_server_announce"])
		local selected = core.get_textlist_index("srv_worlds")
		menu_worldmt(selected, "server_announce", fields["cb_server_announce"])

		return true
	end

	if fields["play"] ~= nil or world_doubleclick or fields["key_enter"] then
		local selected = core.get_textlist_index("sp_worlds")
		gamedata.selected_world = menudata.worldlist:get_raw_index(selected)

		if core.settings:get_bool("enable_server") then
			if selected ~= nil and gamedata.selected_world ~= 0 then
				gamedata.playername     = fields["te_playername"]
				gamedata.password       = fields["te_passwd"]
				gamedata.port           = fields["te_serverport"]
				gamedata.address        = ""

				core.settings:set("port",gamedata.port)
				if fields["te_serveraddr"] ~= nil then
					core.settings:set("bind_address",fields["te_serveraddr"])
				end

				--update last game
				local world = menudata.worldlist:get_raw_element(gamedata.selected_world)
				if world then
					local game, index = gamemgr.find_by_gameid(world.gameid)
					core.settings:set("menu_last_game", game.id)
				end

				core.start()
			else
				gamedata.errormessage =
					fgettext("No world created or selected!")
			end
		else
			if selected ~= nil and gamedata.selected_world ~= 0 then
				gamedata.singleplayer = true
				core.start()
			else
				gamedata.errormessage =
					fgettext("No world created or selected!")
			end
			return true
		end
	end

	if fields["world_create"] ~= nil then
		local create_world_dlg = create_create_world_dlg(true)
		create_world_dlg:set_parent(this)
		this:hide()
		create_world_dlg:show()
		return true
	end

	if fields["world_delete"] ~= nil then
		local selected = core.get_textlist_index("sp_worlds")
		if selected ~= nil and
			selected <= menudata.worldlist:size() then
			local world = menudata.worldlist:get_list()[selected]
			if world ~= nil and
				world.name ~= nil and
				world.name ~= "" then
				local index = menudata.worldlist:get_raw_index(selected)
				local delete_world_dlg = create_delete_world_dlg(world.name,index)
				delete_world_dlg:set_parent(this)
				this:hide()
				delete_world_dlg:show()
			end
		end

		return true
	end

	if fields["world_configure"] ~= nil then
		local selected = core.get_textlist_index("sp_worlds")
		if selected ~= nil then
			local configdialog =
				create_configure_world_dlg(
						menudata.worldlist:get_raw_index(selected))

			if (configdialog ~= nil) then
				configdialog:set_parent(this)
				this:hide()
				configdialog:show()
			end
		end

		return true
	end
end

local function on_change(type, old_tab, new_tab)
end

--------------------------------------------------------------------------------
return {
	name = "local",
	caption = fgettext("Single Player"),
	cbf_formspec = get_formspec,
	cbf_button_handler = main_button_handler,
	on_change = on_change
}
