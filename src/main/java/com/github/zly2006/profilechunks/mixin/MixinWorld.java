package com.github.zly2006.profilechunks.mixin;

import com.github.zly2006.profilechunks.ProfileChunks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class MixinWorld {
    @Shadow @Final public boolean isClient;

    @Redirect(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"
            )
    )
    private void tickBlockEntity(BlockEntityTickInvoker instance) {
        if (!isClient && ProfileChunks.profilingTicks > 0) {
            ProfileChunks.start("block-entity-tick", ChunkPos.toLong(instance.getPos()));
        }
        instance.tick();
        if (!isClient && ProfileChunks.profilingTicks > 0) {
            ProfileChunks.end("block-entity-tick", ChunkPos.toLong(instance.getPos()));
        }
    }
}
