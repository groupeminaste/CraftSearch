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

import fr.zabricraft.craftsearch.commands.BanCmd;
import fr.zabricraft.craftsearch.commands.CraftSearchCmd;
import fr.zabricraft.craftsearch.commands.PardonCmd;
import fr.zabricraft.craftsearch.commands.SearchCmd;
import fr.zabricraft.craftsearch.commands.TempbanCmd;
import fr.zabricraft.craftsearch.events.InventoryClick;
import fr.zabricraft.craftsearch.events.PlayerJoin;
import fr.zabricraft.craftsearch.events.PlayerLogin;
import fr.zabricraft.craftsearch.events.PlayerQuit;
import fr.zabricraft.craftsearch.utils.Lang;
import fr.zabricraft.craftsearch.utils.Metrics;
import fr.zabricraft.craftsearch.utils.Metrics2;
import fr.zabricraft.craftsearch.utils.TranslatedMsg;
import fr.zabricraft.craftsearch.utils.Translater;
import fr.zabricraft.craftsearch.utils.Translation;
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
	private String owner;
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

		getLogger().info("Copyright (C) 2018 Nathan FALLET, Michaël NASS and Jean-Baptiste EJARQUE\n\n"
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
		owner = getConfig().getString("owner");
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
		Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);

		getCommand("craftsearch").setExecutor(new CraftSearchCmd());
		getCommand("craftsearch").setAliases(Arrays.asList("cs"));
		getCommand("search").setExecutor(new SearchCmd());
		getCommand("search").setAliases(Arrays.asList("s"));
		getCommand("ban").setExecutor(new BanCmd());
		getCommand("tempban").setExecutor(new TempbanCmd());
		getCommand("pardon").setExecutor(new PardonCmd());

		Translater.set("inventory_search_title",
				new Translation(new TranslatedMsg(Lang.EN, "§r§rServers"), new TranslatedMsg(Lang.FR, "§r§rServeurs"),
						new TranslatedMsg(Lang.ES, "§r§rServidores"), new TranslatedMsg(Lang.DE, "§r§rServers")));
		Translater.set("inventory_slot_close",
				new Translation(new TranslatedMsg(Lang.EN, "Close"), new TranslatedMsg(Lang.FR, "Fermer"),
						new TranslatedMsg(Lang.ES, "Cerrar"), new TranslatedMsg(Lang.DE, "Schliessen")));
		Translater.set("inventory_slot_previous",
				new Translation(new TranslatedMsg(Lang.EN, "Previous page"),
						new TranslatedMsg(Lang.FR, "Page précédente"), new TranslatedMsg(Lang.ES, "Página anterior"),
						new TranslatedMsg(Lang.DE, "Vorherige Seite")));
		Translater.set("inventory_slot_next",
				new Translation(new TranslatedMsg(Lang.EN, "Next page"), new TranslatedMsg(Lang.FR, "Page suivante"),
						new TranslatedMsg(Lang.ES, "Página siguiente"), new TranslatedMsg(Lang.DE, "Nächste Seite")));
		Translater.set("inventory_slot_page_id",
				new Translation(new TranslatedMsg(Lang.EN, "Page "), new TranslatedMsg(Lang.FR, "Page "),
						new TranslatedMsg(Lang.ES, "Página "), new TranslatedMsg(Lang.DE, "Seite ")));
		Translater.set("inventory_search_slot_others",
				new Translation(new TranslatedMsg(Lang.EN, "See others servers"),
						new TranslatedMsg(Lang.FR, "Voir d'autres serveurs"),
						new TranslatedMsg(Lang.ES, "Ver a otros servidores"),
						new TranslatedMsg(Lang.DE, "Anderen Servers suchen")));
		Translater.set("grade_player",
				new Translation(new TranslatedMsg(Lang.EN, "Player"), new TranslatedMsg(Lang.FR, "Joueur"),
						new TranslatedMsg(Lang.ES, "Jugador"), new TranslatedMsg(Lang.DE, "Spieler")));
		Translater.set("grade_partner",
				new Translation(new TranslatedMsg(Lang.EN, "Partner"), new TranslatedMsg(Lang.FR, "Partenaire"),
						new TranslatedMsg(Lang.ES, "Socio"), new TranslatedMsg(Lang.DE, "Partner")));
		Translater.set("grade_admin",
				new Translation(new TranslatedMsg(Lang.EN, "Admin"), new TranslatedMsg(Lang.FR, "Admin"),
						new TranslatedMsg(Lang.ES, "Admin"), new TranslatedMsg(Lang.DE, "Admin")));
		Translater.set("grade_ads",
				new Translation(new TranslatedMsg(Lang.EN, "Ads"), new TranslatedMsg(Lang.FR, "Publicitaire"),
						new TranslatedMsg(Lang.ES, "Publicista"), new TranslatedMsg(Lang.DE, "Werbung")));
		Translater.set("grade_translater",
				new Translation(new TranslatedMsg(Lang.EN, "Translater"), new TranslatedMsg(Lang.FR, "Traducteur"),
						new TranslatedMsg(Lang.ES, "Traductor"), new TranslatedMsg(Lang.DE, "Übersetzer")));
		Translater.set("grade_builder",
				new Translation(new TranslatedMsg(Lang.EN, "Builder"), new TranslatedMsg(Lang.FR, "Constructeur"),
						new TranslatedMsg(Lang.ES, "Constructor"), new TranslatedMsg(Lang.DE, "Bauer")));
		Translater.set("server_partner",
				new Translation(new TranslatedMsg(Lang.EN, "Partner server"),
						new TranslatedMsg(Lang.FR, "Serveur partenaire"),
						new TranslatedMsg(Lang.ES, "Servidor asociado"), new TranslatedMsg(Lang.DE, "Partner Server")));
		Translater.set("server_mc_premium",
				new Translation(new TranslatedMsg(Lang.EN, "Minecraft Premium server"),
						new TranslatedMsg(Lang.FR, "Serveur Minecraft Premium"),
						new TranslatedMsg(Lang.ES, "Servidor Minecraft Premium"),
						new TranslatedMsg(Lang.DE, "Minecraft Premium Server")));
		Translater.set("server_mc_crack",
				new Translation(new TranslatedMsg(Lang.EN, "Minecraft Cracked server"),
						new TranslatedMsg(Lang.FR, "Serveur Minecraft Cracké"),
						new TranslatedMsg(Lang.ES, "Servidor Minecraft Crack"),
						new TranslatedMsg(Lang.DE, "Crack Minecraft Server")));
		Translater.set("server_close",
				new Translation(new TranslatedMsg(Lang.EN, "Closed server"),
						new TranslatedMsg(Lang.FR, "Serveur fermé"), new TranslatedMsg(Lang.ES, "Servidor cerrado"),
						new TranslatedMsg(Lang.DE, "Server aussgechaltet")));
		Translater.set("server_whitelist",
				new Translation(new TranslatedMsg(Lang.EN, "Whitelisted server"),
						new TranslatedMsg(Lang.FR, "Serveur avec whitelist"),
						new TranslatedMsg(Lang.ES, "Servidor con whitelist"),
						new TranslatedMsg(Lang.DE, "Server mit Whitelist")));
		Translater.set("server_players",
				new Translation(new TranslatedMsg(Lang.EN, "Players: %s"), new TranslatedMsg(Lang.FR, "Joueurs : %s"),
						new TranslatedMsg(Lang.ES, "Jugadores: %s"), new TranslatedMsg(Lang.DE, "Spielern: %s")));
		Translater.set("server_version",
				new Translation(new TranslatedMsg(Lang.EN, "Version : %s"), new TranslatedMsg(Lang.FR, "Version : %s"),
						new TranslatedMsg(Lang.ES, "Versión: %s"), new TranslatedMsg(Lang.DE, "Version: %s")));
		Translater.set("server_login",
				new Translation(new TranslatedMsg(Lang.EN, "Left click to connect"),
						new TranslatedMsg(Lang.FR, "Clic gauche pour se connecter"),
						new TranslatedMsg(Lang.ES, "Clic izquierdo para conectarse"),
						new TranslatedMsg(Lang.DE, "Links Klicken um zu verbinden")));
		Translater.set("server_bookmark",
				new Translation(new TranslatedMsg(Lang.EN, "Right click to add to bookmarks"),
						new TranslatedMsg(Lang.FR, "Clic droit pour ajouter aux favoris"),
						new TranslatedMsg(Lang.ES, "Clic derecho para añadir a sus favoritos"),
						new TranslatedMsg(Lang.DE, "Rechts Klicken um das Server als Favoriten zu speichern")));
		Translater.set("server_bookmark2",
				new Translation(new TranslatedMsg(Lang.EN, "Right click to remove of bookmarks"),
						new TranslatedMsg(Lang.FR, "Clic droit pour retirer des favoris"),
						new TranslatedMsg(Lang.ES, "Clic derecho para quitar de sus favoritos"),
						new TranslatedMsg(Lang.DE, "Rechts Klicken um das Server als Favoriten zu löschen")));
		Translater.set("server_login_date",
				new Translation(new TranslatedMsg(Lang.EN, "Login date: %s"),
						new TranslatedMsg(Lang.FR, "Date de connexion : %s"),
						new TranslatedMsg(Lang.ES, "Fetcha de conexión: %s"),
						new TranslatedMsg(Lang.DE, "Verbindungsdatum: %s")));
		Translater.set("cmd_search_usage", new Translation(new TranslatedMsg(Lang.EN, "§c/%s <your search>"),
				new TranslatedMsg(Lang.FR, "§c/%s <votre recherche>"),
				new TranslatedMsg(Lang.ES, "§c/%s <su búsqueda>"), new TranslatedMsg(Lang.DE, "§c/%s <ihre Suche>")));
		Translater.set("cmd_search_searching",
				new Translation(new TranslatedMsg(Lang.EN, "§aSearching..."),
						new TranslatedMsg(Lang.FR, "§aRecherche en cours..."),
						new TranslatedMsg(Lang.ES, "§aBúsqueda en proceso..."),
						new TranslatedMsg(Lang.DE, "§aDie Suche läuft...")));
		Translater.set("chat_login_wait",
				new Translation(new TranslatedMsg(Lang.EN, "You will be connected to the server in a few seconds..."),
						new TranslatedMsg(Lang.FR, "Vous allez être connecté au serveur dans quelques secondes..."),
						new TranslatedMsg(Lang.ES, "Se conectará al servidor dentro de algunos segundos..."),
						new TranslatedMsg(Lang.DE, "Sie werden in ein paar Sekunden verbunden...")));
		Translater.set("chat_error_fromcs_ctc", new Translation(
				new TranslatedMsg(Lang.EN, "You must be connected from CraftSearch to use Click-To-Connect!"),
				new TranslatedMsg(Lang.FR,
						"Vous devez être connecté depuis CraftSearch pour utiliser Click-To-Connect !"),
				new TranslatedMsg(Lang.ES, "¡Es necesario conectarse con CraftSearch para utilisar Click-To-Connect!"),
				new TranslatedMsg(Lang.DE,
						"Sie müssen von CraftSearch verbunden sein um Click-To-Connect zu nützen!")));

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
			data.put("owner", owner);
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

	public HashMap<String, String> searchServer(String search, String order_by, int start, int limit) {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "CraftSearch:searchServer()");
		data.put("search", search);
		data.put("order_by", order_by);
		data.put("start", start + "");
		data.put("limit", limit + "");
		return query(data);
	}

	public HashMap<String, String> searchServer(String search, String order_by, int pagination) {
		int start = 0;
		int limit = 28;
		if (pagination > 0) {
			start = ((pagination - 1) * 27);
		}
		return searchServer(search, order_by, start, limit);
	}

	public String getServerID() {
		String id = "";
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "CraftSearch:getServerID()");
		HashMap<String, String> response = CraftSearch.getInstance().query(data);
		if (response.containsKey("success") && response.get("success").equals("true") && response.containsKey("id")) {
			id = response.get("id");
		}
		return id;
	}
	
	public File getFile() {
		return super.getFile();
	}

	@Deprecated
	public void connectSwitcher(Player p, String id) {
		getPlayer(p.getUniqueId()).connectSwitcher(id);
	}

}
