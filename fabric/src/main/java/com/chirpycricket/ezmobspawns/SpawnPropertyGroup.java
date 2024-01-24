package com.chirpycricket.ezmobspawns;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import java.util.*;

import static com.chirpycricket.ezmobspawns.CommonClass.*;

public class SpawnPropertyGroup {

    String groupName = "";

    ArrayList<MobSpawnProperties> mobs = new ArrayList();
    List<Pair<MobSpawnProperties>> weightedMobs;
    int totalMobsToPick = 1;

    ArrayList<String> dimensions = new ArrayList();
    ArrayList<String> biomes = new ArrayList();
    ArrayList<Placement> placements = new ArrayList();

    ArrayList<RepeatableArea> areaRestriction = new ArrayList();
    ArrayList<SimpleArea> blacklistedAreas = new ArrayList();

    float chanceToAttemptSpawns = 1.0f;
    boolean runImmediately = true;
    int ticksBetweenSpawnAttempts = 100;
    int spawnCoolDown = 6000;
    ArrayList<Range<Integer>> timeframes = new ArrayList();
    ArrayList<MoonPhases> moonPhases= new ArrayList();
    boolean activateIfStormy = false;
    ArrayList<Biome.Precipitation> weather= new ArrayList();
    ArrayList<Range<Float>> temperatures= new ArrayList();

    ArrayList<Range<Integer>> yRanges= new ArrayList();
    ArrayList<Range<Integer>> lightLevels= new ArrayList();
    ArrayList<String>  requiredBlocks= new ArrayList(), groundBlocks= new ArrayList(), disallowedBlocks= new ArrayList();

    ArrayList<EffectProperties> effects = new ArrayList();
    ArrayList<String> names = new ArrayList();
    ArrayList<String> announcements = new ArrayList();

    boolean enableDebug = false;

    public SpawnPropertyGroup(){
        dimensions.add("minecraft:overworld");
        placements.add(Placement.SURFACE);
        disallowedBlocks.add("minecraft:bedrock");
    }

    public boolean chunkCheck(ServerLevel world, BlockPos pos){
        boolean isValid = isValidDimension(this.dimensions, world)
                        && isValidBiome(this.biomes, world, pos)
                        && isValidTemperature(this.temperatures, world, pos)
                        && isValidArea(this.areaRestriction, pos);
        return isValid;
    }
    public boolean coarseCheck(ServerLevel world, BlockPos pos){
        boolean isValid = (world.random.nextFloat() <= chanceToAttemptSpawns)
                        && chunkCheck(world, pos)
                        && isValidTime(this.timeframes, world)
                        && isValidMoonphase(this.moonPhases, world)
                        && isValidWeather(this.weather, this.activateIfStormy, world, pos);

        return isValid;
    }

    public boolean fineCheck(ServerLevel world, BlockPos pos, MobSpawnProperties properties){
        boolean isValid =
                        isValidYLevel(this.yRanges, pos)
                        && isValidLightLevel(this.lightLevels, world, pos)
                        && isNotBlackListedArea(this.blacklistedAreas, pos)
                        && isValidGroundBlock(this.groundBlocks, world, pos)
                        && isNotInvalidGroundBlock(this.disallowedBlocks, world, pos)
                        && isValidPlacement(this.placements, world, pos)
                        && hasRequiredBlocks(this.requiredBlocks, world, pos);
        return isValid;
    }

    public static boolean checkMobLimit(MobSpawnProperties properties, ServerLevel world, BlockPos pos, int totalNewMobs){
        if(properties == null || !properties.shouldLimit()) return true;
        EntityType type = getEntityType(properties.entityType);
        if(type == null) return false;

        List<? extends Entity> nearbyEntities = world.getEntitiesOfClass(type.create(world).getClass(), new AABB(pos).inflate(128));
        if (nearbyEntities.size() + totalNewMobs >= properties.limit) {
            return false;
        }

        return true;
    }

    public static boolean hasRequiredBlocks(ArrayList<String> allowedBlocks, ServerLevel world, BlockPos pos){
        if (shouldSkipCheck(allowedBlocks)) return true;
        ArrayList<BlockState> check = new ArrayList<>();
        check.add(world.getBlockState(pos.below()));
        for(int i = 1; i <=3; i++){
            if(world.isLoaded(pos.offset(i,0,0))){check.add(world.getBlockState(pos.offset(i,0,0)));}
            if(world.isLoaded(pos.offset(-i,0,0))){check.add(world.getBlockState(pos.offset(-i,0,0)));}
            if(world.isLoaded(pos.offset(0,0,i))){check.add(world.getBlockState(pos.offset(0,0,i)));}
            if(world.isLoaded(pos.offset(0,0,-i))){check.add(world.getBlockState(pos.offset(0,0,-i)));}
        }

        for (final BlockState b : check) {
            if(allowedBlocks.stream().anyMatch(block -> block.equals(getBlockId(b)))){
                return true;
            }
        }
        return false;
    }

    public static boolean isValidGroundBlock(ArrayList<String> allowedBlocks, ServerLevel world, BlockPos pos){
        return shouldSkipCheck(allowedBlocks) || allowedBlocks.stream().anyMatch(block -> block.equals(getBlockId(world.getBlockState(pos.below()))));
    }

    public static boolean isNotInvalidGroundBlock(ArrayList<String> disallowedBlocks, ServerLevel world, BlockPos pos){
        return shouldSkipCheck(disallowedBlocks) || !disallowedBlocks.stream().anyMatch(block -> block.equals(getBlockId(world.getBlockState(pos.below()))));
    }

    public static boolean isValidPlacement(ArrayList<Placement> validPlacements, ServerLevel world, BlockPos pos){
        if (shouldSkipCheck(validPlacements)) return true;
        boolean isValid = false;
        for(Placement p : validPlacements){
            int surfaceY = (int) (world.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()));
            BlockPos ceil = new BlockPos(pos.getX(), surfaceY-1, pos.getZ());
            switch (p){
                case SURFACE:
                    isValid = (world.canSeeSky(pos) || world.getBlockState(ceil).is(BlockTags.LEAVES))
                            && world.getBlockState(pos).isAir() && world.getBlockState(pos.below()).getMaterial().isSolid();
                    break;
                case IN_WATER:
                    ceil = pos;
                    while(ceil.getY() < surfaceY && world.getBlockState(ceil).is(Blocks.WATER)){
                        ceil = ceil.above();
                    }
                    isValid = world.canSeeSky(ceil) && world.getBlockState(pos).is(Blocks.WATER);
                    break;
                case CAVE_WATER:
                    surfaceY = (int) (world.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1.0f);
                    ceil = pos;
                    while(ceil.getY() < surfaceY && !world.getBlockState(ceil.above()).getMaterial().isSolid()){
                        ceil = ceil.above();
                    }
                    isValid = !world.canSeeSky(pos)
                            && ceil.getY() < surfaceY
                            && world.getBlockState(pos).is(Blocks.WATER);
                    break;
                case IN_LAVA:
                    isValid = !world.getBlockState(pos).getMaterial().isSolid() && world.getBlockState(pos.below()).is(Blocks.LAVA);
                    break;
                case AIR:
                    isValid = world.getBlockState(pos).isAir() && world.getBlockState(pos.below()).isAir() && world.canSeeSky(pos);
                    break;
                case UNDERGROUND:
                    surfaceY = (int) (world.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1.0f);
                    ceil = pos;
                    while(ceil.getY() < surfaceY && !world.getBlockState(ceil.above()).getMaterial().isSolid()){
                        ceil = ceil.above();
                    }
                    isValid = !world.canSeeSky(pos)
                            && ceil.getY() < surfaceY
                            && world.getBlockState(pos).isAir()
                            && world.getBlockState(pos.below()).getMaterial().isSolid();
                    break;
            }
            if(isValid) return true;
        }
        return false;
    }

    public static boolean isValidLightLevel(ArrayList<Range<Integer>> validLightLevels, ServerLevel world, BlockPos pos){
        return shouldSkipCheck(validLightLevels) || validLightLevels.stream().anyMatch((lightRange -> lightRange.isWithinRange(world.getMaxLocalRawBrightness(pos))));
    }

    public static boolean isValidYLevel(ArrayList<Range<Integer>> validYLevels, BlockPos pos){
        return shouldSkipCheck(validYLevels) || validYLevels.stream().anyMatch((yRange -> yRange.isWithinRange(pos.getY())));
    }

    public static boolean isValidTemperature(ArrayList<Range<Float>> validTemperature, ServerLevel world, BlockPos pos) {
        return shouldSkipCheck(validTemperature) || validTemperature.stream().anyMatch(temp -> temp.isWithinRange(world.getBiome(pos).getBaseTemperature()));
    }

    public static boolean isValidWeather(ArrayList<Biome.Precipitation> validWeather, boolean activateIfStormy, ServerLevel world, BlockPos pos) {
        return (activateIfStormy && world.isThundering()) ||
                (!activateIfStormy && shouldSkipCheck(validWeather)) ||
                validWeather.stream().anyMatch(weather -> {
                    switch (weather){
                        case SNOW:
                            return world.getBiome(pos).getPrecipitation().equals(Biome.Precipitation.SNOW) && world.getBiome(pos).getTemperature(pos) < .15f;
                        case RAIN:
                            return world.getBiome(pos).getPrecipitation().equals(Biome.Precipitation.RAIN) && world.isRaining() && world.getBiome(pos).getTemperature(pos) >= .15f;
                        case NONE:
                            return world.getBiome(pos).getPrecipitation().equals(Biome.Precipitation.NONE) || !world.isRaining();
                    }
                    return false;
                });
    }
    public static boolean isValidMoonphase(ArrayList<MoonPhases> validMoonPhases, ServerLevel world) {
        return shouldSkipCheck(validMoonPhases) || validMoonPhases.stream().anyMatch(phase -> phase == MoonPhases.values()[world.getMoonPhase()]);
    }

    public static boolean isValidTime(ArrayList<Range<Integer>> validTimeframes, ServerLevel world) {
        return shouldSkipCheck(validTimeframes) || validTimeframes.stream().anyMatch(range -> range.isWithinRange((int)world.getDayTime()%24000));
    }

    public static boolean isValidDimension(ArrayList<String> validDimensions, ServerLevel world) {
        return shouldSkipCheck(validDimensions) || validDimensions.contains(world.dimension().location().toString());
    }

    public static boolean isValidBiome(ArrayList<String> validBiomes, ServerLevel world, BlockPos pos){
        return shouldSkipCheck(validBiomes) || validBiomes.contains(getBiomeId(world, pos))
//                || world.getBiome(pos.atY(-254)).tags().anyMatch(tag -> {
//                    return validBiomes.contains(tag.location().toString());
//                })
//                || world.getBiome(pos.atY(world.getSeaLevel()-1)).tags().anyMatch(tag -> {
//                    return validBiomes.contains(tag.location().toString());
//                })
//                || world.getBiome(pos.atY(-63)).tags().anyMatch(tag -> {
//                    return validBiomes.contains(tag.location().toString());
//                })
                ;
    }

    public static boolean isValidArea(ArrayList<RepeatableArea> validAreas, BlockPos pos){
        return shouldSkipCheck(validAreas) || validAreas.stream().anyMatch(area -> area.isPosWithinArea(pos));
    }

    public static boolean isNotBlackListedArea(ArrayList<SimpleArea> invalidAreas, BlockPos pos){
        return shouldSkipCheck(invalidAreas) || invalidAreas.stream().noneMatch(area -> area.isPosWithinArea(pos));
    }

    public static boolean shouldSkipCheck(ArrayList list){return list == null || list.size() == 0;}

    public MobSpawnProperties pickMob(){
        if(mobs == null || mobs.size() == 0) return null;
        if(weightedMobs == null){
            weightedMobs = new ArrayList<>();
            float totalWeight = 0;
            for(MobSpawnProperties m : mobs){totalWeight += m.weight;}

            float threshold = 0;
            for(MobSpawnProperties m : mobs){
                threshold += m.weight/totalWeight;
                weightedMobs.add(new Pair(m, threshold));
            }
        }
        double rand = Math.random();
        for (Pair<MobSpawnProperties> p : weightedMobs){
            if(rand < p.threshold) {
                return p.entry;
            }
        }
        return weightedMobs.get(weightedMobs.size()-1).entry;
    }

    public MobEffectInstance getEffect(){
        if(effects == null || effects.size() == 0) return null;
        EffectProperties effectProperties = effects.get((int)(Math.random() * effects.size()));
        MobEffect effect = getEffectByString(effectProperties.effect);
        if(effect == null) return null;
        return new MobEffectInstance(effect, effectProperties.duration);
    }

    public String getName(){
        if(names == null || names.size() == 0) return null;
        return names.get((int)(Math.random() * names.size()));
    }
    public String getAnouncement(){
        if(announcements == null || announcements.size() == 0) return null;
        return announcements.get((int)(Math.random() * announcements.size()));
    }

    public static class Pair<T>{
        T entry; float threshold;
        public Pair(T entry, float threshold){ this.entry = entry; this.threshold = threshold;}
    }
    public static class EffectProperties{
        String effect = "minecraft:glowing";
        int duration = 200;
    }
    public static class MobSpawnProperties{
        String entityType = "invalid:entity";
        int minSpawnGroup = 1, maxSpawnGroup = 1, limit = 8, weight = 1;
        public boolean shouldLimit(){return limit>0;}
        public String toString(){return "{mob:"+entityType+", min:"+minSpawnGroup+", max:"+maxSpawnGroup+", limit:"+limit+", weight:"+weight+"}";}
    }
    public static class SimpleArea{
        BlockPos start, end;
        public boolean isPosWithinArea(BlockPos pos){
            return start == null || end == null
                    || (pos.getX() >= start.getX() && pos.getX() < end.getX()
                    && pos.getZ() >= start.getZ() && pos.getZ() < end.getZ());
        }
        public String toString(){return "{start: ["+start.getX()+","+start.getZ()+"], end: ["+end.getX()+","+end.getZ()+"]";}
    }
    public static class RepeatableArea{
        int size, repeat, offsetX, offsetZ;
        public boolean isPosWithinArea(BlockPos pos){
            int xRes =((pos.getX()/16) + offsetX) % (size + repeat);
            if (xRes < 0) xRes = xRes + size + repeat;
            int zRes =((pos.getZ()/16) + offsetZ) % (size + repeat);
            if (zRes < 0) zRes = zRes + size + repeat;
            return size <= 0 || repeat <= 0 || (size > xRes && size > zRes);
        }
        public String toString(){return "{size: "+size+", repeat: "+repeat+", offsetX: "+offsetX+", offsetZ: "+offsetZ+"}";}
    }
    public static class Range<T extends Number>{
        T start, end;
        public Range(T start, T end) {this.start = start; this.end = end;}
        public String toString(){return "("+start+", "+end+")"; }
        public boolean isWithinRange(T current){return current.doubleValue() >= start.doubleValue() && current.doubleValue() <= end.doubleValue();}
    }
    public enum Placement {UNDERGROUND, SURFACE, IN_WATER, CAVE_WATER, IN_LAVA, AIR};
    public enum MoonPhases {FULL, WANING_GIBBOUS, LAST_QUARTER, WANING_CRESCENT,  NEW, WAXING_CRESCENT, FIRST_QUARTER,  WAXING_GIBBOUS};

    public void debugMsg(String str){if(enableDebug) System.out.println(Constants.MOD_NAME + " " + groupName + ": " + str);}

    public void print(){
        System.out.println("PRINTING SPAWN GROUP: " + groupName);
        System.out.println("mobs: " + Arrays.toString(mobs.toArray()));
        System.out.println("totalMobPicks: " + totalMobsToPick);
        System.out.println("biomes: " + Arrays.toString(biomes.toArray()));
        System.out.println("placements: " + Arrays.toString(placements.toArray()));
        System.out.println("runOnChunkGen: " + runImmediately);
        System.out.println("changeToAttemptSpawns: " + chanceToAttemptSpawns);
        System.out.println("ticksBetweenSpawnAttempts: " + ticksBetweenSpawnAttempts);
        System.out.println("spawnCoolDown: " + spawnCoolDown);
        System.out.println("timeframes: " + Arrays.toString(timeframes.toArray()));
        System.out.println("moonPhases: " + Arrays.toString(moonPhases.toArray()));
        System.out.println("weather: " + Arrays.toString(weather.toArray()));
        System.out.println("temperatures: " + Arrays.toString(temperatures.toArray()));
        System.out.println("yRanges: " + Arrays.toString(yRanges.toArray()));
        System.out.println("lightLevels: " + Arrays.toString(lightLevels.toArray()));
        System.out.println("requiredBlocks: " + Arrays.toString(requiredBlocks.toArray()));
        System.out.println("groundBlocks: " + Arrays.toString(groundBlocks.toArray()));
        System.out.println("areaRestriction: " + Arrays.toString(areaRestriction.toArray()));
        System.out.println("blacklistedAreas: " + Arrays.toString(blacklistedAreas.toArray()));
        System.out.println("disallowedBlocks: " + Arrays.toString(disallowedBlocks.toArray()));
        System.out.println("enableDebug: " + enableDebug);
    }

    public void printCoarseCheck(ServerLevel world, BlockPos pos, boolean failuresOnly){
        System.out.println("Coarse checks for " + groupName + " @ " + pos.getX() + " "+ pos.getY() + " "+ pos.getZ() + ":");
        System.out.println("Chance of activation: " + chanceToAttemptSpawns*100 + "%");
        printCheck("isValidDimension", isValidDimension(this.dimensions, world), failuresOnly);
        printCheck("isValidBiome", isValidBiome(this.biomes, world, pos), failuresOnly);
        printCheck("isValidTime", isValidTime(this.timeframes, world), failuresOnly);
        printCheck("isValidMoonphase", isValidMoonphase(this.moonPhases, world), failuresOnly);
        printCheck("isValidWeather", isValidWeather(this.weather, this.activateIfStormy, world, pos), failuresOnly);
        printCheck("isValidTemperature", isValidTemperature(this.temperatures, world, pos), failuresOnly);
        printCheck("isValidArea", isValidArea(this.areaRestriction, pos), failuresOnly);
    }

    public void printFineCheck(ServerLevel world, BlockPos pos, boolean failuresOnly){
        System.out.println("Fine checks for +" + groupName + " @ " + pos.getX() + " "+ pos.getY() + " "+ pos.getZ() + ":");
        printCheck("isValidYLevel", isValidYLevel(this.yRanges, pos), failuresOnly);
        printCheck("isValidLightLevel", isValidLightLevel(this.lightLevels, world, pos), failuresOnly);
        printCheck("isNotBlackListedArea", isNotBlackListedArea(this.blacklistedAreas, pos), failuresOnly);
        printCheck("isValidGroundBlock", isValidGroundBlock(this.groundBlocks, world, pos), failuresOnly);
        printCheck("isNotInvalidGroundBlock", isNotInvalidGroundBlock(this.disallowedBlocks, world, pos), failuresOnly);
        printCheck("isValidPlacement", isValidPlacement(this.placements, world, pos), failuresOnly);
        printCheck("hasRequiredBlocks", hasRequiredBlocks(this.requiredBlocks, world, pos), failuresOnly);
    }

    private void printCheck(String str, boolean res, boolean failuresOnly){
        if(!failuresOnly || (failuresOnly && !res)) System.out.println(str + ": " + res);
    }
}
