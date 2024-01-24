package com.chirpycricket.ezmobspawns;


public class ForgeGlobalProperties {

    int chunkRange = 4;
    float chunkCoverage = 1.0f;
    int maxPtsPerChunk  = 100;
    ForgeSpawnPropertyGroup defaults = new ForgeSpawnPropertyGroup();

    public void print() {
        System.out.println("PRINTING GLOBAL PROPERTIES: ");
        System.out.println("chunkRange: " + chunkRange);
        System.out.println("chunkCoverage: " + chunkCoverage);
        System.out.println("maxPtsPerChunk: " + maxPtsPerChunk);
        defaults.print();
    }
}