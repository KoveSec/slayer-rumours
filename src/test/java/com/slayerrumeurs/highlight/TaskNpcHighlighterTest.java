package com.slayerrumeurs.highlight;

import com.google.gson.Gson;
import com.slayerrumeurs.SlayerRumoursConfig;
import com.slayerrumeurs.task.ActiveTaskState;
import com.slayerrumeurs.task.TaskCatalog;
import java.awt.Color;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskNpcHighlighterTest
{
	@Mock
	private SlayerRumoursConfig config;
	@Mock
	private NPC npc;
	@Mock
	private NPCComposition composition;

	private ActiveTaskState state;
	private TaskNpcHighlighter highlighter;

	@Before
	public void setUp()
	{
		state = new ActiveTaskState();
		highlighter = new TaskNpcHighlighter(config, new TaskCatalog(new Gson()), state);
		when(config.highlightTaskNpcs()).thenReturn(true);
		when(config.highlightColor()).thenReturn(Color.RED);
		when(config.highlightOutline()).thenReturn(true);
	}

	@Test
	public void matchesCataloguedNpcByIdAndName()
	{
		state.setRawTaskName("Abyssal demons");
		state.setTaskId("abyssal-demons");
		state.setRemaining(10);
		highlighter.rebuildMatchers();

		when(npc.getId()).thenReturn(415);
		assertTrue(highlighter.matches(npc));

		when(npc.getId()).thenReturn(1);
		when(npc.getTransformedComposition()).thenReturn(composition);
		when(composition.getName()).thenReturn("Abyssal demon");
		assertTrue(highlighter.matches(npc));
		assertNotNull(highlighter.apply(npc));
	}

	@Test
	public void ignoresNpcsWhenNoTask()
	{
		highlighter.rebuildMatchers();
		when(npc.getId()).thenReturn(415);
		assertFalse(highlighter.matches(npc));
		assertNull(highlighter.apply(npc));
	}
}
