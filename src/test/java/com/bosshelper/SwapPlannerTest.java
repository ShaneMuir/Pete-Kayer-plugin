package com.bosshelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.bosshelper.SwapPlanner.Style;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class SwapPlannerTest
{
	private static final List<Style> ORDER = Arrays.asList(Style.RANGED, Style.MELEE, Style.MAGIC);

	private static Style choose(Style current, int protectedIcon, boolean magicAvailable, long now, long last)
	{
		return SwapPlanner.choose(current, protectedIcon, ORDER, s -> s != Style.MAGIC || magicAvailable, now, last, 1_200);
	}

	@Test
	public void staysWhenHeHasNoOverheadPrayer()
	{
		assertNull(choose(Style.MELEE, -1, false, 10_000, 0));
	}

	@Test
	public void leavesAnUnrecognisedWeaponEvenWhenNoPrayerIsUp()
	{
		assertEquals(Style.RANGED, choose(null, -1, false, 10_000, 0));
		assertNull(choose(null, -1, false, 10_500, 10_000));
	}

	@Test
	public void staysWhenHittingAStyleHeIsNotProtecting()
	{
		assertNull(choose(Style.MELEE, Style.RANGED.getIcon(), false, 10_000, 0));
	}

	@Test
	public void swapsOffAStyleOnceHeProtectsIt()
	{
		assertEquals(Style.MELEE, choose(Style.RANGED, Style.RANGED.getIcon(), false, 10_000, 0));
		assertEquals(Style.RANGED, choose(Style.MELEE, Style.MELEE.getIcon(), false, 10_000, 0));
	}

	@Test
	public void respectsTheCooldown()
	{
		assertNull(choose(Style.RANGED, Style.RANGED.getIcon(), false, 10_500, 10_000));
		assertEquals(Style.MELEE, choose(Style.RANGED, Style.RANGED.getIcon(), false, 11_300, 10_000));
	}

	@Test
	public void usesTheOrderAndSkipsUnavailableStyles()
	{
		assertEquals(Style.RANGED, choose(Style.MELEE, Style.MELEE.getIcon(), true, 10_000, 0));
		assertEquals(Style.MELEE, choose(null, Style.RANGED.getIcon(), true, 10_000, 0));
	}

	@Test
	public void parsesOrderAndItems()
	{
		assertEquals(Arrays.asList(Style.MAGIC, Style.RANGED, Style.MELEE), SwapPlanner.parseOrder("magic, ranged, nonsense"));
		assertEquals(Arrays.asList("Zaryte crossbow", "Dragonfire shield"), SwapPlanner.parseItems(" Zaryte crossbow , Dragonfire shield,, "));
		assertEquals(0, SwapPlanner.parseItems("").size());
	}
}