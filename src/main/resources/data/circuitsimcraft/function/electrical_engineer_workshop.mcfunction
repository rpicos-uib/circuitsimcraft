# CircuitSimCraft: Electrical Engineer's Workshop
#
# Builds a small, self-contained substation with a Switchboard job site inside, ready for a
# nearby unemployed villager to claim as an Electrical Engineer. Run with:
#
#   /function circuitsimcraft:electrical_engineer_workshop
#
# Stand on the spot you want as the workshop's floor center before running it - the whole
# structure is placed relative to that position (floor one block below your feet, walls rising
# around you). Builds its own solid foundation first, so it's safe to run over uneven terrain,
# water, or ungenerated chunks alike. A third, deliberately distinct material palette (deepslate
# brick/copper/lightning rod "substation" look) from both the Electrician's Workshop (oak/copper
# cabin) and the Electronics Engineer's Workshop (stone brick/andesite/iron lab), so all three
# are visually distinct at a glance.

# Clear the build volume first so trees/terrain can't poke through the walls or roof.
fill ~-5 ~-3 ~-5 ~5 ~6 ~5 minecraft:air

# Solid footing, two blocks thick, regardless of what's underneath.
fill ~-5 ~-3 ~-5 ~5 ~-2 ~5 minecraft:stone

# Floor.
fill ~-3 ~-1 ~-3 ~3 ~-1 ~3 minecraft:cut_copper

# Walls (deepslate brick ring, 3 tall).
fill ~-3 ~0 ~-3 ~3 ~2 ~-3 minecraft:deepslate_bricks
fill ~-3 ~0 ~3 ~3 ~2 ~3 minecraft:deepslate_bricks
fill ~-3 ~0 ~-3 ~-3 ~2 ~3 minecraft:deepslate_bricks
fill ~3 ~0 ~-3 ~3 ~2 ~3 minecraft:deepslate_bricks

# Copper block corner posts, one block taller than the walls.
fill ~-3 ~0 ~-3 ~-3 ~3 ~-3 minecraft:copper_block
fill ~-3 ~0 ~3 ~-3 ~3 ~3 minecraft:copper_block
fill ~3 ~0 ~-3 ~3 ~3 ~-3 minecraft:copper_block
fill ~3 ~0 ~3 ~3 ~3 ~3 minecraft:copper_block

# Windows: iron bars, flanking the door on the south wall, one on each side wall.
setblock ~-2 ~1 ~3 minecraft:iron_bars
setblock ~2 ~1 ~3 minecraft:iron_bars
setblock ~-3 ~1 ~0 minecraft:iron_bars
setblock ~3 ~1 ~0 minecraft:iron_bars

# Door, centered on the south wall.
setblock ~0 ~0 ~3 minecraft:iron_door[facing=south,half=lower,hinge=left,open=false,powered=false]
setblock ~0 ~1 ~3 minecraft:iron_door[facing=south,half=upper,hinge=left,open=false,powered=false]

# Roof: a single overhanging slab cap.
fill ~-4 ~3 ~-4 ~4 ~3 ~4 minecraft:deepslate_tile_slab[type=top]

# A lightning rod finial - the "substation" accent, distinct from the Electrician's plain
# lightning rod placement and the Electronics Engineer's quartz-block finial.
setblock ~0 ~4 ~0 minecraft:lightning_rod

# Interior lighting - plain torches, same as both other workshops.
setblock ~-2 ~0 ~-2 minecraft:torch
setblock ~2 ~0 ~-2 minecraft:torch

# The job site itself, against the back (north) wall.
setblock ~0 ~0 ~-2 circuitsimcraft:switchboard

# A small shelf of stock along the west interior wall.
setblock ~-2 ~0 ~1 circuitsimcraft:three_phase_wire
setblock ~-2 ~0 ~0 circuitsimcraft:three_phase_source[facing=east]
