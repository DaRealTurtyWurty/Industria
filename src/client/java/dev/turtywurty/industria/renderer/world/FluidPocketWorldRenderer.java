package dev.turtywurty.industria.renderer.world;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidPocketWorldRenderer implements IndustriaWorldRenderer {
    public static final Map<ResourceKey<Level>, List<WorldFluidPocketsState.FluidPocket>> FLUID_POCKETS = new HashMap<>();

    @Override
    public void render(LevelRenderContext context) {
        if(!DebugRenderingRegistry.debugRendering)
            return;

        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        if (!FLUID_POCKETS.containsKey(player.level().dimension()))
            return;

        List<WorldFluidPocketsState.FluidPocket> nearbyFluidPockets = FLUID_POCKETS.get(player.level().dimension())
                .stream()
                .filter(fluidPocket -> fluidPocket.isWithinDistance(player.blockPosition(), 64))
                .toList();

        PoseStack matrixStack = context.poseStack();
        if (matrixStack == null)
            return;

        MultiBufferSource provider = context.bufferSource();
        if (provider == null)
            return;

        Level world = player.level();
        if (world == null)
            return;

        for (WorldFluidPocketsState.FluidPocket pocket : nearbyFluidPockets) {
            // TODO: Draw different colored particles based on fluid

            for (BlockPos pos : pocket.fluidPositions().keySet()) {
                ParticleUtils.spawnParticleInBlock(world, pos, 1, ParticleTypes.DRIPPING_WATER);
            }
        }
    }
}
