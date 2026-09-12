package com.slayerrumeurs.task;

import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TaskCatalogTest
{
	private TaskCatalog catalog;

	@Before
	public void setUp()
	{
		catalog = new TaskCatalog(new Gson());
	}

	@Test
	public void loadsBundledCatalogVersion1()
	{
		assertEquals(1, catalog.getVersion());
		assertTrue("catalog should include ~30 common tasks", catalog.getTasks().size() >= 30);
	}

	@Test
	public void everyTaskHasUniqueIdDefaultLocationAndNpcMatcher()
	{
		Set<String> ids = new HashSet<>();
		for (TaskDefinition task : catalog.getTasks())
		{
			assertTrue(ids.add(task.getId()));
			assertNotNull(task.getDisplayName());
			assertNotNull(task.defaultLocation());
			assertNotNull(task.defaultLocation().toWorldPoint());
			assertTrue(task.getId(), !task.getNpcIds().isEmpty() || !task.getNpcNamePatterns().isEmpty());
		}
	}

	@Test
	public void looksUpKnownTaskById()
	{
		TaskDefinition spectres = catalog.getById("aberrant-spectres");
		assertNotNull(spectres);
		assertEquals("Aberrant spectres", spectres.getDisplayName());
		assertEquals("stronghold-slayer-cave", spectres.defaultLocation().getId());
		assertNotNull(spectres.findLocation("slayer-tower"));
	}
}
