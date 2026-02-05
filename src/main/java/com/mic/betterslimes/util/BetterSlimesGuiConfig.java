package com.mic.betterslimes.util;

import java.util.List;
import java.util.stream.Collectors;

import com.mic.betterslimes.util.Reference;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class BetterSlimesGuiConfig extends GuiConfig {

	public BetterSlimesGuiConfig(GuiScreen parentScreen) 
	{
		super(parentScreen, getConfigElements(), Reference.MODID, false, false, "betterslimes.config.title");
	}

	private static List<IConfigElement> getConfigElements() 
	{
		return BetterSlimesConfig.config.getCategoryNames().stream()
				.map(categoryName -> new ConfigElement(BetterSlimesConfig.config.getCategory(categoryName).setLanguageKey("betterslimes.config." + categoryName)))
				.collect(Collectors.toList());
	}
}
