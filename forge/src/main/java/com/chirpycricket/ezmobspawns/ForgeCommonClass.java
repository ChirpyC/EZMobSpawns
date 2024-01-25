package com.chirpycricket.ezmobspawns;

import com.chirpycricket.ezmobspawns.platform.Constants;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class ForgeCommonClass {

    public static void init() {
        ForgeConfigReader.init();
//        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
//        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));
//        if (Services.PLATFORM.isModLoaded(Constants.MOD_ID)) {
//            Constants.LOG.info("Hello to " + Constants.MOD_ID);
//        }
    }

    public static String getBiomeId(ServerWorld world, BlockPos pos){
        return world.getBiomeName(pos).get().location().toString();
//        return BuiltinRegistries.BIOME.getKey(world.getBiome(pos)).toString();
//        return world.getBiome(pos).unwrapKey().get().location().toString();
    }

    public static String getBlockId(BlockState b){
        return Registry.BLOCK.getKey(b.getBlock()).toString();
//        return b.getBlock().builtInRegistryHolder().unwrapKey().get().location().toString();
//        return b.getBlockHolder().unwrapKey().get().location().toString();
    }

    public static Effect getEffectByString(String str) {

        if(!Registry.MOB_EFFECT.getOptional(ResourceLocation.tryParse(str)).isPresent()){
            System.out.println(Constants.MOD_ID + " WARNING: Encountered invalid effect id \"" + str + "\" in one of the config files. Skipping.");
            return null;
        }
        return Registry.MOB_EFFECT.getOptional(ResourceLocation.tryParse(str)).get();
    }

    public static int nextRandIntBetween(int min, int max){
        return min + (int)(Math.random()*(max - min));
    }

    public static EntityType getEntityType(String entityTypeStr){
        if(!EntityType.byString(entityTypeStr).isPresent()){
            System.out.println(Constants.MOD_ID + " WARNING: Encountered invalid mob id \"" + entityTypeStr + "\" in one of the config files. Skipping.");
            return null;
        }
        EntityType type = EntityType.byString(entityTypeStr).get();
        if(type != null) {
            return type;
        }
        return null;
    }

    public static void spawnEntity(ServerWorld world, BlockPos pos, ForgeSpawnPropertyGroup spawnProperties, EntityType type){
        Entity e = type.spawn(world, null, null, null, pos, SpawnReason.NATURAL, false, false);
        if(e instanceof LivingEntity){
            LivingEntity entity = (LivingEntity)e;
            EffectInstance effectInstance = spawnProperties.getEffect();
            if(effectInstance != null) entity.addEffect(effectInstance);

            String name = spawnProperties.getName();

            if(name != null) {entity.setCustomName(new StringTextComponent(name));}

            String announcemnt = spawnProperties.getAnouncement();
            if(announcemnt != null){
                if(name != null) {announcemnt = name + " " + announcemnt;}
                else {announcemnt = "A " + entity.getName() + announcemnt;}
                for(ServerPlayerEntity p : world.players()){
                    p.displayClientMessage(new StringTextComponent(announcemnt), false);
                }
            }
        }
    }

}