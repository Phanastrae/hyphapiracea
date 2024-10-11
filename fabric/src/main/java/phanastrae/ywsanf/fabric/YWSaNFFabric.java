package phanastrae.ywsanf.fabric;

import net.fabricmc.api.ModInitializer;
import phanastrae.ywsanf.YWSaNF;

public class YWSaNFFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		YWSaNF.LOGGER.info("YOU WOULDN'T STEAL A BOAT.");
		YWSaNF.LOGGER.info("YOU WOULDN'T STEAL A SHULKER BOX.");
		YWSaNF.LOGGER.info("YOU WOULDN'T STEAL A CREEPER.");
		YWSaNF.LOGGER.info("YOU WOULDN'T STEAL A NETHER FORTRESS.");
	}
}