package com.slayerrumeurs.navigation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;

/**
 * Optional Shortest Path integration. Soft-fails if that Hub plugin is not installed.
 * Only clears paths this plugin created.
 */
@Slf4j
@Singleton
public class ShortestPathService
{
	public static final String NAMESPACE = "shortestpath";
	public static final String MSG_PATH = "path";
	public static final String MSG_CLEAR = "clear";
	public static final String DATA_START = "start";
	public static final String DATA_TARGET = "target";

	private final EventBus eventBus;

	private boolean pathOwnedByUs;
	private WorldPoint lastTarget;

	@Inject
	public ShortestPathService(EventBus eventBus)
	{
		this.eventBus = eventBus;
	}

	public boolean isPathOwnedByUs()
	{
		return pathOwnedByUs;
	}

	public WorldPoint getLastTarget()
	{
		return lastTarget;
	}

	public void pathTo(WorldPoint start, WorldPoint target)
	{
		if (target == null)
		{
			return;
		}
		if (pathOwnedByUs && target.equals(lastTarget))
		{
			return;
		}

		try
		{
			Map<String, Object> data = new HashMap<>();
			if (start != null)
			{
				data.put(DATA_START, start);
			}
			data.put(DATA_TARGET, target);
			eventBus.post(new PluginMessage(NAMESPACE, MSG_PATH, data));
			pathOwnedByUs = true;
			lastTarget = target;
		}
		catch (RuntimeException ex)
		{
			log.debug("Shortest Path request failed", ex);
		}
	}

	public void clearIfOwned()
	{
		if (!pathOwnedByUs)
		{
			return;
		}
		try
		{
			eventBus.post(new PluginMessage(NAMESPACE, MSG_CLEAR));
		}
		catch (RuntimeException ex)
		{
			log.debug("Shortest Path clear failed", ex);
		}
		finally
		{
			pathOwnedByUs = false;
			lastTarget = null;
		}
	}

	public void forgetOwnership()
	{
		pathOwnedByUs = false;
		lastTarget = null;
	}
}
