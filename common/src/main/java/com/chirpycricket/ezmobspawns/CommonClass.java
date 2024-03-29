package com.chirpycricket.ezmobspawns;

import com.chirpycricket.ezmobspawns.platform.Services;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public class CommonClass {

    public static void init() {
        ConfigReader.init();
//        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
//        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));
//        if (Services.PLATFORM.isModLoaded(Constants.MOD_ID)) {
//            Constants.LOG.info("Hello to " + Constants.MOD_ID);
//        }
    }

    public static String getBiomeId(ServerLevel world, BlockPos pos){
        return world.getBiome(pos).unwrapKey().get().location().toString();
    }

    public static String getBlockId(BlockState b){
        return b.getBlockHolder().unwrapKey().get().location().toString();
    }

    public static String getEffectId(MobEffect effect){
        return effect.builtInRegistryHolder().unwrapKey().get().location().toString();
    }

    public static MobEffect getEffectByString(String str) {
        if(!BuiltInRegistries.MOB_EFFECT.getOptional(ResourceLocation.tryParse(str)).isPresent()){
            System.out.println(Constants.MOD_ID + " WARNING: Encountered invalid effect id \"" + str + "\" in one of the config files. Skipping.");
            return null;
        }
        return BuiltInRegistries.MOB_EFFECT.getOptional(ResourceLocation.tryParse(str)).get();
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

    public static void spawnEntity(ServerLevel world, BlockPos pos, SpawnPropertyGroup spawnProperties, EntityType type){
        Entity e = type.spawn(world, pos, MobSpawnType.NATURAL);
        if(e instanceof LivingEntity){
            LivingEntity entity = (LivingEntity)e;
            MobEffectInstance effectInstance = spawnProperties.getEffect();
            if(effectInstance != null) entity.addEffect(effectInstance);

            String name = spawnProperties.getName();
            if(name != null) {entity.setCustomName(Component.literal(name));}

            String announcemnt = spawnProperties.getAnouncement();
            if(announcemnt != null){
                if(name != null) {announcemnt = name + " " + announcemnt;}
                else {announcemnt = "A " + entity.getName() + announcemnt;}
                for(ServerPlayer p : world.players()){
                    p.sendSystemMessage(Component.literal(announcemnt));
                }
            }
        }
    }

}