package com.slayerrumeurs.ui;

import com.slayerrumeurs.SlayerRumoursConfig;
import com.slayerrumeurs.SlayerRumoursPlugin;
import com.slayerrumeurs.task.ActiveTaskState;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class TaskStripOverlay extends OverlayPanel
{
	public static final String MENU_NAV = "Nav";
	public static final String MENU_SETUP = "Setup";
	public static final String OVERLAY_NAME = "Slayer Rumours";

	private final SlayerRumoursPlugin plugin;
	private final SlayerRumoursConfig config;
	private final ActiveTaskState state;

	@Inject
	TaskStripOverlay(SlayerRumoursPlugin plugin, SlayerRumoursConfig config, ActiveTaskState state)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		this.state = state;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, MENU_NAV, OVERLAY_NAME));
		getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, MENU_SETUP, OVERLAY_NAME));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showTaskStrip() || !state.hasTask())
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text(OVERLAY_NAME)
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Task")
			.right(displayName())
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Left")
			.right(Integer.toString(state.getRemaining()))
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left("At")
			.right(plugin.getActiveLocationLabel())
			.build());

		return super.render(graphics);
	}

	private String displayName()
	{
		String resolved = plugin.getActiveTaskDisplayName();
		return resolved != null ? resolved : state.getRawTaskName();
	}
}
