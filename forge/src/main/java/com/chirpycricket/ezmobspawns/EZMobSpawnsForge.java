package com.chirpycricket.ezmobspawns;

import com.chirpycricket.ezmobspawns.platform.Constants;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class EZMobSpawnsForge {
    
    public EZMobSpawnsForge() {
        // Use Forge to bootstrap the Common mod.
        ForgeCommonClass.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverTickEvent(TickEvent.WorldTickEvent e) {
//        for(ServerLevel level : e.getServer().getAllLevels()){
//            SpawnTicker.onTick(level);
//        }
        if(!e.world.isClientSide){
            ForgeSpawnTicker.onTick((ServerWorld)e.world);
        }
    }
}