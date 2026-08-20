package dev.turtywurty.industria.model.conveyor;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

@SuppressWarnings("SpellCheckingInspection")
public class ConveyorFlapsModel extends Model<ConveyorFlapsModel.RenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Industria.id("feeder_conveyor_flaps"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.withDefaultNamespace("textures/block/black_concrete.png");

    private final ModelPart flaps;
    private final Flap flap1, flap2, flap3, flap4;

    public ConveyorFlapsModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        this.flaps = root.getChild("flaps");
        this.flap1 = new Flap(this.flaps.getChild("flap1"));
        this.flap2 = new Flap(this.flaps.getChild("flap2"));
        this.flap3 = new Flap(this.flaps.getChild("flap3"));
        this.flap4 = new Flap(this.flaps.getChild("flap4"));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void setupAnim(RenderState state) {
        super.setupAnim(state);
        this.flap1.top().xRot = state.flap1TopRot;
        this.flap1.mid().xRot = state.flap1MidRot;
        this.flap1.bot().xRot = state.flap1BotRot;

        this.flap2.top().xRot = state.flap2TopRot;
        this.flap2.mid().xRot = state.flap2MidRot;
        this.flap2.bot().xRot = state.flap2BotRot;

        this.flap3.top().xRot = state.flap3TopRot;
        this.flap3.mid().xRot = state.flap3MidRot;
        this.flap3.bot().xRot = state.flap3BotRot;

        this.flap4.top().xRot = state.flap4TopRot;
        this.flap4.mid().xRot = state.flap4MidRot;
        this.flap4.bot().xRot = state.flap4BotRot;
    }

    public static LayerDefinition createMainLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition flaps = root.addOrReplaceChild("flaps", CubeListBuilder.create(),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        addFlap(flaps, "flap1", -4.5F);
        addFlap(flaps, "flap2", -1.5F);
        addFlap(flaps, "flap3", 1.5F);
        addFlap(flaps, "flap4", 4.5F);

        return LayerDefinition.create(mesh, 16, 16);
    }

    private static void addFlap(PartDefinition parent, String name, float x) {
        PartDefinition flap = parent.addOrReplaceChild(name, CubeListBuilder.create(),
                PartPose.offset(x, 0.0F, -0.5F));

        // Build the flap as a chain so lower segments inherit the bend from the segments above them.
        PartDefinition segTop = flap.addOrReplaceChild("segTop", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition segMid = segTop.addOrReplaceChild("segMid", CubeListBuilder.create()
                        .texOffs(0, 3).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        segMid.addOrReplaceChild("segBot", CubeListBuilder.create()
                        .texOffs(0, 6).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));
    }

    public Flap flap1() {
        return flap1;
    }

    public Flap flap2() {
        return flap2;
    }

    public Flap flap3() {
        return flap3;
    }

    public Flap flap4() {
        return flap4;
    }

    public record Flap(ModelPart root) {
        public ModelPart top() {
            return root.getChild("segTop");
        }

        public ModelPart mid() {
            return top().getChild("segMid");
        }

        public ModelPart bot() {
            return mid().getChild("segBot");
        }
    }

    public ModelPart flaps() {
        return flaps;
    }

    public static class RenderState {
        public float flap1TopRot, flap1MidRot, flap1BotRot;
        public float flap2TopRot, flap2MidRot, flap2BotRot;
        public float flap3TopRot, flap3MidRot, flap3BotRot;
        public float flap4TopRot, flap4MidRot, flap4BotRot;

        public void applyForIndex(int index, float topRot, float midRot, float botRot) {
            switch (index) {
                case 0 -> {
                    this.flap1TopRot = topRot;
                    this.flap1MidRot = midRot;
                    this.flap1BotRot = botRot;
                }
                case 1 -> {
                    this.flap2TopRot = topRot;
                    this.flap2MidRot = midRot;
                    this.flap2BotRot = botRot;
                }
                case 2 -> {
                    this.flap3TopRot = topRot;
                    this.flap3MidRot = midRot;
                    this.flap3BotRot = botRot;
                }
                case 3 -> {
                    this.flap4TopRot = topRot;
                    this.flap4MidRot = midRot;
                    this.flap4BotRot = botRot;
                }
            }
        }
    }
}
