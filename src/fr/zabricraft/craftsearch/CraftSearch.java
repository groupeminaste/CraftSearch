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

package fr.zabricraft.craftsearch;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.zabricraft.craftsearch.commands.CraftSearchCmd;
import fr.zabricraft.craftsearch.events.PlayerJoin;
import fr.zabricraft.craftsearch.events.PlayerLogin;
import fr.zabricraft.craftsearch.events.PlayerQuit;
import fr.zabricraft.craftsearch.utils.Metrics;
import fr.zabricraft.craftsearch.utils.Metrics2;
import fr.zabricraft.craftsearch.utils.Updater;
import fr.zabricraft.craftsearch.utils.ZabriPlayer;

public class CraftSearch extends JavaPlugin {

	private static CraftSearch instance;

	public static CraftSearch getInstance() {
		return instance;
	}

	private String IP = "mc.craftsearch.net";
	private int PORT = 25595;
	private String name;
	private String description;
	private String domaine;
	private String custom_ip;
	private boolean premiumGuard;
	private boolean bungeecord;
	private boolean updater;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean connected;
	private ArrayList<ZabriPlayer> players;

	public void setData(String name, String description) {
		if (name != null) {
			this.name = name;
		}
		if (description != null) {
			this.description = description;
		}
	}

	public boolean isPremiumGuard() {
		return premiumGuard;
	}
	
	public boolean isUpdater() {
		return updater;
	}

	public ZabriPlayer getPlayer(UUID uuid) {
		for (ZabriPlayer current : players) {
			if (current.getUUID().equals(uuid)) {
				return current;
			}
		}
		return null;
	}

	public void initPlayer(Player p) {
		players.add(new ZabriPlayer(p));
	}

	public void uninitPlayer(ZabriPlayer p) {
		if (players.contains(p)) {
			players.remove(p);
		}
	}

	public void onEnable() {
		instance = this;
		connected = false;

		getLogger().info("Copyright (C) 2017 Nathan FALLET, Michaël NASS and Jean-Baptiste EJARQUE\n\n"
				+ "This program is free software; you can redistribute it and/or modify\n"
				+ "it under the terms of the GNU General Public License as published by\n"
				+ "the Free Software Foundation; either version 2 of the License, or\n"
				+ "(at your option) any later version.\n\n"
				+ "This program is distributed in the hope that it will be useful,\n"
				+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
				+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n"
				+ "GNU General Public License for more details.\n\n"
				+ "You should have received a copy of the GNU General Public License along\n"
				+ "with this program; if not, write to the Free Software Foundation, Inc.,\n"
				+ "51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.");

		saveDefaultConfig();
		reloadConfig();

		description = getConfig().getString("description");
		name = getConfig().getString("name");
		domaine = getConfig().getString("domain-name");
		custom_ip = getConfig().getString("custom-ip");
		premiumGuard = (getConfig().contains("enable-premiumguard") && getConfig().getBoolean("enable-premiumguard"));
		updater = (!getConfig().contains("enable-checkforupdate") || getConfig().getBoolean("enable-checkforupdate"));
		bungeecord = false;
		players = new ArrayList<ZabriPlayer>();

		File spigot = new File("spigot.yml");
		if (spigot.exists()) {
			FileConfiguration spigot2 = YamlConfiguration.loadConfiguration(spigot);
			if (spigot2.contains("settings.bungeecord") && spigot2.getBoolean("settings.bungeecord")) {
				bungeecord = true;
			}
		}

		if (getConfig().contains("debug-server")) {
			IP = getConfig().getString("debug-server");
		}

		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		Bukkit.getPluginManager().registerEvents(new PlayerJoin(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerLogin(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerQuit(), this);

		getCommand("craftsearch").setExecutor(new CraftSearchCmd());
		getCommand("craftsearch").setAliases(Arrays.asList("cs"));

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				update();
				Updater.checkForUpdate();
			}
		}, 0, 5 * 60 * 20);

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
			new Metrics2(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onDisable() {
		update();
		close();
	}

	public boolean isBungeeCord() {
		return bungeecord;
	}

	private void update() {
		try {
			if (name == null || name.equalsIgnoreCase("My server") || description == null
					|| description.equalsIgnoreCase("Put your description here")) {
				getLogger().warning("WARNING! Setup is not complete!"
						+ " You need to customize some settings in your CraftSearch configuration file.");
				return;
			}
			HashMap<String, String> data = new HashMap<String, String>();
			boolean setup = true;
			data.put("method", "CraftSearch:update()");
			data.put("adding", "plugin:" + getDescription().getName() + ":" + getDescription().getVersion());
			data.put("port", Bukkit.getPort() + "");
			data.put("name", name);
			data.put("online", Bukkit.getOnlineMode() ? "true" : "false");
			if (Bukkit.getOnlineMode()) {
				setup = false;
				getLogger().warning("WARNING! Your server is running on a online-mode enabled server."
						+ " Please disable it in your server.properties files and enable PremiumGuard in the config.yml file in the Plugins/CraftSearch directory.");
			}
			data.put("premiumguard", isPremiumGuard() ? "true" : "false");
			if (isPremiumGuard() && Bukkit.getPort() == 25565) {
				setup = false;
				getLogger().warning("WARNING! Your server is running on the default Minecraft Server port !"
						+ " Keep in mind that you need to set up a BungeeCord server and open the port going to this server!");
			} else if (isPremiumGuard() && !isBungeeCord()) {
				setup = false;
				getLogger()
						.warning("WARNING! PremiumGuard is enabled but bungeecord is disabled in your spigot.yml file."
								+ " You won’t be able to use CraftSearch if that option is disabled.");
			}
			data.put("description", description);
			if (domaine != null && !domaine.isEmpty()) {
				data.put("domaine", domaine);
			}
			if (custom_ip != null && !custom_ip.isEmpty()) {
				data.put("custom_ip", custom_ip);
			}
			data.put("whitelist", Bukkit.hasWhitelist() ? "true" : "false");
			data.put("setup", setup ? "true" : "false");
			HashMap<String, String> response = query(data);
			if (response.containsKey("success")) {
				if (response.get("success").equals("true")) {
					connected = true;
				} else if (response.get("success").equals("false:unreachable")) {
					getLogger().warning(
							"ERROR! Your server is unreachable from CraftSearch. Please check the server port is open.");
				}
			}

		} catch (Exception e) {
		}
	}

	public void close() {
		try {
			if (socket != null && socket.isConnected()) {
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Socket getSocket() {
		try {
			if (socket != null && (socket.isClosed() || !socket.isConnected())) {
				connected = false;
				if (in != null) {
					in.close();
				}
				in = null;
				if (out != null) {
					out.close();
				}
				out = null;
				socket.close();
				socket = null;
			}
			if (socket == null) {
				socket = new Socket();
				socket.connect(new InetSocketAddress(IP, PORT), 3000);
			}
		} catch (Exception e) {
		}
		return socket;
	}

	public ObjectInputStream getInputStream() {
		try {
			if (in == null) {
				in = new ObjectInputStream(getSocket().getInputStream());
			}
		} catch (Exception e) {
		}
		return in;
	}

	public ObjectOutputStream getOutputStream() {
		try {
			if (out == null) {
				out = new ObjectOutputStream(getSocket().getOutputStream());
			}
		} catch (Exception e) {
		}
		return out;
	}

	public HashMap<String, String> query(HashMap<String, String> data) {
		HashMap<String, String> response = new HashMap<String, String>();
		try {
			HashMap<String, String> alive = new HashMap<String, String>();
			alive.put("isAlive", "true");
			getOutputStream().writeObject(alive);
			getOutputStream().flush();
			Object o = getInputStream().readObject();
			if (o instanceof HashMap) {
				alive = (HashMap<String, String>) o;
				if (!alive.containsKey("alive") || !alive.get("alive").equals("true")) {
					throw new NullPointerException();
				}
			}
		} catch (Exception e) {
			try {
				connected = false;
				in = null;
				out = null;
				socket = null;
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		if (!connected && !data.get("method").equals("CraftSearch:update()")) {
			update();
		}
		if (connected || data.get("method").equals("CraftSearch:update()")) {
			try {
				getOutputStream().writeObject(data);
				getOutputStream().flush();
				Object o = getInputStream().readObject();
				if (o instanceof HashMap) {
					response = (HashMap<String, String>) o;
				}
			} catch (Exception e) {
			}
		} else {
			response.put("success", "false:notconnected");
		}
		return response;
	}

	@Deprecated
	public void connectSwitcher(Player p, String id) {
		getPlayer(p.getUniqueId()).connectSwitcher(id);
	}

}
