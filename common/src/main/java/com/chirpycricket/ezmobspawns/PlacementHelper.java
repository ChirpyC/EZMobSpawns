package com.chirpycricket.ezmobspawns;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;

import static com.chirpycricket.ezmobspawns.CommonClass.nextRandIntBetween;

public class PlacementHelper {

    public static ArrayList<BlockPos> getValidLocations(ServerLevel world, ChunkPos chunkPos, SpawnPropertyGroup.MobSpawnProperties msp, SpawnPropertyGroup sp, EntityType type){
        ArrayList<BlockPos> locations = new ArrayList<>();

        for (int i = 0; i < ConfigReader.globalProperties.maxPtsPerChunk && locations.size() < msp.maxSpawnGroup ; i++){
            BlockPos pos = chunkPos.getWorldPosition();

            pos = pos.offset(world.random.nextInt(16), nextRandIntBetween(-64, world.dimensionType().logicalHeight()), world.random.nextInt(16));

            boolean airPlacementAllowed = sp.placements.contains(SpawnPropertyGroup.Placement.AIR);
            if(!airPlacementAllowed && !sp.placements.contains(SpawnPropertyGroup.Placement.SURFACE)) { //favor points below surface
                pos = pos.atY(nextRandIntBetween(-64, world.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ())-1));
            }

            pos = PlacementHelper.findClearSpaceNear(world, pos, type, airPlacementAllowed);

            if(pos != null && sp.fineCheck(world, pos, msp)){
                locations.add(pos);
            }
        }

        return locations;
    }

    public static BlockPos findClearSpaceNear(ServerLevel world, BlockPos pos, EntityType type, boolean airAllowed){
        AABB bb = type.getDimensions().makeBoundingBox(pos.getCenter().x, pos.getCenter().y, pos.getCenter().z);
        if(!airAllowed) {
            int i = 0;
            do {
                i++;
                pos = getLowestSurface(world, pos, type);
                bb = type.getDimensions().makeBoundingBox(pos.getCenter().x, pos.getCenter().y, pos.getCenter().z);
            } while(world.noCollision(null, bb.move(0,-1,0)) && i < 20);
        }

        if(world.noCollision(null, bb)) return pos;
        else return null;
    }

    public static BlockPos getLowestSurface(ServerLevel world, BlockPos p1, EntityType type){
        AABB bb = type.getDimensions().makeBoundingBox(p1.getX(), p1.getY(), p1.getZ());
        BlockPos pos = p1;

        while( !world.getBlockState(pos.below()).getMaterial().isLiquid()
                && ((!world.getBlockState(pos.below()).getMaterial().isSolid()
                        && world.noCollision(null, bb.move(0,-1,0)))
                    || (world.getBlockState(pos.below()).getMaterial().isSolid()
                        && world.getBlockState(pos.below()).is(BlockTags.LEAVES)
                        )
                    )
                && pos.getY()-1 > -63) {
            pos = pos.below();
            bb = bb.move(0,-1,0);
        }
        return pos;
    }
}
