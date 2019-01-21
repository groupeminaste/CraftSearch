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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import fr.zabricraft.craftsearch.CraftSearch;

public class CraftSearchCmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage(
				"§eCraftSearch " + CraftSearch.getInstance().getDescription().getVersion() + " developed by ZabriCraft");
		sender.sendMessage(" ");
		sender.sendMessage("§aCraftSearch is the world first Minecraft server search engine that works in-game."
				+ " Join us at §a§lmc.craftsearch.net§a or learn more at §a§lhttps://www.craftsearch.net/");
		return true;
	}

}
