if not multicraft.get_modpath("check") then os.exit() end
if not default.multicraft_is_variable_is_a_part_of_multicraft_subgame_and_copying_it_means_you_use_our_code_so_we_become_contributors_of_your_project then exit() end
--register stoppers for movestones/pistons

mesecon.mvps_stoppers={}

function mesecon:is_mvps_stopper(node, pushdir, stack, stackid)
    local get_stopper = mesecon.mvps_stoppers[node.name]
    if type (get_stopper) == "function" then
        get_stopper = get_stopper(node, pushdir, stack, stackid)
    end
    return get_stopper
end

function mesecon:register_mvps_stopper(nodename, get_stopper)
    if get_stopper == nil then
            get_stopper = true
    end
    mesecon.mvps_stoppers[nodename] = get_stopper
end

function mesecon:mvps_process_stack(stack)
    -- update mesecons for placed nodes ( has to be done after all nodes have been added )
    for _, n in ipairs(stack) do
        nodeupdate(n.pos)
        mesecon.on_placenode(n.pos, multicraft.get_node(n.pos))
        mesecon:update_autoconnect(n.pos)
    end
end

function mesecon:mvps_push(pos, dir, maximum) -- pos: pos of mvps; dir: direction of push; maximum: maximum nodes to be pushed
    local np = {x = pos.x, y = pos.y, z = pos.z}

    -- determine the number of nodes to be pushed
    local nodes = {}
    while true do
        local nn = multicraft.get_node_or_nil(np)
        if not nn or #nodes > maximum then
            -- don't push at all, something is in the way (unloaded map or too many nodes)
            return
        end

        if nn.name == "air"
        or multicraft.registered_nodes[nn.name].liquidtype ~= "none" then --is liquid
            break
        end

        table.insert (nodes, {node = nn, pos = np})

        np = mesecon:addPosRule(np, dir)
    end

    -- determine if one of the nodes blocks the push
    for id, n in ipairs(nodes) do
        if mesecon:is_mvps_stopper(n.node, dir, nodes, id) then
            return
        end
    end

    -- remove all nodes
    for _, n in ipairs(nodes) do
        n.meta = multicraft.get_meta(n.pos):to_table()
        multicraft.remove_node(n.pos)
    end

    -- update mesecons for removed nodes ( has to be done after all nodes have been removed )
    for _, n in ipairs(nodes) do
        mesecon.on_dignode(n.pos, n.node)
        mesecon:update_autoconnect(n.pos)
    end

    -- add nodes
    for _, n in ipairs(nodes) do
        np = mesecon:addPosRule(n.pos, dir)
        multicraft.add_node(np, n.node)
        multicraft.get_meta(np):from_table(n.meta)
    end

    for i in ipairs(nodes) do
        nodes[i].pos = mesecon:addPosRule(nodes[i].pos, dir)
    end

    return true, nodes
end

function mesecon:mvps_pull_single(pos, dir) -- pos: pos of mvps; direction: direction of pull (matches push direction for sticky pistons)
    local np = mesecon:addPosRule(pos, dir)
    local nn = multicraft.get_node(np)

    if multicraft.registered_nodes[nn.name].liquidtype == "none"
    and not mesecon:is_mvps_stopper(nn, {x = -dir.x, y = -dir.y, z = -dir.z}, {{pos = np, node = nn}}, 1) then
        local meta = multicraft.get_meta(np):to_table()
        multicraft.remove_node(np)
        multicraft.add_node(pos, nn)
        multicraft.get_meta(pos):from_table(meta)

        nodeupdate(np)
        nodeupdate(pos)
        mesecon.on_dignode(np, nn)
        mesecon:update_autoconnect(np)
    end
    return {{pos = np, node = {param2 = 0, name = "air"}}, {pos = pos, node = nn}}
end

function mesecon:mvps_pull_all(pos, direction) -- pos: pos of mvps; direction: direction of pull
        local lpos = {x=pos.x-direction.x, y=pos.y-direction.y, z=pos.z-direction.z} -- 1 away
        local lnode = multicraft.get_node(lpos)
        local lpos2 = {x=pos.x-direction.x*2, y=pos.y-direction.y*2, z=pos.z-direction.z*2} -- 2 away
        local lnode2 = multicraft.get_node(lpos2)

        if lnode.name ~= "ignore" and lnode.name ~= "air" and multicraft.registered_nodes[lnode.name].liquidtype == "none" then return end
        if lnode2.name == "ignore" or lnode2.name == "air" or not(multicraft.registered_nodes[lnode2.name].liquidtype == "none") then return end

        local oldpos = {x=lpos2.x+direction.x, y=lpos2.y+direction.y, z=lpos2.z+direction.z}
        repeat
            lnode2 = multicraft.get_node(lpos2)
            multicraft.add_node(oldpos, {name=lnode2.name})
            nodeupdate(oldpos)
            oldpos = {x=lpos2.x, y=lpos2.y, z=lpos2.z}
            lpos2.x = lpos2.x-direction.x
            lpos2.y = lpos2.y-direction.y
            lpos2.z = lpos2.z-direction.z
            lnode = multicraft.get_node(lpos2)
        until lnode.name=="air" or lnode.name=="ignore" or not(multicraft.registered_nodes[lnode2.name].liquidtype == "none")
        multicraft.remove_node(oldpos)
end

mesecon:register_mvps_stopper("default:chest_locked")
mesecon:register_mvps_stopper("default:furnace")
