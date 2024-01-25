package com.chirpycricket.ezmobspawns;

import net.minecraft.entity.EntityType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;

import static com.chirpycricket.ezmobspawns.ForgeCommonClass.nextRandIntBetween;

public class ForgePlacementHelper {

    public static ArrayList<BlockPos> getValidLocations(ServerWorld world, ChunkPos chunkPos, ForgeSpawnPropertyGroup.MobSpawnProperties msp, ForgeSpawnPropertyGroup sp, EntityType type){
        ArrayList<BlockPos> locations = new ArrayList<>();

        for (int i = 0; i < ForgeConfigReader.globalProperties.maxPtsPerChunk && locations.size() < msp.maxSpawnGroup ; i++){
            BlockPos pos = chunkPos.getWorldPosition();

            pos = pos.offset(world.random.nextInt(16), nextRandIntBetween(-64, world.dimensionType().logicalHeight()), world.random.nextInt(16));

            boolean airPlacementAllowed = sp.placements.contains(ForgeSpawnPropertyGroup.Placement.AIR);
            if(!airPlacementAllowed && !sp.placements.contains(ForgeSpawnPropertyGroup.Placement.SURFACE)) { //favor points below surface
                pos = new BlockPos(pos.getX(), nextRandIntBetween(-64, world.getHeight(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ())-1), pos.getZ());
            }

            pos = ForgePlacementHelper.findClearSpaceNear(world, pos, type, airPlacementAllowed);

            if(pos != null && sp.fineCheck(world, pos, msp)){
                locations.add(pos);
            }
        }

        return locations;
    }

    public static BlockPos findClearSpaceNear(ServerWorld world, BlockPos pos, EntityType type, boolean airAllowed){
        AxisAlignedBB bb = type.getDimensions().makeBoundingBox(pos.getX()+.5f, pos.getY()+.5f, pos.getZ()+.5f);
        if(!airAllowed) {
            int i = 0;
            do {
                i++;
                pos = getLowestSurface(world, pos, type);
                bb = type.getDimensions().makeBoundingBox(pos.getX()+.5f, pos.getY()+.5f, pos.getZ()+.5f);
            } while(world.noCollision(null, bb.move(0,-1,0)) && i < 20);
        }

        if(world.noCollision(null, bb)) return pos;
        else return null;
    }

    public static BlockPos getLowestSurface(ServerWorld world, BlockPos p1, EntityType type){
        AxisAlignedBB bb = type.getDimensions().makeBoundingBox(p1.getX(), p1.getY(), p1.getZ());
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
