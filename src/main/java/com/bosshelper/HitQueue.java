package com.bosshelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Value;

/**
 * The hits that have been wound up but haven't landed yet, by game tick. At any tick it says which
 * protection prayer the next server tick needs, so overlapping attacks are covered one after another.
 *
 * <p>A click sent during client tick {@code t} is in effect for hits landing on tick {@code t + 1}, so the
 * prayer that matters at tick {@code t} is the one for the earliest hit landing after {@code t}.
 */
public class HitQueue
{
	@Value
	public static class Hit
	{
		AttackStyle style;
		int landTick;
	}

	private final List<Hit> hits = new ArrayList<>();

	public void add(AttackStyle style, int landTick)
	{
		hits.add(new Hit(style, landTick));
	}

	/** The style to be praying during tick {@code tick}, for the next server tick, or null if nothing is pending. */
	public AttackStyle next(int tick)
	{
		hits.removeIf(h -> h.getLandTick() <= tick);
		Hit first = null;
		for (Hit h : hits)
		{
			if (first == null || h.getLandTick() < first.getLandTick())
			{
				first = h;
			}
		}
		if (first == null)
		{
			return null;
		}
		AttackStyle best = first.getStyle();
		for (Hit h : hits)
		{
			if (h.getLandTick() == first.getLandTick() && h.getStyle().getPriority() > best.getPriority())
			{
				best = h.getStyle();
			}
		}
		return best;
	}

	/** The hits still to land after {@code tick}, soonest first, for the overlay. */
	public List<Hit> pending(int tick)
	{
		List<Hit> out = new ArrayList<>();
		for (Hit h : hits)
		{
			if (h.getLandTick() > tick)
			{
				out.add(h);
			}
		}
		out.sort(Comparator.comparingInt(Hit::getLandTick));
		return out;
	}

	public void clear()
	{
		hits.clear();
	}
}
