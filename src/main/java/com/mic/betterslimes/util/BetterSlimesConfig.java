package com.mic.betterslimes.util;

import java.io.File;

import com.mic.betterslimes.util.Reference;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BetterSlimesConfig {

	public static Configuration config;
	public static void load(FMLPreInitializationEvent event) {
		File dir = getBetterSlimesConfigurationLocation(event);
		
		if(!dir.exists())
		{
			dir.mkdirs();
		}

		config = new Configuration(new File(dir, "betterslimes.cfg"));
		reloadConfig();
		
		MinecraftForge.EVENT_BUS.register(new BetterSlimesConfig());
	}

	private static void reloadConfig() {
		
		BetterSlimesConfigMobs.load(config);

		if (config.hasChanged()) {
			config.save();
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(Reference.MODID)) {
			reloadConfig();
		}
	}
	
	public static File getBetterSlimesConfigurationLocation(FMLPreInitializationEvent event)
	{
		return new File(event.getModConfigurationDirectory(), "betterslimes");
	}
}
