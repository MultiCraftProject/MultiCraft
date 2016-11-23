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

mt_color_grey  = "#AAAAAA"
mt_color_blue  = "#0000DD"
mt_color_green = "#00DD00"
mt_color_dark_green = "#003300"

--for all other colors ask sfan5 to complete his work!

local menupath = core.get_mainmenu_path()
local basepath = core.get_builtin_path()
defaulttexturedir = core.get_texturepath_share() .. DIR_DELIM .. "base" .. DIR_DELIM
local use_simple_menu = (PLATFORM == "Android" or PLATFORM == "iOS")

dofile(basepath .. DIR_DELIM .. "common" .. DIR_DELIM .. "async_event.lua")
dofile(basepath .. DIR_DELIM .. "common" .. DIR_DELIM .. "filterlist.lua")
dofile(basepath .. DIR_DELIM .. "fstk" .. DIR_DELIM .. "buttonbar.lua")
dofile(basepath .. DIR_DELIM .. "fstk" .. DIR_DELIM .. "dialog.lua")
dofile(basepath .. DIR_DELIM .. "fstk" .. DIR_DELIM .. "tabview.lua")
dofile(basepath .. DIR_DELIM .. "fstk" .. DIR_DELIM .. "ui.lua")
dofile(menupath .. DIR_DELIM .. "common.lua")
dofile(menupath .. DIR_DELIM .. "gamemgr.lua")
dofile(menupath .. DIR_DELIM .. "textures.lua")
dofile(menupath .. DIR_DELIM .. "dlg_create_world.lua")
--dofile(menupath .. DIR_DELIM .. "dlg_delete_mod.lua")
dofile(menupath .. DIR_DELIM .. "dlg_delete_world.lua")
--dofile(menupath .. DIR_DELIM .. "dlg_rename_modpack.lua")
dofile(menupath .. DIR_DELIM .. "dlg_config_world.lua")
if not use_simple_menu then
	dofile(menupath .. DIR_DELIM .. "modmgr.lua")
--	dofile(menupath .. DIR_DELIM .. "store.lua")
	dofile(menupath .. DIR_DELIM .. "dlg_settings_advanced.lua")
end

local tabs = {}

--tabs.mods = dofile(menupath .. DIR_DELIM .. "tab_mods.lua")
tabs.credits = dofile(menupath .. DIR_DELIM .. "tab_credits.lua")
tabs.singleplayer = dofile(menupath .. DIR_DELIM .. "tab_singleplayer.lua")
tabs.multiplayer = dofile(menupath .. DIR_DELIM .. "tab_multiplayer.lua")
tabs.server = dofile(menupath .. DIR_DELIM .. "tab_server.lua")
if not use_simple_menu then
	tabs.settings = dofile(menupath .. DIR_DELIM .. "tab_settings.lua")
	tabs.texturepacks = dofile(menupath .. DIR_DELIM .. "tab_texturepacks.lua")
end

--------------------------------------------------------------------------------
local function main_event_handler(tabview, event)
	if event == "MenuQuit" then
		core.close()
	end
	return true
end

--------------------------------------------------------------------------------
local function init_globals()
	-- Init gamedata
	gamedata.worldindex = 0

	menudata.worldlist = filterlist.create(
		core.get_worlds,
		compare_worlds,
		-- Unique id comparison function
		function(element, uid)
			return element.name == uid
		end,
		-- Filter function
		function(element, gameid)
			return element.gameid == gameid
		end
	)

	menudata.worldlist:add_sort_mechanism("alphabetic", sort_worlds_alphabetic)
	menudata.worldlist:set_sortmode("alphabetic")

	core.setting_set("menu_last_game", "default")

	mm_texture.init()

	-- Create main tabview
	local tv_main = tabview_create("maintab", {x = 12, y = 5.2}, {x = 0, y = 0})

	tv_main:set_autosave_tab(true)
	tv_main:add(tabs.singleplayer)
	
if PLATFORM ~= "iOS" then
	tv_main:add(tabs.multiplayer)
	tv_main:add(tabs.server)
end

	if not use_simple_menu then
		tv_main:add(tabs.settings)
		tv_main:add(tabs.texturepacks)
	end

	--tv_main:add(tabs.mods)
	tv_main:add(tabs.credits)

	tv_main:set_global_event_handler(main_event_handler)
	tv_main:set_fixed_size(false)

	tv_main:set_tab(core.setting_get("maintab_LAST"))
	ui.set_default("maintab")
	tv_main:show()

	-- Create modstore ui
	--if PLATFORM == "Android" then
	--	modstore.init({x = 12, y = 6}, 3, 2)
	--else
	--	modstore.init({x = 12, y = 8}, 4, 3)
	--end

	ui.update()

	core.sound_play("main_menu", false)

	minetest.set_clouds(false)
	mm_texture.set_dirt_bg()
end

init_globals()
