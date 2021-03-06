package com.elikill58.negativity.spigot.commands;

import static com.elikill58.negativity.universal.verif.VerificationManager.CONSOLE;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.elikill58.negativity.spigot.Inv;
import com.elikill58.negativity.spigot.Messages;
import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.WorldRegionBypass;
import com.elikill58.negativity.spigot.inventories.AbstractInventory;
import com.elikill58.negativity.spigot.inventories.AbstractInventory.InventoryType;
import com.elikill58.negativity.spigot.support.EssentialsSupport;
import com.elikill58.negativity.spigot.support.GadgetMenuSupport;
import com.elikill58.negativity.spigot.utils.Utils;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.Cheat.CheatCategory;
import com.elikill58.negativity.universal.CheatKeys;
import com.elikill58.negativity.universal.ReportType;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.OldBansDbMigrator;
import com.elikill58.negativity.universal.permissions.Perm;
import com.elikill58.negativity.universal.translation.MessagesUpdater;
import com.elikill58.negativity.universal.utils.UniversalUtils;
import com.elikill58.negativity.universal.verif.VerificationManager;
import com.elikill58.negativity.universal.verif.Verificator;

public class NegativityCommand implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg) {
		if (arg.length == 0 || arg[0].equalsIgnoreCase("help")) {
			Messages.sendMessageList(sender, "negativity.verif.help");
			FileConfiguration conf = SpigotNegativity.getInstance().getConfig();
			if(conf.getBoolean("report_command"))
				Messages.sendMessage(sender, "report.report_usage");
			if(conf.getBoolean("ban_command"))
				Messages.sendMessageList(sender, "ban.help");
			if(conf.getBoolean("unban_command"))
				Messages.sendMessage(sender, "unban.help");
			if(conf.getBoolean("kick_command"))
				Messages.sendMessage(sender, "kick.help");
			if(conf.getBoolean("report_command"))
				Messages.sendMessage(sender, "report.report_usage");
			Messages.sendMessage(sender, "lang.help");
			return true;
		}

		if (arg[0].equalsIgnoreCase("verif")) {
			if (sender instanceof Player && !Perm.hasPerm(SpigotNegativityPlayer.getNegativityPlayer((Player) sender), Perm.VERIF)) {
				Messages.sendMessage(sender, "not_permission");
				return false;
			}

			if (arg.length < 2) {
				Messages.sendMessage(sender, "not_forget_player");
				return true;
			}

			Player target = Bukkit.getPlayer(arg[1]);
			if (target == null) {
				Messages.sendMessage(sender, "invalid_player", "%arg%", arg[1]);
				return false;
			}

			SpigotNegativityPlayer nTarget = SpigotNegativityPlayer.getNegativityPlayer(target);
			int time = UniversalUtils.getFirstInt(arg).orElse(VerificationManager.getTimeVerif() / 20);
			Set<Cheat> cheatsToVerify = new LinkedHashSet<>();
			if (arg.length == 2 || (arg.length == 3 && UniversalUtils.isInteger(arg[2]))) {
				nTarget.startAllAnalyze();
				Messages.sendMessage(sender, "negativity.verif.start_all", "%name%", target.getName(), "%time%", time);
				cheatsToVerify.addAll(Cheat.CHEATS);
			} else {
				StringJoiner cheatNamesJoiner = new StringJoiner(", ");
				for (int i = 2; i < arg.length; i++) {
					Cheat cheat = Cheat.fromString(arg[i]);
					if (cheat != null) {
						nTarget.startAnalyze(cheat);
						cheatNamesJoiner.add(cheat.getName());
						cheatsToVerify.add(cheat);
					}
				}

				String cheatsList = cheatNamesJoiner.toString();
				if (cheatsList.isEmpty()) {
					Messages.sendMessage(sender, "negativity.verif.start_none");
					return false;
				} else {
					Messages.sendMessage(sender, "negativity.verif.start", "%name%", target.getName(), "%cheat%", cheatsList, "%time%", time);
				}
			}
			UUID askerUUID = (sender instanceof Player ? ((Player) sender).getUniqueId() : CONSOLE);
			VerificationManager.create(askerUUID, target.getUniqueId(), new Verificator(nTarget, sender.getName(), cheatsToVerify));
			SpigotNegativity pl = SpigotNegativity.getInstance();
			Bukkit.getScheduler().runTaskLater(pl, () -> {
				Verificator verif = VerificationManager.getVerificationsFrom(target.getUniqueId(), askerUUID).get();
				verif.generateMessage();
				verif.getMessages().forEach((s) -> sender.sendMessage(Utils.coloredMessage("&a[&2Verif&a] " + s)));
				verif.save();
				VerificationManager.remove(askerUUID, target.getUniqueId());
			}, time * 20);
			return true;
		} else if (arg[0].equalsIgnoreCase("alert")) {
			if (!(sender instanceof Player)) {
				Messages.sendMessage(sender, "only_player");
				return true;
			}
			Player playerSender = (Player) sender;
			SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(playerSender);
			np.disableShowingAlert = !np.disableShowingAlert;
			Messages.sendMessage(playerSender, np.disableShowingAlert ? "negativity.see_no_longer_alert" : "negativity.see_alert");
			return true;
		} else if (arg[0].equalsIgnoreCase("reload")) {
			if (sender instanceof Player && !Perm.hasPerm(SpigotNegativityPlayer.getNegativityPlayer((Player) sender), Perm.RELOAD)) {
				Messages.sendMessage(sender, "not_permission");
				return false;
			}
			Adapter.getAdapter().reload();
			Messages.sendMessage(sender, "negativity.reload_done");
			return true;
		} else if (arg[0].equalsIgnoreCase("mod")) {
			if (!(sender instanceof Player)) {
				Messages.sendMessage(sender, "only_player");
				return true;
			}
			Player p = (Player) sender;
			if (!Perm.hasPerm(SpigotNegativityPlayer.getNegativityPlayer(p), Perm.MOD)) {
				Messages.sendMessage(sender, "not_permission");
				return true;
			}
			AbstractInventory.getInventory(InventoryType.MOD).ifPresent((inv) -> inv.openInventory(p));
			return true;
		} else if (arg[0].equalsIgnoreCase("admin") || arg[0].toLowerCase().contains("manage")) {
			if (arg.length >= 2 && arg[1].equalsIgnoreCase("updateMessages")) {
				if (sender instanceof Player && !Perm.hasPerm(SpigotNegativityPlayer.getNegativityPlayer((Player) sender), Perm.MANAGE_CHEAT)) {
					Messages.sendMessage(sender, "not_permission");
					return true;
				}

				MessagesUpdater.performUpdate("lang", (message, placeholders) -> Messages.sendMessage(sender, message, (Object[]) placeholders));
				return true;
			}

			if (!(sender instanceof Player)) {
				Messages.sendMessage(sender, "only_player");
				return true;
			}
			Player p = (Player) sender;
			if (!Perm.hasPerm(SpigotNegativityPlayer.getNegativityPlayer(p), Perm.MANAGE_CHEAT)) {
				Messages.sendMessage(sender, "not_permission");
				return true;
			}

			AbstractInventory.open(InventoryType.ADMIN, p);
			return true;
		} else if (arg[0].equalsIgnoreCase("migrateoldbans") && sender instanceof ConsoleCommandSender) {
			try {
				OldBansDbMigrator.performMigration();
			} catch (Exception e) {
				sender.sendMessage("An error occurred when performing migration: " + e.getMessage());
				e.printStackTrace();
			}
			return true;
		} else if (arg[0].equalsIgnoreCase("debug")) {
			if (!(sender instanceof Player)) {
				Messages.sendMessage(sender, "only_player");
				return true;
			}
			Player p = (Player) sender;
			SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
			p.sendMessage(ChatColor.YELLOW + "--- Checking debug for bypass | no alert ---");
			Adapter ada = Adapter.getAdapter();
			p.sendMessage(ChatColor.GOLD + ada.getName() + ": " + ada.getVersion() + ". Negativity " + SpigotNegativity.getInstance().getDescription().getVersion());
			long time = System.currentTimeMillis();
			boolean hasBypass = false;
			if (np.TIME_INVINCIBILITY > time) {
				p.sendMessage(ChatColor.RED + "Invincibility (stay " + (time - np.TIME_INVINCIBILITY) + "ms)");
				hasBypass = true;
			}
			if (np.isFreeze) {
				p.sendMessage(ChatColor.RED + "You are currently freezed.");
				hasBypass = true;
			}
			double tps = Utils.getLastTPS();
			if(ada.getConfig().getDouble("tps_alert_stop") > tps) {
				p.sendMessage(ChatColor.RED + "Too low TPS : " + tps);
				hasBypass = true;
			}
			if(!(p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE))) {
				p.sendMessage(ChatColor.RED + "Lot of detection are disabled if you're not on survival/adventure.");
				hasBypass = true;
			}
			int ping = Utils.getPing(p);
			if(np.isInFight || ping > 150)
				hasBypass = true;
			if(arg.length > 1) {
				Cheat c = Cheat.fromString(arg[1]);
				if(c != null) {
					p.sendMessage(ChatColor.GREEN + "Checking for cheat " + c.getName() + ".");
					if(!c.isActive()) {
						p.sendMessage(ChatColor.RED + "Cheat disabled.");
						hasBypass = true;
					}
					if(!np.already_blink && c.getKey().equals(CheatKeys.BLINK)) {
						p.sendMessage(ChatColor.RED + "Bypass for blink.");
						hasBypass = true;
					}
					if(WorldRegionBypass.hasBypass(c, p.getLocation())) {
						p.sendMessage(ChatColor.RED + "You have a bypass actually");
						hasBypass = true;
					}
					if (SpigotNegativity.gadgetMenuSupport && c.getCheatCategory().equals(CheatCategory.MOVEMENT) 
							&& GadgetMenuSupport.checkGadgetsMenuPreconditions(p)) {
						p.sendMessage(ChatColor.RED + "GadgetMenu movement bypass.");
						hasBypass = true;
					}
					if (SpigotNegativity.essentialsSupport && c.getKey().equals(CheatKeys.FLY) && p.hasPermission("essentials.fly")
							&& EssentialsSupport.checkEssentialsPrecondition(p)) {
						p.sendMessage(ChatColor.RED + "Essentials fly bypass.");
						hasBypass = true;
					}
					if(np.isInFight && c.isBlockedInFight()) {
						p.sendMessage(ChatColor.RED + "Bypass because your are in fight.");
						hasBypass = true;
					}
					if(ping > c.getMaxAlertPing()) {
						p.sendMessage(ChatColor.RED + "To high ping ! " + ChatColor.YELLOW + "(" + ping + " > " + c.getMaxAlertPing() + ")");
						hasBypass = true;
					}
					if(!np.hasDetectionActive(c)) {
						p.sendMessage(ChatColor.RED + "Detection of " + c.getName() + " not active.");
						hasBypass = true;
					}
					if(!hasBypass)
						SpigotNegativity.alertMod(ReportType.INFO, p, c, 100, "");
				} else
					p.sendMessage(ChatColor.RED + "Unknow cheat " + arg[1] + ".");
			} else
				p.sendMessage(ChatColor.YELLOW + (np.isInFight ? "In fight, " : "") + "Ping: " + ping + "ms (by default, at 150ms you bypass it)");
			p.sendMessage(hasBypass ? ChatColor.RED + "Warn: You have bypass, so you cannot be detected." : ChatColor.GREEN + "Good news: you can be detected !");
			return true;
		}
		
		if(sender instanceof Player && Perm.hasPerm(SpigotNegativityPlayer.getNegativityPlayer((Player) sender), Perm.CHECK)) {
			Player targetPlayer = Bukkit.getPlayer(arg[0]);
			if (targetPlayer != null) {
				Player playerSender = (Player) sender;
				Inv.CHECKING.put(playerSender, targetPlayer);
				AbstractInventory.open(InventoryType.CHECK_MENU, playerSender, targetPlayer);
				return true;
			}
		}

		Messages.sendMessageList(sender, "negativity.verif.help");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] arg) {
		List<String> suggestions = new ArrayList<>();
		String prefix = arg[arg.length - 1].toLowerCase(Locale.ROOT);
		if (arg.length == 1) {
			// /negativity |
			for (Player p : Utils.getOnlinePlayers()) {
				if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase()) || prefix.isEmpty()) {
					suggestions.add(p.getName());
				}
			}
			if ("verif".startsWith(prefix))
				suggestions.add("verif");
			if ("reload".startsWith(prefix))
				suggestions.add("reload");
			if ("alert".startsWith(prefix))
				suggestions.add("alert");
			if ("debug".startsWith(prefix))
				suggestions.add("debug");
			if ("admin".startsWith(prefix) && (sender instanceof Player) && Perm.hasPerm(SpigotNegativityPlayer.getNegativityPlayer((Player) sender), Perm.MANAGE_CHEAT))
				suggestions.add("admin");
		} else {
			if (arg[0].equalsIgnoreCase("verif")) {
				if (arg.length == 2) {
					// /negativity verif |
					for (Player p : Utils.getOnlinePlayers()) {
						if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase()) || prefix.isEmpty()) {
							suggestions.add(p.getName());
						}
					}
				} else if (Bukkit.getPlayer(arg[1]) != null) {
					// /negativity verif <target> |
					for (Cheat c : Cheat.values()) {
						if (c.getName().toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase()) || prefix.isEmpty()) {
							suggestions.add(c.getName());
						}
					}
				}
			} else if (arg[0].equalsIgnoreCase("admin") && arg.length == 2) {
				if (sender instanceof Player && Perm.hasPerm(SpigotNegativityPlayer.getNegativityPlayer((Player) sender), Perm.MANAGE_CHEAT)) {
					suggestions.add("updateMessages");
				}
			}
		}
		return suggestions;
	}
}
