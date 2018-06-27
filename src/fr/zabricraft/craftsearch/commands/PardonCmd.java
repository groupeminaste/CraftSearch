/*
 *  Copyright (C) 2018 Groupe MINASTE
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

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import fr.zabricraft.craftsearch.CraftSearch;
import fr.zabricraft.craftsearch.utils.Sanction;

public class PardonCmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("method", "ZabriPlayer:removeSanction()");
			data.put("player_name", args[0]);
			data.put("player_uuid", "");
			data.put("type", Sanction.BAN.toString());
			HashMap<String, String> response = CraftSearch.getInstance().query(data);
			if (response.containsKey("success") && response.get("success").equals("true")) {
				Bukkit.broadcastMessage("§e" + args[0] + " was pardoned by " + sender.getName() + ".");
			} else {
				sender.sendMessage(
						"§cError while banning player, please check CraftSearch Sanction System is correctly configured.");
			}
		} else {
			sender.sendMessage("§c/ban <player> [reason]");
		}
		return true;
	}

}
