package com.slayerrumeurs.task;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TaskResolverTest
{
	private TaskResolver resolver;

	@Before
	public void setUp()
	{
		resolver = new TaskResolver(new TaskCatalog(new Gson()));
	}

	@Test
	public void resolvesDisplayNameAndAliases()
	{
		assertEquals("abyssal-demons", resolver.resolve("Abyssal demons").getId());
		assertEquals("abyssal-demons", resolver.resolve("abby demons").getId());
		assertEquals("abyssal-demons", resolver.resolve("ABYSSAL DEMON").getId());
	}

	@Test
	public void singularizesPluralAssignmentNames()
	{
		assertEquals("fire-giants", resolver.resolve("Fire giants").getId());
		assertEquals("fire-giants", resolver.resolve("fire giant").getId());
		assertEquals("dust-devils", resolver.resolve("Dust devil").getId());
	}

	@Test
	public void ignoresPunctuationAndNbsp()
	{
		assertEquals("cave-horrors", resolver.resolve("Cave\u00A0horrors!").getId());
		assertEquals("nechryael", resolver.resolve("greater nechryael").getId());
	}

	@Test
	public void returnsNullForUnknownTasks()
	{
		assertNull(resolver.resolve(""));
		assertNull(resolver.resolve(null));
		assertNull(resolver.resolve("TzHaar-Ket-Zek"));
	}

	@Test
	public void normalizeStripsNoise()
	{
		assertEquals("aberrant spectres", TaskResolver.normalize("  Aberrant  SPECTRES. "));
		assertEquals("aberrant spectre", TaskResolver.singularize("aberrant spectres"));
	}

	@Test
	public void resolvesEveryCataloguedDisplayName()
	{
		TaskCatalog catalog = new TaskCatalog(new Gson());
		for (TaskDefinition task : catalog.getTasks())
		{
			TaskDefinition resolved = resolver.resolve(task.getDisplayName());
			assertNotNull(task.getDisplayName(), resolved);
			assertEquals(task.getId(), resolved.getId());
		}
	}
}
