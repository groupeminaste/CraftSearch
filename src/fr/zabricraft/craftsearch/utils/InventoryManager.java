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

package fr.zabricraft.craftsearch.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.zabricraft.craftsearch.CraftSearch;

public class InventoryManager {
	
	public static void openSearch(final String search, final Player player, final int pagination) {
		HashMap<String, String> servers = CraftSearch.getInstance().searchServer(search, "relevance",
				(pagination < 0 ? 0 : pagination) + 1);
		ZabriPlayer zp = CraftSearch.getInstance().getPlayer(player.getUniqueId());
		if (zp != null) {
			Lang l = zp.getLang();
			Inventory i = Bukkit.createInventory(null, 36, Translater.get("inventory_search_title", l)
					+ (!search.replaceAll("%", "").isEmpty() ? " : " + search.replaceAll("%", " ") : ""));
			int page = pagination;
			if (page < 0) {
				page = 0;
			}
			int j = 0;
			while (servers.containsKey("server" + (j+1) + "_id")) {
				if (j < 27) {
					ItemStack si = makeServItem(servers, (j+1), l, false);
					if (si != null) {
						i.addItem(si);
					} else {
						i.addItem(Items.setName(new ItemStack(Material.WOOL), "§cError with this server !"));
					}
				}
				j++;
			}
			i.setItem(31,
					Items.setName(new ItemStack(Material.WOOL), "§a" + Translater.get("inventory_slot_close", l)));
			if (page > 0) {
				i.setItem(29,
						Items.setLore(
								Items.setName(new ItemStack(Material.WOOL),
										"§e" + Translater.get("inventory_slot_previous", l)),
								"§a" + Translater.get("inventory_slot_page_id", l) + page, "§9Search : " + search));
			}
			if (j > 27) {
				i.setItem(33, Items.setLore(
						Items.setName(new ItemStack(Material.WOOL), "§e" + Translater.get("inventory_slot_next", l)),
						"§a" + Translater.get("inventory_slot_page_id", l) + (page + 2), "§9Search : " + search));
			}
			player.openInventory(i);
		} else {
			player.sendMessage("§cError !");
		}
	}
	
	public static ItemStack makeServItem(HashMap<String, String> servers, int j, Lang lang, boolean bookmark,
			String... cl) {
		try {
			int color = 15;
			if (servers.get("server" + j + "_open").equals("true")) {
				if (servers.get("server" + j + "_partner").equals("true")) {
					color = 11;
				} else if (servers.get("server" + j + "_whitelist").equals("true")) {
					color = 4;
				} else {
					color = 5;
				}
			}
			ItemStack result = Items.setName(Items.createItem(Material.WOOL, color),
					"§6" + servers.get("server" + j + "_name"));
			ArrayList<String> lore = new ArrayList<String>();
			String current = "";
			for (char c : servers.get("server" + j + "_description").toCharArray()) {
				if ((current.length() > 40 && c == ' ') || c == '\n') {
					lore.add("§a" + current);
					current = "";
				} else {
					current += c;
				}
			}
			if (!current.isEmpty()) {
				lore.add("§a" + current);
			}
			lore.add("");
			if (servers.get("server" + j + "_open").equals("true")) {
				if (servers.get("server" + j + "_partner").equals("true")) {
					lore.add("§7" + Translater.get("server_partner", lang));
				} else if (servers.get("server" + j + "_online").equals("true")) {
					lore.add("§7" + Translater.get("server_mc_premium", lang));
				} else {
					lore.add("§7" + Translater.get("server_mc_crack", lang));
				}
				if (servers.get("server" + j + "_whitelist").equals("true")) {
					lore.add("");
					lore.add("§6" + Translater.get("server_whitelist", lang));
				}
				lore.add("");
				lore.add("§b" + Translater.get("server_players", lang, servers.get("server" + j + "_players_online")
						+ "/" + servers.get("server" + j + "_players_max")));
			} else {
				lore.add("§c" + Translater.get("server_close", lang));
			}
			lore.add("");
			lore.add("§d" + Translater.get("server_version", lang, servers.get("server" + j + "_version")));
			lore.add("");
			lore.add("§eIP: " + servers.get("server" + j + "_domaine"));
			for (String cls : cl) {
				lore.add("");
				lore.add("§c" + cls);
			}
			lore.add("");
			lore.add("§a" + Translater.get("server_login", lang));
			if (bookmark) {
				lore.add("§a" + Translater.get("server_bookmark2", lang));
			} else {
				lore.add("§a" + Translater.get("server_bookmark", lang));
			}
			lore.add("");
			lore.add("§9ID: " + servers.get("server" + j + "_id"));
			result = Items.setLore(result, lore);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}