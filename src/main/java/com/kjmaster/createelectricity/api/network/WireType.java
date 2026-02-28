package com.kjmaster.createelectricity.api.network;

/**
 * Defines the physical properties of different wire materials.
 */
public enum WireType {

    // Tier 1: Early Game. High resistance, low amperage capacity.
    COPPER(0.5f, 15, 250, 0.2f),

    // Tier 2: Mid-Game. Low resistance, but melts easily under high load.
    GOLD(0.1f, 10, 1000, 0.3f),

    // Tier 3: Late Game. Bulky, handles massive current and voltage.
    HEAVY_COPPER(0.05f, 100, 4000, 0.1f);

    private final float resistancePerMeter;
    private final int maxAmperage;
    private final int maxVoltage;

    // Used by the renderer to determine how far the catenary curve droops
    private final float slack;

    WireType(float resistancePerMeter, int maxAmperage, int maxVoltage, float slack) {
        this.resistancePerMeter = resistancePerMeter;
        this.maxAmperage = maxAmperage;
        this.maxVoltage = maxVoltage;
        this.slack = slack;
    }

    public float getResistancePerMeter() {
        return resistancePerMeter;
    }

    public int getMaxAmperage() {
        return maxAmperage;
    }

    public int getMaxVoltage() {
        return maxVoltage;
    }

    public float getSlack() {
        return slack;
    }
}