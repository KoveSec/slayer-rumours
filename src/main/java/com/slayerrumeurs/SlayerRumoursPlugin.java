package com.slayerrumeurs;

import com.google.inject.Provides;
import com.slayerrumeurs.gear.InventorySetupService;
import com.slayerrumeurs.highlight.TaskNpcHighlighter;
import com.slayerrumeurs.navigation.ShortestPathService;
import com.slayerrumeurs.preference.PreferenceStore;
import com.slayerrumeurs.task.ActiveTaskState;
import com.slayerrumeurs.task.SlayerChatParser;
import com.slayerrumeurs.task.SlayerGameStateReader;
import com.slayerrumeurs.task.TaskCatalog;
import com.slayerrumeurs.task.TaskDefinition;
import com.slayerrumeurs.task.TaskLocation;
import com.slayerrumeurs.task.TaskResolver;
import com.slayerrumeurs.ui.PreferredLocationsPanel;
import com.slayerrumeurs.ui.TaskStripOverlay;
import com.slayerrumeurs.ui.WorldMapTaskPoint;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.npcoverlay.NpcOverlayService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Slayer Rumours",
	description = "Preferred locations, Shortest Path, and Inventory Setups for your current Slayer task",
	tags = {"slayer", "task", "path", "inventory", "location"}
)
public class SlayerRumoursPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private SlayerRumoursConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TaskStripOverlay taskStripOverlay;

	@Inject
	private NpcOverlayService npcOverlayService;

	@Inject
	private TaskNpcHighlighter taskNpcHighlighter;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PreferredLocationsPanel preferredLocationsPanel;

	@Inject
	private ShortestPathService shortestPathService;

	@Inject
	private InventorySetupService inventorySetupService;

	@Inject
	private PreferenceStore preferenceStore;

	@Inject
	private TaskCatalog catalog;

	@Inject
	private TaskResolver resolver;

	@Inject
	private SlayerGameStateReader gameStateReader;

	@Inject
	private ActiveTaskState state;

	private NavigationButton navigationButton;
	private boolean navigationAdded;
	private WorldMapTaskPoint worldMapPoint;
	private String lastAssignedTaskId;

	@Provides
	SlayerRumoursConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SlayerRumoursConfig.class);
	}

	@Override
	protected void startUp()
	{
		navigationButton = NavigationButton.builder()
			.tooltip("Slayer Rumours")
			.icon(PreferredLocationsPanel.createNavigationIcon())
			.priority(6)
			.panel(preferredLocationsPanel)
			.build();
		syncSidebar();

		overlayManager.add(taskStripOverlay);
		npcOverlayService.registerHighlighter(taskNpcHighlighter);
		inventorySetupService.refresh();

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			syncFromGameState(false);
		}

		log.debug("Slayer Rumours started");
	}

	@Override
	protected void shutDown()
	{
		if (navigationAdded)
		{
			clientToolbar.removeNavigation(navigationButton);
			navigationAdded = false;
		}

		overlayManager.remove(taskStripOverlay);
		npcOverlayService.unregisterHighlighter(taskNpcHighlighter);
		npcOverlayService.rebuild();
		removeWorldMapPoint();
		shortestPathService.clearIfOwned();
		inventorySetupService.clearIfOpenedByUs();
		state.clear();
		lastAssignedTaskId = null;
		log.debug("Slayer Rumours stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			syncFromGameState(false);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int varpId = event.getVarpId();
		if (varpId == VarPlayerID.SLAYER_COUNT
			|| varpId == VarPlayerID.SLAYER_TARGET
			|| varpId == VarPlayerID.SLAYER_AREA
			|| varpId == VarPlayerID.SLAYER_COUNT_ORIGINAL)
		{
			syncFromGameState(false);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		SlayerChatParser.Result parsed = SlayerChatParser.parse(Text.removeTags(event.getMessage()));
		if (parsed.getKind() == SlayerChatParser.Kind.COMPLETED)
		{
			clearActiveTask();
			return;
		}
		if (parsed.getKind() == SlayerChatParser.Kind.ASSIGNED && parsed.getRawTaskName() != null)
		{
			int remaining = parsed.getRemaining() != null
				? parsed.getRemaining()
				: Math.max(gameStateReader.getRemaining(client), 1);
			applyAssignment(parsed.getRawTaskName(), remaining, true);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!SlayerRumoursConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		if ("showSidebar".equals(event.getKey()))
		{
			syncSidebar();
			return;
		}

		if (event.getKey() != null && (event.getKey().startsWith(PreferenceStore.LOCATION_KEY_PREFIX)
			|| PreferenceStore.SETUP_MAP_KEY.equals(event.getKey())))
		{
			refreshActivePreferences();
			refreshDisplays(false);
			return;
		}

		if ("useShortestPath".equals(event.getKey()) && !config.useShortestPath())
		{
			shortestPathService.clearIfOwned();
			state.setPathOwnedByUs(false);
		}

		refreshDisplays(false);
	}

	@Subscribe
	public void onPluginMessage(PluginMessage message)
	{
		inventorySetupService.onPluginMessage(message);
		if (InventorySetupService.NAMESPACE.equals(message.getNamespace())
			&& InventorySetupService.MSG_SETUPS_CHANGED.equals(message.getName()))
		{
			preferredLocationsPanel.refreshSetupHints();
		}
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked event)
	{
		if (event.getOverlay() != taskStripOverlay)
		{
			return;
		}
		String option = event.getEntry().getOption();
		if (TaskStripOverlay.MENU_NAV.equals(option))
		{
			requestPath();
		}
		else if (TaskStripOverlay.MENU_SETUP.equals(option))
		{
			openMappedSetup();
		}
	}

	public String getActiveTaskDisplayName()
	{
		TaskDefinition task = currentTask();
		if (task != null)
		{
			return task.getDisplayName();
		}
		return state.getRawTaskName();
	}

	public String getActiveLocationLabel()
	{
		TaskDefinition task = currentTask();
		if (task == null)
		{
			return "Unknown";
		}
		TaskLocation location = task.findLocation(state.getPreferredLocationId());
		if (location == null)
		{
			location = task.defaultLocation();
		}
		return location == null ? "Unknown" : location.getLabel();
	}

	private void syncFromGameState(boolean treatAsNewAssignment)
	{
		int remaining = gameStateReader.getRemaining(client);
		if (remaining <= 0)
		{
			if (state.hasTask())
			{
				clearActiveTask();
			}
			return;
		}

		String name = gameStateReader.lookupTaskName(client);
		if (name == null)
		{
			name = state.getRawTaskName();
		}
		if (name == null || name.isEmpty())
		{
			state.setRemaining(remaining);
			return;
		}
		applyAssignment(name, remaining, treatAsNewAssignment);
	}

	private void applyAssignment(String rawName, int remaining, boolean fromChat)
	{
		TaskDefinition task = resolver.resolve(rawName);
		String taskId = task == null ? null : task.getId();
		boolean newAssignment = fromChat
			|| taskId == null && !rawName.equalsIgnoreCase(state.getRawTaskName())
			|| taskId != null && !taskId.equals(lastAssignedTaskId);

		state.setRawTaskName(rawName);
		state.setRemaining(remaining);
		state.setTaskId(taskId);
		refreshActivePreferences();

		if (newAssignment)
		{
			lastAssignedTaskId = taskId != null ? taskId : rawName.toLowerCase();
			if (config.useShortestPath() && config.autoPathOnAssign())
			{
				requestPath();
			}
			if (config.autoOpenSetupOnAssign())
			{
				openMappedSetup();
			}
		}

		refreshDisplays(false);
	}

	private void clearActiveTask()
	{
		shortestPathService.clearIfOwned();
		inventorySetupService.clearIfOpenedByUs();
		removeWorldMapPoint();
		state.clear();
		lastAssignedTaskId = null;
		taskNpcHighlighter.rebuildMatchers();
		npcOverlayService.rebuild();
	}

	private void refreshActivePreferences()
	{
		TaskDefinition task = currentTask();
		if (task == null)
		{
			state.setPreferredLocationId(null);
			state.setPreferredPoint(null);
			state.setActiveSetupName(null);
			return;
		}

		String locationId = preferenceStore.getPreferredLocationId(task.getId());
		TaskLocation location = task.findLocation(locationId);
		if (location == null)
		{
			location = task.defaultLocation();
		}
		state.setPreferredLocationId(location == null ? null : location.getId());
		state.setPreferredPoint(location == null ? null : location.toWorldPoint());
		state.setActiveSetupName(preferenceStore.getSetupName(task.getId()));
	}

	private void refreshDisplays(boolean forcePath)
	{
		taskNpcHighlighter.rebuildMatchers();
		npcOverlayService.rebuild();
		updateWorldMapPin();
		state.setPathOwnedByUs(shortestPathService.isPathOwnedByUs());
		if (forcePath)
		{
			requestPath();
		}
		else if (config.useShortestPath() && shortestPathService.isPathOwnedByUs())
		{
			WorldPoint target = state.getPreferredPoint();
			Player local = client.getLocalPlayer();
			shortestPathService.pathTo(local == null ? null : local.getWorldLocation(), target);
			state.setPathOwnedByUs(shortestPathService.isPathOwnedByUs());
		}
	}

	private void updateWorldMapPin()
	{
		removeWorldMapPoint();
		if (!config.showWorldMapPin() || !state.hasTask() || state.getPreferredPoint() == null)
		{
			return;
		}

		BufferedImage icon = itemManager.getImage(ItemID.SLAYER_GEM);
		String name = getActiveTaskDisplayName() + " (" + getActiveLocationLabel() + ")";
		worldMapPoint = new WorldMapTaskPoint(state.getPreferredPoint(), icon, name);
		worldMapPointManager.add(worldMapPoint);
	}

	private void removeWorldMapPoint()
	{
		if (worldMapPoint != null)
		{
			worldMapPointManager.remove(worldMapPoint);
			worldMapPoint = null;
		}
	}

	private void requestPath()
	{
		if (!config.useShortestPath() || !state.hasTask() || state.getPreferredPoint() == null)
		{
			return;
		}
		Player local = client.getLocalPlayer();
		shortestPathService.pathTo(local == null ? null : local.getWorldLocation(), state.getPreferredPoint());
		state.setPathOwnedByUs(shortestPathService.isPathOwnedByUs());
	}

	private void openMappedSetup()
	{
		String setupName = state.getActiveSetupName();
		if (setupName == null || setupName.isEmpty())
		{
			log.debug("No Inventory Setup mapped for the current task");
			return;
		}
		inventorySetupService.view(setupName);
	}

	private void syncSidebar()
	{
		if (navigationButton == null)
		{
			return;
		}
		if (config.showSidebar() && !navigationAdded)
		{
			clientToolbar.addNavigation(navigationButton);
			navigationAdded = true;
		}
		else if (!config.showSidebar() && navigationAdded)
		{
			clientToolbar.removeNavigation(navigationButton);
			navigationAdded = false;
		}
	}

	private TaskDefinition currentTask()
	{
		return state.getTaskId() == null ? null : catalog.getById(state.getTaskId());
	}
}
