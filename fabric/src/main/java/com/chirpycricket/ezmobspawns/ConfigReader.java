package com.chirpycricket.ezmobspawns;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import java.io.*;
import java.util.*;

import static com.chirpycricket.ezmobspawns.Constants.CONFIG_PREFIX;

public class ConfigReader {

    public static GlobalProperties globalProperties = new GlobalProperties();
    public static ArrayList<SpawnPropertyGroup> spawnProperties = new ArrayList<SpawnPropertyGroup>();

    final static private String RESOURCE_PACK_DIR = "resourcepacks", CONFIG_DIR = "config";
    public static void init() {

        String configDirStr = Minecraft.getInstance().getResourcePackDirectory().toString();
        configDirStr = configDirStr.substring(0, configDirStr.length()-RESOURCE_PACK_DIR.length()) + CONFIG_DIR;
        File configFolder = new File(configDirStr+ File.separator );

        //get all files in config
        ArrayList<String> files = getConfigFiles(configFolder);
        for(String file : files){
            String filename = configFolder + File.separator + file;
            try {
                JsonObject json = new Gson().fromJson(new FileReader(filename), JsonObject.class);
                ArrayList<SpawnPropertyGroup> sp = parseJson(json);
                if(sp != null) spawnProperties.addAll(sp);
            }
            catch (IOException e) {
                System.out.println(Constants.MOD_ID + " ENCOUNTERED JSON EXCEPTION in " + file + ":\n" + e.getMessage());
            }
        }
    }

    public static ArrayList<String> getConfigFiles(final File folder) {
        ArrayList<String> fileNames = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getConfigFiles(fileEntry);
            } else {
                if(fileEntry.getName().startsWith(CONFIG_PREFIX)) {
                    fileNames.add(fileEntry.getName());
                }
            }
        }
        return fileNames;
    }

    private static ArrayList<SpawnPropertyGroup> parseJson(String jsonStr){
        JsonObject json = new Gson().fromJson(jsonStr, JsonObject.class);
        return parseJson(json);
    }

    private static ArrayList<SpawnPropertyGroup> parseJson(JsonObject json) {
        //System.out.println("JSON IS " + json.get("spawnGroups"));
        if (json.has("globalSettings")) {
            JsonObject globalSettings = json.getAsJsonObject("globalSettings");
            if (globalSettings.has("chunkRange")) {
                globalProperties.chunkRange = globalSettings.get("chunkRange").getAsInt();
            }
            if (globalSettings.has("chunkCoverage")) {
                globalProperties.chunkCoverage = globalSettings.get("chunkCoverage").getAsFloat();
            }
            if (globalSettings.has("maxPtsPerChunk")) {
                globalProperties.maxPtsPerChunk = globalSettings.get("maxPtsPerChunk").getAsInt();
            }
            if (globalSettings.has("spawnGroupDefaults")) {
                JsonObject entry = globalSettings.get("spawnGroupDefaults").getAsJsonObject();
                globalProperties.defaults = parseSpawnGroup(entry, false);
            }
            if(globalProperties.defaults.enableDebug) {globalProperties.print();}
        }

        ArrayList<SpawnPropertyGroup> res = new ArrayList<>();
        if (isJsonArrayPresent(json, "spawnGroups")) {
            for (JsonElement entryObj : json.getAsJsonArray("spawnGroups")) {
                JsonObject entry = entryObj.getAsJsonObject();
                SpawnPropertyGroup sp = parseSpawnGroup(entry, true);
                if(sp != null) {
                    res.add(sp);
                    if(sp.enableDebug) {sp.print();}
                }
            }
            if(res.size() > 0) return res;
        }
        return null;
    }

    private static SpawnPropertyGroup parseSpawnGroup(JsonObject entry, boolean useMobs) {
        SpawnPropertyGroup defaults = globalProperties.defaults;
        SpawnPropertyGroup sp = new SpawnPropertyGroup();

        if (isJsonArrayPresent(entry, "mobs")) {
            JsonArray mobsArray = entry.get("mobs").getAsJsonArray();
            for (JsonElement m : mobsArray) {
                JsonObject mobJsonEntry = m.getAsJsonObject();
                if (mobJsonEntry.has("mob")) {
                    SpawnPropertyGroup.MobSpawnProperties msp = new SpawnPropertyGroup.MobSpawnProperties();
                    msp.entityType = mobJsonEntry.get("mob").getAsString();
                    if (mobJsonEntry.has("min")) {
                        msp.minSpawnGroup = mobJsonEntry.get("min").getAsInt();
                    }
                    if (mobJsonEntry.has("max")) {
                        msp.maxSpawnGroup = mobJsonEntry.get("max").getAsInt();
                    }
                    if (mobJsonEntry.has("limit")) {
                        msp.limit = mobJsonEntry.get("limit").getAsInt();
                    }
                    if (mobJsonEntry.has("weight")) {
                        msp.weight = mobJsonEntry.get("weight").getAsInt();
                    }
                    sp.mobs.add(msp);
                }
            }
        }
        else if(useMobs) {return null;}

        if (entry.has("groupName")) {sp.groupName = entry.get("groupName").getAsString();}
        else {sp.groupName = defaults.groupName;}

        if (entry.has("totalMobsToPick")) {sp.totalMobsToPick = entry.get("totalMobsToPick").getAsInt();}
        else {sp.totalMobsToPick = defaults.totalMobsToPick;}

        if (isJsonArrayPresent(entry, "dimensions")) {
            entry.getAsJsonArray("dimensions").forEach(str -> sp.dimensions.add(str.getAsString()));
            if(sp.dimensions.size()>1) {
                sp.dimensions.remove(0); //remove default
            }
        }
        else {sp.dimensions = defaults.dimensions;}

        if (isJsonArrayPresent(entry, "biomes")) {entry.getAsJsonArray("biomes").forEach(str -> sp.biomes.add(str.getAsString()));}
        else {sp.biomes = defaults.biomes;}

        if (isJsonArrayPresent(entry, "placement")) {
            entry.getAsJsonArray("placement").forEach(str -> sp.placements.add(SpawnPropertyGroup.Placement.valueOf(str.getAsString().toUpperCase())));
            if(sp.placements.size()>1){
                sp.placements.remove(0); //remove default
            }
        }
        else {sp.placements = defaults.placements;}

        if (entry.has("chanceToAttemptSpawns")) {sp.chanceToAttemptSpawns = entry.get("chanceToAttemptSpawns").getAsInt();}
        else {sp.chanceToAttemptSpawns = defaults.chanceToAttemptSpawns;}

        if (entry.has("runImmediately")) {sp.runImmediately = entry.get("runImmediately").getAsBoolean();}
        else {sp.runImmediately = defaults.runImmediately;}

        if (entry.has("ticksBetweenAttempts")) {sp.ticksBetweenSpawnAttempts = entry.get("ticksBetweenAttempts").getAsInt();}
        else {sp.ticksBetweenSpawnAttempts = defaults.ticksBetweenSpawnAttempts;}

        if (entry.has("spawnCoolDown")) {sp.spawnCoolDown = entry.get("spawnCoolDown").getAsInt();}
        else {sp.spawnCoolDown = defaults.spawnCoolDown;}

        if (entry.has("enableDebug")) {sp.enableDebug = entry.get("enableDebug").getAsBoolean();}
        else {sp.enableDebug = defaults.enableDebug;}

        if (isJsonArrayPresent(entry, "timeframes")) {
            entry.getAsJsonArray("timeframes").forEach(rng -> {
                if (parseRange(rng.getAsJsonObject()) != null) {
                    sp.timeframes.add(parseRange(rng.getAsJsonObject()));
                }
            });
        }
        else {sp.timeframes = defaults.timeframes;}

        if (isJsonArrayPresent(entry, "moonphases")) {entry.getAsJsonArray("moonphases").forEach(str -> sp.moonPhases.add(SpawnPropertyGroup.MoonPhases.valueOf(str.getAsString().toUpperCase())));}
        else {sp.moonPhases = defaults.moonPhases;}

        if (isJsonArrayPresent(entry, "weather")) {
            entry.getAsJsonArray("weather").forEach(str -> {
                if(str.getAsString().toLowerCase().equals("storm")){
                    sp.activateIfStormy = true;
                }
                else {
                    sp.weather.add(Biome.Precipitation.valueOf(str.getAsString().toUpperCase()));
                }
            });
        }
        else {sp.weather = defaults.weather;}

        if (isJsonArrayPresent(entry, "temperatures")) {
            entry.getAsJsonArray("temperatures").forEach(rng -> {
                if (parseRange(rng.getAsJsonObject()) != null) {
                    sp.temperatures.add(parseRange(rng.getAsJsonObject()));
                }
            });
        }
        else {sp.temperatures = defaults.temperatures;}

        if (isJsonArrayPresent(entry, "lightLevels")) {
            entry.getAsJsonArray("lightLevels").forEach(rng -> {
                if (parseRange(rng.getAsJsonObject()) != null) {
                    sp.lightLevels.add(parseRange(rng.getAsJsonObject()));
                }
            });
        }
        else {sp.lightLevels = defaults.lightLevels;}

        if (isJsonArrayPresent(entry, "yRanges")) {
            entry.getAsJsonArray("yRanges").forEach(rng -> {
                if (parseRange(rng.getAsJsonObject()) != null) {
                    sp.yRanges.add(parseRange(rng.getAsJsonObject()));
                }
            });
        }
        else {sp.yRanges = defaults.yRanges;}

        if (isJsonArrayPresent(entry, "requiredBlocks")) {
            entry.getAsJsonArray("requiredBlocks").forEach(str -> sp.requiredBlocks.add(str.getAsString()));
        }
        else {sp.requiredBlocks = defaults.requiredBlocks;}

        if (isJsonArrayPresent(entry, "groundBlocks")) {entry.getAsJsonArray("groundBlocks").forEach(str -> sp.groundBlocks.add(str.getAsString()));}
        else {sp.groundBlocks = defaults.groundBlocks;}

        if (isJsonArrayPresent(entry, "disallowedBlocks")) {
            entry.getAsJsonArray("disallowedBlocks").forEach(str -> sp.disallowedBlocks.add(str.getAsString()));
            if(sp.disallowedBlocks.size()>1){
                sp.disallowedBlocks.remove(0); //remove default
            }
        }
        else {sp.disallowedBlocks = defaults.disallowedBlocks;}

        if (isJsonArrayPresent(entry, "areaRestriction")) {
            JsonArray areaRestrictionJsonArray = entry.get("areaRestriction").getAsJsonArray();
            ArrayList<SpawnPropertyGroup.RepeatableArea> spawnAreas = new ArrayList<>();
            for (JsonElement ar : areaRestrictionJsonArray) {
                SpawnPropertyGroup.RepeatableArea area = new SpawnPropertyGroup.RepeatableArea();
                JsonObject mobJsonEntry = ar.getAsJsonObject();
                if (mobJsonEntry.has("areaSize")) {
                    area.size = mobJsonEntry.get("areaSize").getAsInt();
                }
                if (mobJsonEntry.has("repeat")) {
                    area.repeat = mobJsonEntry.get("repeat").getAsInt();
                }
                if (mobJsonEntry.has("offsetX")) {
                    area.offsetX = mobJsonEntry.get("offsetX").getAsInt();
                }
                if (mobJsonEntry.has("offsetZ")) {
                    area.offsetZ = mobJsonEntry.get("offsetZ").getAsInt();
                }
                spawnAreas.add(area);
            }
            sp.areaRestriction = spawnAreas;
        }
        else {sp.areaRestriction = defaults.areaRestriction;}
        if (isJsonArrayPresent(entry, "blacklistedAreas")) {
            JsonArray blacklistedAreasJsonArray = entry.get("blacklistedAreas").getAsJsonArray();
            ArrayList<SpawnPropertyGroup.SimpleArea> blacklistedAreas = new ArrayList<>();
            for (JsonElement ar : blacklistedAreasJsonArray) {
                SpawnPropertyGroup.SimpleArea area = new SpawnPropertyGroup.SimpleArea();
                JsonObject mobJsonEntry = ar.getAsJsonObject();
                if (mobJsonEntry.has("startX") && mobJsonEntry.has("startZ")) {
                    area.start = new BlockPos(mobJsonEntry.get("startX").getAsInt(), 0, mobJsonEntry.get("startZ").getAsInt());
                }
                if (mobJsonEntry.has("endX") && mobJsonEntry.has("endZ")) {
                    area.end = new BlockPos(mobJsonEntry.get("endX").getAsInt(), 0, mobJsonEntry.get("endZ").getAsInt());
                }
                blacklistedAreas.add(area);
            }
            sp.blacklistedAreas = blacklistedAreas;
        }
        else {sp.blacklistedAreas = defaults.blacklistedAreas;}

        if (isJsonArrayPresent(entry, "effects")) {
            entry.getAsJsonArray("effects").forEach(jsonObj -> {
                SpawnPropertyGroup.EffectProperties effectProperties = new SpawnPropertyGroup.EffectProperties();
                JsonObject jsonEntry = jsonObj.getAsJsonObject();
                if (jsonEntry.has("effect")) {
                    effectProperties.effect = jsonEntry.get("effect").getAsString();
                }
                if (jsonEntry.has("duration")) {
                    effectProperties.duration = jsonEntry.get("duration").getAsInt();
                }
                sp.effects.add(effectProperties);
            });
        }

        if (isJsonArrayPresent(entry, "names")) {entry.getAsJsonArray("names").forEach(str -> sp.names.add(str.getAsString()));}
        else {sp.names = defaults.names;}

        if (isJsonArrayPresent(entry, "announcements")) {entry.getAsJsonArray("announcements").forEach(str -> sp.announcements.add(str.getAsString()));}
        else {sp.announcements = defaults.announcements;}

        return sp;
    }

    private static SpawnPropertyGroup.Range parseRange(JsonObject json){
        Number start, end;
        if (json.has("start")){
            start = json.get("start").getAsFloat();
            if (json.has("end")){
                end = json.get("end").getAsFloat();
                return new SpawnPropertyGroup.Range(start, end);
            }
        }
        return null;
    }

    private static boolean isJsonArrayPresent(JsonObject json, String field){
        return  json.get(field) != null && json.get(field).isJsonArray() && json.getAsJsonArray(field).size() > 0;
    }
}

