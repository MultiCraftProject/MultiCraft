local c_dirt_with_grass				= minetest.get_content_id("default:dirt_with_grass")
local c_dirt_with_snow				= minetest.get_content_id("default:dirt_with_snow")
local c_dirt_with_dry_grass			= minetest.get_content_id("default:dirt_with_dry_grass")
local c_dirt_with_coniferous_litter	= minetest.get_content_id("default:dirt_with_coniferous_litter")
local c_sand						= minetest.get_content_id("default:sand")
local c_desert_sand					= minetest.get_content_id("default:desert_sand")
local c_silver_sand					= minetest.get_content_id("default:silver_sand")
--
local c_air							= minetest.get_content_id("air")
local c_snow						= minetest.get_content_id("default:snow")
local c_rose						= minetest.get_content_id("flowers:rose")
local c_viola						= minetest.get_content_id("flowers:viola")
local c_geranium					= minetest.get_content_id("flowers:geranium")
local c_tulip						= minetest.get_content_id("flowers:tulip")
local c_dandelion_y					= minetest.get_content_id("flowers:dandelion_yellow")
local c_dandelion_w					= minetest.get_content_id("flowers:dandelion_white")
local c_bush_leaves					= minetest.get_content_id("default:bush_leaves")
local c_bush_stem					= minetest.get_content_id("default:bush_stem")
local c_a_bush_leaves				= minetest.get_content_id("default:acacia_bush_leaves")
local c_a_bush_stem					= minetest.get_content_id("default:acacia_bush_stem")
local c_water_source				= minetest.get_content_id("default:water_source")
local c_water_flowing				= minetest.get_content_id("default:water_flowing")
-------------------------------------------------------------------------------
-- function to copy tables
-------------------------------------------------------------------------------
function villages.shallowCopy(original)
	local copy = {}
	for key, value in pairs(original) do
		copy[key] = value
	end
	return copy
end

function villages.round(num, numDecimalPlaces)
	local mult = 10^(numDecimalPlaces or 0)
	return math.floor(num * mult + 0.5) / mult
end
-------------------------------------------------------------------------------
--
-------------------------------------------------------------------------------
function villages.find_surface_heightmap(pos, minp)
	local surface_mat = {
		c_dirt_with_grass,
		c_dirt_with_snow,
		c_dirt_with_dry_grass,
		c_dirt_with_coniferous_litter,
		c_sand,
		c_desert_sand,
		c_silver_sand
	}
	local p6 = villages.shallowCopy(pos)
	local heightmap = minetest.get_mapgen_object("heightmap")
	-- get height of current pos p6
	local hm_i = (p6.x - minp.x + 1) + (((p6.z - minp.z)) * 80)
	p6.y = heightmap[hm_i]
	local vi = va:index(p6.x, p6.y, p6.z)
	local viname = minetest.get_name_from_content_id(data[vi])

	for i, mats in ipairs(surface_mat) do
		local node_check = va:index(p6.x, p6.y + 1, p6.z)
		if node_check and vi and data[vi] == mats and
			(data[node_check] ~= c_water_source) then
			--	local tmp = minetest.get_name_from_content_id(data[node_check])
			return p6, mats
		end
	end
end
-------------------------------------------------------------------------------
-- function to find surface block y coordinate
-- returns surface postion
-------------------------------------------------------------------------------
function villages.find_surface(pos)
	local p6 = villages.shallowCopy(pos)

	-- baseplate material, to replace dirt with grass and where buildings can be built
	local surface_mat = {
		"default:dirt_with_grass",
		"default:dirt_with_snow",
		"default:dirt_with_dry_grass",
		"default:dirt_with_coniferous_litter",
		"default:sand",
		"default:desert_sand",
	--	"default:snow"
	}
	local cnt = 0
	local itter -- count up or down
	local cnt_max = 200
	-- check, in which direction to look for surface
	local s = minetest.get_node_or_nil(p6)
	if s and string.find(s.name, "air") then
		itter = -1
	else
		itter = 1
	end
	while cnt < cnt_max do
		cnt = cnt + 1
		s = minetest.get_node_or_nil(p6)
		if s == nil or s.name == "ignore" then return nil end
		for i, mats in ipairs(surface_mat) do
			local node_check = minetest.get_node_or_nil({x = p6.x, y = p6.y + 1, z = p6.z})
			if node_check and s and s.name == mats and
				(string.find(node_check.name, "air") or
				 string.find(node_check.name, "snow") or
				 string.find(node_check.name, "fern") or
				 string.find(node_check.name, "flower") or
				 string.find(node_check.name, "bush") or
				 string.find(node_check.name, "tree") or
				 string.find(node_check.name, "grass")) then
				return p6, mats
			end
		end
		p6.y = p6.y + itter
	end
	return nil
end
-------------------------------------------------------------------------------
-- check distance for new building
-------------------------------------------------------------------------------
function villages.check_distance(building_pos, building_size)
	local distance
	for i, built_house in ipairs(village_info) do
		distance = math.sqrt(
			((building_pos.x - built_house["pos"].x) * (building_pos.x - built_house["pos"].x)) +
			((building_pos.z - built_house["pos"].z) * (building_pos.z - built_house["pos"].z)))
		if distance < building_size or
			distance < built_house["hsize"] then
			return false
		end
	end
	return true
end

local mod_storage = minetest.get_mod_storage()
-------------------------------------------------------------------------------
-- save list of generated villages
-------------------------------------------------------------------------------
function villages.save()
	mod_storage:set_string("villages_in_world", minetest.serialize(villages_in_world))
end
-------------------------------------------------------------------------------
-- load list of generated villages
-------------------------------------------------------------------------------
function villages.load()
	return minetest.deserialize(mod_storage:get_string("villages_in_world")) or {}
end
-------------------------------------------------------------------------------
-- check distance to other villages
-------------------------------------------------------------------------------
function villages.check_distance_other_villages(center_new_chunk)
	for i, pos in ipairs(villages_in_world) do
		local distance = vector.distance(center_new_chunk, pos)
		if villages.debug then
			minetest.chat_send_all("Dist: " .. distance)
		end
		if distance < villages.min_dist_villages then
			return false
		end
	end
	return true
end
-------------------------------------------------------------------------------
-- fill chests
-------------------------------------------------------------------------------
function villages.fill_chest(pos)
	-- find chests within radius
	--local chestpos = minetest.find_node_near(pos, 6, {"default:chest"})
	local chestpos = pos
	-- initialize chest (mts chests don't have meta)
	local meta = minetest.get_meta(chestpos)
	if meta:get_string("infotext") ~= "Chest" then
		minetest.registered_nodes["default:chest"].on_construct(chestpos)
	end
	-- fill chest
	local inv = minetest.get_inventory({type="node", pos=chestpos})
	if inv then
		-- always
		inv:add_item("main", "default:apple " .. math.random(1, 3))
		-- low value items
		if math.random(0, 1) < 1 then
			inv:add_item("main", "farming:bread " .. math.random(0, 3))
			inv:add_item("main", "default:steel_ingot " .. math.random(0, 3))
		end
		-- medium value items
		if math.random(0, 3) < 1 then
			inv:add_item("main", "default:pick_steel " .. math.random(0, 1))
			inv:add_item("main", "default:axe_steel " .. math.random(0, 1))
			inv:add_item("main", "fire:flint_and_steel " .. math.random(0, 1))
			inv:add_item("main", "bucket:bucket_empty " .. math.random(0, 1))
			inv:add_item("main", "default:sword_steel " .. math.random(0, 1))
		end
	end
end
-------------------------------------------------------------------------------
-- initialize furnace
-------------------------------------------------------------------------------
function villages.initialize_furnace(pos)
	-- find chests within radius
	local furnacepos = minetest.find_node_near(pos, 7, --radius
		{"default:furnace"})
	-- initialize furnacepos (mts furnacepos don't have meta)
	if furnacepos then
		local meta = minetest.get_meta(furnacepos)
		if meta:get_string("infotext") ~= "furnace" then
			minetest.registered_nodes["default:furnace"].on_construct(furnacepos)
		end
	end
end
-------------------------------------------------------------------------------
-- initialize furnace, chests, bookshelves
-------------------------------------------------------------------------------
function villages.initialize_nodes()
	for i, built_house in ipairs(village_info) do
		for j, schem in ipairs(schematic_table) do
			if village_info[i]["name"] == schem["name"] then
				building_all_info = schem
				break
			end
		end

		local width = building_all_info["hwidth"]
		local depth = building_all_info["hdepth"]
		local height = building_all_info["hheight"]

		local p = village_info[i]["pos"]
		for yi = 1, height do
			for xi = 0, width do
				for zi = 0, depth do
					local ptemp = {x = p.x + xi, y = p.y + yi, z = p.z + zi}
					local node = minetest.get_node(ptemp)
					if node.name == "default:furnace" or
						node.name == "default:chest" or
						node.name == "default:bookshelf" then
						minetest.registered_nodes[node.name].on_construct(ptemp)
					end
					-- when chest is found -> fill with stuff
					if node.name == "default:chest" then
						minetest.after(3, villages.fill_chest, ptemp)
					end
				end
			end
		end
	end
end
-------------------------------------------------------------------------------
-- randomize table
-------------------------------------------------------------------------------
function shuffle(tbl)
	local table = villages.shallowCopy(tbl)
	local size = #table
	for i = size, 1, -1 do
		local rand = math.random(size)
		table[i], table[rand] = table[rand], table[i]
	end
	return table
end
-------------------------------------------------------------------------------
-- get heightmap
-------------------------------------------------------------------------------
function villages.determine_heightmap(data, va, minp, maxp)
	-- max height and min height, initialize with impossible values for easier first time setting
	local max_y = -100
	local min_y = 100
	-- only analyze the center 40x40 of a chunk
	local cmaxp = {
		x = maxp.x - quarter_map_chunk_size,
		y = maxp.y, -- -quarter_map_chunk_size,
		z = maxp.z - quarter_map_chunk_size
	}
	local cminp = {
		x = minp.x + quarter_map_chunk_size,
		y = minp.y, -- + quarter_map_chunk_size,
		z = minp.z + quarter_map_chunk_size
	}
	-- walk through chunk and find surfaces
	for xi = cminp.x, cmaxp.x do
		for zi = cminp.z, cmaxp.z do
			local pos_surface = villages.find_surface({x = xi, y = cmaxp.y, z = zi}, data, va)
			-- check, if new found surface is higher or lower stored min_y or max_y
			if pos_surface then
				if pos_surface.y < min_y then
					min_y = pos_surface.y
				end
				if pos_surface.y > max_y then
					max_y = pos_surface.y
				end
			end
		end
	end
	-- return the difference between highest and lowest pos in chunk
	return max_y - min_y
end
-------------------------------------------------------------------------------
-- evaluate heightmap
-------------------------------------------------------------------------------
function villages.evaluate_heightmap()
	-- max height and min height, initialize with impossible values for easier first time setting
	local max_y = -50000
	local min_y = 50000
	-- only evaluate the center square of heightmap 40 x 40
	local square_start = 1621
	local square_end = 1661
	for j = 1, 40, 1 do
		for i = square_start, square_end, 1 do
			-- skip buggy heightmaps, return high value
			if heightmap[i] == nil then return end
			if heightmap[i] == -31000 or
				heightmap[i] == 31000 then
				return max_height_difference + 1
			end
			if heightmap[i] < min_y then
				min_y = heightmap[i]
			end
			if heightmap[i] > max_y then
				max_y = heightmap[i]
			end
		end
		-- set next line
		square_start = square_start + 80
		square_end = square_end + 80
	end
	-- return the difference between highest and lowest pos in chunk
	local height_diff = max_y - min_y
	-- filter buggy heightmaps
	if height_diff <= 1 then
		return max_height_difference + 1
	end
	-- debug info
	if villages.debug then
		minetest.chat_send_all("Heightdiff: " .. height_diff)
	end
	return height_diff
end
