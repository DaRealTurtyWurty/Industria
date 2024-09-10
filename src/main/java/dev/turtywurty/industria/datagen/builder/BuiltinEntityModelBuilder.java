package dev.turtywurty.industria.datagen.builder;

import com.google.gson.JsonObject;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.ModelIds;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Identifier id;
    private final ItemModelGenerator writer;
    private final List<DisplaySettings> displaySettings = new ArrayList<>();

    private BuiltinEntityModelBuilder(Identifier id, ItemModelGenerator writer) {
        this.id = id;
        this.writer = writer;
    }

    private void write() {
        this.writer.writer.accept(this.id, () -> {
            var object = new JsonObject();
            object.addProperty("parent", "minecraft:builtin/entity");

            var display = new JsonObject();
            for (DisplaySettings displaySetting : this.displaySettings) {
                var displayObject = new JsonObject();
                var rotation = new JsonObject();
                rotation.addProperty("x", displaySetting.rotation.x);
                rotation.addProperty("y", displaySetting.rotation.y);
                rotation.addProperty("z", displaySetting.rotation.z);
                displayObject.add("rotation", rotation);
                var translation = new JsonObject();
                translation.addProperty("x", displaySetting.translation.x);
                translation.addProperty("y", displaySetting.translation.y);
                translation.addProperty("z", displaySetting.translation.z);
                displayObject.add("translation", translation);
                var scale = new JsonObject();
                scale.addProperty("x", displaySetting.scale.x);
                scale.addProperty("y", displaySetting.scale.y);
                scale.addProperty("z", displaySetting.scale.z);
                displayObject.add("scale", scale);
                display.add(displaySetting.name, displayObject);
            }

            object.add("display", display);
            return object;
        });
    }

    public static void write(ItemModelGenerator writer, Identifier id, DefaultDisplaySettingsBuilder defaultBuilder) {
        new BuiltinEntityModelBuilder.Builder(writer, id).defaultDisplaySettings(defaultBuilder).build().write();
    }

    public static void write(ItemModelGenerator writer, Item item, DefaultDisplaySettingsBuilder defaultBuilder) {
        write(writer, ModelIds.getItemModelId(item), defaultBuilder);
    }

    public static void write(ItemModelGenerator writer, ItemConvertible item, DefaultDisplaySettingsBuilder defaultBuilder) {
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

        public Map<String, DisplaySettings> build() {
            return this.displaySettings;
        }
    }

    public static class Builder {
        private final ItemModelGenerator writer;
        private final Identifier id;

        private final Map<String, DisplaySettings.Builder> displaySettings = new HashMap<>();
        private DefaultDisplaySettingsBuilder defaultDisplaySettingsBuilder;

        public Builder(ItemModelGenerator writer, Identifier id) {
            this.writer = writer;
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
            var builder = new BuiltinEntityModelBuilder(this.id, this.writer);
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
