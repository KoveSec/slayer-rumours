package com.slayerrumeurs.ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;

public class WorldMapTaskPoint extends WorldMapPoint
{
	private final BufferedImage image;
	private final Point imagePoint;

	public WorldMapTaskPoint(WorldPoint worldPoint, BufferedImage icon, String name)
	{
		super(worldPoint, null);

		int width = Math.max(16, icon == null ? 16 : icon.getWidth() + 4);
		int height = Math.max(16, icon == null ? 16 : icon.getHeight() + 8);
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		if (icon != null)
		{
			graphics.drawImage(icon, (width - icon.getWidth()) / 2, 0, null);
		}
		graphics.dispose();

		imagePoint = new Point(image.getWidth() / 2, image.getHeight());
		setSnapToEdge(true);
		setJumpOnClick(true);
		setName(name);
		setImage(image);
		setImagePoint(imagePoint);
	}

	@Override
	public void onEdgeSnap()
	{
		setImage(image);
		setImagePoint(null);
	}

	@Override
	public void onEdgeUnsnap()
	{
		setImage(image);
		setImagePoint(imagePoint);
	}
}
