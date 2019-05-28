local has_armor = minetest.get_modpath("3d_armor")
local player_inventory = {}
local inventory_cache = {}

local ofs_tab = {}
local ofs_img = {}
local bg = {}
local rot = {}

rot["all"] = "^[transformR270"
rot["inv"] = "^[transformR270"
rot["blocks"] = ""
rot["deco"] = ""
rot["mese"] = ""
rot["rail"] = ""
rot["misc"] = ""
rot["food"] = ""
rot["tools"] = ""
rot["combat"] = ""
rot["matr"] = ""
rot["brew"] = ""

ofs_tab["all"] = "10.11,0.84"
ofs_tab["inv"] = "10.11,6.93"
ofs_tab["blocks"] = "-0.31,-0.35"
ofs_tab["deco"] = "0.72,-0.35"
ofs_tab["mese"] = "1.78,-0.35"
ofs_tab["rail"] = "2.81,-0.35"
ofs_tab["misc"] = "3.85,-0.35"
ofs_tab["food"] = "4.9,-0.35"
ofs_tab["tools"] = "5.93,-0.35"
ofs_tab["combat"] = "6.96,-0.35"
ofs_tab["matr"] = "8,-0.35"
ofs_tab["brew"] = "9.01,-0.35"

ofs_img["all"] = "10.25,1"
ofs_img["inv"] = "10.25,7.11"
ofs_img["blocks"] = "-0.16,-0.15"
ofs_img["deco"] = "0.87,-0.15"
ofs_img["mese"] = "1.92,-0.15"
ofs_img["rail"] = "2.96,-0.15"
ofs_img["misc"] = "4,-0.15"
ofs_img["food"] = "5.05,-0.15"
ofs_img["tools"] = "6.1,-0.15"
ofs_img["combat"] = "7.15,-0.15"
ofs_img["matr"] = "8.17,-0.15"
ofs_img["brew"] = "9.2,-0.15"

bg["inv"] = "default_chest_front.png"
bg["blocks"] = "default_grass_side.png"
bg["deco"] = "default_sapling.png"
bg["mese"] = "jeija_lightstone_gray_on.png"
bg["rail"] = "boats_inventory.png"
bg["misc"] = "bucket_water.png"
bg["food"] = "default_apple.png"
bg["tools"] = "default_tool_diamondpick.png"
bg["combat"] = "default_tool_steelsword.png"
bg["matr"] = "default_emerald.png"
bg["brew"] = "potions_bottle.png"
bg["all"] = "default_paper.png"

local function init_creative_cache(tab_name, group)
	inventory_cache[tab_name] = {}
	local i_cache = inventory_cache[tab_name]
	local item_list = {}
	if group == "all" then
		for name, _ in pairs(minetest.registered_items) do
			table.insert(item_list, name)
		end
	elseif group then
		local input = io.open(minetest.get_modpath("creative")..
			"/categories/"..group..".lua", "r")
		if input then
			local data = input:read('*all')
			item_list = data and minetest.deserialize(data) or {}
			io.close(input)
		end
	end
	for _, name in pairs(item_list) do
		local def = minetest.registered_items[name]
		if def and def.description and def.description ~= "" and
				(not def.groups or def.groups.not_in_creative_inventory ~= 1) then
			i_cache[name] = def.description
		end
	end
	table.sort(i_cache)
end

local function init_creative_inventory(player_name)
	player_inventory[player_name] = {
		size = 0,
		filter = "",
		start_i = 0
	}

	minetest.create_detached_inventory("creative_" .. player_name, {
		allow_move = function(inv, from_list, from_index, to_list, to_index, count, player)
			local name = player and player:get_player_name() or ""
			if not creative.is_enabled_for(name) or
					to_list == "main" then
				return 0
			end
			return count
		end,
		allow_put = function(inv, listname, index, stack, player)
			return 0
		end,
		allow_take = function(inv, listname, index, stack, player)
			local name = player and player:get_player_name() or ""
			if not creative.is_enabled_for(name) then
				return 0
			end
			return -1
		end,
		on_move = function(inv, from_list, from_index, to_list, to_index, count, player)
		end,
		on_take = function(inv, listname, index, stack, player)
			if stack and stack:get_count() > 0 then
				minetest.log("action", player_name.." takes "..
					stack:get_name().." from creative inventory")
			end
		end,
	}, player_name)

	return player_inventory[player_name]
end

local function update_creative_inventory(player_name, tab_name)
	local player = minetest.get_player_by_name(player_name)
	if not player or not player:is_player() then
		return
	end
	local creative_list = {}
	local inv = player_inventory[player_name] or
			init_creative_inventory(player_name)
	local player_inv = minetest.get_inventory({type="detached",
			name="creative_"..player_name})
	if not player_inv then
		player_inventory[player_name] = nil
		return
	end

	local items = inventory_cache[tab_name] or {}
	for name, description in pairs(items) do
		if name:find(inv.filter, 1, true) or
				description:lower():find(inv.filter, 1, true) then
			creative_list[#creative_list+1] = name
		end
	end

	table.sort(creative_list)
	player_inv:set_size("main", #creative_list)
	player_inv:set_list("main", creative_list)
	inv.size = #creative_list
end

-- Create the trash field
local trash = minetest.create_detached_inventory("creative_trash", {
	-- Allow the stack to be placed and remove it in on_put()
	-- This allows the creative inventory to restore the stack
	allow_put = function(inv, listname, index, stack, player)
		return stack:get_count()
	end,
	on_put = function(inv, listname)
		inv:set_list(listname, {})
	end,
})
trash:set_size("main", 1)

local function get_creative_formspec(player_name, start_i, pagenum, page, pagemax)
	pagenum = math.floor(pagenum) or 1
	pagemax = (pagemax and pagemax ~= 0) and pagemax or 1
	local slider_height = 4 / pagemax - 0.04
	local slider_pos = 4 / pagemax * (pagenum - 1) + 2.14
	local main_list = "list[detached:creative_" .. player_name ..
		";main;0.02,1.68;9,5;"..tostring(start_i).."]"
	local name = "all"
	if page ~= nil then name = page end
	if name == "inv" then
		main_list = "image[-0.2,1.6;11.35,2.33;creative_bg.png]"..
			"list[current_player;main;0.02,3.68;9,3;9]"
		if has_armor then
			main_list = main_list.."image[-0.3,0.15;3,4.3;inventory_armor.png]"..
			"list[detached:"..player_name.."_armor;armor;0.03,1.69;1,1;]"..
			"list[detached:"..player_name.."_armor;armor;0.03,2.69;1,1;1]"..
			"list[detached:"..player_name.."_armor;armor;0.99,1.69;1,1;2]"..
			"list[detached:"..player_name.."_armor;armor;0.99,2.69;1,1;3]"
		end
	end
	local formspec = "image_button_exit[10.4,-0.1;0.75,0.75;close.png;exit;;true;true;]"..
		"background[-0.2,-0.26;11.55,8.49;inventory_creative.png]"..
		sfinv.gui_bg..
		sfinv.listcolors..
		"label[-5,-5;"..name.."]"..
		"image_button[-0.16,-0.15;1,1;"..bg["blocks"]..";build;;;false]"..	--build blocks
		"image_button[0.87,-0.15;1,1;"..bg["deco"]..";deco;;;false]"..		--decoration blocks
		"image_button[1.92,-0.15;1,1;"..bg["mese"]..";mese;;;false]"..		--bluestone
		"image_button[2.96,-0.15;1,1;"..bg["rail"]..";rail;;;false]"..		--transportation
		"image_button[4,-0.15;1,1;"..bg["misc"]..";misc;;;false]"..			--miscellaneous
		"image_button[5.05,-0.15;1,1;"..bg["food"]..";food;;;false]"..		--foodstuff
		"image_button[6.1,-0.15;1,1;"..bg["tools"]..";tools;;;false]"..		--tools
		"image_button[7.15,-0.15;1,1;"..bg["combat"]..";combat;;;false]"..	--combat
		"image_button[8.17,-0.15;1,1;"..bg["matr"]..";matr;;;false]"..		--materials
		"image_button[9.2,-0.15;1,1;"..bg["brew"]..";brew;;;false]"..		--brewing
		"image_button[10.25,1;1,1;"..bg["all"]..";default;;;false]"..		--all items
		"image_button[10.25,7.11;1,1;"..bg["inv"]..";inv;;;false]"..		--inventory
		"image_button_exit[10.3,2.5;1,1;creative_home_set.png;sethome_set;;true;true;]"..
		"image_button_exit[10.3,3.5;1,1;creative_home_go.png;sethome_go;;true;true;]"..
		"image[0,0.95;5,0.75;fnt_"..name..".png]"..
		"image_button[9.145,1.65;0.81,0.6;creative_up.png;creative_prev;]"..
		"image_button[9.145,6.08;0.81,0.6;creative_down.png;creative_next;]"..
		"list[current_player;main;0.02,6.93;9,1;]"..main_list..
		"list[detached:creative_trash;main;9.03,6.94;1,1;]"..
		"image["..ofs_tab[name]..";1.45,1.45;creative_active.png"..rot[name].."]"..
		"image["..ofs_img[name]..";1,1;"..bg[name].."]"..
		"image[9.165," .. tostring(slider_pos) .. ";0.7,"..tostring(slider_height) .. ";creative_slider.png]"

	if name == "all" then
		formspec = formspec .. "field_close_on_enter[search;false]"..
			"field[5.31,1.27;4.0,0.75;search;;]"..
			"image_button[9.14,0.93;0.81,0.82;creative_search.png;creative_search;;;false]"
	end
	if pagenum ~= nil then
		formspec = formspec .. "p"..tostring(pagenum)
	end
	return formspec
end

local function register_tab(name, title, group)
	init_creative_cache(name, group)
	sfinv.register_page("creative:" .. name, {
		title = title,
		is_in_nav = function(self, player, context)
			return creative.is_enabled_for(player:get_player_name())
		end,
		get = function(self, player, context)
			local player_name = player:get_player_name()
			update_creative_inventory(player_name, name)
			local inv = player_inventory[player_name]
			local start_i = inv.start_i or 0
			local pagenum = math.floor(start_i / (5*9) + 1)
			local pagemax = math.ceil(inv.size / (5*9))
			local formspec = get_creative_formspec(player_name, start_i,
					pagenum, name, pagemax)
			return sfinv.make_formspec(player, context, formspec, false, "size[11,7.7]")
		end,
		on_enter = function(self, player, context)
			local player_name = player:get_player_name()
			local inv = player_inventory[player_name]
			if inv then
				inv.start_i = 0
			end
		end,
		on_player_receive_fields = function(self, player, context, fields)
			local player_name = player:get_player_name()
			local inv = player_inventory[player_name]
			if not inv then
				return
			end
			inv.filter = ""

			if fields.build then
				sfinv.set_page(player, "creative:blocks")
			elseif fields.deco then
				sfinv.set_page(player, "creative:deco")
			elseif fields.mese then
				sfinv.set_page(player, "creative:mese")
			elseif fields.rail then
				sfinv.set_page(player, "creative:rail")
			elseif fields.misc then
				sfinv.set_page(player, "creative:misc")
			elseif fields.default then
				sfinv.set_page(player, "creative:all")
			elseif fields.food then
				sfinv.set_page(player, "creative:food")
			elseif fields.tools then
				sfinv.set_page(player, "creative:tools")
			elseif fields.combat then
				sfinv.set_page(player, "creative:combat")
			elseif fields.matr then
				sfinv.set_page(player, "creative:matr")
			elseif fields.inv then
				sfinv.set_page(player, "creative:inv")
			elseif fields.brew then
				sfinv.set_page(player, "creative:brew")
			elseif fields.search and
					(fields.creative_search or
					fields.key_enter_field == "search") then
				inv.start_i = 0
				inv.filter = fields.search:lower()
				update_creative_inventory(player_name, name)
				sfinv.set_player_inventory_formspec(player, context)
			elseif not fields.quit then
				local start_i = inv.start_i or 0
				if fields.creative_prev then
					start_i = start_i - 5*9
					if start_i < 0 then
						start_i = inv.size - (inv.size % (5*9))
						if inv.size == start_i then
							start_i = math.max(0, inv.size - (5*9))
						end
					end
				elseif fields.creative_next then
					start_i = start_i + 5*9
					if start_i >= inv.size then
						start_i = 0
					end
				end
				inv.start_i = start_i
				sfinv.set_player_inventory_formspec(player, context)
			end
		end
	})
end

register_tab("inv", "Inv")
minetest.after(0, function()
	register_tab("all", "All", "all")
	register_tab("blocks", "1", "building")
	register_tab("deco", "2", "decorative")
	register_tab("mese", "3", "mese")
	register_tab("rail", "4", "rail")
	register_tab("misc", "5", "misc")
	register_tab("food", "6", "foodstuffs")
	register_tab("tools", "7", "tools")
	register_tab("combat", "8", "combat")
	register_tab("matr", "9", "materials")
	register_tab("brew", "10", "brewing")
end)

local old_homepage_name = sfinv.get_homepage_name
function sfinv.get_homepage_name(player)
	if creative.is_enabled_for(player:get_player_name()) then
		return "creative:all"
	else
		return old_homepage_name(player)
	end
end
