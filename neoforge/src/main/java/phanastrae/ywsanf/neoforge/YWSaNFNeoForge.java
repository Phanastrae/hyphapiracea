package phanastrae.ywsanf.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import phanastrae.ywsanf.YWSaNF;

@Mod(YWSaNF.MOD_ID)
public class YWSaNFNeoForge {

    public YWSaNFNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        YWSaNF.LOGGER.info("YOU WOULDN'T STEAL A MINECART.");
        YWSaNF.LOGGER.info("YOU WOULDN'T STEAL A CHEST.");
        YWSaNF.LOGGER.info("YOU WOULDN'T STEAL A ZOMBIE.");
        YWSaNF.LOGGER.info("YOU WOULDN'T STEAL A NETHER FORTRESS.");
    }
}
