package com.slayerrumeurs.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
class TaskCatalogFile
{
	private int version;
	private List<TaskDefinition> tasks = new ArrayList<>();

	public List<TaskDefinition> getTasks()
	{
		return tasks == null ? Collections.emptyList() : tasks;
	}
}
