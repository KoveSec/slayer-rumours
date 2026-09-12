package com.slayerrumeurs.navigation;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ShortestPathServiceTest
{
	@Mock
	private EventBus eventBus;

	private ShortestPathService service;

	@Before
	public void setUp()
	{
		service = new ShortestPathService(eventBus);
	}

	@Test
	public void postsPathWithWorldPointTargetAndTracksOwnership()
	{
		WorldPoint start = new WorldPoint(3222, 3218, 0);
		WorldPoint target = new WorldPoint(2441, 9776, 0);

		service.pathTo(start, target);

		ArgumentCaptor<PluginMessage> captor = ArgumentCaptor.forClass(PluginMessage.class);
		verify(eventBus).post(captor.capture());
		PluginMessage message = captor.getValue();
		assertEquals("shortestpath", message.getNamespace());
		assertEquals("path", message.getName());
		assertEquals(target, message.getData().get("target"));
		assertEquals(start, message.getData().get("start"));
		assertTrue(service.isPathOwnedByUs());
	}

	@Test
	public void onlyClearsPathsItOwns()
	{
		service.clearIfOwned();
		verify(eventBus, never()).post(org.mockito.ArgumentMatchers.any());

		service.pathTo(null, new WorldPoint(1, 1, 0));
		service.clearIfOwned();

		ArgumentCaptor<PluginMessage> captor = ArgumentCaptor.forClass(PluginMessage.class);
		verify(eventBus, times(2)).post(captor.capture());
		PluginMessage clear = captor.getAllValues().get(1);
		assertEquals("shortestpath", clear.getNamespace());
		assertEquals("clear", clear.getName());
		assertFalse(service.isPathOwnedByUs());
	}

	@Test
	public void doesNotRepostIdenticalOwnedTarget()
	{
		WorldPoint target = new WorldPoint(10, 10, 0);
		service.pathTo(null, target);
		service.pathTo(null, target);
		verify(eventBus, times(1)).post(org.mockito.ArgumentMatchers.any());
	}
}
