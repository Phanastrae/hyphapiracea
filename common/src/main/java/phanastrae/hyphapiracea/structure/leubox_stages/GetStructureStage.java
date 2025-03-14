package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.structure.StructureType;

import java.util.Optional;

public class GetStructureStage extends AbstractLeukboxStage {

    private final ResourceLocation structureId;
    private final StructureType structureType;
    private final boolean rotateStructure;

    public GetStructureStage(BlockPos leukboxPos, ResourceLocation structureId, StructureType structureType, boolean rotateStructure) {
        super(leukboxPos, LeukboxStage.GET_STRUCTURE);

        this.structureId = structureId;
        this.structureType = structureType;
        this.rotateStructure = rotateStructure;
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        if(this.structureType == StructureType.STRUCTURE) {
            // get structure from RL
            Optional<Structure> structureOptional = getStructure(serverLevel.registryAccess(), this.structureId);
            if (structureOptional.isPresent()) {
                return new GetStructureStartStage(this.leukboxPos, structureOptional.get(), this.structureId, this.structureType, this.rotateStructure);
            } else {
                return this.getError("invalid_structure");
            }
        } else {
            // pass null structure
            return new GetStructureStartStage(this.leukboxPos, null, this.structureId, this.structureType, this.rotateStructure);
        }
    }

    public static Optional<Structure> getStructure(RegistryAccess registryAccess, ResourceLocation resourceLocation) {
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, resourceLocation);

        Optional<Registry<Structure>> structureRegistryOptional = registryAccess.registry(Registries.STRUCTURE);
        if (structureRegistryOptional.isEmpty()) {
            return Optional.empty();
        }

        Optional<Holder.Reference<Structure>> structureReferenceOptional = structureRegistryOptional.get().getHolder(key);
        return structureReferenceOptional.map(Holder.Reference::value);
    }
}
