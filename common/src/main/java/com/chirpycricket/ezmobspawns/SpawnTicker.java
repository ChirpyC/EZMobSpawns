package com.chirpycricket.ezmobspawns;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import java.util.*;
import static com.chirpycricket.ezmobspawns.CommonClass.*;
import static com.chirpycricket.ezmobspawns.PlacementHelper.getValidLocations;

public class SpawnTicker {

    static int tick = 0;
    protected static HashMap<ChunkPos, HashMap<SpawnPropertyGroup, TickData>> chunkMap = new HashMap<>();

    public static void onTick(ServerLevel world) {
        if(world.players().size() == 0) return;

        //get list of chunks loaded around players
        HashSet<ChunkPos> loadedChunks = getActiveChunks(world);

        //shuffle chunks and pick subset
        List<ChunkPos> selectedChunks = new ArrayList<>();
        ChunkPos[] posArray = new ChunkPos[loadedChunks.size()];
        posArray = loadedChunks.toArray(posArray);
        Collections.addAll(selectedChunks, posArray);
        if(ConfigReader.globalProperties.chunkCoverage < 1.0f){
            Collections.shuffle(selectedChunks);
            selectedChunks = selectedChunks.subList(0, (int)(ConfigReader.globalProperties.chunkCoverage * selectedChunks.size()));
        }

        //add fresh ticker if encountered new chunk
        updateChunkMap(world, selectedChunks);

        //pick subset of chunks and try triggering them
        for(ChunkPos chunkPos : selectedChunks) {
            for(SpawnPropertyGroup sp : chunkMap.get(chunkPos).keySet()){
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

    private static HashSet<ChunkPos> getActiveChunks(ServerLevel world){
        HashSet<ChunkPos> loadedChunks = new HashSet<>();
        for(ServerPlayer p : world.players()){
            int range = ConfigReader.globalProperties.chunkRange;
            ChunkPos init = p.chunkPosition();
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

    private static void updateChunkMap(ServerLevel world, List<ChunkPos> chunks){
        for(ChunkPos chunkPos : chunks){
            if(!chunkMap.containsKey(chunkPos)){
                chunkMap.put(chunkPos, new HashMap<>());
                for(SpawnPropertyGroup sp : ConfigReader.spawnProperties){
                    sp.chunkCheck(world, chunkPos.getMiddleBlockPosition(32));
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

    private static void trySpawns(ServerLevel world, ChunkPos chunkPos, SpawnPropertyGroup sp, TickData td) {
        BlockPos pos = chunkPos.getMiddleBlockPosition(32);
        sp.debugMsg("triggered @ chunk " + pos.getX() + " " + pos.getY() + " " + pos.getZ());

        //do quick checks
        if(!sp.coarseCheck(world, pos)){
            sp.debugMsg("failed coarse check @ " + pos.getX() + " ~ " + pos.getZ());
            if(sp.enableDebug) sp.printCoarseCheck(world, pos, false);
            return;
        }

        //quick group checks passed, now check per mob, per location
        for(int i = 0; i < sp.totalMobsToPick; i++) {
            SpawnPropertyGroup.MobSpawnProperties msp = sp.pickMob();
            EntityType type = getEntityType(msp.entityType);
            if(type == null){continue;}

            int totalToSpawn = nextRandIntBetween(msp.minSpawnGroup, msp.maxSpawnGroup);

            if(!SpawnPropertyGroup.checkMobLimit(msp, world, pos, totalToSpawn)){
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
