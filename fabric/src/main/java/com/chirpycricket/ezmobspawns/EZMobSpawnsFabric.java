package com.chirpycricket.ezmobspawns;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class EZMobSpawnsFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        // Use Fabric to bootstrap the Common mod.
        CommonClass.init();
        ServerTickEvents.END_WORLD_TICK.register(SpawnTicker::onTick);
    }
}
