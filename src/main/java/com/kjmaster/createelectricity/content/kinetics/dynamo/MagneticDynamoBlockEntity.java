package com.kjmaster.createelectricity.content.kinetics.dynamo;

import com.kjmaster.createelectricity.AllBlockEntityTypes;
import com.kjmaster.createelectricity.api.capability.ElectrodynamicCapabilities;
import com.kjmaster.createelectricity.api.capability.ElectrodynamicStorage;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public class MagneticDynamoBlockEntity extends KineticBlockEntity {

    // Early game tier values
    public static final int DYNAMO_NOMINAL_VOLTAGE = 100;
    public static final int DYNAMO_MAX_AMPERAGE = 10;
    public static final int DYNAMO_CAPACITY = 4000;

    // The amount of FE generated per 1 RPM
    public static final float FE_GENERATED_PER_RPM = 1.5f;

    private final ElectrodynamicStorage energyStorage;

    private int lastTickAmps = 0;

    public MagneticDynamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        // Initialize our custom electrodynamic storage.
        // As a generator, we allow extract but not insert.
        this.energyStorage = new ElectrodynamicStorage(DYNAMO_CAPACITY, DYNAMO_NOMINAL_VOLTAGE, DYNAMO_MAX_AMPERAGE) {
            @Override
            public boolean canReceive() {
                return false;
            }
        };
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                ElectrodynamicCapabilities.POWER,
                AllBlockEntityTypes.MAGNETIC_DYNAMO.get(),
                MagneticDynamoBlockEntity::getEnergyStorage
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        generateEnergy();

        System.out.println("Energy: " + this.energyStorage.getEnergyStored());

        // If the electrical load changed, force Create to recalculate the kinetic network
        int currentAmps = this.energyStorage.getCurrentAmperage();
        if (currentAmps != lastTickAmps) {
            lastTickAmps = currentAmps;

            if (hasNetwork()) {
                detachKinetics();
                attachKinetics();
            }
        }
    }

    private void generateEnergy() {
        float speed = Math.abs(getSpeed());

        // If it's not spinning, we do nothing
        if (speed <= 0) {
            this.energyStorage.resetCurrentAmperage();
            return;
        }

        // Calculate generation based on RPM
        int feToGenerate = (int) (speed * FE_GENERATED_PER_RPM);

        if (feToGenerate > 0) {
            // Because our API mandates physical constraints, we don't just add FE.
            // We calculate how much Amperage the dynamo generated this tick based on its nominal voltage.
            int generatedAmps = Math.max(1, feToGenerate / this.energyStorage.getNominalVoltage());

            // Internally push it into the buffer (ignoring the 'canReceive' block we set for outside grids)
            int acceptedAmps = Math.min(
                    generatedAmps,
                    (this.energyStorage.getMaxEnergyStored() - this.energyStorage.getEnergyStored()) / this.energyStorage.getNominalVoltage()
            );

            // Directly modify the backing energy logic
            int internalFE = acceptedAmps * this.energyStorage.getNominalVoltage();
            this.energyStorage.setEnergy(this.energyStorage.getEnergyStored() + internalFE);
        }
    }

    @Override
    public float calculateStressApplied() {
        // Get the actual load currently being drawn from the generator
        int activeLoadAmps = this.energyStorage.getCurrentAmperage();

        if (activeLoadAmps == 0) {
            return 0.0f; // Spins freely if nothing is plugged in!
        }

        // Define a base impact per Ampere.
        // Example: 1 Ampere creates 8.0 SU of resistance per RPM.
        float impactPerAmp = 8.0f;

        return activeLoadAmps * impactPerAmp;
    }

    /**
     * Exposes the energy storage so the NeoForge Capability system can attach it.
     */
    @Nullable
    public ElectrodynamicStorage getEnergyStorage(@Nullable Direction side) {
        // Optionally, restrict which sides can connect to wires.
        // For example, disallowing connection to the shaft face.
        if (side != null && side == getBlockState().getValue(MagneticDynamoBlock.FACING)) {
            return null;
        }
        return this.energyStorage;
    }

    // --- NBT Serialization ---


    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("Energy", this.energyStorage.getEnergyStored());
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        this.energyStorage.setEnergy(compound.getInt("Energy"));

    }
}