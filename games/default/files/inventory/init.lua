local show_armor = minetest.get_modpath("3d_armor")

local function drop_fields(player, name)
	local inv = player:get_inventory()
	for i,stack in ipairs(inv:get_list(name)) do
		minetest.item_drop(stack, player, player:get_pos())
		stack:clear()
		inv:set_stack(name, i, stack)
	end
end

minetest.register_on_joinplayer(function(player)
	local inv = player:get_inventory()
	if inv then
		inv:set_size("main", 9*4)
	end
end)

sfinv.override_page("sfinv:inventory", {
	title = "Inventory",
	get = function(self, player, context)
		local form = [[
				background[-0.2,-0.26;9.41,9.49;formspec_inventory.png]
				background[-0.2,-0.26;9.41,9.49;formspec_inventory_inventory.png]
				image_button_exit[8.4,-0.1;0.75,0.75;close.png;exit;;true;true;]
				listcolors[#9990;#FFF7;#FFF0;#160816;#D4D2FF]
				list[current_player;craft;4,1;2,1;1]
				list[current_player;craft;4,2;2,1;4]
				list[current_player;craftpreview;7.05,1.53;1,1;]
				list[detached:split;main;8,3.14;1,1;]
				image[1.5,0;2,4;default_player2d.png]
			]]
		if show_armor then
			local player_name = player:get_player_name()
			form = form ..
			"list[detached:"..player_name.."_armor;armor;0,0;1,1;]"..
			"list[detached:"..player_name.."_armor;armor;0,1;1,1;1]"..
			"list[detached:"..player_name.."_armor;armor;0,2;1,1;2]"..
			"list[detached:"..player_name.."_armor;armor;0,3;1,1;3]"
		end
		return sfinv.make_formspec(player, context, form, true)
	end,
	on_player_receive_fields = function(self, player, context, fields)
		if fields.quit then
			drop_fields(player, "craft")
		end
	end,
})

local split_inv = minetest.create_detached_inventory("split", {
	allow_move = function(_, _, _, _, _, count, _)
		return count
	end,
	allow_put = function(_, _, _, stack, _)
		return stack:get_count() / 2
	end,
	allow_take = function(_, _, _, stack, _)
		return stack:get_count()
	end,
})
split_inv:set_size("main", 1)
