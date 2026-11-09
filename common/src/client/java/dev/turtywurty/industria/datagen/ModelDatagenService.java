package dev.turtywurty.industria.datagen;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

import java.util.ServiceLoader;

public interface ModelDatagenService {
    static ModelDatagenService get() {
        return ServiceHolder.INSTANCE;
    }

    void addModelLoader(JsonObject json, Identifier loaderId);

    void configureBlockStateModel(JsonObject json, Identifier typeId);

    final class ServiceHolder {
        private static final ModelDatagenService INSTANCE = ServiceLoader.load(
                ModelDatagenService.class,
                ModelDatagenService.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No model datagen service is available for this platform"));

        private ServiceHolder() {
        }
    }
}
