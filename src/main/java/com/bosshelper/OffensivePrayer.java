package com.bosshelper;

import lombok.Getter;
import net.runelite.api.Prayer;
import net.runelite.api.gameval.InterfaceID;

/** The offensive prayers the swapper switches with the weapon, and the prayer book widget for each. */
@Getter
public enum OffensivePrayer
{
	PIETY(Prayer.PIETY, InterfaceID.Prayerbook.PRAYER27),
	RIGOUR(Prayer.RIGOUR, InterfaceID.Prayerbook.PRAYER25),
	AUGURY(Prayer.AUGURY, InterfaceID.Prayerbook.PRAYER28);

	private final Prayer prayer;
	private final int widgetId;

	OffensivePrayer(Prayer prayer, int widgetId)
	{
		this.prayer = prayer;
		this.widgetId = widgetId;
	}
}
