package com.chirpycricket.ezmobspawns;

import com.chirpycricket.ezmobspawns.platform.TickData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

import static com.chirpycricket.ezmobspawns.ForgeCommonClass.*;
import static com.chirpycricket.ezmobspawns.ForgePlacementHelper.getValidLocations;

public class ForgeSpawnTicker {

    static int tick = 0;
    protected static HashMap<ChunkPos, HashMap<ForgeSpawnPropertyGroup, TickData>> chunkMap = new HashMap<>();

    public static void onTick(ServerWorld world) {
        if(world.players().size() == 0) return;

        //get list of chunks loaded around players
        HashSet<ChunkPos> loadedChunks = getActiveChunks(world);

        //shuffle chunks and pick subset
        List<ChunkPos> selectedChunks = new ArrayList<>();
        ChunkPos[] posArray = new ChunkPos[loadedChunks.size()];
        posArray = loadedChunks.toArray(posArray);
        Collections.addAll(selectedChunks, posArray);
        if(ForgeConfigReader.globalProperties.chunkCoverage < 1.0f){
            Collections.shuffle(selectedChunks);
            selectedChunks = selectedChunks.subList(0, (int)(ForgeConfigReader.globalProperties.chunkCoverage * selectedChunks.size()));
        }

        //add fresh ticker if encountered new chunk
        updateChunkMap(world, selectedChunks);

        //pick subset of chunks and try triggering them
        for(ChunkPos chunkPos : selectedChunks) {
            for(ForgeSpawnPropertyGroup sp : chunkMap.get(chunkPos).keySet()){
                TickData td = chunkMap.get(chunkPos).get(sp);
                td.tick(tick);
                if (td.shouldTrigger(tick)) {
                    trySpawns(world, chunkPos, sp, td);
                    td.resetTicks(tick);
                }
            }
        }

        //once a day, remove inactive chunks from map to keep size reasonable
        tick++;
        if(tick%24000 == 0){
            pruneChunkMap(loadedChunks);
        }
    }

    private static HashSet<ChunkPos> getActiveChunks(ServerWorld world){
        HashSet<ChunkPos> loadedChunks = new HashSet<>();
        for(ServerPlayerEntity p : world.players()){
            int range = ForgeConfigReader.globalProperties.chunkRange;
            ChunkPos init = p.getLastSectionPos().chunk();
            for(int x = init.x-range; x < init.x+range; x++) {
                for (int z = init.z - range; z < init.z + range; z++) {
                    ChunkPos pos = new ChunkPos(x, z);
                    if(world.hasChunk(pos.x, pos.z)){
                        loadedChunks.add(pos);
                    }
                }
            }
        }
        return loadedChunks;
    }

    private static void updateChunkMap(ServerWorld world, List<ChunkPos> chunks){
        for(ChunkPos chunkPos : chunks){
            if(!chunkMap.containsKey(chunkPos)){
                chunkMap.put(chunkPos, new HashMap<>());
                for(ForgeSpawnPropertyGroup sp : ForgeConfigReader.spawnProperties){
                    sp.chunkCheck(world, new BlockPos(chunkPos.getWorldPosition().getX()+8, 32, chunkPos.getWorldPosition().getZ()+8));
                    chunkMap.get(chunkPos).put(sp, new TickData(sp.ticksBetweenSpawnAttempts, sp.spawnCoolDown, sp.runImmediately));
                }
            }
        }
    }

    private static void pruneChunkMap(HashSet<ChunkPos> activeChunks){
        for(ChunkPos chunkPos : chunkMap.keySet()){
            if(!activeChunks.contains(chunkPos)) {
                chunkMap.remove(chunkPos);
            }
        }
    }

    private static void trySpawns(ServerWorld world, ChunkPos chunkPos, ForgeSpawnPropertyGroup sp, TickData td) {
        BlockPos pos = new BlockPos(chunkPos.getWorldPosition().getX()+8, 32, chunkPos.getWorldPosition().getZ()+8);
        sp.debugMsg("triggered @ chunk " + pos.getX() + " " + pos.getY() + " " + pos.getZ());

        //do quick checks
        if(!sp.coarseCheck(world, pos)){
            sp.debugMsg("failed coarse check @ " + pos.getX() + " ~ " + pos.getZ());
            if(sp.enableDebug) sp.printCoarseCheck(world, pos, false);
            return;
        }

        //quick group checks passed, now check per mob, per location
        for(int i = 0; i < sp.totalMobsToPick; i++) {
            ForgeSpawnPropertyGroup.MobSpawnProperties msp = sp.pickMob();
            EntityType type = getEntityType(msp.entityType);
            if(type == null){continue;}

            int totalToSpawn = nextRandIntBetween(msp.minSpawnGroup, msp.maxSpawnGroup);

            if(!ForgeSpawnPropertyGroup.checkMobLimit(msp, world, pos, totalToSpawn)){
                sp.debugMsg("mob limit reached");
            }
            else {
                ArrayList<BlockPos> positions = getValidLocations(world, chunkPos, msp, sp, type);

                if(positions.size() < msp.minSpawnGroup) {
                    sp.debugMsg("found " + positions.size() + "/" + msp.minSpawnGroup + " spawn locations");
                }
                else {
                    sp.debugMsg("spawning mob #" + (i+1) + ": " + totalToSpawn + " x " + type.getDescription().getString());
                    for(BlockPos bp : positions){
                        spawnEntity(world, bp, sp, type);
                    }
                }
            }
            td.startCoolDown(tick);
        }
    }
}
