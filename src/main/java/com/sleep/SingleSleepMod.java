package com.sleep;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleSleepMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("SingleSleep");
    private static final long NIGHT_START = 12541;
    private static final long NIGHT_END = 23458;

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            
            if (state.getBlock() instanceof BedBlock) {
                handleSleepEvent((ServerPlayerEntity) player, (ServerWorld) world);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    private void handleSleepEvent(ServerPlayerEntity player, ServerWorld world) {
        long time = world.getTimeOfDay() % 24000;
        if (time >= NIGHT_START && time < NIGHT_END) {
            long timeToSkip = NIGHT_END - time;
            world.setTimeOfDay(world.getTimeOfDay() + timeToSkip);
            
            world.getPlayers().forEach(p -> {
                if (p.isSleeping()) p.wakeUp();
                p.sendMessage(Text.literal(
                    player.getName().getString() + " slept through the night!"
                ), false);
            });
            
            world.setWeather(0, 0, false, false);
        }
    }
}
