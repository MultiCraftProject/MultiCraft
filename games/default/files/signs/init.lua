signs = {}

signs.sign_positions = {
	[0] = {{x =  0.0075, y = 0.18, z = -0.065},  math.pi},
	[1] = {{x = -0.065,  y = 0.18, z =  0.0075}, math.pi / 2},
	[2] = {{x =  0.0075, y = 0.18, z =  0.065},  0},
	[3] = {{x =  0.065,  y = 0.18, z =  0.0075}, math.pi * 1.5},
}

signs.wall_sign_positions = {
	[0] = {{x =  0.437, y = -0.005, z =  0.06},  math.pi / 2},
	[1] = {{x = -0.437, y = -0.005, z = -0.06},  math.pi * 1.5},
	[2] = {{x = -0.06,  y = -0.005, z =  0.437}, math.pi},
	[3] = {{x =  0.06,  y = -0.005, z = -0.437}, 0},
}

local function generate_sign_line_texture(string, texture, row)
	for i = 1, 20 do
		local char = string:byte(i)
		if char and (char >= 32 and char <= 126) then
			texture = texture .. ":" .. (i - 1) * 16 .. ","
				.. row * 20 .. "=signs_" .. char .. ".png"
		elseif not char then
			break
		end
	end
	return texture
end

function signs.generate_sign_texture(string)
	if not string then
		return "blank.png"
	end
	local x_max = #string * 16
	local row = 0
	local texture = "[combine:" .. 16 * 20 .. "x100"
	for i = 1, 5 do
		local line_string = string:sub((20 * (i - 1)) + 1, 20 * i)
		if line_string == "" then
			break
		end
		texture = generate_sign_line_texture(line_string, texture, row)
		row = row + 1
	end
	return texture
end		

minetest.register_entity("signs:sign_text", {
	initial_properties = {
		visual = "upright_sprite",
		textures = {"blank.png", "blank.png"},
		visual_size = {x = 0.7, y = 0.6},
		collisionbox = {0, 0, 0, 0, 0, 0},
		selectionbox = {0, 0, 0, 0, 0, 0},
	},
	pointable = false,
	on_activate = function(self, staticdata)
		self.object:set_properties({
			textures = {signs.generate_sign_texture(staticdata), "blank.png"}
		})
	end,
	get_staticdata = function(self)
		local meta = minetest.get_meta(self.object:get_pos())
		local text = meta:get_string("sign_text")
		if text and text ~= "" then
			return text
		end
		return " "
	end
})

local function check_text(pos, wall)
	local meta = minetest.get_meta(pos)
	local text = meta:get_string("sign_text")
	if text and text ~= "" then
		local found = false
		for _, obj in pairs(minetest.get_objects_inside_radius(pos, 0.5)) do
                        local ent = obj:get_luaentity()
                        if ent and ent.name == "signs:sign_text" then
                                found = true
                                break
                        end
                end
		if not found then
			local p2 = minetest.get_node(pos).param2
			if wall then
				local obj = minetest.add_entity(vector.add(pos,
					signs.wall_sign_positions[p2][1]), "signs:sign_text")
				obj:set_properties({
					textures = {signs.generate_sign_texture(text), "blank.png"}
				})
				obj:set_yaw(signs.wall_sign_positions[p2][2])
			else
				local obj = minetest.add_entity(vector.add(pos,
					signs.sign_positions[p2][1]), "signs:sign_text")
				obj:set_properties({
					textures = {signs.generate_sign_texture(text), "blank.png"}
				})
				obj:set_yaw(signs.sign_positions[p2][2])
			end
		end
	end
end

minetest.register_lbm({
        label = "Check for sign text",
        name = "signs:sign_text",
        nodenames = {"signs:sign"},
        run_at_every_load = true,
        action = function(pos, node)
                check_text(pos, false)
        end,
})

minetest.register_lbm({
        label = "Check for sign text (Wall)",
        name = "signs:wall_sign_text",
        nodenames = {"signs:wall_sign"},
        run_at_every_load = true,
        action = function(pos, node)
		check_text(pos, true)
	end,
})

minetest.register_node("signs:sign", {
	description = "Sign",
	tiles = {"default_wood.png"},
	drawtype = "nodebox",
	paramtype = "light",
	paramtype2 = "facedir",
	node_placement_prediction = "",
	node_box = {
		type = "fixed",
		fixed = {
			{-0.375, -0.125, -0.0625, 0.375, 0.5, 0.0625}, -- NodeBox1
			{-0.0625, -0.5, -0.0625, 0.0625, -0.125, 0.0625}, -- NodeBox2
		}
	},
	on_place = function(itemstack, placer, pointed_thing)
		if pointed_thing.type == "node" then
			local undery = pointed_thing.under.y
			local posy = pointed_thing.above.y
			if undery > posy then -- Trying to place on celling, not allowed
				return itemstack
			elseif undery == posy then -- Wall sign
				local count, success = minetest.item_place(
					ItemStack("signs:wall_sign"), placer, pointed_thing)
				if success then
					return itemstack:take_item(1)
				end
			else -- Normal sign
				return minetest.item_place(itemstack, placer, pointed_thing)
			end
		end
	end,
	on_construct = function(pos)
		local meta = minetest.get_meta(pos)
		meta:set_string("formspec", "field[text;;${sign_text}]")
	end,
	on_destruct = function(pos)
		for _, obj in pairs(minetest.get_objects_inside_radius(pos, 0.5)) do
			local ent = obj:get_luaentity()
			if ent and ent.name == "signs:sign_text" then
				obj:remove()
				break
			end
		end
	end,
	on_punch = function(pos)
		check_text(pos, false)
	end,
	on_receive_fields = function(pos, formname, fields, sender)
		if not fields.text then
			return
		end
		local p2 = minetest.get_node(pos).param2
		if p2 > 3 then
			return
		end
		local found = false
		for _, obj in pairs(minetest.get_objects_inside_radius(pos, 0.5)) do
			local ent = obj:get_luaentity()
			if ent and ent.name == "signs:sign_text" then
				obj:set_properties(
					{textures = {signs.generate_sign_texture(fields.text), "blank.png"}
				})
				obj:set_pos(vector.add(pos, signs.sign_positions[p2][1]))
				obj:set_yaw(signs.sign_positions[p2][2])
				found = true
			end
		end
		if not found then
			local obj = minetest.add_entity(vector.add(pos, 
				signs.sign_positions[p2][1]), "signs:sign_text")
			obj:set_properties({
				textures = {signs.generate_sign_texture(fields.text), "blank.png"}
			})
			obj:set_yaw(signs.sign_positions[p2][2])
		end
		local meta = minetest.get_meta(pos)
		meta:set_string("sign_text", fields.text)
	end,
	groups = {oddly_breakable_by_hand = 1, choppy = 3, attached_node = 1},
})

minetest.register_node("signs:wall_sign", {
	description = "Sign",
	tiles = {"default_wood.png"},
	drawtype = "nodebox",
	paramtype = "light",
	paramtype2 = "wallmounted",
	node_box = {
		type = "wallmounted",
		wall_top    = {-0.4375, 0.4375, -0.3125, 0.4375, 0.5, 0.3125},
		wall_bottom = {-0.4375, -0.5, -0.3125, 0.4375, -0.4375, 0.3125},
		wall_side   = {-0.5, -0.3125, -0.4375, -0.4375, 0.3125, 0.4375},
	},
	drop = "signs:sign",
	on_construct = function(pos)
		local meta = minetest.get_meta(pos)
		meta:set_string("formspec", "field[text;;${sign_text}]")
	end,
	on_destruct = function(pos)
		for _, obj in pairs(minetest.get_objects_inside_radius(pos, 0.5)) do
			local ent = obj:get_luaentity()
			if ent and ent.name == "signs:sign_text" then
				obj:remove()
				break
			end
		end
	end,
	on_punch = function(pos)
                check_text(pos, true)
        end,
	on_receive_fields = function(pos, formname, fields, sender)
		if not fields.text then
			return
		end
		local p2 = minetest.get_node(pos).param2 - 2
		if p2 > 3 and p2 < 0 then
			return
		end
		local found = false
		for _, obj in pairs(minetest.get_objects_inside_radius(pos, 0.5)) do
			local ent = obj:get_luaentity()
			if ent and ent.name == "signs:sign_text" then
				obj:set_properties({
					textures = {signs.generate_sign_texture(fields.text), "blank.png"}
				})
				obj:set_pos(vector.add(pos, signs.wall_sign_positions[p2][1]))
				obj:set_yaw(signs.wall_sign_positions[p2][2])
				found = true
			end
		end
		if not found then
			local obj = minetest.add_entity(vector.add(pos, 
				signs.wall_sign_positions[p2][1]), "signs:sign_text")
			obj:set_properties({
				textures = {signs.generate_sign_texture(fields.text), "blank.png"}
			})
			obj:set_yaw(signs.wall_sign_positions[p2][2])
		end
		local meta = minetest.get_meta(pos)
		meta:set_string("sign_text", fields.text)
	end,
	groups = {oddly_breakable_by_hand = 1, choppy = 3,
		not_in_creative_inventory = 1, attached_node = 1},
})

dofile(minetest.get_modpath("signs") .. "/legacy.lua")

