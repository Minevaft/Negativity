package com.elikill58.negativity.spigot.protocols;

import static com.elikill58.negativity.spigot.utils.ItemUtils.STATIONARY_WATER;
import static com.elikill58.negativity.spigot.utils.LocationUtils.hasMaterialsAround;
import static com.elikill58.negativity.spigot.utils.LocationUtils.hasOtherThanExtended;
import static com.elikill58.negativity.spigot.utils.LocationUtils.hasOtherThan;
import static com.elikill58.negativity.universal.utils.UniversalUtils.parseInPorcent;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.utils.LocationUtils;
import com.elikill58.negativity.spigot.utils.Utils;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.CheatKeys;
import com.elikill58.negativity.universal.ReportType;

public class JesusProtocol extends Cheat implements Listener {

	public JesusProtocol() {
		super(CheatKeys.JESUS, false, Material.WATER_BUCKET, CheatCategory.MOVEMENT, true, "waterwalk", "water",
				"water walk");
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.getGameMode().equals(GameMode.ADVENTURE))
			return;
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
		if (!np.hasDetectionActive(this))
			return;
		if (np.hasElytra() || p.isInsideVehicle() || Utils.isSwimming(p))
			return;
		Location loc = p.getLocation(), to = e.getTo(), from = e.getFrom();
		if (hasMaterialsAround(loc, "ICE", "TRAPDOOR", "SLAB", "STAIRS", "CARPET", "WATER_LILY", "LILY")
				|| hasMaterialsAround(loc.clone().subtract(0, 1, 0), "ICE", "TRAPDOOR", "SLAB", "STAIRS", "CARPET", "WATER_LILY", "LILY"))
			return;
		Location under = loc.clone().subtract(0, 1, 0);
		Material type = loc.getBlock().getType(), underType = under.getBlock().getType();
		boolean isInWater = type.name().contains("WATER"), isOnWater = underType.name().contains("WATER");
		boolean mayCancel = false;
		int ping = Utils.getPing(p);
		double dif = e.getFrom().getY() - e.getTo().getY();
		if (type.name().equalsIgnoreCase("AIR") && isOnWater && !LocationUtils.hasBoatAroundHim(loc) && !p.isFlying()) {
			if (!hasOtherThanExtended(under, STATIONARY_WATER)) {
				double reliability = 0;
				if (dif < 0.0005 && dif > 0.00000005)
					reliability = dif * 10000000 - 1;
				else if (dif < 0.1 && dif > 0.089)
					reliability = dif * 1000;
				else if (dif == 0.5)
					reliability = 75;
				else if (dif < 0.30001 && dif > 0.3000)
					reliability = dif * 100 * 2.5;
				else if (dif < 0.002 && dif > -0.002 && dif != 0.0)
					reliability = Math.abs(dif * 5000);
				else if (dif == 0.0)
					reliability = 90;
				mayCancel = SpigotNegativity.alertMod(ReportType.WARNING, p, this, parseInPorcent(reliability),
						"Warn for Jesus: " + np.getWarn(this) + " (Stationary_water aroud him) Diff: " + dif
								+ " and ping: " + ping);
			}
		}
		if (dif == -0.5 && (isInWater || isOnWater) && !type.name().contains("SLAB") && !type.name().contains("FENCE") && !LocationUtils.hasMaterialsAround(under, "FENCE")) {
			mayCancel = SpigotNegativity.alertMod(ReportType.WARNING, p, this, parseInPorcent(98), "Warn for Jesus: "
					+ np.getWarn(this) + ", dif: -0.5, isIn: " + isInWater + ", isOn: " + isOnWater + " " + ping + ", type: " + type.name() + ", type Under: " + underType.name());
		}

		boolean jesusState = np.contentBoolean.getOrDefault("jesus-state", false);
		if (dif == np.contentDouble.getOrDefault("jesus-last-y-" + jesusState, 0.0) && isInWater && !np.isInFight) {
			if (!hasOtherThan(under, STATIONARY_WATER) && !Utils.isSwimming(p)) {
				mayCancel = SpigotNegativity.alertMod(np.getWarn(this) > 10 ? ReportType.VIOLATION : ReportType.WARNING,
						p, this, parseInPorcent((dif + 5) * 10),
						"Warn for Jesus: " + np.getWarn(this) + " (Stationary_water aroud him) Difference between 2 y: "
								+ dif + " (other: "
								+ np.contentDouble.getOrDefault("jesus-last-y-" + (!jesusState), 0.0)
								+ ") and ping: " + ping);
			}
		}
		np.contentDouble.put("jesus-last-y-" + jesusState, dif);
		np.contentBoolean.put("jesus-state", !jesusState);

		double distanceAbs = to.distance(from) - Math.abs(from.getY() - to.getY());
		Location upper = loc.clone().add(0, 1, 0);
		float distanceFall = p.getFallDistance();
		if (isInWater && isOnWater && distanceFall < 1 && distanceAbs > p.getWalkSpeed()
				&& !upper.getBlock().isLiquid() && !p.isFlying()) {
			if (!hasMaterialsAround(loc, "WATER_LILY") && !hasMaterialsAround(upper, "WATER_LILY")
					&& !hasOtherThan(under, "WATER")) {
				mayCancel = SpigotNegativity.alertMod(ReportType.WARNING, p, Cheat.forKey(CheatKeys.JESUS), 98,
						"In water, distance: " + distanceAbs + ", ping: " + ping,
						hoverMsg("main", "%distance%", distanceAbs));
			}
		}

		if (isSetBack() && mayCancel)
			p.teleport(p.getLocation().subtract(0, 1, 0));
	}
	
	@Override
	public boolean isBlockedInFight() {
		return true;
	}
}
