package com.bosshelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

/** Draws the boss's next-attack countdown and every incoming hit above the boss. */
public class BossOverlay extends Overlay
{
	private static final int LINE_HEIGHT = 14;

	private final Client client;
	private final BossHelperPlugin plugin;
	private final BossHelperConfig config;

	@Inject
	private BossOverlay(Client client, BossHelperPlugin plugin, BossHelperConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		NPC boss = plugin.getBoss();
		if (!config.showTimers() || boss == null || !plugin.isFightActive())
		{
			return null;
		}
		int tick = client.getTickCount();

		int untilNext = plugin.getLastBossAttackTick() + plugin.getModule().getAttackIntervalTicks() - tick;
		String headline = untilNext > 0 ? "Attack in " + untilNext : "Attack now";
		Point base = boss.getCanvasTextLocation(graphics, headline, boss.getLogicalHeight() + 40);
		if (base == null)
		{
			return null;
		}
		OverlayUtil.renderTextLocation(graphics, base, headline, untilNext > 0 ? Color.WHITE : Color.ORANGE);

		List<HitQueue.Hit> pending = plugin.getQueue().pending(tick);
		for (int i = 0; i < pending.size(); i++)
		{
			HitQueue.Hit hit = pending.get(i);
			String text = hit.getStyle().getLabel() + " lands in " + (hit.getLandTick() - tick);
			Point at = new Point(base.getX(), base.getY() + LINE_HEIGHT * (i + 1));
			OverlayUtil.renderTextLocation(graphics, at, text, colourOf(hit.getStyle()));
		}
		return null;
	}

	static Color colourOf(AttackStyle style)
	{
		switch (style)
		{
			case MAGIC:
				return new Color(80, 140, 255);
			case RANGED:
				return new Color(70, 200, 90);
			default:
				return new Color(230, 70, 70);
		}
	}
}
