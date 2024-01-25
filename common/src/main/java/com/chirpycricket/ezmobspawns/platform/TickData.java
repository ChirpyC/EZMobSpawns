package com.chirpycricket.ezmobspawns.platform;

public class TickData{

    int lastTick = 0, triggerDelay = 6000, coolDownDelay = 6000;
    boolean isInCooldown = false;
    public TickData(int triggerDelay, int coolDownDelay, boolean startImmediately){
        this.triggerDelay = triggerDelay;
        this.coolDownDelay = coolDownDelay;
        if(startImmediately){lastTick = -triggerDelay-1;}
    }

    public void tick(int currentTick){
        if(isInCooldown && currentTick-lastTick > coolDownDelay) {
            isInCooldown = false;
            lastTick = currentTick;
        }
    }

    public void resetTicks(int currentTick){lastTick = currentTick;}
    public boolean shouldTrigger(int currentTick){return !isInCooldown && currentTick-lastTick > triggerDelay;}
    public void startCoolDown(int currentTick){lastTick = currentTick; isInCooldown = true;}
}