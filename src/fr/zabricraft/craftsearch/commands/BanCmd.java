/*
 *  Copyright (C) 2018 Nathan FALLET, Michaël NASS and Jean-Baptiste EJARQUE
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 */

package fr.zabricraft.craftsearch.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.zabricraft.craftsearch.CraftSearch;
import fr.zabricraft.craftsearch.utils.Sanction;
import fr.zabricraft.craftsearch.utils.ZabriPlayer;

public class BanCmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1) {
			Player p = Bukkit.getPlayer(args[0]);
			if (p != null) {
				ZabriPlayer zp = CraftSearch.getInstance().getPlayer(p.getUniqueId());
				if (zp != null) {
					String reason = "You were banned by an operator.";
					if (args.length > 1) {
						reason = "";
						for (int i = 1; i < args.length; i++) {
							reason += " " + args[i];
						}
						reason = reason.trim();
					}
					if (zp.addSanction(Sanction.BAN, reason, 0)) {
						Bukkit.broadcastMessage(
								"§e" + p.getName() + " was banned by " + sender.getName() + " : " + reason);
					} else {
						sender.sendMessage(
								"§cError while banning player, please check CraftSearch Sanction System is correctly configured.");
					}
				} else {
					sender.sendMessage("§cUnable to find player datas for " + args[0]);
				}
			} else {
				sender.sendMessage("§cUnknown player: " + args[0]);
			}
		}else {
			sender.sendMessage("§c/ban <player> [reason]");
		}
		return true;
	}

}
