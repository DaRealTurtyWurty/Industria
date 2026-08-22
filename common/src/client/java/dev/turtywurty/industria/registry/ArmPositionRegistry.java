package dev.turtywurty.industria.registry;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ArmPositionRegistry {
    private static final List<Entry> ARM_POSITIONS = new ArrayList<>();

    public static void register(Predicate<ItemStack> predicate, DynamicArmPosition armPosition) {
        ARM_POSITIONS.add(new Entry(predicate, armPosition));
    }

    public static List<DynamicArmPosition> getArmPosition(ItemStack stack) {
        List<DynamicArmPosition> armPositions = new ArrayList<>();
        for (Entry entry : ARM_POSITIONS) {
            if (entry.predicate.test(stack)) {
                armPositions.add(entry.armPosition);
            }
        }

        return armPositions;
    }

    public record Entry(Predicate<ItemStack> predicate, DynamicArmPosition armPosition) {}

    @FunctionalInterface
    public interface DynamicArmPosition {
        void apply(HumanoidRenderState bipedEntityRenderState, ModelPart leftArm, ModelPart rightArm);
    }
}
