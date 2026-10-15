package com.bosshelper;

import java.util.Set;
import net.runelite.api.NPC;

/**
 * Pete Kayer's PvP Challenges. The tables come from watching real fights:
 * ranged lands 2 ticks after the animation whatever the distance, and spells land 1 + (1 + distance) / 3 ticks
 * after it. His melee, and the Voidwaker special, land on the animation tick, so they can't be prayed against
 * in time; resting on Protect from Magic is what covers the special.
 */
public class PeteKayerModule implements BossModule
{
	private static final String NAME = "Pete Kayer";

	private static final Set<Integer> RANGED = Set.of(426, 425, 4230, 9166);
	private static final Set<Integer> MAGIC = Set.of(1979, 420);
	private static final int VOIDWAKER_SPECIAL = 11275;
	private static final Set<Integer> MELEE = Set.of(1658, 1659, 7514, 7644, 1667, 390, 440, 430);

	private static final int RANGED_LAND_TICKS = 2;
	private static final int ATTACK_INTERVAL_TICKS = 5;

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public boolean isBoss(NPC npc)
	{
		return npc != null && NAME.equals(npc.getName());
	}

	@Override
	public boolean isAttackAnimation(int animation)
	{
		return RANGED.contains(animation) || MAGIC.contains(animation) || MELEE.contains(animation)
			|| animation == VOIDWAKER_SPECIAL;
	}

	@Override
	public BossAttack attackFor(int animation, int distanceTiles)
	{
		if (RANGED.contains(animation))
		{
			return new BossAttack(AttackStyle.RANGED, RANGED_LAND_TICKS);
		}
		if (MAGIC.contains(animation))
		{
			return new BossAttack(AttackStyle.MAGIC, spellLandTicks(distanceTiles));
		}
		if (animation == VOIDWAKER_SPECIAL)
		{
			return new BossAttack(AttackStyle.MAGIC, 0);
		}
		return null;
	}

	static int spellLandTicks(int distanceTiles)
	{
		return 1 + (1 + Math.max(distanceTiles, 0)) / 3;
	}

	@Override
	public int getAttackIntervalTicks()
	{
		return ATTACK_INTERVAL_TICKS;
	}

	@Override
	public AttackStyle getRestStyle()
	{
		return AttackStyle.MAGIC;
	}
}
