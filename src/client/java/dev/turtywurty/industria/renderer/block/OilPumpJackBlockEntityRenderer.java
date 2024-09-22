package dev.turtywurty.industria.renderer.block;

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
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

public class OilPumpJackBlockEntityRenderer implements BlockEntityRenderer<OilPumpJackBlockEntity> {
    private static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/oil_pump_jack.png");

    private final BlockEntityRendererFactory.Context context;
    private final OilPumpJackModel model;
    private final OilPumpJackModel.Parts parts;

    public OilPumpJackBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;

        this.model = new OilPumpJackModel(context.getLayerModelPart(OilPumpJackModel.LAYER_LOCATION));
        this.parts = this.model.getParts();
    }

    @Override
    public void render(OilPumpJackBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + switch (entity.getCachedState().get(Properties.HORIZONTAL_FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));

        float clientRotation = entity.clientRotation + 0.1f * tickDelta;
        if (clientRotation > Math.PI * 2) {
            clientRotation -= (float) (Math.PI * 2);
            entity.reverseCounterWeights = !entity.reverseCounterWeights;
        }

        entity.clientRotation = clientRotation;

        // Save previous values
        float previousWheelPitch = parts.wheel().pitch;
        float previousCounterWeightsPitch = parts.counterWeights().pitch;
        float previousPitmanArmPitch = parts.pitmanArm().pitch;
        float previousArmPitch = parts.arm().pitch;

        Vector3f attachmentAPosition = getAttachmentPosition(parts.attachmentA());
        Vector3f attachmentBPosition = getAttachmentPosition(parts.attachmentB());

        Vector3f attachmentCPosition = getAttachmentPosition(parts.attachmentC())
                .add(1, 1, 1)
                .div(16f);

        Vector3f attachmentDPosition = getAttachmentPosition(parts.attachmentD())
                .add(1, 1, 1)
                .div(16f);

        Vector3f attachmentEPosition = getAttachmentPosition(parts.attachmentE());

        parts.wheel().pitch = -clientRotation;
        parts.counterWeights().pitch = parts.wheel().pitch;
        parts.pitmanArm().pitch = -parts.counterWeights().pitch - (entity.reverseCounterWeights ?
                map(clientRotation, 0, (float) Math.PI * 2f, 0, (float) -Math.PI / 8f) :
                map(clientRotation, 0, (float) Math.PI * 2f, (float) -Math.PI / 8f, 0));

        // Calculate arm pitch
        calculateArmPitch(attachmentAPosition, attachmentBPosition);

        // Draw bridle
        drawBridle(matrices, vertexConsumers, attachmentBPosition, attachmentCPosition, attachmentDPosition);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(TEXTURE_LOCATION));
        this.model.render(matrices, vertexConsumer, light, overlay);
        matrices.pop();

        // Reset values
        parts.wheel().pitch = previousWheelPitch;
        parts.counterWeights().pitch = previousCounterWeightsPitch;
        parts.pitmanArm().pitch = previousPitmanArmPitch;
        parts.arm().pitch = previousArmPitch;
    }

    private void drawBridle(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vector3f attachmentBPosition, Vector3f attachmentCPosition, Vector3f attachmentDPosition) {
        matrices.push();
        matrices.translate(0, 1.5f, 0);

        matrices.push();
        matrices.translate((attachmentBPosition.x - attachmentCPosition.x) / 16f, (attachmentBPosition.y - attachmentCPosition.y) / 16f, (attachmentBPosition.z - attachmentCPosition.z) / 16f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(parts.arm().pitch));
        matrices.translate((attachmentCPosition.x - attachmentBPosition.x) / 16f, (attachmentCPosition.y - attachmentBPosition.y) / 16f, (attachmentCPosition.z - attachmentBPosition.z) / 16f);
        VertexConsumer linesConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        linesConsumer.vertex(matrices.peek(), attachmentCPosition.x, attachmentCPosition.y, attachmentCPosition.z)
                .color(20, 20, 20, 255)
                .normal(0, 0, 0);
        matrices.pop();

        linesConsumer.vertex(matrices.peek(), attachmentDPosition.x, attachmentDPosition.y, attachmentDPosition.z)
                .color(20, 20, 20, 255)
                .normal(0, 0, 0);

        matrices.pop();
    }

    private void calculateArmPitch(Vector3f attachmentAPosition, Vector3f attachmentBPosition) {
        float a = (float) (11 * Math.sin(parts.counterWeights().pitch));
        float b = (float) (11 * Math.cos(parts.counterWeights().pitch));
        attachmentAPosition = attachmentAPosition.add(0, a, -b);

        double pitch = Math.toDegrees(parts.counterWeights().pitch + parts.pitmanArm().pitch);
        double angle = Math.toRadians(Math.abs(90 - pitch));
        float c = (float) (44 * Math.cos(angle));
        float d = (float) (44 * Math.sin(angle));
        attachmentAPosition = attachmentAPosition.sub(0, d, c);

        attachmentAPosition = attachmentAPosition.sub(15.5f, 13, 0);

        Vector3f difference = attachmentAPosition.sub(attachmentBPosition);
        float angleX = (float) Math.atan2(difference.y, Math.sqrt(difference.x * difference.x + difference.z * difference.z));
        parts.arm().pitch = -angleX;
    }

    private static Vector3f getAttachmentPosition(ModelPart part) {
        ModelPart.Cuboid cuboid = part.cuboids.getFirst();
        return new Vector3f(cuboid.minX, cuboid.minY, cuboid.minZ);
    }

    public static float map(float value, float fromStart, float fromEnd, float toStart, float toEnd) {
        return toStart + (value - fromStart) * (toEnd - toStart) / (fromEnd - fromStart);
    }
}
