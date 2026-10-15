package com.bosshelper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class FightGateTest
{
	@Test
	public void inactiveBeforeAnyAttack()
	{
		FightGate gate = new FightGate();
		gate.update(false, 100);
		assertFalse(gate.isActive(100));
		assertFalse(gate.isActive(5_000));
	}

	@Test
	public void talkingToBossIsNotTheFight()
	{
		FightGate gate = new FightGate();
		for (int tick = 100; tick < 140; tick++)
		{
			gate.update(true, tick);
			assertFalse(gate.isActive(tick));
		}
	}

	@Test
	public void activeOnceTargetedAndAttacking()
	{
		FightGate gate = new FightGate();
		gate.update(true, 100);
		gate.onAttack(100);
		assertTrue(gate.isActive(100));
		gate.update(false, 105);
		assertTrue(gate.isActive(105));
		assertTrue(gate.isActive(100 + FightGate.ENGAGED_GRACE_TICKS));
	}

	@Test
	public void attackingSomethingElseDoesNotStartIt()
	{
		FightGate gate = new FightGate();
		gate.onAttack(100);
		assertFalse(gate.isActive(100));
	}

	@Test
	public void endsWhenAttacksStopAndResets()
	{
		FightGate gate = new FightGate();
		gate.update(true, 100);
		gate.onAttack(100);
		gate.update(true, 112);
		assertFalse(gate.isActive(100 + FightGate.ATTACK_GRACE_TICKS + 1));
		gate.onAttack(200);
		gate.update(true, 200);
		assertTrue(gate.isActive(200));
		gate.reset();
		assertFalse(gate.isActive(200));
	}
}