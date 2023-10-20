package com.github.zly2006.profilechunks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;

public class profileChunks implements ModInitializer {
    public static final String MOD_ID = "profile-chunks";
    public static int profilingTicks = 0;
    public static ServerCommandSource feedbackSource = null;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(server -> profilingTicks--);
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("profile").then(literal("chunks")
                .executes(context -> {
                    if (profilingTicks > 0) {
                        context.getSource().sendError(Text.literal("Already profiling chunks!"));
                        return 0;
                    }
                    profilingTicks = 100;
                    feedbackSource = context.getSource();
                    return 1;
                })
                .then(argument("ticks", IntegerArgumentType.integer(20)).executes(context -> {
                    int ticks = IntegerArgumentType.getInteger(context, "ticks");
                    if (profilingTicks > 0 && ticks != 0) {
                        context.getSource().sendError(Text.literal("Already profiling chunks!"));
                        return 0;
                    }
                    profilingTicks = ticks;
                    feedbackSource = context.getSource();
                    return 1;
                }))
        ));
    }

    public static void start(String label, long chunk) {

    }

    public static void end(String label, long chunk) {

    }
}
