package com.slayerrumeurs;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(SlayerRumoursConfig.GROUP)
public interface SlayerRumoursConfig extends Config
{
	String GROUP = "slayer-rumours";

	@ConfigSection(
		name = "Display",
		description = "Task strip, sidebar, and world map.",
		position = 0
	)
	String displaySection = "displaySection";

	@ConfigSection(
		name = "Navigation",
		description = "Optional Shortest Path integration.",
		position = 1
	)
	String navigationSection = "navigationSection";

	@ConfigSection(
		name = "Inventory Setups",
		description = "Optional Inventory Setups integration.",
		position = 2
	)
	String setupsSection = "setupsSection";

	@ConfigSection(
		name = "Highlights",
		description = "On-task NPC highlighting.",
		position = 3
	)
	String highlightSection = "highlightSection";

	@ConfigItem(
		keyName = "showTaskStrip",
		name = "Task strip",
		description = "Show the overlay with task name, remaining kills, location, and Nav/Setup actions.",
		position = 0,
		section = displaySection
	)
	default boolean showTaskStrip()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSidebar",
		name = "Preferred locations panel",
		description = "Show the sidebar for choosing a location and Inventory Setup name per task.",
		position = 1,
		section = displaySection
	)
	default boolean showSidebar()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showWorldMapPin",
		name = "World map pin",
		description = "Pin the preferred location on the world map while a task is active.",
		position = 2,
		section = displaySection
	)
	default boolean showWorldMapPin()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useShortestPath",
		name = "Use Shortest Path",
		description = "Ask the Shortest Path plugin to route to the preferred location. Soft-fails if that plugin is missing.",
		warning = "Requires the Shortest Path plugin from the Plugin Hub.",
		position = 0,
		section = navigationSection
	)
	default boolean useShortestPath()
	{
		return true;
	}

	@ConfigItem(
		keyName = "autoPathOnAssign",
		name = "Auto-path on assign",
		description = "Automatically request a Shortest Path route when a new Slayer task is assigned.",
		position = 1,
		section = navigationSection
	)
	default boolean autoPathOnAssign()
	{
		return false;
	}

	@ConfigItem(
		keyName = "autoOpenSetupOnAssign",
		name = "Auto-open setup on assign",
		description = "Automatically open the mapped Inventory Setup when a new Slayer task is assigned.",
		warning = "Requires the Inventory Setups plugin from the Plugin Hub.",
		position = 0,
		section = setupsSection
	)
	default boolean autoOpenSetupOnAssign()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightTaskNpcs",
		name = "Highlight task NPCs",
		description = "Highlight NPCs that match the active catalogued Slayer task.",
		position = 0,
		section = highlightSection
	)
	default boolean highlightTaskNpcs()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight color",
		description = "Color used for on-task NPC highlights.",
		position = 1,
		section = highlightSection
	)
	default Color highlightColor()
	{
		return new Color(255, 80, 80, 200);
	}

	@ConfigItem(
		keyName = "highlightHull",
		name = "Highlight hull",
		description = "Draw a hull around matching NPCs.",
		position = 2,
		section = highlightSection
	)
	default boolean highlightHull()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightTile",
		name = "Highlight tile",
		description = "Highlight the tile under matching NPCs.",
		position = 3,
		section = highlightSection
	)
	default boolean highlightTile()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightOutline",
		name = "Highlight outline",
		description = "Draw an outline around matching NPCs.",
		position = 4,
		section = highlightSection
	)
	default boolean highlightOutline()
	{
		return true;
	}
}
