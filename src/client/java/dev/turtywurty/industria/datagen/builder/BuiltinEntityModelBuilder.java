package dev.turtywurty.industria.datagen.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BuiltinEntityModelBuilder {
    private static final DefaultDisplaySettingsBuilder DEFAULT_ITEM = new DefaultDisplaySettingsBuilder()
            .thirdPersonBothHands(new DisplaySettings.Builder()
                    .translation(0, 3, 1)
                    .scale(0.55f))
            .firstPersonRightHand(new DisplaySettings.Builder()
                    .rotation(0, -90, 25)
                    .translation(1.13f, 3.2f, 1.13f)
                    .scale(0.68f))
            .firstPersonLeftHand(new DisplaySettings.Builder()
                    .rotation(0, 90, -25)
                    .translation(1.13f, 3.2f, 1.13f)
                    .scale(0.68f))
            .ground(new DisplaySettings.Builder()
                    .translation(0, 2, 0)
                    .scale(0.5f))
            .head(new DisplaySettings.Builder()
                    .rotation(0, 180, 0)
                    .translation(0, 13, 7))
            .fixed(new DisplaySettings.Builder()
                    .rotation(0, 180, 0));

    private static final DefaultDisplaySettingsBuilder DEFAULT_BLOCK = new DefaultDisplaySettingsBuilder()
            .thirdPersonBothHands(new DisplaySettings.Builder()
                    .rotation(75, 45, 0)
                    .translation(0, 2.5f, 0)
                    .scale(0.375f))
            .firstPersonRightHand(new DisplaySettings.Builder()
                    .rotation(0, 45, 0)
                    .translation(0, 0, 0)
                    .scale(0.4f))
            .firstPersonLeftHand(new DisplaySettings.Builder()
                    .rotation(0, 225, 0)
                    .translation(0, 0, 0)
                    .scale(0.4f))
            .ground(new DisplaySettings.Builder()
                    .rotation(0, 0, 0)
                    .translation(0, 3, 0)
                    .scale(0.25f))
            .fixed(new DisplaySettings.Builder()
                    .rotation(0, 0, 0)
                    .translation(0, 0, 0)
                    .scale(0.5f))
            .gui(new DisplaySettings.Builder()
                    .rotation(30, 225, 0)
                    .translation(0, 0, 0)
                    .scale(0.625f));

    private static final DefaultDisplaySettingsBuilder DEFAULT_WEAPON = new DefaultDisplaySettingsBuilder()
            .thirdPersonRightHand(new DisplaySettings.Builder()
                    .rotation(0, -90, 55)
                    .translation(0, 4, 0.5f)
                    .scale(0.85f))
            .thirdPersonLeftHand(new DisplaySettings.Builder()
                    .rotation(0, 90, -55)
                    .translation(0, 4, 0.5f)
                    .scale(0.85f))
            .firstPersonRightHand(new DisplaySettings.Builder()
                    .rotation(0, -90, 25)
                    .translation(1.13f, 3.2f, 1.13f)
                    .scale(0.68f))
            .firstPersonLeftHand(new DisplaySettings.Builder()
                    .rotation(0, 90, -25)
                    .translation(1.13f, 3.2f, 1.13f)
                    .scale(0.68f));

    private static final DefaultDisplaySettingsBuilder DEFAULT_ROD = new DefaultDisplaySettingsBuilder()
            .thirdPersonRightHand(new DisplaySettings.Builder()
                    .rotation(0, 90, 55)
                    .translation(0, 4, 2.5f)
                    .scale(0.85f))
            .thirdPersonLeftHand(new DisplaySettings.Builder()
                    .rotation(0, -90, -55)
                    .translation(0, 4, 2.5f)
                    .scale(0.85f))
            .firstPersonRightHand(new DisplaySettings.Builder()
                    .rotation(0, 90, 25)
                    .translation(0, 1.6f, 0.8f)
                    .scale(0.68f))
            .firstPersonLeftHand(new DisplaySettings.Builder()
                    .rotation(0, -90, -25)
                    .translation(0, 1.6f, 0.8f)
                    .scale(0.68f));

    private final Item item;
    private final Identifier id;
    private final ItemModelGenerators writer;
    private final List<DisplaySettings> displaySettings = new ArrayList<>();

    private BuiltinEntityModelBuilder(Item item, Identifier id, ItemModelGenerators writer) {
        this.item = item;
        this.id = id;
        this.writer = writer;
    }

    public static void write(ItemModelGenerators itemModelGenerator, ItemLike item) {
        write(itemModelGenerator, item, null);
    }

    private void write() {
        this.writer.modelOutput.accept(this.id, () -> {
            var object = new JsonObject();
            object.addProperty("parent", "minecraft:builtin/entity");

            var display = new JsonObject();
            for (DisplaySettings displaySetting : this.displaySettings) {
                var displayObject = new JsonObject();

                if(displaySetting.rotation.x != 0 || displaySetting.rotation.y != 0 || displaySetting.rotation.z != 0) {
                    var rotation = new JsonArray();
                    rotation.add(displaySetting.rotation.x);
                    rotation.add(displaySetting.rotation.y);
                    rotation.add(displaySetting.rotation.z);
                    displayObject.add("rotation", rotation);
                }

                if(displaySetting.translation.x != 0 || displaySetting.translation.y != 0 || displaySetting.translation.z != 0) {
                    var translation = new JsonArray();
                    translation.add(displaySetting.translation.x);
                    translation.add(displaySetting.translation.y);
                    translation.add(displaySetting.translation.z);
                    displayObject.add("translation", translation);
                }

                if(displaySetting.scale.x != 1 || displaySetting.scale.y != 1 || displaySetting.scale.z != 1) {
                    var scale = new JsonArray();
                    scale.add(displaySetting.scale.x);
                    scale.add(displaySetting.scale.y);
                    scale.add(displaySetting.scale.z);
                    displayObject.add("scale", scale);
                }

                display.add(displaySetting.name, displayObject);
            }

            object.add("display", display);
            return object;
        });

//        if(item instanceof BlockItem) {
//            this.writer.output.accept(this.item, ItemModels.special(ModelIds.getItemModelId(this.item), DynamicItemRendererInit.getItemRenderer(this.item)));
//        }
    }

    public static void write(ItemModelGenerators writer, Item item, Identifier id, DefaultDisplaySettingsBuilder defaultBuilder) {
        new BuiltinEntityModelBuilder.Builder(writer, item, id).defaultDisplaySettings(defaultBuilder).build().write();
    }

    public static void write(ItemModelGenerators writer, Item item, DefaultDisplaySettingsBuilder defaultBuilder) {
        write(writer, item, ModelLocationUtils.getModelLocation(item), defaultBuilder);
    }

    public static void write(ItemModelGenerators writer, ItemLike item, DefaultDisplaySettingsBuilder defaultBuilder) {
        write(writer, item.asItem(), defaultBuilder);
    }

    public static DefaultDisplaySettingsBuilder defaultItem() {
        return DEFAULT_ITEM;
    }

    public static DefaultDisplaySettingsBuilder defaultBlock() {
        return DEFAULT_BLOCK;
    }

    public static DefaultDisplaySettingsBuilder defaultWeapon() {
        return DEFAULT_WEAPON;
    }

    public static DefaultDisplaySettingsBuilder defaultRod() {
        return DEFAULT_ROD;
    }

    public static class DefaultDisplaySettingsBuilder {
        private final Map<String, DisplaySettings> displaySettings = new HashMap<>();

        public DefaultDisplaySettingsBuilder addDisplaySettings(String name, DisplaySettings.Builder displaySettings) {
            this.displaySettings.put(name, displaySettings.build(name));
            return this;
        }

        public DefaultDisplaySettingsBuilder thirdPersonRightHand(DisplaySettings.Builder builder) {
            return addDisplaySettings("thirdperson_righthand", builder);
        }

        public DefaultDisplaySettingsBuilder thirdPersonLeftHand(DisplaySettings.Builder builder) {
            return addDisplaySettings("thirdperson_lefthand", builder);
        }

        public DefaultDisplaySettingsBuilder thirdPersonBothHands(DisplaySettings.Builder builder) {
            return thirdPersonRightHand(builder).thirdPersonLeftHand(builder);
        }

        public DefaultDisplaySettingsBuilder firstPersonRightHand(DisplaySettings.Builder builder) {
            return addDisplaySettings("firstperson_righthand", builder);
        }

        public DefaultDisplaySettingsBuilder firstPersonLeftHand(DisplaySettings.Builder builder) {
            return addDisplaySettings("firstperson_lefthand", builder);
        }

        public DefaultDisplaySettingsBuilder firstPersonBothHands(DisplaySettings.Builder builder) {
            return firstPersonRightHand(builder).firstPersonLeftHand(builder);
        }

        public DefaultDisplaySettingsBuilder ground(DisplaySettings.Builder builder) {
            return addDisplaySettings("ground", builder);
        }

        public DefaultDisplaySettingsBuilder gui(DisplaySettings.Builder builder) {
            return addDisplaySettings("gui", builder);
        }

        public DefaultDisplaySettingsBuilder head(DisplaySettings.Builder builder) {
            return addDisplaySettings("head", builder);
        }

        public DefaultDisplaySettingsBuilder fixed(DisplaySettings.Builder builder) {
            return addDisplaySettings("fixed", builder);
        }

        public DefaultDisplaySettingsBuilder copy() {
            var builder = new DefaultDisplaySettingsBuilder();
            for (Map.Entry<String, DisplaySettings> displaySettingsEntry : this.displaySettings.entrySet()) {
                builder.displaySettings.put(displaySettingsEntry.getKey(), displaySettingsEntry.getValue().copy());
            }

            return builder;
        }

        public DefaultDisplaySettingsBuilder copyModify(String name, Consumer<DisplaySettings> modifier) {
            DefaultDisplaySettingsBuilder copy = copy();
            DisplaySettings settings = copy.displaySettings.get(name);
            if (settings == null) {
                settings = new DisplaySettings(name);
            }

            modifier.accept(settings);
            copy.displaySettings.put(name, settings);
            return copy;
        }

        public DefaultDisplaySettingsBuilder copyModifyThirdPersonRightHand(Consumer<DisplaySettings> modifier) {
            return copyModify("thirdperson_righthand", modifier);
        }

        public DefaultDisplaySettingsBuilder copyModifyThirdPersonLeftHand(Consumer<DisplaySettings> modifier) {
            return copyModify("thirdperson_lefthand", modifier);
        }

        public DefaultDisplaySettingsBuilder copyModifyThirdPersonBothHands(Consumer<DisplaySettings> modifier) {
            return copyModifyThirdPersonRightHand(modifier).copyModifyThirdPersonLeftHand(modifier);
        }

        public DefaultDisplaySettingsBuilder copyModifyFirstPersonRightHand(Consumer<DisplaySettings> modifier) {
            return copyModify("firstperson_righthand", modifier);
        }

        public DefaultDisplaySettingsBuilder copyModifyFirstPersonLeftHand(Consumer<DisplaySettings> modifier) {
            return copyModify("firstperson_lefthand", modifier);
        }

        public DefaultDisplaySettingsBuilder copyModifyFirstPersonBothHands(Consumer<DisplaySettings> modifier) {
            return copyModifyFirstPersonRightHand(modifier).copyModifyFirstPersonLeftHand(modifier);
        }

        public DefaultDisplaySettingsBuilder copyModifyGround(Consumer<DisplaySettings> modifier) {
            return copyModify("ground", modifier);
        }

        public DefaultDisplaySettingsBuilder copyModifyGui(Consumer<DisplaySettings> modifier) {
            return copyModify("gui", modifier);
        }

        public DefaultDisplaySettingsBuilder copyModifyHead(Consumer<DisplaySettings> modifier) {
            return copyModify("head", modifier);
        }

        public DefaultDisplaySettingsBuilder copyModifyFixed(Consumer<DisplaySettings> modifier) {
            return copyModify("fixed", modifier);
        }

        public DefaultDisplaySettingsBuilder copyModifyAll(Consumer<DisplaySettings> modifier) {
            return copyModifyThirdPersonBothHands(modifier)
                    .copyModifyFirstPersonBothHands(modifier)
                    .copyModifyGround(modifier)
                    .copyModifyGui(modifier)
                    .copyModifyHead(modifier)
                    .copyModifyFixed(modifier);
        }

        public Map<String, DisplaySettings> build() {
            return this.displaySettings;
        }
    }

    public static class Builder {
        private final ItemModelGenerators writer;
        private final Item item;
        private final Identifier id;

        private final Map<String, DisplaySettings.Builder> displaySettings = new HashMap<>();
        private DefaultDisplaySettingsBuilder defaultDisplaySettingsBuilder;

        public Builder(ItemModelGenerators writer, Item item, Identifier id) {
            this.writer = writer;
            this.item = item;
            this.id = id;
        }

        public Builder addDisplaySettings(String name, DisplaySettings.Builder displaySettings) {
            this.displaySettings.put(name, displaySettings);
            return this;
        }

        public Builder defaultDisplaySettings(DefaultDisplaySettingsBuilder defaultBuilder) {
            this.defaultDisplaySettingsBuilder = defaultBuilder;
            return this;
        }

        public BuiltinEntityModelBuilder build() {
            var builder = new BuiltinEntityModelBuilder(this.item, this.id, this.writer);
            if (this.defaultDisplaySettingsBuilder != null) {
                for (Map.Entry<String, DisplaySettings> displaySettingsEntry : this.defaultDisplaySettingsBuilder.build().entrySet()) {
                    builder.displaySettings.add(displaySettingsEntry.getValue());
                }
            }

            for (Map.Entry<String, DisplaySettings.Builder> builderEntry : this.displaySettings.entrySet()) {
                builder.displaySettings.add(builderEntry.getValue().build(builderEntry.getKey()));
            }
            return builder;
        }
    }

    public static class DisplaySettings {
        private final String name;
        private final Vector3f rotation;
        private final Vector3f translation;
        private final Vector3f scale;

        public DisplaySettings(String name, Vector3f rotation, Vector3f translation, Vector3f scale) {
            this.name = name;
            this.rotation = rotation;
            this.translation = translation;
            this.scale = scale;
        }

        public DisplaySettings(String name) {
            this(name, new Vector3f(), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
        }

        public void setRotation(float x, float y, float z) {
            this.rotation.set(x, y, z);
        }

        public void setTranslation(float x, float y, float z) {
            this.translation.set(x, y, z);
        }

        public void setScale(float x, float y, float z) {
            this.scale.set(x, y, z);
        }

        public void translate(float x, float y, float z) {
            this.translation.add(x, y, z);
        }

        public void rotate(float x, float y, float z) {
            this.rotation.add(x, y, z);
        }

        public void scale(float x, float y, float z) {
            this.scale.mul(x, y, z);
        }

        public DisplaySettings copy() {
            return new DisplaySettings(this.name, new Vector3f(this.rotation), new Vector3f(this.translation), new Vector3f(this.scale));
        }

        public static class Builder {
            private final Vector3f rotation = new Vector3f();
            private final Vector3f translation = new Vector3f();
            private final Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);

            public Builder rotation(float x, float y, float z) {
                this.rotation.set(x, y, z);
                return this;
            }

            public Builder translation(float x, float y, float z) {
                this.translation.set(x, y, z);
                return this;
            }

            public Builder scale(float x, float y, float z) {
                this.scale.set(x, y, z);
                return this;
            }

            public Builder scale(float scale) {
                this.scale.set(scale, scale, scale);
                return this;
            }

            public DisplaySettings build(String name) {
                return new DisplaySettings(name, this.rotation, this.translation, this.scale);
            }
        }
    }
}
