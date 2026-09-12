package com.slayerrumeurs.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
public class TaskLocation
{
	private String id;
	private String label;
	private int x;
	private int y;
	private int plane;
	private List<String> tags = new ArrayList<>();
	private String hint;

	public WorldPoint toWorldPoint()
	{
		return new WorldPoint(x, y, plane);
	}

	public List<String> getTags()
	{
		return tags == null ? Collections.emptyList() : tags;
	}

	@Override
	public String toString()
	{
		return label != null ? label : id;
	}
}
