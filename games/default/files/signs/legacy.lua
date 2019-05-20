minetest.register_node(":default:sign_wall_wood", {
	tiles = {"default_wood.png"},
	paramtype = "light",
	paramtype2 = "wallmounted",
	sunlight_propagates = true,
	is_ground_content = false,
	walkable = false,
	groups = {choppy = 2, attached_node = 1, flammable = 2,
		oddly_breakable_by_hand = 3, not_in_creative_inventory = 1},
	legacy_wallmounted = true,
})

minetest.register_lbm({
	label = "Upgrade legacy signs",
	name = "signs:sign_wall_update",
	nodenames = {"default:sign_wall_wood"},
	run_at_every_load = false,
	action = function(pos, node)
		local meta = minetest.get_meta(pos)
		local text = meta:get_string("text")
		local p2 = minetest.get_node(pos).param2
		if p2 <= 1 then
			minetest.set_node(pos, {name = "signs:sign", param2 = 0})
			if text and text ~= "" then
				local obj = minetest.add_entity(vector.add(pos,
					signs.sign_positions[0][1]), "signs:sign_text")
				obj:set_properties({
					textures = {signs.generate_sign_texture(text), "blank.png"}
				})
				obj:set_yaw(signs.sign_positions[0][2])
			end
		elseif p2 <= 5 then
			p2 = p2 - 2
			minetest.set_node(pos, {name = "signs:wall_sign", param2 = p2 + 2})
			if text and text ~= "" then
				local obj = minetest.add_entity(vector.add(pos,
					signs.wall_sign_positions[p2][1]), "signs:sign_text")
				obj:set_properties({
					textures = {signs.generate_sign_texture(text), "blank.png"}
				})
				obj:set_yaw(signs.wall_sign_positions[p2][2])
			end
		end
	end,
})
