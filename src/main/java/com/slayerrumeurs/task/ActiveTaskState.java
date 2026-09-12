package com.slayerrumeurs.task;

import javax.inject.Singleton;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@Singleton
public class ActiveTaskState
{
	private String taskId;
	private String rawTaskName;
	private int remaining;
	private String preferredLocationId;
	private WorldPoint preferredPoint;
	private String activeSetupName;
	private boolean pathOwnedByUs;

	public boolean hasTask()
	{
		return remaining > 0 && rawTaskName != null && !rawTaskName.isEmpty();
	}

	public void clear()
	{
		taskId = null;
		rawTaskName = null;
		remaining = 0;
		preferredLocationId = null;
		preferredPoint = null;
		activeSetupName = null;
		pathOwnedByUs = false;
	}

	public static ActiveTaskState none()
	{
		return new ActiveTaskState();
	}
}
