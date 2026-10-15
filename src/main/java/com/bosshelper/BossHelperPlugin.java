package com.bosshelper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PostMenuSort;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

/**
 * Pete Kayer's PvP Challenges, ported from the Microbot plugin to plain RuneLite: protection prayers read from
 * his attacks, weapon swaps picked from his overhead prayer, tank gear between swings, a Voidwaker special
 * assist, and tick timers. All input is sent as menu actions, never the mouse.
 */
@Slf4j
@PluginDescriptor(
	name = "Pete Kayer Helper",
	description = "Pete Kayer's challenges: attack timers, prayer panel, auto prayer, weapon swaps and special assist",
	tags = {"pete kayer", "prayer", "pvp", "ferox", "challenges", "timer"}
)
public class BossHelperPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BossHelperConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BossOverlay bossOverlay;

	@Inject
	private PrayerPanelOverlay panelOverlay;

	@Getter
	private final BossModule module = new PeteKayerModule();

	@Getter
	private final HitQueue queue = new HitQueue();

	private final FightGate gate = new FightGate();

	private PrayerController prayers;
	private GearSwapper swapper;
	private boolean wasFightActive;

	@Getter
	private NPC boss;

	/** Tick of the boss's last attack animation, for the cadence timer. */
	@Getter
	private int lastBossAttackTick = -1000;

	/** The protection prayer wanted right now, for the panel; null when nothing is wanted. */
	@Getter
	private AttackStyle desired;

	@Override
	protected void startUp()
	{
		prayers = new PrayerController(client);
		swapper = new GearSwapper(client);
		overlayManager.add(bossOverlay);
		overlayManager.add(panelOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(bossOverlay);
		overlayManager.remove(panelOverlay);
		gate.reset();
		reset();
	}

	@Provides
	BossHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BossHelperConfig.class);
	}

	/** True once the player has attacked Pete (or he has attacked the player) and the attacks haven't stopped. */
	public boolean isFightActive()
	{
		return gate.isActive(client.getTickCount());
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (module.isBoss(event.getNpc()))
		{
			boss = event.getNpc();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc() == boss)
		{
			boss = null;
			queue.clear();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			gate.reset();
			reset();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Actor actor = event.getActor();
		if (client.getLocalPlayer() == null)
		{
			return;
		}
		int tick = client.getTickCount();

		if (actor == client.getLocalPlayer())
		{
			if (GearSwapper.isAttackAnimation(actor.getAnimation()))
			{
				gate.onAttack(tick);
			}
			if (swapper != null && config.autoSwap())
			{
				swapper.onPlayerAnimation(actor.getAnimation());
			}
			return;
		}

		if (!(actor instanceof NPC))
		{
			return;
		}
		NPC npc = (NPC) actor;
		if (!module.isBoss(npc) || npc.getInteracting() != client.getLocalPlayer())
		{
			return;
		}
		boss = npc;

		int animation = npc.getAnimation();
		if (module.isAttackAnimation(animation))
		{
			lastBossAttackTick = tick;
			gate.onAttack(tick);
		}
		int distance = npc.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation());
		BossAttack attack = module.attackFor(animation, distance);
		if (attack != null)
		{
			queue.add(attack.getStyle(), tick + attack.getLandTicks());
		}
		// React straight away: a click sent now is in effect for hits landing next tick.
		refreshPrayer();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (swapper != null && event.getMessage() != null)
		{
			swapper.onGameMessage(event.getMessage());
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		int tick = client.getTickCount();

		// Nothing but the reactive protection prayer happens until the player has started the fight by attacking
		// Pete: talking to him targets him just like attacking does, so targeting alone isn't enough.
		boolean engaged = boss != null && client.getLocalPlayer() != null
			&& client.getLocalPlayer().getInteracting() == boss;
		gate.update(engaged, tick);
		boolean fightActive = gate.isActive(tick);

		refreshPrayer();

		if (wasFightActive && !fightActive && swapper != null && config.autoSwap())
		{
			swapper.onFightEnded(config);
		}
		wasFightActive = fightActive;

		if (swapper == null)
		{
			return;
		}
		if (!fightActive)
		{
			swapper.reset();
			return;
		}
		if (config.autoSwap())
		{
			if (boss != null)
			{
				swapper.onGameTick(boss, config);
			}
			else
			{
				swapper.onPeteMissing();
			}
		}
	}

	/**
	 * With the staff in hand, the left-click on Pete becomes "Cast Ice Barrage" instead of a staff bash: a menu
	 * entry added at the tail, which is the left-click. It sends the two clicks of a manual cast directly.
	 */
	@Subscribe
	public void onPostMenuSort(PostMenuSort event)
	{
		if (swapper == null || !config.clickToCast() || client.isMenuOpen() || !swapper.castWindowOpen(config))
		{
			return;
		}
		Menu menu = client.getMenu();
		MenuEntry[] entries = menu.getMenuEntries();
		for (int i = entries.length - 1; i >= 0; i--)
		{
			MenuEntry entry = entries[i];
			if (!"Attack".equals(entry.getOption()) || entry.getNpc() == null || !module.isBoss(entry.getNpc()))
			{
				continue;
			}
			final NPC pete = entry.getNpc();
			MenuEntry cast = menu.createMenuEntry(-1)
				.setOption("Cast Ice Barrage")
				.setTarget(entry.getTarget())
				.setType(MenuAction.RUNELITE)
				.setIdentifier(entry.getIdentifier())
				.setParam0(entry.getParam0())
				.setParam1(entry.getParam1())
				.onClick(e -> swapper.castFromClick(pete));
			cast.setWorldViewId(entry.getWorldViewId());
			return;
		}
	}

	private void refreshPrayer()
	{
		int tick = client.getTickCount();
		AttackStyle incoming = queue.next(tick);
		desired = incoming != null
			? incoming
			: (config.restOnMagic() && isFightActive() ? module.getRestStyle() : null);
		if (config.autoPrayer() && prayers != null)
		{
			prayers.ensure(desired);
		}
	}

	private void reset()
	{
		boss = null;
		desired = null;
		lastBossAttackTick = -1000;
		wasFightActive = false;
		queue.clear();
		if (swapper != null)
		{
			swapper.reset();
		}
	}
}
