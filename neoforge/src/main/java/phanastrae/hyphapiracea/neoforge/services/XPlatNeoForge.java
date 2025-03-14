package phanastrae.hyphapiracea.neoforge.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import phanastrae.hyphapiracea.services.XPlatInterface;

public class XPlatNeoForge implements XPlatInterface {

    @Override
    public String getLoader() {
        return "neoforge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public void sendPayload(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public CreativeModeTab.Builder createCreativeModeTabBuilder() {
        return CreativeModeTab.builder();
    }
}
