/*
 *  Copyright (C) 2017 Nathan FALLET, Michaël NASS and Jean-Baptiste EJARQUE
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

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.zabricraft.craftsearch.CraftSearch;

public class PlayerQuit implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		CraftSearch.getInstance().uninitPlayer(CraftSearch.getInstance().getPlayer(e.getPlayer().getUniqueId()));
		Bukkit.getScheduler().scheduleSyncDelayedTask(CraftSearch.getInstance(), new Runnable() {
			@Override
			public void run() {
				CraftSearch.getInstance().update();
			}
		}, 20);
	}

}
