package com.github.zly2006.profilechunks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ProfileChunks implements ModInitializer {
    public static final String MOD_ID = "profile-chunks";
    public static int profilingTicks = 0;
    public static int totalProfilingTicks = 0;
    public static ServerCommandSource feedbackSource = null;
    public static Long2ObjectMap<Map<String, Long>> chunkNanoTimeMap = new Long2ObjectRBTreeMap<>();
    private static long startNanoTime = 0;

    private static int top(ServerCommandSource source) {
        if (chunkNanoTimeMap.isEmpty()) {
            source.sendError(Text.literal("No profiling data!"));
            return 0;
        }
        source.sendMessage(Text.literal("Top 10 profiling data:"));
        Map<Long, Long> map = new HashMap<>();
        for (Map.Entry<Long, Map<String, Long>> entry : chunkNanoTimeMap.long2ObjectEntrySet()) {
            long sum = 0;
            for (Long value : entry.getValue().values()) {
                sum += value;
            }
            map.put(entry.getKey(), sum / totalProfilingTicks);
        }
        map.entrySet().stream().sorted(Map.Entry.<Long, Long>comparingByValue().reversed()).limit(10).forEachOrdered(entry -> {
            ChunkPos chunkPos = new ChunkPos(entry.getKey());
            source.sendMessage(Text.literal("Chunk " + chunkPos + ": " + entry.getValue() / 1e6 + "ms")
                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/profile chunks show " + chunkPos.getStartPos().getX() + " " + chunkPos.getStartPos().getZ()))));
        });
        return 1;
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            profilingTicks--;
            if (profilingTicks == 0) {
                totalProfilingTicks++;
                feedbackSource.sendError(Text.literal("Profiling finished!"));
                top(feedbackSource);
            }
        });
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("profile").then(literal("chunks")
                .executes(context -> {
                    if (profilingTicks > 0) {
                        context.getSource().sendError(Text.literal("Already profiling chunks!"));
                        return 0;
                    }
                    profilingTicks = 40;
                    totalProfilingTicks = 40;
                    chunkNanoTimeMap.clear();
                    feedbackSource = context.getSource();
                    return 1;
                })
                .then(argument("ticks", IntegerArgumentType.integer(20))
                        .suggests((context, builder) -> builder.suggest(100).buildFuture())
                        .executes(context -> {
                            int ticks = IntegerArgumentType.getInteger(context, "ticks");
                            if (profilingTicks > 0 && ticks != 0) {
                                context.getSource().sendError(Text.literal("Already profiling chunks!"));
                                return 0;
                            }
                            profilingTicks = ticks;
                            totalProfilingTicks = ticks;
                            chunkNanoTimeMap.clear();
                            feedbackSource = context.getSource();
                            return 1;
                        }))
                .then(literal("top").executes(context -> top(context.getSource())))
                .then(literal("show").then(argument("pos", ColumnPosArgumentType.columnPos())
                        .suggests((context, builder) -> builder.suggest("~ ~").suggest(0).buildFuture())
                        .executes(context -> {
                            ChunkPos chunkPos = ColumnPosArgumentType.getColumnPos(context, "pos").toChunkPos();
                            long chunk = chunkPos.toLong();
                            Map<String, Long> map = chunkNanoTimeMap.get(chunk);
                            if (map == null) {
                                context.getSource().sendError(Text.literal("No data for chunk " + chunk));
                                return 0;
                            }
                            context.getSource().sendError(Text.literal("Chunk " + chunk + " profiling data:"));
                            for (Map.Entry<String, Long> entry : map.entrySet()) {
                                context.getSource().sendMessage(Text.literal(entry.getKey() + ": " + entry.getValue() / totalProfilingTicks / 1e6 + "ms"));
                            }
                            return 1;
                        })))));
    }

    public static void start(String label, long chunk) {
        startNanoTime = System.nanoTime();
    }

    public static void end(String label, long chunk) {
        if (startNanoTime == 0) {
            throw new IllegalStateException("startNanoTime is 0");
        }
        Map<String, Long> map = chunkNanoTimeMap.computeIfAbsent(chunk, pos -> new HashMap<>());
        long nanoTime = System.nanoTime() - startNanoTime;
        startNanoTime = 0;
        map.put(label, map.getOrDefault(label, 0L) + nanoTime);
    }
}
