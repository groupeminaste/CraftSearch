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

package fr.zabricraft.craftsearch.utils;

import java.util.HashMap;
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

	public Lang getLang() {
		Lang l = Lang.EN;
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "ZabriPlayer:getLang()");
		data.put("player", Bukkit.getPlayer(uuid).getName());
		HashMap<String, String> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && response.get("success").equals("true") && response.containsKey("lang")) {
			l = Lang.get(response.get("lang"));
		}
		return l;
	}

	public void setLang(Lang lang) {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "ZabriPlayer:setLang()");
		data.put("player", Bukkit.getPlayer(uuid).getName());
		data.put("lang", lang.toString());
		CraftSearch.getInstance().query(data);
	}

	public Grade getGrade() {
		Grade g = Grade.PLAYER;
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "ZabriPlayer:getGrade()");
		data.put("player", Bukkit.getPlayer(uuid).getName());
		HashMap<String, String> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && response.get("success").equals("true")
				&& response.containsKey("grade")) {
			g = Grade.get(response.get("grade"));
		}
		return g;
	}

	public String getCurrentServer() {
		String current_server = "";
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "ZabriPlayer:getCurrentServer()");
		data.put("player", Bukkit.getPlayer(uuid).getName());
		HashMap<String, String> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && response.get("success").equals("true")
				&& response.containsKey("current_server")) {
			current_server = response.get("current_server");
		}
		return current_server;
	}

	public boolean isBanned() {
		boolean banned = false;
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "ZabriPlayer:isBanned()");
		data.put("player_name", Bukkit.getPlayer(uuid).getName());
		data.put("player_uuid", uuid.toString());
		HashMap<String, String> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && response.get("success").equals("true")
				&& response.containsKey("banned")) {
			banned = response.get("banned").equals("true");
		}
		return banned;
	}

	public boolean addSanction(Sanction type, String reason, long expiration) {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "ZabriPlayer:addSanction()");
		data.put("player_name", Bukkit.getPlayer(uuid).getName());
		data.put("player_uuid", uuid.toString());
		data.put("type", type.toString());
		data.put("reason", reason);
		data.put("expiration", expiration + "");
		HashMap<String, String> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && response.get("success").equals("true")) {
			if (type.equals(Sanction.BAN)) {
				Bukkit.getPlayer(uuid).kickPlayer("You were banned: " + reason);
			}
			return true;
		}
		return false;
	}
	
	public boolean removeSanction(Sanction type) {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "ZabriPlayer:removeSanction()");
		data.put("player_name", Bukkit.getPlayer(uuid).getName());
		data.put("player_uuid", uuid.toString());
		data.put("type", type.toString());
		HashMap<String, String> response = CraftSearch.getInstance().query(data);
		return (response.containsKey("success") && response.get("success").equals("true"));
	}

	public void connectSwitcher(String serverid) {
		Player p = Bukkit.getPlayer(uuid);
		if (connectedFromCraftSearch()) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("ConnectSwitcher");
			out.writeUTF(serverid);
			p.sendMessage("§a" + Translater.get("chat_login_wait", getLang()));
			p.sendPluginMessage(CraftSearch.getInstance(), "BungeeCord", out.toByteArray());
		} else {
			p.sendMessage("§c" + Translater.get("chat_error_fromcs_ctc", getLang()));
		}
	}

	public boolean connectedFromCraftSearch() {
		String current_server = getCurrentServer();
		String serverID = CraftSearch.getInstance().getServerID();
		if (current_server != null && !current_server.isEmpty() && serverID != null && !serverID.isEmpty()) {
			return current_server.equals(serverID);
		}
		return false;
	}

}
