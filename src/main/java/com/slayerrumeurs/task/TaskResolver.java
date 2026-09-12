package com.slayerrumeurs.task;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskResolver
{
	private final Map<String, TaskDefinition> byNormalizedName;

	@Inject
	public TaskResolver(TaskCatalog catalog)
	{
		Map<String, TaskDefinition> index = new LinkedHashMap<>();
		for (TaskDefinition task : catalog.getTasks())
		{
			indexName(index, task.getDisplayName(), task);
			for (String alias : task.getAliases())
			{
				indexName(index, alias, task);
			}
		}
		byNormalizedName = index;
	}

	public TaskDefinition resolve(String rawName)
	{
		if (rawName == null)
		{
			return null;
		}
		String normalized = normalize(rawName);
		if (normalized.isEmpty())
		{
			return null;
		}

		TaskDefinition exact = byNormalizedName.get(normalized);
		if (exact != null)
		{
			return exact;
		}

		String singular = singularize(normalized);
		if (!singular.equals(normalized))
		{
			return byNormalizedName.get(singular);
		}
		return null;
	}

	static String normalize(String raw)
	{
		if (raw == null)
		{
			return "";
		}
		String value = raw.toLowerCase(Locale.ROOT).replace('\u00A0', ' ').trim();
		value = value.replaceAll("[^a-z0-9]+", " ").trim();
		return value.replaceAll("\\s+", " ");
	}

	static String singularize(String normalized)
	{
		if (normalized.endsWith("ies") && normalized.length() > 4)
		{
			return normalized.substring(0, normalized.length() - 3) + "y";
		}
		if (normalized.endsWith("sses") || normalized.endsWith("ss"))
		{
			return normalized;
		}
		if (normalized.endsWith("s") && normalized.length() > 3)
		{
			return normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}

	private static void indexName(Map<String, TaskDefinition> index, String name, TaskDefinition task)
	{
		String normalized = normalize(name);
		if (normalized.isEmpty())
		{
			return;
		}
		index.putIfAbsent(normalized, task);
		String singular = singularize(normalized);
		if (!singular.equals(normalized))
		{
			index.putIfAbsent(singular, task);
		}
	}
}
