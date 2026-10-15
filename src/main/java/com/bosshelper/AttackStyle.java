package com.bosshelper;

import lombok.Getter;
import net.runelite.api.Prayer;
import net.runelite.api.gameval.InterfaceID;

/**
 * The three attack styles a boss can hit with, the protection prayer that stops each one, and the prayer
 * book widget that switches that prayer on (the target of the menu action).
 */
@Getter
public enum AttackStyle
{
	MELEE("Melee", Prayer.PROTECT_FROM_MELEE, InterfaceID.Prayerbook.PRAYER15, 0),
	RANGED("Ranged", Prayer.PROTECT_FROM_MISSILES, InterfaceID.Prayerbook.PRAYER14, 1),
	MAGIC("Magic", Prayer.PROTECT_FROM_MAGIC, InterfaceID.Prayerbook.PRAYER13, 2);

	private final String label;
	private final Prayer prayer;
	private final int widgetId;
	/** Which style wins when two hits land on the same tick and only one prayer can cover them. */
	private final int priority;

	AttackStyle(String label, Prayer prayer, int widgetId, int priority)
	{
		this.label = label;
		this.prayer = prayer;
		this.widgetId = widgetId;
		this.priority = priority;
	}
}
