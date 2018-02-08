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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.zabricraft.craftsearch.CraftSearch;
import fr.zabricraft.craftsearch.utils.InventoryManager;
import fr.zabricraft.craftsearch.utils.Translater;
import fr.zabricraft.craftsearch.utils.ZabriPlayer;

public class SearchCmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ZabriPlayer zp = CraftSearch.getInstance().getPlayer(p.getUniqueId());
			if (args.length > 0) {
				String search = "";
				for (String a : args) {
					search += (search.isEmpty() ? "" : " ") + a;
				}
				p.sendMessage(Translater.get("cmd_search_searching", zp.getLang()));
				InventoryManager.openSearch(search, p, 0);
			} else {
				p.sendMessage(Translater.get("cmd_search_usage", zp.getLang(), label));
			}
		}
		return true;
	}

}
