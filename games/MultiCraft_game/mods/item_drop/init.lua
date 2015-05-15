if not multicraft.get_modpath("check") then os.exit() end
if not default.multicraft_is_variable_is_a_part_of_multicraft_subgame_and_copying_it_means_you_use_our_code_so_we_become_contributors_of_your_project then exit() end
multicraft.register_globalstep(function(dtime)
   for _,player in ipairs(multicraft.get_connected_players()) do
      if player:get_hp() > 0 or not multicraft.setting_getbool("enable_damage") then
         local pos = player:getpos()
         pos.y = pos.y+0.5
         local inv = player:get_inventory()
         local ctrl = player:get_player_control()
         if ctrl.up or ctrl.left or ctrl.right then

            for _,object in ipairs(multicraft.get_objects_inside_radius(pos, 2)) do
               local en = object:get_luaentity()
               if not object:is_player() and en and en.name == "__builtin:item" then
                  if inv and
                     inv:room_for_item("main", ItemStack(en.itemstring)) then
                     inv:add_item("main", ItemStack(en.itemstring))
                     if en.itemstring ~= "" then
                        multicraft.sound_play("item_drop_pickup", {
                           to_player = player:get_player_name(),
                           gain = 0.4,
                        })
                     end
                     en.itemstring = ""
                     object:remove()
                  end
               end
            end

         end
      end
   end
end)

function multicraft.handle_node_drops(pos, drops, digger)
        local inv
        if multicraft.setting_getbool("creative_mode") and digger and digger:is_player() then
                inv = digger:get_inventory()
        end
        for _,item in ipairs(drops) do
                local count, name
                if type(item) == "string" then
                        count = 1
                        name = item
                else
                        count = item:get_count()
                        name = item:get_name()
                end
                if not inv or not inv:contains_item("main", ItemStack(name)) then
                        for i=1,count do
                                local obj = multicraft.add_item(pos, name)
                                if obj ~= nil then
                                        obj:get_luaentity().collect = true
                                        local x = math.random(1, 5)
                                        if math.random(1,2) == 1 then
                                                x = -x
                                        end
                                        local z = math.random(1, 5)
                                        if math.random(1,2) == 1 then
                                                z = -z
                                        end
                                        obj:setvelocity({x=1/x, y=obj:getvelocity().y, z=1/z})

                                        -- FIXME this doesnt work for deactiveted objects
                                        if multicraft.setting_get("remove_items") and tonumber(multicraft.setting_get("remove_items")) then
                                                multicraft.after(tonumber(multicraft.setting_get("remove_items")), function(obj)
                                                        obj:remove()
                                                end, obj)
                                        end
                                end
                        end
                end
        end
end

