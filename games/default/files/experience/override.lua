minetest.override_item("default:stone_with_coal", {
	groups = {cracky = 3, xp_min = 0, xp_max = 2},
})

minetest.override_item("default:wood", {
	groups = {dig_immediate = 3, xp_min = 4, xp_max = 6},
})
