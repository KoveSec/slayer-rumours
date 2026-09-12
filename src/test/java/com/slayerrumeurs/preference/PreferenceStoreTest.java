package com.slayerrumeurs.preference;

import com.google.gson.Gson;
import com.slayerrumeurs.SlayerRumoursConfig;
import net.runelite.client.config.ConfigManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PreferenceStoreTest
{
	@Mock
	private ConfigManager configManager;

	private PreferenceStore store;

	@Before
	public void setUp()
	{
		store = new PreferenceStore(configManager, new Gson());
	}

	@Test
	public void persistsPreferredLocationUnderPrefLocPrefix()
	{
		store.setPreferredLocationId("abyssal-demons", "catacombs");

		verify(configManager).setConfiguration(
			SlayerRumoursConfig.GROUP,
			"pref.loc.abyssal-demons",
			"catacombs");
	}

	@Test
	public void readsStoredLocation()
	{
		when(configManager.getConfiguration(SlayerRumoursConfig.GROUP, "pref.loc.wyrms"))
			.thenReturn("karuulm");
		assertEquals("karuulm", store.getPreferredLocationId("wyrms"));
	}

	@Test
	public void persistsSetupMapAsJson()
	{
		when(configManager.getConfiguration(SlayerRumoursConfig.GROUP, PreferenceStore.SETUP_MAP_KEY))
			.thenReturn(null);

		store.setSetupName("gargoyles", "Gargs max");

		ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
		verify(configManager).setConfiguration(
			eq(SlayerRumoursConfig.GROUP),
			eq(PreferenceStore.SETUP_MAP_KEY),
			json.capture());
		assertTrue(json.getValue().contains("gargoyles"));
		assertTrue(json.getValue().contains("Gargs max"));
	}

	@Test
	public void readsAndClearsSetupMapping()
	{
		when(configManager.getConfiguration(SlayerRumoursConfig.GROUP, PreferenceStore.SETUP_MAP_KEY))
			.thenReturn("{\"gargoyles\":\"Gargs max\"}");

		assertEquals("Gargs max", store.getSetupName("gargoyles"));
		assertNull(store.getSetupName("wyrms"));

		store.setSetupName("gargoyles", "  ");
		verify(configManager).unsetConfiguration(SlayerRumoursConfig.GROUP, PreferenceStore.SETUP_MAP_KEY);
	}

	@Test
	public void locationKeyMatchesContract()
	{
		assertEquals("pref.loc.nechryael", PreferenceStore.locationKey("nechryael"));
	}
}
