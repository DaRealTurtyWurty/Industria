package dev.turtywurty.industria.renderer.world;

import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidPocketWorldRenderer implements IndustriaWorldRenderer {
    public static final Map<RegistryKey<World>, List<WorldFluidPocketsState.FluidPocket>> FLUID_POCKETS = new HashMap<>();

    @Override
    public void render(WorldRenderContext context) {
        if(!DebugRenderingRegistry.debugRendering)
            return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null)
            return;

        if (!FLUID_POCKETS.containsKey(player.getEntityWorld().getRegistryKey()))
            return;

        List<WorldFluidPocketsState.FluidPocket> nearbyFluidPockets = FLUID_POCKETS.get(player.getEntityWorld().getRegistryKey())
                .stream()
                .filter(fluidPocket -> fluidPocket.isWithinDistance(player.getBlockPos(), 64))
                .toList();

        MatrixStack matrixStack = context.matrixStack();
        if (matrixStack == null)
            return;

        VertexConsumerProvider provider = context.consumers();
        if (provider == null)
            return;

        World world = player.getWorld();
        if (world == null)
            return;

        for (WorldFluidPocketsState.FluidPocket pocket : nearbyFluidPockets) {
            // TODO: Draw different colored particles based on fluid

            for (BlockPos pos : pocket.fluidPositions().keySet()) {
                ParticleUtil.spawnParticlesAround(world, pos, 1, ParticleTypes.DRIPPING_WATER);
            }
        }
    }
}
