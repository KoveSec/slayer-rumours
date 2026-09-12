package com.slayerrumeurs.highlight;

import com.slayerrumeurs.SlayerRumoursConfig;
import com.slayerrumeurs.task.ActiveTaskState;
import com.slayerrumeurs.task.TaskCatalog;
import com.slayerrumeurs.task.TaskDefinition;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.client.game.npcoverlay.HighlightedNpc;
import net.runelite.client.util.ColorUtil;

@Singleton
public class TaskNpcHighlighter implements Function<NPC, HighlightedNpc>
{
	private final SlayerRumoursConfig config;
	private final TaskCatalog catalog;
	private final ActiveTaskState state;

	private volatile Set<Integer> npcIds = Collections.emptySet();
	private volatile List<Pattern> namePatterns = Collections.emptyList();

	@Inject
	public TaskNpcHighlighter(SlayerRumoursConfig config, TaskCatalog catalog, ActiveTaskState state)
	{
		this.config = config;
		this.catalog = catalog;
		this.state = state;
	}

	public void rebuildMatchers()
	{
		if (!state.hasTask() || state.getTaskId() == null)
		{
			npcIds = Collections.emptySet();
			namePatterns = Collections.emptyList();
			return;
		}

		TaskDefinition task = catalog.getById(state.getTaskId());
		if (task == null)
		{
			npcIds = Collections.emptySet();
			namePatterns = Collections.emptyList();
			return;
		}

		npcIds = Collections.unmodifiableSet(new HashSet<>(task.getNpcIds()));
		List<Pattern> compiled = new ArrayList<>();
		for (String snippet : task.getNpcNamePatterns())
		{
			if (snippet == null || snippet.isEmpty())
			{
				continue;
			}
			compiled.add(Pattern.compile("(?:\\s|^)" + Pattern.quote(snippet) + "s?(?:\\s|$)", Pattern.CASE_INSENSITIVE));
		}
		namePatterns = Collections.unmodifiableList(compiled);
	}

	@Override
	public HighlightedNpc apply(NPC npc)
	{
		if (!config.highlightTaskNpcs() || !state.hasTask() || npc == null)
		{
			return null;
		}
		if (!matches(npc))
		{
			return null;
		}

		Color color = config.highlightColor();
		return HighlightedNpc.builder()
			.npc(npc)
			.highlightColor(color)
			.fillColor(ColorUtil.colorWithAlpha(color, Math.max(8, color.getAlpha() / 12)))
			.hull(config.highlightHull())
			.tile(config.highlightTile())
			.outline(config.highlightOutline())
			.borderWidth(2)
			.build();
	}

	boolean matches(NPC npc)
	{
		if (npcIds.contains(npc.getId()))
		{
			return true;
		}

		NPCComposition composition = npc.getTransformedComposition();
		String name = composition != null ? composition.getName() : npc.getName();
		if (name == null)
		{
			return false;
		}
		name = name.replace('\u00A0', ' ');
		for (Pattern pattern : namePatterns)
		{
			if (pattern.matcher(name).find())
			{
				return true;
			}
		}
		return false;
	}
}
