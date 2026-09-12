package com.slayerrumeurs;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SlayerRumoursPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SlayerRumoursPlugin.class);
		RuneLite.main(args);
	}
}
