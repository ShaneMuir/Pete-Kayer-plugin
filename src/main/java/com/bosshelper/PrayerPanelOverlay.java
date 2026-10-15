package com.bosshelper;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/** A small panel saying which protection prayer should be on right now. */
public class PrayerPanelOverlay extends OverlayPanel
{
	private final BossHelperPlugin plugin;
	private final BossHelperConfig config;

	@Inject
	private PrayerPanelOverlay(BossHelperPlugin plugin, BossHelperConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showPrayerPanel() || plugin.getBoss() == null)
		{
			return null;
		}
		AttackStyle desired = plugin.getDesired();
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(plugin.getModule().getName())
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Pray")
			.right(desired == null ? "-" : desired.getLabel())
			.rightColor(desired == null ? java.awt.Color.GRAY : BossOverlay.colourOf(desired))
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Auto prayer")
			.right(config.autoPrayer() ? "ON" : "off")
			.build());
		return super.render(graphics);
	}
}
