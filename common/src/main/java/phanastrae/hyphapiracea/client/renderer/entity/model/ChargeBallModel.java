package phanastrae.hyphapiracea.client.renderer.entity.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import phanastrae.hyphapiracea.entity.ChargeballEntity;

public class ChargeBallModel extends HierarchicalModel<ChargeballEntity> {
    private static final int ROTATION_SPEED = 16;
    private final ModelPart bone;
    private final ModelPart chargeball;
    private final ModelPart aura;

    public ChargeBallModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.bone = root.getChild("bone");
        this.aura = this.bone.getChild("aura");
        this.chargeball = this.bone.getChild("chargeball");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        partdefinition1.addOrReplaceChild(
                "aura",
                CubeListBuilder.create()
                        .texOffs(15, 20)
                        .addBox(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 9)
                        .addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
        );
        partdefinition1.addOrReplaceChild(
                "chargeball",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public void setupAnim(ChargeballEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float degToRad = (float) Math.PI / 180;
        this.chargeball.yRot = -ageInTicks * ROTATION_SPEED * degToRad;
        this.aura.yRot = ageInTicks * ROTATION_SPEED * degToRad * entity.getElectricCharge();
        this.aura.xRot = ageInTicks * ROTATION_SPEED * degToRad * entity.getMagneticCharge();
        this.aura.zRot = ageInTicks * ROTATION_SPEED * degToRad * entity.getMagneticCharge();
    }

    @Override
    public ModelPart root() {
        return this.bone;
    }
}
