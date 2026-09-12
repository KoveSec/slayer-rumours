package com.slayerrumeurs.gear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;

/**
 * Optional Inventory Setups integration via PluginMessage only.
 * Soft-fails if that Hub plugin is missing.
 */
@Slf4j
@Singleton
public class InventorySetupService
{
	public static final String NAMESPACE = "inventory-setups";
	public static final String MSG_GET_SETUPS = "get-setups";
	public static final String MSG_VIEW = "view";
	public static final String MSG_CLEAR = "clear";
	public static final String MSG_SETUPS_CHANGED = "setups-changed";
	public static final String DATA_SETUPS = "setups";
	public static final String DATA_SETUP = "setup";

	private final EventBus eventBus;

	private volatile boolean available;
	private volatile List<String> setupNames = Collections.emptyList();
	private String setupOpenedByUs;

	@Inject
	public InventorySetupService(EventBus eventBus)
	{
		this.eventBus = eventBus;
	}

	public boolean isAvailable()
	{
		return available;
	}

	public List<String> getSetupNames()
	{
		return setupNames;
	}

	public boolean hasSetup(String name)
	{
		if (name == null || name.isEmpty() || !available)
		{
			return false;
		}
		for (String setup : setupNames)
		{
			if (name.equals(setup))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Posts get-setups. Inventory Setups fills the collection synchronously when present.
	 */
	public void refresh()
	{
		ResponseCollection response = new ResponseCollection();
		try
		{
			eventBus.post(new PluginMessage(NAMESPACE, MSG_GET_SETUPS, Map.of(DATA_SETUPS, response)));
		}
		catch (RuntimeException ex)
		{
			log.debug("Inventory Setups get-setups failed", ex);
			return;
		}

		if (response.answered)
		{
			available = true;
			setupNames = Collections.unmodifiableList(new ArrayList<>(response));
		}
	}

	public boolean view(String setupName)
	{
		if (setupName == null || setupName.trim().isEmpty())
		{
			return false;
		}
		String name = setupName.trim();
		try
		{
			eventBus.post(new PluginMessage(NAMESPACE, MSG_VIEW, Map.of(DATA_SETUP, name)));
			setupOpenedByUs = name;
			return true;
		}
		catch (RuntimeException ex)
		{
			log.debug("Inventory Setups view failed", ex);
			return false;
		}
	}

	public void clearIfOpenedByUs()
	{
		if (setupOpenedByUs == null)
		{
			return;
		}
		try
		{
			eventBus.post(new PluginMessage(NAMESPACE, MSG_CLEAR, Map.of(DATA_SETUP, setupOpenedByUs)));
		}
		catch (RuntimeException ex)
		{
			log.debug("Inventory Setups clear failed", ex);
		}
		finally
		{
			setupOpenedByUs = null;
		}
	}

	public void onPluginMessage(PluginMessage message)
	{
		if (message == null || !NAMESPACE.equals(message.getNamespace()))
		{
			return;
		}
		if (!MSG_SETUPS_CHANGED.equals(message.getName()))
		{
			return;
		}
		Object raw = message.getData() == null ? null : message.getData().get(DATA_SETUPS);
		if (!(raw instanceof Collection))
		{
			return;
		}
		List<String> names = new ArrayList<>();
		for (Object item : (Collection<?>) raw)
		{
			if (item instanceof String)
			{
				names.add((String) item);
			}
		}
		available = true;
		setupNames = Collections.unmodifiableList(names);
	}

	private static final class ResponseCollection extends ArrayList<String>
	{
		private boolean answered;

		@Override
		public boolean add(String value)
		{
			answered = true;
			return super.add(value);
		}

		@Override
		public boolean addAll(Collection<? extends String> collection)
		{
			answered = true;
			return super.addAll(collection);
		}
	}
}
