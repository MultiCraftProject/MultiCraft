/*
Minetest
Copyright (C) 2013-2016 kwolekr, Ryan Kwolek <kwolekr@minetest.net>
Copyright (C) 2014-2017 paramat

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 3.0 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/


#include "mapgen.h"
#include "voxel.h"
#include "noise.h"
#include "mapblock.h"
#include "mapnode.h"
#include "map.h"
#include "content_sao.h"
#include "nodedef.h"
#include "voxelalgorithms.h"
#include "profiler.h" // For TimeTaker
#include "settings.h" // For g_settings
#include "emerge.h"
#include "dungeongen.h"
#include "cavegen.h"
#include "mg_biome.h"
#include "mg_ore.h"
#include "mg_decoration.h"
#include "mapgen_v7p.h"


FlagDesc flagdesc_mapgen_v7p[] = {
	{"mountains",  MGV7P_MOUNTAINS},
	{"ridges",     MGV7P_RIDGES},
	{NULL,         0}
};


///////////////////////////////////////////////////////////////////////////////


MapgenV7P::MapgenV7P(int mapgenid, MapgenV7PParams *params, EmergeManager *emerge)
	: MapgenBasic(mapgenid, params, emerge)
{
	this->spflags         = params->spflags;
	this->hell_top        = params->hell_top;
	this->hell_lava_level = params->hell_lava_level;
	this->hell_threshold  = params->hell_threshold;

	// Average of mgv6 small caves count
	small_caves_count = 6 * csize.X * csize.Z * MAP_BLOCKSIZE / 50000;

	// Upper limit of bedrock
	bedrock_level = water_level - 64;

	// Hell tapering
	s16 taper_margin = csize.Y + 32; // 32 is taper distance
	hell_y_max = hell_top + taper_margin;
	hell_y_min = hell_lava_level - taper_margin;
	offset_amp = -4.0f / (32.0f * 32.0f);

	// Mapgen-specific dungeon noise
	// Dungeons per mapchunk = floor(noise)
	np_helldun_density  = &params->np_helldun_density;

	// 2D noise
	noise_terrain_base    = new Noise(&params->np_terrain_base,    seed, csize.X, csize.Z);
	noise_terrain_alt     = new Noise(&params->np_terrain_alt,     seed, csize.X, csize.Z);
	noise_terrain_persist = new Noise(&params->np_terrain_persist, seed, csize.X, csize.Z);
	noise_height_select   = new Noise(&params->np_height_select,   seed, csize.X, csize.Z);
	noise_filler_depth    = new Noise(&params->np_filler_depth,    seed, csize.X, csize.Z);

	if (spflags & MGV7P_MOUNTAINS) {
		noise_mount_height = new Noise(&params->np_mount_height, seed, csize.X, csize.Z);
		noise_mountain     = new Noise(&params->np_mountain,     seed, csize.X, csize.Z);
	}

	if (spflags & MGV7P_RIDGES) {
		noise_ridge_uwater = new Noise(&params->np_ridge_uwater, seed, csize.X, csize.Z);
		noise_ridge        = new Noise(&params->np_ridge,        seed, csize.X, csize.Z);
	}

	// 3D noise, 1 down overgeneration
	noise_hell = new Noise(&params->np_hell, seed, csize.X, csize.Y + 1, csize.Z);

	// Resolve additional nodes
	c_bedrock         = ndef->getId("mapgen_bedrock");
	c_hellstone_brick = ndef->getId("mapgen_hellstone_brick");
}


MapgenV7P::~MapgenV7P()
{
	delete noise_terrain_base;
	delete noise_terrain_alt;
	delete noise_terrain_persist;
	delete noise_height_select;
	delete noise_filler_depth;

	if (spflags & MGV7P_MOUNTAINS) {
		delete noise_mount_height;
		delete noise_mountain;
	}

	if (spflags & MGV7P_RIDGES) {
		delete noise_ridge_uwater;
		delete noise_ridge;
	}

	delete noise_hell;
}


MapgenV7PParams::MapgenV7PParams()
{
	spflags            = MGV7P_MOUNTAINS | MGV7P_RIDGES;
	hell_top           = -768;
	hell_lava_level    = -1010;
	hell_threshold     = 0.2;

	np_terrain_base     = NoiseParams(4,    35,  v3f(600,  600,  600),  82341, 5, 0.6,  2.0);
	np_terrain_alt      = NoiseParams(4,    25,  v3f(600,  600,  600),  5934,  5, 0.6,  2.0);
	np_terrain_persist  = NoiseParams(0.6,  0.1, v3f(2000, 2000, 2000), 539,   3, 0.6,  2.0);
	np_height_select    = NoiseParams(-8,   16,  v3f(500,  500,  500),  4213,  6, 0.7,  2.0);
	np_filler_depth     = NoiseParams(0,    1.2, v3f(150,  150,  150),  261,   3, 0.7,  2.0);
	np_mount_height     = NoiseParams(128,  56,  v3f(1000, 1000, 1000), 72449, 3, 0.6,  2.0);
	np_ridge_uwater     = NoiseParams(0,    1,   v3f(1000, 1000, 1000), 85039, 5, 0.6,  2.0);
	np_mountain         = NoiseParams(-0.6, 1,   v3f(250,  250,  250),  5333,  5, 0.63, 2.0);
	np_ridge            = NoiseParams(0,    1,   v3f(100,  100,  100),  6467,  4, 0.75, 2.0);
	np_hell             = NoiseParams(0,    1,   v3f(192,  64,   192),  723,   4, 0.63, 2.0);
	np_helldun_density  = NoiseParams(-1,   3.5, v3f(128,  128,  128),  11,    1, 0.0,  2.0);
}


void MapgenV7PParams::readParams(const Settings *settings)
{
	settings->getFlagStrNoEx("mgv7p_spflags", spflags, flagdesc_mapgen_v7p);
	settings->getS16NoEx("mgv7p_hell_top",                hell_top);
	settings->getS16NoEx("mgv7p_hell_lava_level",         hell_lava_level);
	settings->getFloatNoEx("mgv7p_hell_threshold",        hell_threshold);

	settings->getNoiseParams("mgv7p_np_terrain_base",     np_terrain_base);
	settings->getNoiseParams("mgv7p_np_terrain_alt",      np_terrain_alt);
	settings->getNoiseParams("mgv7p_np_terrain_persist",  np_terrain_persist);
	settings->getNoiseParams("mgv7p_np_height_select",    np_height_select);
	settings->getNoiseParams("mgv7p_np_filler_depth",     np_filler_depth);
	settings->getNoiseParams("mgv7p_np_mount_height",     np_mount_height);
	settings->getNoiseParams("mgv7p_np_ridge_uwater",     np_ridge_uwater);
	settings->getNoiseParams("mgv7p_np_mountain",         np_mountain);
	settings->getNoiseParams("mgv7p_np_ridge",            np_ridge);
	settings->getNoiseParams("mgv7p_np_hell",             np_hell);
	settings->getNoiseParams("mgv7p_np_helldun_density",  np_helldun_density);
}


void MapgenV7PParams::writeParams(Settings *settings) const
{
	settings->setFlagStr("mgv7p_spflags", spflags, flagdesc_mapgen_v7p, U32_MAX);
	settings->setS16("mgv7p_hell_top",                    hell_top);
	settings->setS16("mgv7p_hell_lava_level",             hell_lava_level);
	settings->setFloat("mgv7p_hell_threshold",            hell_threshold);

	settings->setNoiseParams("mgv7p_np_terrain_base",     np_terrain_base);
	settings->setNoiseParams("mgv7p_np_terrain_alt",      np_terrain_alt);
	settings->setNoiseParams("mgv7p_np_terrain_persist",  np_terrain_persist);
	settings->setNoiseParams("mgv7p_np_height_select",    np_height_select);
	settings->setNoiseParams("mgv7p_np_filler_depth",     np_filler_depth);
	settings->setNoiseParams("mgv7p_np_mount_height",     np_mount_height);
	settings->setNoiseParams("mgv7p_np_ridge_uwater",     np_ridge_uwater);
	settings->setNoiseParams("mgv7p_np_mountain",         np_mountain);
	settings->setNoiseParams("mgv7p_np_ridge",            np_ridge);
	settings->setNoiseParams("mgv7p_np_hell",             np_hell);
	settings->setNoiseParams("mgv7p_np_helldun_density",  np_helldun_density);
}


///////////////////////////////////////////////////////////////////////////////


int MapgenV7P::getSpawnLevelAtPoint(v2s16 p)
{
	// If enabled, first check if inside a river
	if (spflags & MGV7P_RIDGES) {
		float width = 0.2;
		float uwatern = NoisePerlin2D(&noise_ridge_uwater->np, p.X, p.Y, seed) * 2;
		if (fabs(uwatern) <= width)
			return MAX_MAP_GENERATION_LIMIT; // Unsuitable spawn point
	}

	// Base/mountain terrain calculation
	s16 y = baseTerrainLevelAtPoint(p.X, p.Y);
	if (spflags & MGV7P_MOUNTAINS)
		y = MYMAX(mountainLevelAtPoint(p.X, p.Y), y);

	if (y < water_level || y > water_level + 16)
		return MAX_MAP_GENERATION_LIMIT; // Unsuitable spawn point
	else
		return y + 2; // +2 because surface is at y and due to biome 'dust'
}


void MapgenV7P::makeChunk(BlockMakeData *data)
{
	// Pre-conditions
	assert(data->vmanip);
	assert(data->nodedef);
	assert(data->blockpos_requested.X >= data->blockpos_min.X &&
		data->blockpos_requested.Y >= data->blockpos_min.Y &&
		data->blockpos_requested.Z >= data->blockpos_min.Z);
	assert(data->blockpos_requested.X <= data->blockpos_max.X &&
		data->blockpos_requested.Y <= data->blockpos_max.Y &&
		data->blockpos_requested.Z <= data->blockpos_max.Z);

	TimeTaker t("makeChunk");

	this->generating = true;
	this->vm = data->vmanip;
	this->ndef = data->nodedef;

	v3s16 blockpos_min = data->blockpos_min;
	v3s16 blockpos_max = data->blockpos_max;
	node_min = blockpos_min * MAP_BLOCKSIZE;
	node_max = (blockpos_max + v3s16(1, 1, 1)) * MAP_BLOCKSIZE - v3s16(1, 1, 1);
	full_node_min = (blockpos_min - 1) * MAP_BLOCKSIZE;
	full_node_max = (blockpos_max + 2) * MAP_BLOCKSIZE - v3s16(1, 1, 1);

	blockseed = getBlockSeed2(full_node_min, seed);

	bool hell_chunk = node_max.Y <= hell_y_max and node_min.Y >= hell_y_min;

	if (node_max.Y <= bedrock_level and !hell_chunk) {
		// Generate bedrock only
		generateSinglenode(c_bedrock);
	} else {
		// Generate base terrain or stone for hell
		s16 stone_surface_max_y = -MAX_MAP_GENERATION_LIMIT;
		if (hell_chunk) {
			generateSinglenode(c_stone);
			stone_surface_max_y = node_max.Y;
		} else {
			stone_surface_max_y = generateTerrain();
		}

		if (!hell_chunk) {
			// Generate rivers
			if (spflags & MGV7P_RIDGES)
				generateRidgeTerrain();

			// Create heightmap
			updateHeightmap(node_min, node_max);
		}

		// Init biome generator, place biome-specific nodes, and build biomemap
		biomegen->calcBiomeNoise(node_min);
		MgStoneType stone_type = generateBiomes();

		// Generate mgv6 type caves or hell caves
		if (flags & MG_CAVES) {
			if (hell_chunk)
				generateHellCaves();
			else
				generateCaves(stone_surface_max_y, water_level);
		}

		// Generate the registered ores
		m_emerge->oremgr->placeAllOres(this, blockseed, node_min, node_max);

		// Generate dungeons
		if (flags & MG_DUNGEONS) {
			if (hell_chunk && full_node_min.Y > hell_lava_level &&
					full_node_max.Y < hell_top) {
				DungeonParams dp;

				dp.seed             = seed;
				dp.c_water          = c_water_source;
				dp.c_river_water    = c_river_water_source;
				dp.only_in_ground   = false;
				dp.corridor_len_min = 8;
				dp.corridor_len_max = 32;
				dp.rooms_min        = 8;
				dp.rooms_max        = 32;
				dp.y_min            = -MAX_MAP_GENERATION_LIMIT;
				dp.y_max            = MAX_MAP_GENERATION_LIMIT;
				dp.np_density       = *np_helldun_density;
				dp.np_alt_wall      = nparams_dungeon_alt_wall;

				dp.c_wall     = c_hellstone_brick;
				dp.c_alt_wall = CONTENT_IGNORE;
				dp.c_stair    = c_hellstone_brick;

				dp.diagonal_dirs       = false;
				dp.holesize            = v3s16(3, 3, 3);
				dp.room_size_min       = v3s16(8, 4, 8);
				dp.room_size_max       = v3s16(16, 8, 16);
				dp.room_size_large_min = v3s16(8, 4, 8);
				dp.room_size_large_max = v3s16(16, 8, 16);
				dp.notifytype          = GENNOTIFY_DUNGEON;
			
				DungeonGen dgen(ndef, &gennotify, &dp);
				dgen.generate(vm, blockseed, full_node_min, full_node_max);
			} else if (!hell_chunk) {
				generateDungeons(stone_surface_max_y, stone_type);
			}
		}

		if (!hell_chunk) {
			// Generate the registered decorations
			if (flags & MG_DECORATIONS)
				m_emerge->decomgr->placeAllDecos(this, blockseed, node_min, node_max);

			// Sprinkle some dust on top after everything else was generated
			dustTopNodes();
		}

		// Update liquids
		updateLiquid(&data->transforming_liquid, full_node_min, full_node_max);

		// Calculate lighting
		if (flags & MG_LIGHT)
			calcLighting(node_min - v3s16(0, 1, 0), node_max + v3s16(0, 1, 0),
				full_node_min, full_node_max, true);
	}

	this->generating = false;

	printf("makeChunk: %lums\n", t.stop());
}


float MapgenV7P::baseTerrainLevelAtPoint(s16 x, s16 z)
{
	float hselect = NoisePerlin2D(&noise_height_select->np, x, z, seed);
	hselect = rangelim(hselect, 0.0, 1.0);

	float persist = NoisePerlin2D(&noise_terrain_persist->np, x, z, seed);

	noise_terrain_base->np.persist = persist;
	float height_base = NoisePerlin2D(&noise_terrain_base->np, x, z, seed);

	noise_terrain_alt->np.persist = persist;
	float height_alt = NoisePerlin2D(&noise_terrain_alt->np, x, z, seed);

	if (height_alt > height_base)
		return height_alt;

	return (height_base * hselect) + (height_alt * (1.0 - hselect));
}


float MapgenV7P::baseTerrainLevelFromMap(int index)
{
	float hselect     = rangelim(noise_height_select->result[index], 0.0, 1.0);
	float height_base = noise_terrain_base->result[index];
	float height_alt  = noise_terrain_alt->result[index];

	if (height_alt > height_base)
		return height_alt;

	return (height_base * hselect) + (height_alt * (1.0 - hselect));
}


float MapgenV7P::mountainLevelAtPoint(s16 x, s16 z)
{
	float mnt_h_n = NoisePerlin2D(&noise_mount_height->np, x, z, seed);
	float mnt_n = NoisePerlin2D(&noise_mountain->np, x, z, seed);

	return mnt_n * mnt_h_n;
}


float MapgenV7P::mountainLevelFromMap(int idx_xz)
{
	float mounthn = noise_mount_height->result[idx_xz];
	float mountn = noise_mountain->result[idx_xz];

	return mountn * mounthn;
}


void MapgenV7P::generateSinglenode(content_t c_node)
{
	MapNode n_node(c_node);

	for (s16 z = node_min.Z; z <= node_max.Z; z++)
	for (s16 y = node_min.Y - 1; y <= node_max.Y + 1; y++) {
		u32 vi = vm->m_area.index(node_min.X, y, z);

		for (s16 x = node_min.X; x <= node_max.X; x++, vi++) {
			if (vm->m_data[vi].getContent() == CONTENT_IGNORE)
				vm->m_data[vi] = n_node;
		}
	}
}


int MapgenV7P::generateTerrain()
{
	MapNode n_stone(c_stone);
	MapNode n_bedrock(c_bedrock);
	MapNode n_water(c_water_source);
	MapNode n_air(CONTENT_AIR);

	//// Calculate noise for terrain generation
	noise_terrain_persist->perlinMap2D(node_min.X, node_min.Z);
	float *persistmap = noise_terrain_persist->result;

	noise_terrain_base ->perlinMap2D(node_min.X, node_min.Z, persistmap);
	noise_terrain_alt  ->perlinMap2D(node_min.X, node_min.Z, persistmap);
	noise_height_select->perlinMap2D(node_min.X, node_min.Z);

	if (spflags & MGV7P_MOUNTAINS) {
		noise_mount_height->perlinMap2D(node_min.X, node_min.Z);
		noise_mountain    ->perlinMap2D(node_min.X, node_min.Z);
	}

	//// Place nodes
	const v3s16 &em = vm->m_area.getExtent();
	s16 stone_surface_max_y = -MAX_MAP_GENERATION_LIMIT;
	u32 index2d = 0;

	for (s16 z = node_min.Z; z <= node_max.Z; z++)
	for (s16 x = node_min.X; x <= node_max.X; x++, index2d++) {
		s16 surface_y = baseTerrainLevelFromMap(index2d);
		if (spflags & MGV7P_MOUNTAINS)
			surface_y = MYMAX(mountainLevelFromMap(index2d), surface_y);

		if (surface_y > stone_surface_max_y)
			stone_surface_max_y = surface_y;

		u32 vi = vm->m_area.index(x, node_min.Y - 1, z);

		for (s16 y = node_min.Y - 1; y <= node_max.Y + 1; y++) {
			if (vm->m_data[vi].getContent() == CONTENT_IGNORE) {
				if (y <= surface_y) {
					if (y <= bedrock_level)
						vm->m_data[vi] = n_bedrock; // Bedrock
					else
						vm->m_data[vi] = n_stone; // Base and mountain terrain
				} else if (y <= water_level) {
					vm->m_data[vi] = n_water; // Water
				} else {
					vm->m_data[vi] = n_air; // Air
				}
			}
			vm->m_area.add_y(em, vi, 1);
		}
	}

	return stone_surface_max_y;
}


void MapgenV7P::generateRidgeTerrain()
{
	if (node_max.Y < water_level - 16)
		return;

	noise_ridge       ->perlinMap2D(node_min.X, node_min.Z);
	noise_ridge_uwater->perlinMap2D(node_min.X, node_min.Z);

	MapNode n_water(c_water_source);
	MapNode n_air(CONTENT_AIR);

	float width = 0.2;

	for (s16 z = node_min.Z; z <= node_max.Z; z++)
	for (s16 y = node_min.Y - 1; y <= node_max.Y + 1; y++) {
		u32 vi = vm->m_area.index(node_min.X, y, z);

		for (s16 x = node_min.X; x <= node_max.X; x++, vi++) {
			int index2d = (z - node_min.Z) * csize.X + (x - node_min.X);

			float uwatern = noise_ridge_uwater->result[index2d] * 2;
			if (fabs(uwatern) > width)
				continue;

			float altitude = y - water_level;
			float height_mod = (altitude + 17) / 2.5;
			float width_mod  = width - fabs(uwatern);
			float nridge = noise_ridge->result[index2d] * MYMAX(altitude, 0) / 7.0;

			if (nridge + width_mod * height_mod < 0.6)
				continue;

			vm->m_data[vi] = (y > water_level) ? n_air : n_water;
		}
	}
}


void MapgenV7P::generateCaves(s16 max_stone_y, s16 large_cave_depth)
{
	if (max_stone_y < node_min.Y)
		return;

	PseudoRandom ps(blockseed + 21343);
	PseudoRandom ps2(blockseed + 1032);

	u32 large_caves_count = (node_max.Y <= large_cave_depth) ? ps.range(0, 2) : 0;

	for (u32 i = 0; i < small_caves_count + large_caves_count; i++) {
		CavesV6 cave(ndef, &gennotify, water_level, c_water_source, c_lava_source);

		bool large_cave = (i >= small_caves_count);
		cave.makeCave(vm, node_min, node_max, &ps, &ps2,
			large_cave, max_stone_y, heightmap);
	}
}


void MapgenV7P::generateHellCaves()
{
	MapNode n_lava(c_lava_source);
	MapNode n_air(CONTENT_AIR);

	// Calculate noise
	noise_hell->perlinMap3D(node_min.X, node_min.Y - 1, node_min.Z);

	// Cache taper values
	float *taper = new float[csize.Y + 1];
	u8 taper_index = 0;  // Index zero at column top

	for (s16 y = node_max.Y; y >= node_min.Y - 1; y--, taper_index++) {
		float offset = 0.0f;
		if (y > hell_top)
			offset = (y - hell_top) * (y - hell_top) * offset_amp;
		else if (y < hell_lava_level)
			offset = (hell_lava_level - y) * (hell_lava_level - y) * offset_amp;
		taper[taper_index] = offset;
	}

	// Place nodes
	v3s16 em = vm->m_area.getExtent();
	u32 index2d = 0;

	for (s16 z = node_min.Z; z <= node_max.Z; z++)
	for (s16 x = node_min.X; x <= node_max.X; x++, index2d++) {
		// Reset taper index to column top
		taper_index = 0;
		// Initial voxelmanip index at column top
		u32 vi = vm->m_area.index(x, node_max.Y, z);
		// Initial 3D noise index at column top
		u32 index3d = (z - node_min.Z) * zstride_1d + csize.Y * ystride +
			(x - node_min.X);
		// Don't excavate the overgenerated stone at node_max.Y + 1,
		// this creates a 'roof' over the caverns, preventing light in
		// caverns at mapchunk borders when generating mapchunks upwards.
		// This 'roof' is excavated when the mapchunk above is generated.
		for (s16 y = node_max.Y; y >= node_min.Y - 1; y--,
				index3d -= ystride,
				vm->m_area.add_y(em, vi, -1),
				taper_index++) {
			float n_hell = noise_hell->result[index3d] + taper[taper_index];

			if (n_hell > hell_threshold) {
				content_t c = vm->m_data[vi].getContent();
				if (ndef->get(c).is_ground_content) {
					if (y <= hell_lava_level)
						vm->m_data[vi] = n_lava;
					else
						vm->m_data[vi] = n_air;
				}
			}
		}
	}
}
