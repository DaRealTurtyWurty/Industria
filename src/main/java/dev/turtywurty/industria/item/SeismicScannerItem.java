package dev.turtywurty.industria.item;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.component.FluidPocketsComponent;
import dev.turtywurty.industria.init.ComponentTypeInit;
import dev.turtywurty.industria.network.OpenSeismicScannerPayload;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeismicScannerItem extends Item {
    public static final Text TITLE = Text.translatable("screen." + Industria.MOD_ID + ".seismic_scanner");

    public SeismicScannerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient)
            return super.use(world, user, hand);

        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            WorldFluidPocketsState state = WorldFluidPocketsState.getServerState((ServerWorld) world);
            List<WorldFluidPocketsState.FluidPocket> existsBelow = state.existsBelow(user.getBlockPos());
            if (existsBelow.isEmpty()) {
                user.sendMessage(Text.literal("There are no fluid fluidPockets here!"), false); // TODO: Translatable
                return ActionResult.FAIL;
            }

            stack.set(ComponentTypeInit.FLUID_POCKETS, new FluidPocketsComponent(existsBelow));
            return ActionResult.SUCCESS;
        }

        ServerPlayNetworking.send((ServerPlayerEntity) user, new OpenSeismicScannerPayload(stack));
        return ActionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof PlayerEntity player))
            return;

        boolean selected = player.getEquippedStack(EquipmentSlot.MAINHAND).isOf(this) ||
                player.getEquippedStack(EquipmentSlot.OFFHAND).isOf(this);
        if (selected) {
            WorldFluidPocketsState state = WorldFluidPocketsState.getServerState(world);
            List<Text> fluidBelow = new ArrayList<>();
            for (WorldFluidPocketsState.FluidPocket fluidPocket : state.existsBelow(player.getBlockPos())) {
                Optional<RegistryKey<Fluid>> regKey = fluidPocket.fluidState().getRegistryEntry().getKey();
                regKey.ifPresent(key -> fluidBelow.add(Text.translatable(key.getValue().toTranslationKey()).append(": " + fluidPocket.minY())));
            }

            if (fluidBelow.isEmpty())
                return;

            MutableText text = Text.literal("Fluids below: "); // TODO: Translatable
            for (int index = 0; index < fluidBelow.size(); index++) {
                text.append(fluidBelow.get(index));
                if (index != fluidBelow.size() - 1) {
                    text.append(Text.literal(", "));
                }
            }

            player.sendMessage(text, false);
        }
    }
}
