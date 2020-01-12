/*
 *  Copyright (C) 2020 Groupe MINASTE
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

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Items {

	public static ItemStack createItem(Material m, int data) {
		ItemStack i = new ItemStack(m, 1);
		return i;
	}

	public static ItemStack getItem(String s, int q) {
		Material m;
		boolean hasm = false;
		try {
			m = Material.getMaterial(s);
		} catch (NumberFormatException e) {
			if (s.matches("[0-9]+:[0-9]+") || s.matches("[A-Za-z_-]+:[0-9]+")) {
				String[] s2 = s.split(":");
				try {
					m = Material.getMaterial(s2[0]);
				} catch (NumberFormatException e2) {
					m = Material.getMaterial(s2[0].toUpperCase());
				}
				hasm = true;
			} else {
				m = Material.getMaterial(s.toUpperCase());
			}
		}
		ItemStack i = new ItemStack(m, q);
		if (hasm) {
			i = new ItemStack(m, q);
		}
		return i;
	}

	public static ItemStack setName(ItemStack i, String name) {
		ItemMeta im = i.getItemMeta();
		im.setDisplayName("§r" + ChatColor.translateAlternateColorCodes('&', name));
		i.setItemMeta(im);
		return i;
	}

	public static ItemStack setLore(ItemStack i, String... lore) {
		return setLore(i, Arrays.asList(lore));
	}

	public static ItemStack setLore(ItemStack i, List<String> lore) {
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		i.setItemMeta(im);
		return i;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getHead(String player){
		ItemStack i = new ItemStack(Material.PLAYER_HEAD, 1);
		SkullMeta im = (SkullMeta) i.getItemMeta();
		im.setOwner(player);
		i.setItemMeta(im);
		return i;
	}

}
