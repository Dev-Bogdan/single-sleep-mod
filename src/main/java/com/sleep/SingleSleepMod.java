package com.example.sleep;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleSleepMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("SingleSleep");

    @Override
    public void onInitialize() {
        LOGGER.info("Single Sleep Mod initialized!");
        
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                long time = world.getTimeOfDay() % 24000;
                boolean isNight = time >= 12541 && time < 23458;
                
                if (isNight) {
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        if (player.isSleeping()) {
                            LOGGER.info("Player {} is sleeping at time {}", player.getName().getString(), time);
                            
                            long timeToSkip = (23458 - time);
                            world.setTimeOfDay(world.getTimeOfDay() + timeToSkip);
                            
                            world.getPlayers().forEach(p -> {
                                if (p.isSleeping()) p.wakeUp();
                                p.sendMessage(Text.literal(
                                    player.getName().getString() + " slept through the night!"
                                ), false);
                            });
                            
                            world.setWeather(0, 0, false, false);
                            break;
                        }
                    }
                }
            }
        });
    }
}