function round(num)
	return math.floor(num + 0.5)
end

local lasttime = "default"

minetest.register_globalstep(function(dtime)
	local now = round((minetest.get_timeofday() * 24) % 12)
	if now ~= lasttime then
		lasttime = now
		if now == 12 then now = 0 end
		local players  = minetest.get_connected_players()
		for i,player in ipairs(players) do
			if string.sub(player:get_wielded_item():get_name(), 0, 6) == "watch:" then
				player:set_wielded_item("watch:"..now)
			end
			for i,stack in ipairs(player:get_inventory():get_list("main")) do
				if i<9 and string.sub(stack:get_name(), 0, 6) == "watch:" then
					player:get_inventory():remove_item("main", stack:get_name())
					player:get_inventory():add_item("main", "watch:"..now)
				end
			end
		end
	end
end)

local images = {
	"watch_0.png",
	"watch_1.png",
	"watch_2.png",
	"watch_3.png",
	"watch_4.png",
	"watch_5.png",
	"watch_6.png",
	"watch_7.png",
	"watch_8.png",
	"watch_9.png",
	"watch_10.png",
	"watch_11.png",
}

local i
for i,img in ipairs(images) do
	local inv = 1
	if i == 1 then
		inv = 0
	end
	minetest.register_tool("watch:"..(i-1), {
		description = "Watch",
		inventory_image = img,
		wield_image = img,
		groups = {not_in_creative_inventory=inv}
	})
end

minetest.register_craft({
	output = 'watch:0',
	recipe = {
	{'', 'default:gold_ingot', ''},
	{'default:gold_ingot', 'default:bluestone_dust', 'default:gold_ingot'},
	{'', 'default:gold_ingot', ''}
	},
})
