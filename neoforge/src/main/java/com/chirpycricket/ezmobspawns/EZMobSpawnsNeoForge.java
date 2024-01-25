package com.chirpycricket.ezmobspawns;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

@Mod(Constants.MOD_ID)
public class EZMobSpawnsNeoForge {
    public EZMobSpawnsNeoForge() {
        // Use NeoForge to bootstrap the Common mod.
        CommonClass.init();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverTickEvent(TickEvent.ServerTickEvent e) {
        for(ServerLevel level : e.getServer().getAllLevels()){
            SpawnTicker.onTick(level);
        }
    }
}