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

package fr.zabricraft.craftsearch.events;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import fr.zabricraft.craftsearch.CraftSearch;

public class PlayerLogin implements Listener {

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		if (CraftSearch.getInstance().isPremiumGuard()) {
			try {
				URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + e.getPlayer().getName());
				InputStream stream = url.openStream();
				byte[] buf = new byte[8];
				String msg = "";
				while (stream.read(buf) >= 0) {
					for (byte bit : buf) {
						msg += (char) bit;
					}
					buf = new byte[8];
				}
				msg = msg.trim();
				if (msg.isEmpty()
						|| !msg.equals("{\"id\":\"" + e.getPlayer().getUniqueId().toString().replaceAll("-", "")
								+ "\",\"name\":\"" + e.getPlayer().getName() + "\"}")) {
					e.disallow(Result.KICK_OTHER, "Only premium players can join this server.");
					return;
				}
			} catch (Exception e2) {
				e.disallow(Result.KICK_OTHER, "Only premium players can join this server.");
				return;
			}
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Plugin:isPlayerBanned()");
		data.put("name", e.getPlayer().getName());
		data.put("uuid", e.getPlayer().getUniqueId().toString());
		Map<String, Object> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success")) {
			if ((boolean) response.get("success") && response.containsKey("banned")
					&& (boolean) response.get("banned")) {
				e.disallow(Result.KICK_BANNED, "I'm sorry but you were banned on a CraftSearch sanction list.");
				return;
			}
		}
	}

}
