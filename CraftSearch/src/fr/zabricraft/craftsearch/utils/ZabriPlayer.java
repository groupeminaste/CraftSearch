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

package fr.zabricraft.craftsearch.utils;

import java.util.HashMap;
import java.util.Map;
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
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Public:getPlayerDetails()");
		data.put("name", Bukkit.getPlayer(uuid).getName());
		Map<String, Object> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && (boolean) response.get("success") && response.containsKey("lang")) {
			l = Lang.get((String) response.get("lang"));
		}
		return l;
	}

	public void setLang(Lang lang) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Plugin:setPlayerLang()");
		data.put("name", Bukkit.getPlayer(uuid).getName());
		data.put("lang", lang.toString());
		CraftSearch.getInstance().query(data);
	}

	public Grade getGrade() {
		Grade g = Grade.PLAYER;
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Public:getPlayerDetails()");
		data.put("name", Bukkit.getPlayer(uuid).getName());
		Map<String, Object> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && (boolean) response.get("success")
				&& response.containsKey("grade")) {
			g = Grade.get((String) response.get("grade"));
		}
		return g;
	}

	public String getCurrentServer() {
		String current_server = "";
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Public:getPlayerDetails()");
		data.put("name", Bukkit.getPlayer(uuid).getName());
		Map<String, Object> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && (boolean) response.get("success")
				&& response.containsKey("current_server")) {
			current_server = (String) response.get("current_server");
		}
		return current_server;
	}

	public boolean isBanned() {
		boolean banned = false;
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Plugin:isPlayerBanned()");
		data.put("name", Bukkit.getPlayer(uuid).getName());
		data.put("uuid", uuid.toString());
		Map<String, Object> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && (boolean) response.get("success")
				&& response.containsKey("banned")) {
			banned = (boolean) response.get("banned");
		}
		return banned;
	}

	public boolean addSanction(Sanction type, String reason, long expiration) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Plugin:addPlayerSanction()");
		data.put("name", Bukkit.getPlayer(uuid).getName());
		data.put("uuid", uuid.toString());
		data.put("type", type.toString());
		data.put("reason", reason);
		data.put("expiration", expiration);
		Map<String, Object> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && (boolean) response.get("success")) {
			if (type.equals(Sanction.BAN)) {
				Bukkit.getPlayer(uuid).kickPlayer("You were banned: " + reason);
			}
			return true;
		}
		return false;
	}
	
	public boolean removeSanction(Sanction type) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Plugin:removePlayerSanction()");
		data.put("name", Bukkit.getPlayer(uuid).getName());
		data.put("uuid", uuid.toString());
		data.put("type", type.toString());
		Map<String, Object> response = CraftSearch.getInstance().query(data);
		return (response.containsKey("success") && (boolean) response.get("success"));
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
