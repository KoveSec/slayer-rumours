package com.slayerrumeurs.task;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskCatalog
{
	public static final String RESOURCE_PATH = "/com/slayerrumeurs/tasks.json";

	private final int version;
	private final List<TaskDefinition> tasks;
	private final Map<String, TaskDefinition> byId;

	@Inject
	public TaskCatalog(Gson gson)
	{
		this(gson, TaskCatalog.class.getResourceAsStream(RESOURCE_PATH));
	}

	TaskCatalog(Gson gson, InputStream inputStream)
	{
		if (inputStream == null)
		{
			throw new IllegalStateException("Missing bundled catalog " + RESOURCE_PATH);
		}

		try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8))
		{
			TaskCatalogFile file = gson.fromJson(reader, TaskCatalogFile.class);
			if (file == null || file.getTasks().isEmpty())
			{
				throw new IllegalStateException("Task catalog is empty");
			}

			version = file.getVersion();
			Map<String, TaskDefinition> indexed = new LinkedHashMap<>();
			for (TaskDefinition task : file.getTasks())
			{
				if (task.getId() == null || task.getId().isEmpty())
				{
					throw new IllegalStateException("Task is missing an id");
				}
				if (indexed.put(task.getId(), task) != null)
				{
					throw new IllegalStateException("Duplicate task id: " + task.getId());
				}
				if (task.defaultLocation() == null)
				{
					throw new IllegalStateException("Task " + task.getId() + " has no locations");
				}
			}
			byId = Collections.unmodifiableMap(indexed);
			tasks = Collections.unmodifiableList(new ArrayList<>(indexed.values()));
		}
		catch (IOException ex)
		{
			throw new IllegalStateException("Unable to read task catalog", ex);
		}
	}

	public int getVersion()
	{
		return version;
	}

	public List<TaskDefinition> getTasks()
	{
		return tasks;
	}

	public TaskDefinition getById(String taskId)
	{
		return byId.get(taskId);
	}
}
