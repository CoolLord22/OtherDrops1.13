package com.gmail.zariust.otherdrops.parameters.actions;

import com.gmail.zariust.otherdrops.Log;
import org.bukkit.Location;
import org.bukkit.Particle;

/**
 * A particle effect for the "particleeffect" action, using the Bukkit particle API (1.9+).
 * Replaces the old NMS/reflection "ParticleLib" implementation (EnumParticle, PacketPlayOutWorldParticles,
 * playerConnection), which no longer exists on 1.17+ servers.
 * <p>
 * Config format (unchanged): NAME@speed@count@radius, where NAME is a Bukkit Particle name
 * (see known_lists/Particle.txt) and radius is how far the particles spread around the location.
 */
public class ParticleEffect {
    private final Particle type;
    private double speed = 1;
    private int count = 1;
    private double radius = 1;

    public ParticleEffect(Particle type) {
        this.type = type;
    }

    /** Parse a particle name. Returns null (with a warning) for unknown names or particles that need extra data. */
    public static ParticleEffect parse(String name) {
        Particle particle;
        try {
            particle = Particle.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            Log.logWarning("Invalid particle '" + name + "' (see known_lists/Particle.txt); skipping...");
            return null;
        }
        if (particle.getDataType() != Void.class) {
            // DUST, BLOCK, ITEM, etc. need a colour/block/item argument that the config format can't express yet
            Log.logWarning("Particle '" + name + "' needs extra data (colour, block or item) and isn't supported; skipping...");
            return null;
        }
        return new ParticleEffect(particle);
    }

    public Particle getType() {
        return type;
    }

    public double getSpeed() {
        return speed;
    }

    public int getCount() {
        return count;
    }

    public double getRadius() {
        return radius;
    }

    public void setSpeed(double inp) {
        this.speed = inp;
    }

    public void setCount(int inp) {
        this.count = inp;
    }

    public void setRadius(double inp) {
        this.radius = inp;
    }

    /** Spawns the particles at the location. Visible to every player close enough to see them. */
    public void sendToLocation(Location location, double speed, int count, double radius) {
        if (location == null || location.getWorld() == null) return;
        location.getWorld().spawnParticle(type, location, count, radius, radius, radius, speed);
    }

    /** Kept for compatibility with any existing callers; the Bukkit API is always available on 1.17+. */
    public static boolean isCompatible() {
        return true;
    }
}