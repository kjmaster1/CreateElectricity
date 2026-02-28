package com.kjmaster.createelectricity.api.capability;

import net.neoforged.neoforge.energy.IEnergyStorage;


/**
 * Extends the standard {@link IEnergyStorage} to ensure compatibility with standard
 * Forge Energy (FE) mods, while internally enforcing physical constraints of
 * Voltage and Amperage.
 * <p>
 * Formula: Power (FE) = Voltage * Amperage
 */
public interface IElectrodynamicPower extends IEnergyStorage {

    /**
     * Gets the nominal operating voltage of this electrical component.
     * For generators, this is the voltage they attempt to push into the grid.
     * For machines, this is the minimum voltage required to operate.
     *
     * @return The voltage tier (e.g., 100V, 240V, 1000V).
     */
    int getNominalVoltage();

    /**
     * Gets the current flow of amperage in the present tick.
     * This is used for visual feedback (e.g., sparks) and
     * calculating Joule heating (Amps^2 * Resistance).
     *
     * @return The current amperage throughput.
     */
    int getCurrentAmperage();

    /**
     * Gets the maximum safe amperage this component can handle before
     * overload.
     *
     * @return The maximum allowable amperage.
     */
    int getMaxAmperage();

    /**
     * Attempts to push power into the component using strict physical units.
     *
     * @param voltage     The voltage of the incoming power grid. If this greatly exceeds
     * the nominal voltage, the block logic may trigger a short circuit.
     * @param maxAmperage The maximum amount of current the source can provide this tick.
     * @param simulate    If true, the action will only be simulated.
     * @return The amount of amperage that was successfully accepted.
     */
    int receivePower(int voltage, int maxAmperage, boolean simulate);

    /**
     * Attempts to extract power from the component using strict physical units.
     *
     * @param voltage     The voltage the extracting network is operating at.
     * @param maxAmperage The maximum amount of current the network is attempting to draw.
     * @param simulate    If true, the action will only be simulated.
     * @return The amount of amperage that was successfully extracted.
     */
    int extractPower(int voltage, int maxAmperage, boolean simulate);
}
