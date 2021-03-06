package com.elikill58.negativity.spigot.protocols;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.utils.ItemUtils;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.CheatKeys;
import com.elikill58.negativity.universal.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

public class FastStairsProtocol extends Cheat implements Listener {

	public FastStairsProtocol() {
		super(CheatKeys.FAST_STAIRS, false, ItemUtils.BIRCH_WOOD_STAIRS, CheatCategory.MOVEMENT, true, "stairs");
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
		if(!np.hasDetectionActive(this))
			return;
		if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.getGameMode().equals(GameMode.ADVENTURE))
			return;
		if(p.getFallDistance() != 0)
			return;
		String blockName = e.getTo().clone().subtract(0, 0.0001, 0).getBlock().getType().name();
		if(!blockName.contains("STAIRS") || p.hasPotionEffect(PotionEffectType.SPEED))
			return;
		Location from = e.getFrom().clone();
		from.setY(e.getTo().getY());
		double distance = from.distance(e.getTo()), lastDistance = np.contentDouble.getOrDefault("stairs-distance", 0.0);
		if(distance > 0.45 && lastDistance > distance) {
			boolean mayCancel = SpigotNegativity.alertMod(ReportType.WARNING, p, this, UniversalUtils.parseInPorcent(distance * 140),
					"No fall damage. Block: " + blockName + ", distance: " + distance + ", lastDistance: " + lastDistance,
					hoverMsg("main", "%distance%", String.format("%.2f", distance)));
			if(mayCancel && isSetBack())
				e.setCancelled(true);
		}
		np.contentDouble.put("stairs-distance", distance);
	}
}
