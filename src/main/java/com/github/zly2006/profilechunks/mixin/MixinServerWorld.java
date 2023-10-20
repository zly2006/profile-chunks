package com.github.zly2006.profilechunks.mixin;

import com.github.zly2006.profilechunks.ProfileChunks;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class MixinServerWorld {
    @Inject(
            method = "tickBlock",
            at = @At("HEAD")
    )
    private void beginTickBlock(BlockPos pos, Block block, CallbackInfo ci) {
        if (ProfileChunks.profilingTicks > 0) {
            ProfileChunks.start("block-scheduled-tick", ChunkPos.toLong(pos));
        }
    }

    @Inject(
            method = "tickBlock",
            at = @At("RETURN")
    )
    private void endTickBlock(BlockPos pos, Block block, CallbackInfo ci) {
        if (ProfileChunks.profilingTicks > 0) {
            ProfileChunks.end("block-scheduled-tick", ChunkPos.toLong(pos));
        }
    }

    @Inject(
            method = "tickFluid",
            at = @At("HEAD")
    )
    private void beginTickFluid(BlockPos pos, Fluid fluid, CallbackInfo ci) {
        if (ProfileChunks.profilingTicks > 0) {
            ProfileChunks.start("fluid-scheduled-tick", ChunkPos.toLong(pos));
        }
    }

    @Inject(
            method = "tickFluid",
            at = @At("RETURN")
    )
    private void endTickFluid(BlockPos pos, Fluid fluid, CallbackInfo ci) {
        if (ProfileChunks.profilingTicks > 0) {
            ProfileChunks.end("fluid-scheduled-tick", ChunkPos.toLong(pos));
        }
    }

    @Inject(
            method = "tickChunk",
            at = @At("HEAD")
    )
    private void beginTickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        if (ProfileChunks.profilingTicks > 0) {
            ProfileChunks.start("random-tick", chunk.getPos().toLong());
        }
    }

    @Inject(
            method = "tickChunk",
            at = @At("RETURN")
    )
    private void endTickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        if (ProfileChunks.profilingTicks > 0) {
            ProfileChunks.end("random-tick", chunk.getPos().toLong());
        }
    }

    @Inject(
            method = "tickEntity",
            at = @At("HEAD")
    )
    private void beginTickEntity(Entity entity, CallbackInfo ci) {
        if (ProfileChunks.profilingTicks > 0) {
            ProfileChunks.start("entity-tick", entity.getChunkPos().toLong());
        }
    }

    @Inject(
            method = "tickEntity",
            at = @At("RETURN")
    )
    private void endTickEntity(Entity entity, CallbackInfo ci) {
        if (ProfileChunks.profilingTicks > 0) {
            ProfileChunks.end("entity-tick", entity.getChunkPos().toLong());
        }
    }

    @Inject(
            method = "processBlockEvent",
            at = @At("HEAD")
    )
    private void beginProcessBlockEvent(BlockEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (ProfileChunks.profilingTicks > 0) {
            ProfileChunks.start("block-event", ChunkPos.toLong(event.pos()));
        }
    }

    @Inject(
            method = "processBlockEvent",
            at = @At("RETURN")
    )
    private void endProcessBlockEvent(BlockEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (ProfileChunks.profilingTicks > 0) {
            ProfileChunks.end("block-event", ChunkPos.toLong(event.pos()));
        }
    }
}
