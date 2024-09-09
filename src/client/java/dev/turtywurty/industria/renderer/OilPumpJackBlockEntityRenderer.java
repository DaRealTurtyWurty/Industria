package dev.turtywurty.industria.renderer;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.model.OilPumpJackModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class OilPumpJackBlockEntityRenderer implements BlockEntityRenderer<OilPumpJackBlockEntity> {
    private static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/oil_pump_jack.png");

    private final BlockEntityRendererFactory.Context context;
    private final OilPumpJackModel model;

    public OilPumpJackBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;

        this.model = new OilPumpJackModel(context.getLayerModelPart(OilPumpJackModel.LAYER_LOCATION));
    }

    @Override
    public void render(OilPumpJackBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 0, 0.5f);

        Direction facing = entity.getCachedState().get(Properties.HORIZONTAL_FACING);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(facing.asRotation()));
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        }

        float clientRotation = entity.clientRotation + 0.1f * tickDelta;
        if (clientRotation > Math.PI * 2) {
            clientRotation -= (float) (Math.PI * 2);
            entity.reverseCounterWeights = !entity.reverseCounterWeights;
        }

        entity.clientRotation = clientRotation;

        OilPumpJackModel.Parts parts = this.model.getParts();
        float previousWheelPitch = parts.wheel().pitch;
        float previousCounterWeightsPitch = parts.counterWeights().pitch;
        float previousPitmanArmPitch = parts.pitmanArm().pitch;
        float previousArmPitch = parts.arm().pitch;

        parts.wheel().pitch = -clientRotation;

        // This is a bit of a hack to get the counter weights to move with the wheel
        parts.counterWeights().pitch = entity.reverseCounterWeights ?
                map(clientRotation, 0, (float) Math.PI * 2f, -0.5f, (float) Math.PI / 2f) :
                map(clientRotation, 0, (float) Math.PI * 2f, (float) Math.PI / 2f, -0.5f);

        // keep at a constant position on the arm
        parts.pitmanArm().pitch = -parts.counterWeights().pitch + (entity.reverseCounterWeights ?
                map(clientRotation, 0, (float) Math.PI * 2f, 0, (float) -Math.PI / 8f) :
                map(clientRotation, 0, (float) Math.PI * 2f, (float) -Math.PI / 8f, 0));

        ModelPart attachmentAPart = parts.attachmentA();
        ModelPart.Cuboid attachmentACuboid = attachmentAPart.getRandomCuboid(this.context.getTextRenderer().random);
        Vec3d attachmentAPosition = new Vec3d(attachmentACuboid.minX, attachmentACuboid.minY, attachmentACuboid.minZ);

        double a = 11 * Math.sin(parts.counterWeights().pitch);
        double b = 11 * Math.cos(parts.counterWeights().pitch);
        attachmentAPosition = attachmentAPosition.add(0, a, -b);

        double pitch = Math.toDegrees(parts.counterWeights().pitch + parts.pitmanArm().pitch);
        double angle = Math.toRadians(Math.abs(90 - pitch));
        double x = 44 * Math.cos(angle);
        double y = 44 * Math.sin(angle);
        attachmentAPosition = attachmentAPosition.add(0, y, x);

        attachmentAPosition = attachmentAPosition.add(15.5, 13, 0);

        ModelPart attachmentBPart = parts.attachmentB();
        ModelPart.Cuboid attachmentBCuboid = attachmentBPart.getRandomCuboid(this.context.getTextRenderer().random);
        Vec3d attachmentBPosition = new Vec3d(attachmentBCuboid.minX, attachmentBCuboid.minY, attachmentBCuboid.minZ);

        Vec3d difference = attachmentAPosition.subtract(attachmentBPosition);
        double angleX = Math.atan2(difference.y, Math.sqrt(difference.x * difference.x + difference.z * difference.z));
        parts.arm().pitch = (float) -angleX;

        ModelPart attachmentCPart = parts.attachmentC();
        ModelPart.Cuboid attachmentCCuboid = attachmentCPart.getRandomCuboid(this.context.getTextRenderer().random);
        Vector3f attachmentCPosition = new Vector3f(attachmentCCuboid.minX, attachmentCCuboid.minY, attachmentCCuboid.minZ)
                .add(1, 1, 1)
                .div(16f);

        // attachment c should rotate with the arm
        float length = (float) Math.sqrt(difference.x * difference.x + difference.y * difference.y + difference.z * difference.z);
        float height = (float) Math.sqrt(difference.y * difference.y + difference.z * difference.z);

        ModelPart attachmentDPart = parts.attachmentD();
        ModelPart.Cuboid attachmentDCuboid = attachmentDPart.getRandomCuboid(this.context.getTextRenderer().random);
        Vector3f attachmentDPosition = new Vector3f(attachmentDCuboid.minX, attachmentDCuboid.minY, attachmentDCuboid.minZ)
                .add(1, 1, 1)
                .div(16f);

        matrices.push();
        matrices.translate((attachmentBPosition.x - attachmentCPosition.x) / 16f, (attachmentBPosition.y - attachmentCPosition.y) / 16f, (attachmentBPosition.z - attachmentCPosition.z) / 16f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(parts.arm().pitch));
        matrices.translate((attachmentCPosition.x - attachmentBPosition.x) / 16f, (attachmentCPosition.y - attachmentBPosition.y) / 16f, (attachmentCPosition.z - attachmentBPosition.z) / 16f);
        VertexConsumer linesConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        linesConsumer.vertex(matrices.peek(), attachmentCPosition.x, attachmentCPosition.y, attachmentCPosition.z)
                .color(255, 0, 0, 255)
                .normal(0, 0, 0);
        matrices.pop();

        linesConsumer.vertex(matrices.peek(), attachmentDPosition.x, attachmentDPosition.y, attachmentDPosition.z)
                .color(255, 0, 0, 255)
                .normal(0, 0, 0);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(TEXTURE_LOCATION));
        this.model.render(matrices, vertexConsumer, light, overlay);

        parts.wheel().pitch = previousWheelPitch;
        parts.counterWeights().pitch = previousCounterWeightsPitch;
        parts.pitmanArm().pitch = previousPitmanArmPitch;
        parts.arm().pitch = previousArmPitch;
        matrices.pop();
    }

    public static float map(float value, float fromStart, float fromEnd, float toStart, float toEnd) {
        return toStart + (value - fromStart) * (toEnd - toStart) / (fromEnd - fromStart);
    }
}
