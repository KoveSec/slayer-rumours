package com.slayerrumeurs.gear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InventorySetupServiceTest
{
	@Mock
	private EventBus eventBus;

	private InventorySetupService service;

	@Before
	public void setUp()
	{
		service = new InventorySetupService(eventBus);
	}

	@Test
	public void refreshFillsSetupsWhenPeerAnswersGetSetups()
	{
		doAnswer((Answer<Void>) invocation ->
		{
			PluginMessage message = invocation.getArgument(0);
			assertEquals("inventory-setups", message.getNamespace());
			assertEquals("get-setups", message.getName());
			@SuppressWarnings("unchecked")
			Collection<String> setups = (Collection<String>) message.getData().get("setups");
			setups.add("Gargs max");
			setups.add("Nechs");
			return null;
		}).when(eventBus).post(org.mockito.ArgumentMatchers.any());

		service.refresh();

		assertTrue(service.isAvailable());
		assertTrue(service.hasSetup("Gargs max"));
		assertFalse(service.hasSetup("Missing"));
		assertEquals(2, service.getSetupNames().size());
	}

	@Test
	public void refreshStaysUnavailableWhenNobodyAnswers()
	{
		service.refresh();
		assertFalse(service.isAvailable());
		assertTrue(service.getSetupNames().isEmpty());
	}

	@Test
	public void viewPostsSetupName()
	{
		service.view("Gargs max");

		ArgumentCaptor<PluginMessage> captor = ArgumentCaptor.forClass(PluginMessage.class);
		verify(eventBus).post(captor.capture());
		PluginMessage message = captor.getValue();
		assertEquals("inventory-setups", message.getNamespace());
		assertEquals("view", message.getName());
		assertEquals("Gargs max", message.getData().get("setup"));
	}

	@Test
	public void appliesSetupsChangedBroadcast()
	{
		service.onPluginMessage(new PluginMessage(
			"inventory-setups",
			"setups-changed",
			Map.of("setups", List.of("A", "B"), "version", 1)));

		assertTrue(service.isAvailable());
		assertTrue(service.hasSetup("A"));
	}
}
