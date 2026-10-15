package com.bosshelper;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;

/**
 * Switches protection prayers on with a menu action on the prayer book widget - no mouse involved. It only
 * ever clicks a prayer that is off (clicking an active one would turn it off), and sends at most one click
 * per prayer per game tick, so a click that gets dropped is simply tried again on the next tick.
 */
public class PrayerController
{
	private final Client client;
	private int lastSendTick = -1;
	private AttackStyle lastSendStyle;

	public PrayerController(Client client)
	{
		this.client = client;
	}

	/** @return true if a click was sent. Must be called on the client thread. */
	public boolean ensure(AttackStyle style)
	{
		if (style == null || client.isPrayerActive(style.getPrayer()))
		{
			return false;
		}
		if (client.getBoostedSkillLevel(Skill.PRAYER) <= 0)
		{
			return false;
		}
		int tick = client.getTickCount();
		if (tick == lastSendTick && style == lastSendStyle)
		{
			return false;
		}
		lastSendTick = tick;
		lastSendStyle = style;
		client.menuAction(-1, style.getWidgetId(), MenuAction.CC_OP, 1, -1, "Activate", "Activate");
		return true;
	}
}
