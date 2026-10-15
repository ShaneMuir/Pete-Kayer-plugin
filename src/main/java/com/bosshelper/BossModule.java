package com.bosshelper;

import net.runelite.api.NPC;

/**
 * Everything the helper needs to know about one boss: how to recognise it, which animations are attacks,
 * what each attack hits with and when it lands, and what to pray on between attacks.
 */
public interface BossModule
{
	String getName();

	boolean isBoss(NPC npc);

	/** Any attack animation at all (including ones that can't be reacted to), used for the cadence timer. */
	boolean isAttackAnimation(int animation);

	/**
	 * The hit coming from this animation when the boss is {@code distanceTiles} from the player, or null if
	 * the animation is an attack that can't be prayed against in time (or isn't an attack).
	 */
	BossAttack attackFor(int animation, int distanceTiles);

	/** Game ticks between the boss's attacks. */
	int getAttackIntervalTicks();

	/** The prayer to sit on while no hit is incoming, or null to leave the prayers alone. */
	AttackStyle getRestStyle();
}
