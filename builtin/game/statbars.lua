-- cache setting
--[[local enable_damage = core.settings:get_bool("enable_damage")

local health_bar_definition =
{
	hud_elem_type = "statbar",
	position = { x=0.5, y=1 },
	text = "server_flags_damage.png",
	number = 20,
	direction = 0,
	size = { x=24, y=24 },
	offset = { x=(-10*24)-25, y=-(48+24+16)},
}

local breath_bar_definition =
{
	hud_elem_type = "statbar",
	position = { x=0.5, y=1 },
	text = "halo.png",
	number = 20,
	direction = 0,
	size = { x=24, y=24 },
	offset = {x=25,y=-(48+24+16)},
}

local hud_ids = {}

local function initialize_builtin_statbars(player)

	if not player:is_player() then
		return
	end

	local name = player:get_player_name()

	if name == "" then
		return
	end

	if (hud_ids[name] == nil) then
		hud_ids[name] = {}
		-- flags are not transmitted to client on connect, we need to make sure
		-- our current flags are transmitted by sending them actively
		player:hud_set_flags(player:hud_get_flags())
	end

	if player:hud_get_flags().healthbar and enable_damage then
 		if hud_ids[name].id_healthbar == nil then
			health_bar_definition.number = player:get_hp()
			hud_ids[name].id_healthbar  = player:hud_add(health_bar_definition)
		end
	else
		if hud_ids[name].id_healthbar ~= nil then
			player:hud_remove(hud_ids[name].id_healthbar)
			hud_ids[name].id_healthbar = nil
		end
	end

	if (player:get_breath() < 11) then
		if player:hud_get_flags().breathbar and enable_damage then
			if hud_ids[name].id_breathbar == nil then
				hud_ids[name].id_breathbar = player:hud_add(breath_bar_definition)
			end
		else
			if hud_ids[name].id_breathbar ~= nil then
				player:hud_remove(hud_ids[name].id_breathbar)
				hud_ids[name].id_breathbar = nil
			end
		end
	elseif hud_ids[name].id_breathbar ~= nil then
		player:hud_remove(hud_ids[name].id_breathbar)
		hud_ids[name].id_breathbar = nil
	end
end

local function cleanup_builtin_statbars(player)

	if not player:is_player() then
		return
	end

	local name = player:get_player_name()

	if name == "" then
		return
	end

	hud_ids[name] = nil
end

local function player_event_handler(player,eventname)
	assert(player:is_player())

	local name = player:get_player_name()

	if name == "" then
		return
	end

	if eventname == "health_changed" then
		initialize_builtin_statbars(player)

		if hud_ids[name].id_healthbar ~= nil then
			player:hud_change(hud_ids[name].id_healthbar,"number",player:get_hp())
			return true
		end
	end

	if eventname == "breath_changed" then
		initialize_builtin_statbars(player)

		if hud_ids[name].id_breathbar ~= nil then
			player:hud_change(hud_ids[name].id_breathbar,"number",player:get_breath()*2)
			return true
		end
	end

	if eventname == "hud_changed" then
		initialize_builtin_statbars(player)
		return true
	end

	return false
end

function core.hud_replace_builtin(name, definition)

	if definition == nil or
		type(definition) ~= "table" or
		definition.hud_elem_type ~= "statbar" then
		return false
	end

	if name == "health" then
		health_bar_definition = definition

		for name,ids in pairs(hud_ids) do
			local player = core.get_player_by_name(name)
			if  player and hud_ids[name].id_healthbar then
				player:hud_remove(hud_ids[name].id_healthbar)
				initialize_builtin_statbars(player)
			end
		end
		return true
	end

	if name == "breath" then
		breath_bar_definition = definition

		for name,ids in pairs(hud_ids) do
			local player = core.get_player_by_name(name)
			if  player and hud_ids[name].id_breathbar then
				player:hud_remove(hud_ids[name].id_breathbar)
				initialize_builtin_statbars(player)
			end
		end
		return true
	end

	return false
end

core.register_on_joinplayer(initialize_builtin_statbars)
core.register_on_leaveplayer(cleanup_builtin_statbars)
core.register_playerevent(player_event_handler)]]

-- Hud Item name

local hud, timer, wield = {}, {}, {}
local timeout = 2

local function add_text(player)
	local player_name = player:get_player_name()
	hud[player_name] = player:hud_add({
		hud_elem_type = "text",
		position = {x = 0.5, y = 0.975},
		offset = {x = 0, y = -100},
		alignment = {x = 0, y = 0},
		number = 0xFFFFFF,
		text = "",
	})
end

core.register_on_joinplayer(function(player)
	core.after(1, add_text, player)
end)

core.register_globalstep(function(dtime)
	local players = core.get_connected_players()
	for i = 1, #players do
		local player = players[i]
		local player_name = player:get_player_name()

		local wielded_item = player:get_wielded_item()
		local wielded_item_name = wielded_item:get_name()

		timer[player_name] = timer[player_name] and timer[player_name] + dtime or 0
		wield[player_name] = wield[player_name] or ""

		if timer[player_name] > timeout and hud[player_name] then
			player:hud_change(hud[player_name], "text", "")
			timer[player_name] = 0
			return
		end

		if hud[player_name] and wielded_item_name ~= wield[player_name] then
			wield[player_name] = wielded_item_name
			timer[player_name] = 0

			local def = core.registered_items[wielded_item_name]
			local meta = wielded_item:get_meta()
			local meta_desc = meta:get_string("description")
			meta_desc = meta_desc:gsub("\27", ""):gsub("%(c@#%w%w%w%w%w%w%)", "")

			local description = meta_desc ~= "" and meta_desc or
				(def and (def.description:match("(.-)\n") or def.description) or "")

			player:hud_change(hud[player_name], "text", description)
		end
	end
end)
