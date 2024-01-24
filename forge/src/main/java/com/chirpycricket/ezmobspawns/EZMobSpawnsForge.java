package com.chirpycricket.ezmobspawns;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class EZMobSpawnsForge {
    
    public EZMobSpawnsForge() {
        // Use Forge to bootstrap the Common mod.
        CommonClass.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverTickEvent(TickEvent.WorldTickEvent e) {
//        for(ServerLevel level : e.getServer().getAllLevels()){
//            SpawnTicker.onTick(level);
//        }
        if(!e.world.isClientSide){
            SpawnTicker.onTick((ServerLevel)e.world);
        }
    }
}