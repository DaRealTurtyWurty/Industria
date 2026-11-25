package dev.turtywurty.industria.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class CokeOvenSmokeProvider extends CampfireSmokeParticle.CosyProvider {
    public CokeOvenSmokeProvider(SpriteSet sprites) {
        super(sprites);
    }

    @Override
    public CampfireSmokeParticle createParticle(SimpleParticleType type, ClientLevel level,
        double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
        var smoke = (CampfireSmokeParticle) super.createParticle(type, level, x, y, z, vx, vy, vz, random);
        smoke.setColor(0.025F, 0.025F, 0.025F);
        smoke.setLifetime(random.nextIntBetweenInclusive(120, 160));
        return smoke;
    }
}
