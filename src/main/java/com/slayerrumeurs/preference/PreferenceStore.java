package com.slayerrumeurs.preference;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.slayerrumeurs.SlayerRumoursConfig;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class PreferenceStore
{
	public static final String LOCATION_KEY_PREFIX = "pref.loc.";
	public static final String SETUP_MAP_KEY = "setup.map";

	private static final Type SETUP_MAP_TYPE = new TypeToken<Map<String, String>>()
	{
	}.getType();

	private final ConfigManager configManager;
	private final Gson gson;

	@Inject
	public PreferenceStore(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	public String getPreferredLocationId(String taskId)
	{
		if (taskId == null)
		{
			return null;
		}
		return configManager.getConfiguration(SlayerRumoursConfig.GROUP, locationKey(taskId));
	}

	public void setPreferredLocationId(String taskId, String locationId)
	{
		if (taskId == null)
		{
			return;
		}
		if (locationId == null || locationId.isEmpty())
		{
			configManager.unsetConfiguration(SlayerRumoursConfig.GROUP, locationKey(taskId));
			return;
		}
		configManager.setConfiguration(SlayerRumoursConfig.GROUP, locationKey(taskId), locationId);
	}

	public String getSetupName(String taskId)
	{
		if (taskId == null)
		{
			return null;
		}
		String mapped = getSetupMap().get(taskId);
		return mapped == null || mapped.isEmpty() ? null : mapped;
	}

	public void setSetupName(String taskId, String setupName)
	{
		if (taskId == null)
		{
			return;
		}
		Map<String, String> map = new LinkedHashMap<>(getSetupMap());
		if (setupName == null || setupName.trim().isEmpty())
		{
			map.remove(taskId);
		}
		else
		{
			map.put(taskId, setupName.trim());
		}
		writeSetupMap(map);
	}

	public Map<String, String> getSetupMap()
	{
		String json = configManager.getConfiguration(SlayerRumoursConfig.GROUP, SETUP_MAP_KEY);
		if (json == null || json.isEmpty())
		{
			return Collections.emptyMap();
		}
		try
		{
			Map<String, String> parsed = gson.fromJson(json, SETUP_MAP_TYPE);
			return parsed == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(parsed));
		}
		catch (RuntimeException ex)
		{
			return Collections.emptyMap();
		}
	}

	public static String locationKey(String taskId)
	{
		return LOCATION_KEY_PREFIX + taskId;
	}

	private void writeSetupMap(Map<String, String> map)
	{
		if (map.isEmpty())
		{
			configManager.unsetConfiguration(SlayerRumoursConfig.GROUP, SETUP_MAP_KEY);
			return;
		}
		configManager.setConfiguration(SlayerRumoursConfig.GROUP, SETUP_MAP_KEY, gson.toJson(map));
	}
}
