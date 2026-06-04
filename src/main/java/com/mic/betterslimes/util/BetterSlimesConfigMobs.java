package com.mic.betterslimes.util;

import com.mic.betterslimes.entity.slimes.Quazar;
import net.minecraftforge.common.config.Configuration;

import static com.mic.betterslimes.entity.EntityBetterSlime.damageMultiplier;


public class BetterSlimesConfigMobs {
	
    public static boolean startupMessage = true;
    public static boolean convertSlimes = true;
    public static boolean convertIgnoreChance = true;
	
	public static int blueSlime = 14;
	public static int redSlime = 7;
	public static int yellowSlime = 4;
	public static int purpleSlime = 2;
	public static int blackSlime = 80;
	public static int iceSlime = 8;
	public static int jungleSlime = 20;
	public static int sandSlime = 20;
    public static int spectralSlime = 12;
    public static int hauntedSlime = 12;
	public static int kingChance = 5;
	public static int ironSlime = 40;
	public static int goldSlime = 20;
    public static int knightSlime = 6;
    
	public static int splitChance = 50;




    public static int quazarSize = 32;

    public static int leapWarning = 20;
    public static int leapCooldown = 120;
    public static int leapLandingRadius = 10;
    public static float leapLandingDamage = 18.0F;
    public static float leapLaunchMultiplier = 1.0F;

    public static int specialWarning = 50;
    public static int specialCooldown = 900;
    public static int specialSequenceCooldown = 60;
    public static int specialSequenceMax = 3;
    public static int meteorLandingRadius = 16;
    public static float meteorLandingDamage = 24.0F;
    public static float meteorLaunchMultiplier = 1.0F;


	public static final int MAX = Short.MAX_VALUE;

	public static void load(Configuration config) 
    {
		String category1 = "General Config";
//Adds config category
		config.addCustomCategoryComment(category1, "General config values.");


//Format is category, key, default value, comment
		startupMessage = config.getBoolean("Start-Up Message?", category1, startupMessage, "Give a start-up thank you?");
		kingChance = config.getInt("King Slime Spawn Chance", category1, kingChance, 0, 100, "0 for never and 100 for every night.");
		splitChance = config.getInt("Slime Splitting Chance", category1, splitChance, 0, 100, "0 for never and 100 for always.");
        damageMultiplier = config.getFloat("Damage Multiplier", category1, damageMultiplier, 0, MAX, "Custom slime damage multiplier");
        convertSlimes = config.getBoolean("Convert Slimes?", category1, convertSlimes, "Convert slimes that spawn to their biome-specific type, if applicable?");
        convertIgnoreChance = config.getBoolean("Ignore Spawn Chance?", category1, convertIgnoreChance, "If convert slimes is enabled, ignore the spawn chance being 0 when spawning a specific slime?");




		String category2 = "Spawn Chances";
//Adds config category
		config.addCustomCategoryComment(category2, "Slime spawn chances.");


		blueSlime = config.getInt("Blue Slime Spawn Chance", category2, blueSlime, 0, 100, "0 for never and 100 for always.");
		redSlime = config.getInt("Red Slime Spawn Chance", category2, redSlime, 0, 100, "0 for never and 100 for always.");
		yellowSlime = config.getInt("Yellow Slime Spawn Chance", category2, yellowSlime, 0, 100, "0 for never and 100 for always.");
		purpleSlime = config.getInt("Purple Slime Spawn Chance", category2, purpleSlime, 0, 100, "0 for never and 100 for always.");
		blackSlime = config.getInt("Black Slime Spawn Chance", category2, blackSlime, 0, 100, "0 for never and 100 for always.");
		iceSlime = config.getInt("Ice Slime Spawn Chance", category2, iceSlime, 0, 100, "0 for never and 100 for always.");
		jungleSlime = config.getInt("Jungle Slime Spawn Chance", category2, jungleSlime, 0, 100, "0 for never and 100 for always.");
		sandSlime = config.getInt("Sand Slime Spawn Chance", category2, sandSlime, 0, 100, "0 for never and 100 for always.");
		spectralSlime = config.getInt("Spectral Slime Spawn Chance", category2, spectralSlime, 0, 100, "0 for never and 100 for always.");
		hauntedSlime = config.getInt("Haunted Slime Spawn Chance", category2, hauntedSlime, 0, 100, "0 for never and 100 for always.");
		ironSlime = config.getInt("Iron Slime Spawn Chance", category2, ironSlime, 0, 100, "0 for never and 100 for always.");
		goldSlime = config.getInt("Gold Slime Spawn Chance", category2, goldSlime, 0, 100, "0 for never and 100 for always.");
		knightSlime = config.getInt("Knight Slime Spawn Chance", category2, knightSlime, 0, 100, "0 for never and 100 for always.");



    
		String category3 = "Quazar Config";
//Adds config category
		config.addCustomCategoryComment(category3, "Quazar specific configs.");


        quazarSize = config.getInt("Quazar size", category3, 32, 0, MAX, "Size of Quazar in blocks");

        leapWarning = config.getInt("Quazar leap warning", category3, 20, 0, MAX, "Length of the animation the boss does before leap attacks in ticks. \nMust be shorter than leapCooldown");
        leapCooldown = config.getInt("Quazar leap cooldown", category3, 120, 0, MAX, "Length of the cooldown between Quazar's leaps");
        leapLandingRadius = config.getInt("Quazar leap damage radius", category3, 12, 0, MAX, "Damage radius of the leap attack");
        leapLandingDamage = config.getFloat("Quazar leap landing damage", category3, 18.0F, 0, MAX, "Damage the leap attack deals on landing");
        leapLaunchMultiplier = config.getFloat("Quazar leap launch", category3, 1.0F, 0, MAX, "Launch speed all entities affected by the leap attack get");

        specialWarning = config.getInt("Quazar special warning", category3, 50, 0, MAX, "Length of the animation the boss does before special attacks in ticks. \nMust be shorter than specialCooldown");
        specialCooldown = config.getInt("Quazar special cooldown", category3, 900, 0, MAX, "Cooldown after special attack sequences in ticks");
        specialSequenceCooldown = config.getInt("Quazar special inbetween cooldown", category3, 60, 0, MAX, "Cooldown inbetween each part of a special attack in ticks");
        specialSequenceMax = config.getInt("Quazar special sequence count", category3, 3, 0, MAX, "Special attacks done in sequence");
        meteorLandingRadius = config.getInt("Quazar special meteor damage radius", category3, 16, 0, MAX, "Damage radius of the special meteor attack");
        meteorLandingDamage = config.getFloat("Quazar special meteor damage", category3, 24.0F, 0, MAX, "Damage the special meteor attack deals on landing");
        meteorLaunchMultiplier = config.getFloat("Quazar special meteor launch", category3, 1.0F, 0, MAX, "Launch speed all entities affected by the special meteor attack get");
    }
}
