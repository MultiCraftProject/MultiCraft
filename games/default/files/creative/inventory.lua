local player_inventory = {}
local inventory_cache = {}

local offset = {}
local hoch = {}
local bg = {}

offset["blocks"] = "-0.29,-0.25"
offset["deco"] = "0.98,-0.25"
offset["mese"] = "2.23,-0.25"
offset["rail"] = "3.495,-0.25"
offset["misc"] = "4.75,-0.25"
offset["all"] = "8.99,-0.25"
offset["food"] = "-0.29,8.12"
offset["tools"] = "0.98,8.12"
offset["combat"] = "2.23,8.12"
offset["brew"] = "4.78,8.12"
offset["matr"] = "3.495,8.12"
offset["inv"] = "8.99,8.12"

hoch["blocks"] = ""
hoch["deco"] = ""
hoch["mese"] = ""
hoch["rail"] = ""
hoch["misc"] = ""
hoch["all"] = ""
hoch["food"] = "^[transformfy"
hoch["tools"] = "^[transformfy"
hoch["combat"] = "^[transformfy"
hoch["brew"] = "^[transformfy"
hoch["matr"] = "^[transformfy"
hoch["inv"] = "^[transformfy"

bg["blocks"] = "default_grass_side.png"
bg["deco"] = "creative_deko.png"
bg["mese"] = "creative_mese.png"
bg["rail"] = "boats_inventory.png"
bg["misc"] = "bucket_water.png"
bg["all"] = "creative_all.png"
bg["food"] = "creative_food.png"
bg["tools"] = "creative_tool.png"
bg["combat"] = "creative_sword.png"
bg["brew"] = "creative_brew.png"
bg["matr"] = "default_emerald.png"
bg["inv"] = "creative_inv.png"

local function get_item_list(group)
	local item_list = {}
    for name, def in pairs(minetest.registered_items) do
        if (not def.groups.not_in_creative_inventory or
				def.groups.not_in_creative_inventory == 0) and
				def.description and def.description ~= "" then
            if minetest.get_item_group(name, group) > 0 then
				item_list[name] = def
            end
		end
	end
	return item_list
end

local function init_creative_cache(items)
	inventory_cache[items] = {}
	local i_cache = inventory_cache[items]

	for name, def in pairs(items) do
		if def.groups.not_in_creative_inventory ~= 1 and
				def.description and def.description ~= "" then
			i_cache[name] = def
		end
	end
	table.sort(i_cache)
	return i_cache
end

function creative.init_creative_inventory(player)
	local player_name = player:get_player_name()
	player_inventory[player_name] = {
		size = 0,
		filter = "",
		start_i = 0
	}

	minetest.create_detached_inventory("creative_" .. player_name, {
		allow_move = function(inv, from_list, from_index, to_list, to_index, count, player2)
			local name = player2 and player2:get_player_name() or ""
			if not creative.is_enabled_for(name) or
					to_list == "main" then
				return 0
			end
			return count
		end,
		allow_put = function(inv, listname, index, stack, player2)
			return 0
		end,
		allow_take = function(inv, listname, index, stack, player2)
			local name = player2 and player2:get_player_name() or ""
			if not creative.is_enabled_for(name) then
				return 0
			end
			return -1
		end,
		on_move = function(inv, from_list, from_index, to_list, to_index, count, player2)
		end,
		on_take = function(inv, listname, index, stack, player2)
			if stack and stack:get_count() > 0 then
				minetest.log("action", player_name .. " takes " .. stack:get_name().. " from creative inventory")
			end
		end,
	}, player_name)

	return player_inventory[player_name]
end

function creative.update_creative_inventory(player_name, tab_content)
	local creative_list = {}
	local inv = player_inventory[player_name] or
			creative.init_creative_inventory(minetest.get_player_by_name(player_name))
	local player_inv = minetest.get_inventory({type = "detached", name = "creative_" .. player_name})

	local items = inventory_cache[tab_content] or init_creative_cache(tab_content)

	for name, def in pairs(items) do
		if def.name:find(inv.filter, 1, true) or
				def.description:lower():find(inv.filter, 1, true) then
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

creative.formspec_add = ""

local function get_creative_formspec(player_name, start_i, pagenum, page, pagemax)
    pagenum = math.floor(pagenum) or 1
    local slider_height = 4 / pagemax
    local slider_pos = slider_height * (pagenum - 1) + 2.2
    local formspec = ""
    local main_list = "list[detached:creative_" .. player_name ..
		";main;0,1.75;9,5;"..tostring(start_i).."]"
    local name = "all"
    if page ~= nil then name = page end
    if name == "inv" then
        main_list = "image[-0.2,1.7;11.35,2.33;creative_bg.png]"..
            "image[-0.3,0.15;3,4.3;inventory_armor.png]"..
            "list[current_player;main;0,3.75;9,3;9]"..
            "list[detached:"..player_name.."_armor;armor;0.02,1.7;1,1;]"..
            "list[detached:"..player_name.."_armor;armor;0.02,2.7;1,1;1]"..
            "list[detached:"..player_name.."_armor;armor;0.98,1.7;1,1;2]"..
            "list[detached:"..player_name.."_armor;armor;0.98,2.7;1,1;3]"
    end
    local formspec = "image_button_exit[8.4,-0.1;0.75,0.75;close.png;exit;;true;true;]"..
        "background[-0.19,-0.25;10.5,9.87;inventory_creative.png]"..
        "bgcolor[#080808BB;true]"..
        "listcolors[#9990;#FFF7;#FFF0;#160816;#D4D2FF]"..
        "label[-5,-5;"..name.."]"..
        "image[" .. offset[name] .. ";1.5,1.44;creative_active.png"..hoch[name]..
		"^[combine:107x98:21,17="..bg[name].."]"..
        "image_button[-0.1,0;1,1;"..bg["blocks"]..";build;;;false]".. --build blocks
        "image_button[1.15,0;1,1;"..bg["deco"]..";deco;;;false]"..    --decoration blocks
        "image_button[2.415,0;1,1;"..bg["mese"]..";mese;;;false]"..   --bluestone
        "image_button[3.693,0;1,1;"..bg["rail"]..";rail;;;false]"..   --transportation
        "image_button[4.93,0;1,1;"..bg["misc"]..";misc;;;false]"..    --miscellaneous
        "image_button[9.19,0;1,1;"..bg["all"]..";default;;;false]"..  --search
        "image[0,1;5,0.75;fnt_"..name..".png]"..
        "list[current_player;main;0,7;9,1;]"..
        main_list..
        "image_button[9.03,1.74;0.85,0.6;creative_up.png;creative_prev;]"..
        "image_button[9.03,6.15;0.85,0.6;creative_down.png;creative_next;]"..
        "image_button[-0.1,8.28;1,1;"..bg["food"]..";food;;;false]"..   --foodstuff
        "image_button[1.15,8.28;1,1;"..bg["tools"]..";tools;;;false]".. --tools
        "image_button[2.415,8.28;1,1;"..bg["combat"]..";combat;;;false]".. --combat
        "image_button[3.693,8.28;1,1;"..bg["matr"]..";matr;;;false]"..  --brewing
        "image_button[4.93,8.28;1,1;"..bg["brew"]..";brew;;;false]"..   --materials
        "image_button[9.19,8.28;1,1;"..bg["inv"]..";inv;;;false]"..     --inventory
        "list[detached:creative_trash;main;9.02,7.02;1,1;]"..
        "image[9.04," .. tostring(slider_pos) .. ";0.78,"..tostring(slider_height) .. ";creative_slider.png]"

	if name == "all" then
		formspec = formspec .. "field_close_on_enter[suche;false]" ..
			"field[5.3,1.3;4,0.75;suche;;]"
	end
	if pagenum ~= nil then
		formspec = formspec .. "p"..tostring(pagenum)
	end
	return formspec
end

function creative.register_tab(name, title, items)
	sfinv.register_page("creative:" .. name, {
		title = title,
		is_in_nav = function(self, player, context)
			return creative.is_enabled_for(player:get_player_name())
		end,
		get = function(self, player, context)
			local player_name = player:get_player_name()
			creative.update_creative_inventory(player_name, items)
			local inv = player_inventory[player_name]
			local start_i = inv.start_i or 0
			local pagenum = math.floor(start_i / (5*9) + 1)
			local pagemax = math.ceil(inv.size / (5*9))
			local formspec =  get_creative_formspec(player_name, start_i,
					pagenum, name, pagemax)
			return sfinv.make_formspec(player, context, formspec, false, "size[10,9.3]")
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
			assert(inv)
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
			elseif fields.suche and
					fields.key_enter_field == "suche" then
				inv.start_i = 0
				inv.filter = fields.suche:lower()
				creative.update_creative_inventory(player_name, items)
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

creative.register_tab("all", "All", minetest.registered_items)
creative.register_tab("inv", "Inv", {})
minetest.after(0, function()
	creative.register_tab("blocks", "1", get_item_list("building"))
	creative.register_tab("deco", "2", get_item_list("decorative"))
	creative.register_tab("mese", "3", get_item_list("mese"))
	creative.register_tab("rail", "4", get_item_list("rail"))
	creative.register_tab("misc", "5", get_item_list("misc"))
	creative.register_tab("food", "6", get_item_list("foodstuffs"))
	creative.register_tab("tools", "7", get_item_list("tools"))
	creative.register_tab("combat", "8", get_item_list("combat"))
	creative.register_tab("matr", "9", get_item_list("materials"))
	creative.register_tab("brew", "10", get_item_list("brewing"))
end)

local old_homepage_name = sfinv.get_homepage_name
function sfinv.get_homepage_name(player)
	if creative.is_enabled_for(player:get_player_name()) then
		return "creative:all"
	else
		return old_homepage_name(player)
	end
end
