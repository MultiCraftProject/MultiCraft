multicraft.register_alias("mapgen_dandelion", "flowers:dandelion_yellow")
multicraft.register_alias("mapgen_rose", "flowers:rose")

multicraft.register_alias("mapgen_oxeye_daisy", "flowers:oxeye_daisy")

multicraft.register_alias("mapgen_tulip_orange", "flowers:tulip_orange")
multicraft.register_alias("mapgen_tulip_pink", "flowers:tulip_pink")
multicraft.register_alias("mapgen_tulip_red", "flowers:tulip_red")
multicraft.register_alias("mapgen_tulip_white", "flowers:tulip_white")

multicraft.register_alias("mapgen_allium", "flowers:allium")

multicraft.register_alias("mapgen_paeonia", "flowers:paeonia")

multicraft.register_alias("mapgen_houstonia", "flowers:houstonia")

multicraft.register_alias("mapgen_blue_orchid", "flowers:blue_orchid")

multicraft.register_on_generated(function(minp, maxp, seed)
	if maxp.y >= 3 and minp.y <= 0 then
		-- Generate flowers
		local perlin1 = multicraft.get_perlin(436, 3, 0.6, 100)
		-- Assume X and Z lengths are equal
		local divlen = 16
		local divs = (maxp.x-minp.x)/divlen+1;
		for divx=0,divs-1 do
		for divz=0,divs-1 do
			local x0 = minp.x + math.floor((divx+0)*divlen)
			local z0 = minp.z + math.floor((divz+0)*divlen)
			local x1 = minp.x + math.floor((divx+1)*divlen)
			local z1 = minp.z + math.floor((divz+1)*divlen)
			-- Determine flowers amount from perlin noise
			local grass_amount = math.floor(perlin1:get2d({x=x0, y=z0}) * 9)
			-- Find random positions for flowers based on this random
			local pr = PseudoRandom(seed+456)
			for i=0,grass_amount do
				local x = pr:next(x0, x1)
				local z = pr:next(z0, z1)
				-- Find ground level (0...15)
				local ground_y = nil
				for y=30,0,-1 do
					if multicraft.get_node({x=x,y=y,z=z}).name ~= "air" then
						ground_y = y
						break
					end
				end
				
				if ground_y then
					local p = {x=x,y=ground_y+1,z=z}
					local nn = multicraft.get_node(p).name
					-- Check if the node can be replaced
					if multicraft.registered_nodes[nn] and
						multicraft.registered_nodes[nn].buildable_to then
						nn = multicraft.get_node({x=x,y=ground_y,z=z}).name
						if nn == "default:dirt_with_grass" then
							--local flower_choice = pr:next(1, 11)
							local flower_choice = math.random(0, 11)
							local flower = "default:grass"
							if flower_choice == 1 then
								flower = "flowers:dandelion_yellow"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 2 then
								flower = "flowers:rose"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 3 then
								flower = "flowers:oxeye_daisy"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 4 then
								flower = "flowers:tulip_orange"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 5 then
								flower = "flowers:tulip_pink"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 6 then
								flower = "flowers:tulip_red"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 7 then
								flower = "flowers:tulip_white"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 8 then
								flower = "flowers:allium"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 9 then
								flower = "flowers:paeonia"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 10 then
								flower = "flowers:houstonia"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 11 then
								flower = "flowers:blue_orchid"
								multicraft.set_node(p, {name=flower})
							elseif flower_choice == 12 then
								flower = "flowers:fern"
								multicraft.set_node(p, {name=flower})
							else
								flower = "default:grass"
								multicraft.set_node(p, {name=flower})
							end
							
						end
					end
				end
				
			end
		end
		end
	end
end)
