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

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import fr.zabricraft.craftsearch.CraftSearch;

public class ZabriPlayer {

	private UUID uuid;

	public ZabriPlayer(Player p) {
		this.uuid = p.getUniqueId();
	}

	public UUID getUUID() {
		return uuid;
	}

	public void connectSwitcher(String serverid) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("ConnectSwitcher");
		out.writeUTF(serverid);
		Bukkit.getPlayer(uuid).sendPluginMessage(CraftSearch.getInstance(), "BungeeCord", out.toByteArray());
	}

}
