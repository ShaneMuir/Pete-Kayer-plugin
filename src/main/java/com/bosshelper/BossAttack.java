package com.bosshelper;

import lombok.Value;

/** One incoming hit: the style it will hit with and how many game ticks after the animation it lands. */
@Value
public class BossAttack
{
	AttackStyle style;
	int landTicks;
}
