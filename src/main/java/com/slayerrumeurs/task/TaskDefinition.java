package com.slayerrumeurs.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class TaskDefinition
{
	private String id;
	private String displayName;
	private List<String> aliases = new ArrayList<>();
	private List<Integer> npcIds = new ArrayList<>();
	private List<String> npcNamePatterns = new ArrayList<>();
	private String defaultLocationId;
	private List<TaskLocation> locations = new ArrayList<>();

	public List<String> getAliases()
	{
		return aliases == null ? Collections.emptyList() : aliases;
	}

	public List<Integer> getNpcIds()
	{
		return npcIds == null ? Collections.emptyList() : npcIds;
	}

	public List<String> getNpcNamePatterns()
	{
		return npcNamePatterns == null ? Collections.emptyList() : npcNamePatterns;
	}

	public List<TaskLocation> getLocations()
	{
		return locations == null ? Collections.emptyList() : locations;
	}

	public TaskLocation findLocation(String locationId)
	{
		if (locationId == null)
		{
			return null;
		}
		for (TaskLocation location : getLocations())
		{
			if (locationId.equals(location.getId()))
			{
				return location;
			}
		}
		return null;
	}

	public TaskLocation defaultLocation()
	{
		TaskLocation configured = findLocation(defaultLocationId);
		if (configured != null)
		{
			return configured;
		}
		List<TaskLocation> all = getLocations();
		return all.isEmpty() ? null : all.get(0);
	}
}
