package com.slayerrumeurs.task;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SlayerChatParserTest
{
	@Test
	public void parsesNewTaskWithCount()
	{
		SlayerChatParser.Result result = SlayerChatParser.parse("Your new task is to kill 142 Aberrant spectres.");
		assertEquals(SlayerChatParser.Kind.ASSIGNED, result.getKind());
		assertEquals("Aberrant spectres", result.getRawTaskName());
		assertEquals(Integer.valueOf(142), result.getRemaining());
	}

	@Test
	public void parsesAssignedAndCurrentlyAssigned()
	{
		SlayerChatParser.Result assigned = SlayerChatParser.parse(
			"You're assigned to kill 87 abyssal demons; only 87 more to go.");
		assertEquals("abyssal demons", assigned.getRawTaskName());
		assertEquals(Integer.valueOf(87), assigned.getRemaining());

		SlayerChatParser.Result current = SlayerChatParser.parse(
			"You're currently assigned to kill Dust devils; only 12 more to go.");
		assertEquals("Dust devils", current.getRawTaskName());
		assertEquals(Integer.valueOf(12), current.getRemaining());
	}

	@Test
	public void parsesKonarBalanceTask()
	{
		SlayerChatParser.Result result = SlayerChatParser.parse(
			"You are to bring balance to 150 Fire giants in the Catacombs of Kourend.");
		assertEquals(SlayerChatParser.Kind.ASSIGNED, result.getKind());
		assertEquals("Fire giants", result.getRawTaskName());
		assertEquals(Integer.valueOf(150), result.getRemaining());
		assertEquals("Catacombs of Kourend", result.getLocationHint());
	}

	@Test
	public void parsesCompletion()
	{
		assertEquals(SlayerChatParser.Kind.COMPLETED,
			SlayerChatParser.parse("You've completed your task; return to a Slayer master.").getKind());
		assertEquals(SlayerChatParser.Kind.COMPLETED,
			SlayerChatParser.parse("Your Slayer task has been reset.").getKind());
	}

	@Test
	public void ignoresUnrelatedChat()
	{
		assertEquals(SlayerChatParser.Kind.NONE, SlayerChatParser.parse("Welcome to RuneScape.").getKind());
		assertEquals(SlayerChatParser.Kind.NONE, SlayerChatParser.parse(null).getKind());
		assertNull(SlayerChatParser.parse("Welcome to RuneScape.").getRawTaskName());
	}
}
