package com.kjmaster.createelectricity.api.capability;

/**
 * Standard implementation of {@link IElectrodynamicPower}.
 * <p>
 * This class serves as the backing logic for electrical Block Entities. It handles
 * the mathematical conversion between standard Forge Energy and Electrodynamics.
 */
public class ElectrodynamicStorage implements IElectrodynamicPower {

    public static final int UNIVERSAL_FOREIGN_VOLTAGE = 240;

    protected int energy;
    protected int capacity;
    protected int maxInsert;
    protected int maxExtract;

    protected int nominalVoltage;
    protected int maxAmperage;
    protected int currentAmperage;

    public ElectrodynamicStorage(int capacity, int nominalVoltage, int maxAmperage) {
        this.capacity = capacity;
        this.nominalVoltage = nominalVoltage;
        this.maxAmperage = maxAmperage;
        // Default standard FE limits derived from physical limits
        this.maxInsert = nominalVoltage * maxAmperage;
        this.maxExtract = nominalVoltage * maxAmperage;
    }

    // --- Electrodynamic Specific Logic ---

    @Override
    public int getNominalVoltage() {
        return this.nominalVoltage;
    }

    @Override
    public int getCurrentAmperage() {
        return this.currentAmperage;
    }

    @Override
    public int getMaxAmperage() {
        return this.maxAmperage;
    }

    public void resetCurrentAmperage() {
        this.currentAmperage = 0;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    @Override
    public int receivePower(int voltage, int maxAmperage, boolean simulate) {
        if (!canReceive()) return 0;

        // Cannot push power into a machine if the pressure (Voltage) is too low
        if (voltage < this.nominalVoltage) return 0;

        int energyToReceive = voltage * maxAmperage;
        int energyAccepted = Math.min(this.capacity - this.energy, Math.min(this.maxInsert, energyToReceive));

        // Convert the accepted FE back into Amps (rounded down)
        int amperageAccepted = energyAccepted / voltage;

        if (!simulate) {
            this.energy += (amperageAccepted * voltage);
            this.currentAmperage = amperageAccepted;
        }

        return amperageAccepted;
    }

    @Override
    public int extractPower(int voltage, int maxAmperage, boolean simulate) {
        if (!canExtract()) return 0;

        int energyRequested = voltage * maxAmperage;
        int energyExtracted = Math.min(this.energy, Math.min(this.maxExtract, energyRequested));

        int amperageExtracted = energyExtracted / voltage;

        if (!simulate) {
            this.energy -= (amperageExtracted * voltage);
            // Extraction doesn't necessarily dictate 'internal' load amperage, but can be tracked
            this.currentAmperage = amperageExtracted;
        }

        return amperageExtracted;
    }

    // Standard IEnergyStorage Overrides - Translation Layer

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        // Translate FE into a physical Ampere draw using the universal voltage
        int maxForeignAmps = maxReceive / UNIVERSAL_FOREIGN_VOLTAGE;

        int ampsAccepted = this.receivePower(UNIVERSAL_FOREIGN_VOLTAGE, maxForeignAmps, simulate);

        // Translate back to standard FE for the response
        return ampsAccepted * UNIVERSAL_FOREIGN_VOLTAGE;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int maxForeignAmps = maxExtract / UNIVERSAL_FOREIGN_VOLTAGE;
        int ampsExtracted = this.extractPower(UNIVERSAL_FOREIGN_VOLTAGE, maxForeignAmps, simulate);
        return ampsExtracted * UNIVERSAL_FOREIGN_VOLTAGE;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxInsert > 0;
    }
}
