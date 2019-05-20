local dyes = {
	{"white", "White"},
	{"grey", "Grey"},
	{"black", "Black"},
	{"red", "Red"},
	{"yellow", "Yellow"},
	{"green", "Green"},
	{"cyan", "Cyan"},
	{"blue", "Blue"},
	{"magenta", "Magenta"},
	{"orange", "Orange"},
	{"violet", "Violet"},
	{"brown", "Brown"},
	{"pink", "Pink"},
	{"dark_grey", "Dark Grey"},
	{"dark_green", "Dark Green"},
}

function default.node_wool_defaults(table)
	table = table or {}
	table.footstep = table.footstep or
		{name = "wool_coat_movement", gain = 1.0}
	table.dug = table.dug or
		{name = "wool_coat_movement", gain = 0.25}
	table.place = table.place or
		{name = "default_place_node", gain = 1.0}
	return table
end

local wool_sound = default.node_wool_defaults()

for i = 1, #dyes do
	local name, desc = unpack(dyes[i])

	minetest.register_node("wool:" .. name, {
		description = desc .. " Wool",
		tiles = {"wool_" .. name .. ".png"},
				is_ground_content = false,
		groups = {snappy = 2, choppy = 2, oddly_breakable_by_hand = 3,
			flammable = 3, wool = 1},
		sounds = wool_sound,
	})

	minetest.register_craft{
		type = "shapeless",
		output = "wool:" .. name,
		recipe = {"group:dye,color_" .. name, "group:wool"},
	}
end

-- Legacy
minetest.register_alias("wool:dark_blue", "wool:blue")
minetest.register_alias("wool:gold", "wool:yellow")