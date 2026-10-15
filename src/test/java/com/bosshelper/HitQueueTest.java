package com.bosshelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class HitQueueTest
{
	@Test
	public void idleQueueHasNothingPending()
	{
		assertNull(new HitQueue().next(0));
	}

	@Test
	public void spellLandTicksFollowDistance()
	{
		assertEquals(1, PeteKayerModule.spellLandTicks(1));
		assertEquals(2, PeteKayerModule.spellLandTicks(2));
		assertEquals(2, PeteKayerModule.spellLandTicks(4));
		assertEquals(3, PeteKayerModule.spellLandTicks(5));
	}

	@Test
	public void rangedThenCloseMagicAreCoveredInOrder()
	{
		HitQueue q = new HitQueue();
		q.add(AttackStyle.RANGED, 12);
		q.add(AttackStyle.MAGIC, 16);
		assertEquals(AttackStyle.RANGED, q.next(10));
		assertEquals(AttackStyle.MAGIC, q.next(12));
		assertNull(q.next(16));
	}

	@Test
	public void sameTickHitsGoToMagic()
	{
		HitQueue q = new HitQueue();
		q.add(AttackStyle.RANGED, 12);
		q.add(AttackStyle.MAGIC, 12);
		assertEquals(AttackStyle.MAGIC, q.next(10));
	}

	@Test
	public void pendingListsOnlyHitsStillToLand()
	{
		HitQueue q = new HitQueue();
		q.add(AttackStyle.MAGIC, 11);
		q.add(AttackStyle.RANGED, 13);
		assertEquals(2, q.pending(10).size());
		assertEquals(1, q.pending(11).size());
		assertEquals(AttackStyle.RANGED, q.pending(11).get(0).getStyle());
	}

	@Test
	public void peteAttacksMapToStylesAndLandTicks()
	{
		PeteKayerModule pete = new PeteKayerModule();
		assertEquals(2, pete.attackFor(9166, 3).getLandTicks());
		assertEquals(AttackStyle.RANGED, pete.attackFor(9166, 3).getStyle());
		assertEquals(AttackStyle.MAGIC, pete.attackFor(1979, 1).getStyle());
		assertEquals(0, pete.attackFor(11275, 1).getLandTicks());
		assertNull(pete.attackFor(440, 1));
	}
}