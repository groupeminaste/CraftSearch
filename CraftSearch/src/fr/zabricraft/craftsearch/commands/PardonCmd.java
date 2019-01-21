/*
 *  Copyright (C) 2019 Groupe MINASTE
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
import java.util.Map;

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
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("method", "Plugin:removePlayerSanction()");
			data.put("name", args[0]);
			data.put("uuid", "");
			data.put("type", Sanction.BAN.toString());
			Map<String, Object> response = CraftSearch.getInstance().query(data);
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
