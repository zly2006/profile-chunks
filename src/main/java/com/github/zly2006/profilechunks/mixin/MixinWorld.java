package com.github.zly2006.profilechunks.mixin;

import com.github.zly2006.profilechunks.profileChunks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(World.class)
public class MixinWorld {
    @Inject(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void beginTickBlockEntity(CallbackInfo ci, Profiler profiler, Iterator iterator, BlockEntityTickInvoker blockEntityTickInvoker) {
        if (profileChunks.profilingTicks > 0) {
            profileChunks.start("block-entity-tick", ChunkPos.toLong(blockEntityTickInvoker.getPos()));
        }
    }

    @Inject(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void endTickBlockEntity(CallbackInfo ci, Profiler profiler, Iterator iterator, BlockEntityTickInvoker blockEntityTickInvoker) {
        if (profileChunks.profilingTicks > 0) {
            profileChunks.end("block-entity-tick", ChunkPos.toLong(blockEntityTickInvoker.getPos()));
        }
    }
}
