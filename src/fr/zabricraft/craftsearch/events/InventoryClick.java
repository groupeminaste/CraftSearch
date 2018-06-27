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

package fr.zabricraft.craftsearch.events;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.zabricraft.craftsearch.CraftSearch;
import fr.zabricraft.craftsearch.utils.InventoryManager;
import fr.zabricraft.craftsearch.utils.Translater;
import fr.zabricraft.craftsearch.utils.ZabriPlayer;

public class InventoryClick implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			ZabriPlayer zp = CraftSearch.getInstance().getPlayer(p.getUniqueId());
			if (zp != null) {
				if (e.getInventory().getName().startsWith(Translater.get("inventory_search_title", zp.getLang()))) {
					e.setCancelled(true);
					if (e.getSlot() == 29 && e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()
							&& e.getCurrentItem().getItemMeta().hasLore()) {
						InventoryManager.openSearch(
								e.getCurrentItem().getItemMeta().getLore().get(1).replaceFirst("§9Search : ", ""), p,
								Integer.parseInt(e.getCurrentItem().getItemMeta().getLore().get(0).replaceFirst(
										"§a" + Translater.get("inventory_slot_page_id", zp.getLang()), "")) - 1);
					} else if (e.getSlot() == 33 && e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()
							&& e.getCurrentItem().getItemMeta().hasLore()) {
						InventoryManager.openSearch(
								e.getCurrentItem().getItemMeta().getLore().get(1).replaceFirst("§9Search : ", ""), p,
								Integer.parseInt(e.getCurrentItem().getItemMeta().getLore().get(0).replaceFirst(
										"§a" + Translater.get("inventory_slot_page_id", zp.getLang()), "")) - 1);
					} else if (e.getSlot() == 31) {
						p.closeInventory();
					} else if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()
							&& e.getCurrentItem().getItemMeta().hasLore()) {
						List<String> lore = e.getCurrentItem().getItemMeta().getLore();
						String id = lore.get(lore.size() - 1).replaceFirst("§9ID: ", "");
						if (e.isLeftClick()) {
							zp.connectSwitcher(id);
						} else if (e.isRightClick()) {
							// ADD THIS SERVER IN PLAYER BOOKMARKS (SOON)
						}
					}
				}
			}
		}
	}

}
