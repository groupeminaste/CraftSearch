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

package fr.zabricraft.craftsearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

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

	private String API = "https://apps.craftsearch.net/index.php";
	private String name;
	private String description;
	private String ip;
	private String owner;
	private boolean premiumGuard;
	private boolean bungeecord;
	private boolean updater;
	private String token;
	private boolean debug;
	private ArrayList<ZabriPlayer> players;

	public void setData(String name, String description) {
		if (name != null) {
			this.name = name;
		}
		if (description != null) {
			this.description = description;
		}
		update();
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

		getLogger().info("Copyright (C) 2018 Groupe MINASTE\n\n"
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

		token = "";
		description = getConfig().getString("description");
		name = getConfig().getString("name");
		ip = getConfig().contains("ip") ? getConfig().getString("ip")
				: (getConfig().contains("domain-name") ? getConfig().getString("domain-name")
						: (getConfig().contains("custom-ip") ? getConfig().getString("custom-ip") : ""));
		owner = getConfig().getString("owner");
		premiumGuard = (getConfig().contains("enable-premiumguard") && getConfig().getBoolean("enable-premiumguard"));
		updater = (!getConfig().contains("enable-checkforupdate") || getConfig().getBoolean("enable-checkforupdate"));
		bungeecord = false;
		players = new ArrayList<ZabriPlayer>();
		debug = (getConfig().contains("debug") && getConfig().getBoolean("debug"));

		File spigot = new File("spigot.yml");
		if (spigot.exists()) {
			FileConfiguration spigot2 = YamlConfiguration.loadConfiguration(spigot);
			if (spigot2.contains("settings.bungeecord") && spigot2.getBoolean("settings.bungeecord")) {
				bungeecord = true;
			}
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			initPlayer(p);
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
				new Translation(new TranslatedMsg(Lang.EN, "Servers"), new TranslatedMsg(Lang.FR, "Serveurs"),
						new TranslatedMsg(Lang.ES, "Servidores"), new TranslatedMsg(Lang.DE, "Servers"),
						new TranslatedMsg(Lang.SR, "Сервери"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Servers")));
		Translater.set("inventory_slot_close",
				new Translation(new TranslatedMsg(Lang.EN, "Close"), new TranslatedMsg(Lang.FR, "Fermer"),
						new TranslatedMsg(Lang.ES, "Cerrar"), new TranslatedMsg(Lang.DE, "Schliessen"),
						new TranslatedMsg(Lang.SR, "Затвори"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Sluiten")));
		Translater.set("inventory_slot_previous",
				new Translation(new TranslatedMsg(Lang.EN, "Previous page"),
						new TranslatedMsg(Lang.FR, "Page précédente"), new TranslatedMsg(Lang.ES, "Página anterior"),
						new TranslatedMsg(Lang.DE, "Vorherige Seite"), new TranslatedMsg(Lang.SR, "Претходна Страна"),
						new TranslatedMsg(Lang.RU, ""), new TranslatedMsg(Lang.NL, "Vorige pagina")));
		Translater.set("inventory_slot_next",
				new Translation(new TranslatedMsg(Lang.EN, "Next page"), new TranslatedMsg(Lang.FR, "Page suivante"),
						new TranslatedMsg(Lang.ES, "Página siguiente"), new TranslatedMsg(Lang.DE, "Nächste Seite"),
						new TranslatedMsg(Lang.SR, "Следећу Страну"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Volgende pagina")));
		Translater.set("inventory_slot_page_id",
				new Translation(new TranslatedMsg(Lang.EN, "Page "), new TranslatedMsg(Lang.FR, "Page "),
						new TranslatedMsg(Lang.ES, "Página "), new TranslatedMsg(Lang.DE, "Seite "),
						new TranslatedMsg(Lang.SR, "Страну "), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Pagina ")));
		Translater.set("inventory_search_slot_others", new Translation(new TranslatedMsg(Lang.EN, "See others servers"),
				new TranslatedMsg(Lang.FR, "Voir d'autres serveurs"),
				new TranslatedMsg(Lang.ES, "Ver a otros servidores"),
				new TranslatedMsg(Lang.DE, "Anderen Servers suchen"), new TranslatedMsg(Lang.SR, "Види други сервери"),
				new TranslatedMsg(Lang.RU, ""), new TranslatedMsg(Lang.NL, "Andere servers bekijken")));
		Translater.set("grade_player",
				new Translation(new TranslatedMsg(Lang.EN, "Player"), new TranslatedMsg(Lang.FR, "Joueur"),
						new TranslatedMsg(Lang.ES, "Jugador"), new TranslatedMsg(Lang.DE, "Spieler"),
						new TranslatedMsg(Lang.SR, "Играч"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Speler")));
		Translater.set("grade_partner",
				new Translation(new TranslatedMsg(Lang.EN, "Partner"), new TranslatedMsg(Lang.FR, "Partenaire"),
						new TranslatedMsg(Lang.ES, "Socio"), new TranslatedMsg(Lang.DE, "Partner"),
						new TranslatedMsg(Lang.SR, "Партнер"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Partner")));
		Translater.set("grade_admin",
				new Translation(new TranslatedMsg(Lang.EN, "Admin"), new TranslatedMsg(Lang.FR, "Admin"),
						new TranslatedMsg(Lang.ES, "Admin"), new TranslatedMsg(Lang.DE, "Admin"),
						new TranslatedMsg(Lang.SR, "Админ"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Beheerder")));
		Translater.set("grade_ads",
				new Translation(new TranslatedMsg(Lang.EN, "Ads"), new TranslatedMsg(Lang.FR, "Publicitaire"),
						new TranslatedMsg(Lang.ES, "Publicista"), new TranslatedMsg(Lang.DE, "Werbung"),
						new TranslatedMsg(Lang.SR, "Рекламе"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Adverteerder")));
		Translater.set("grade_translater",
				new Translation(new TranslatedMsg(Lang.EN, "Translater"), new TranslatedMsg(Lang.FR, "Traducteur"),
						new TranslatedMsg(Lang.ES, "Traductor"), new TranslatedMsg(Lang.DE, "Übersetzer"),
						new TranslatedMsg(Lang.SR, "Преводилац"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Vertaler")));
		Translater.set("grade_builder",
				new Translation(new TranslatedMsg(Lang.EN, "Builder"), new TranslatedMsg(Lang.FR, "Constructeur"),
						new TranslatedMsg(Lang.ES, "Constructor"), new TranslatedMsg(Lang.DE, "Bauer"),
						new TranslatedMsg(Lang.SR, "Градитељ"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Fabrikant")));
		Translater.set("server_partner", new Translation(new TranslatedMsg(Lang.EN, "Partner server"),
				new TranslatedMsg(Lang.FR, "Serveur partenaire"), new TranslatedMsg(Lang.ES, "Servidor asociado"),
				new TranslatedMsg(Lang.DE, "Partner Server"), new TranslatedMsg(Lang.SR, "Партнер сервер"),
				new TranslatedMsg(Lang.RU, ""), new TranslatedMsg(Lang.NL, "Partner server")));
		Translater.set("server_mc_premium",
				new Translation(new TranslatedMsg(Lang.EN, "Minecraft Premium server"),
						new TranslatedMsg(Lang.FR, "Serveur Minecraft Premium"),
						new TranslatedMsg(Lang.ES, "Servidor Minecraft Premium"),
						new TranslatedMsg(Lang.DE, "Minecraft Premium Server"),
						new TranslatedMsg(Lang.SR, "Маинкрафт Премиум сервер"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Server Minecraft Premium")));
		Translater.set("server_mc_crack",
				new Translation(new TranslatedMsg(Lang.EN, "Minecraft Cracked server"),
						new TranslatedMsg(Lang.FR, "Serveur Minecraft Cracké"),
						new TranslatedMsg(Lang.ES, "Servidor Minecraft Crack"),
						new TranslatedMsg(Lang.DE, "Crack Minecraft Server"),
						new TranslatedMsg(Lang.SR, "Маинкрафт Крак сервер"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Free server minecraft")));
		Translater.set("server_close", new Translation(new TranslatedMsg(Lang.EN, "Closed server"),
				new TranslatedMsg(Lang.FR, "Serveur fermé"), new TranslatedMsg(Lang.ES, "Servidor cerrado"),
				new TranslatedMsg(Lang.DE, "Server aussgechaltet"), new TranslatedMsg(Lang.SR, "Затворен сервер"),
				new TranslatedMsg(Lang.RU, ""), new TranslatedMsg(Lang.NL, "Gesloten server")));
		Translater.set("server_whitelist", new Translation(new TranslatedMsg(Lang.EN, "Whitelisted server"),
				new TranslatedMsg(Lang.FR, "Serveur avec whitelist"),
				new TranslatedMsg(Lang.ES, "Servidor con whitelist"),
				new TranslatedMsg(Lang.DE, "Server mit Whitelist"), new TranslatedMsg(Lang.SR, "Сервер са уајтлист"),
				new TranslatedMsg(Lang.RU, ""), new TranslatedMsg(Lang.NL, "Server met witte lijst")));
		Translater.set("server_players",
				new Translation(new TranslatedMsg(Lang.EN, "Players: %s"), new TranslatedMsg(Lang.FR, "Joueurs : %s"),
						new TranslatedMsg(Lang.ES, "Jugadores: %s"), new TranslatedMsg(Lang.DE, "Spielern: %s"),
						new TranslatedMsg(Lang.SR, "Играче: %s"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Speler: %s")));
		Translater.set("server_version",
				new Translation(new TranslatedMsg(Lang.EN, "Version : %s"), new TranslatedMsg(Lang.FR, "Version : %s"),
						new TranslatedMsg(Lang.ES, "Versión: %s"), new TranslatedMsg(Lang.DE, "Version: %s"),
						new TranslatedMsg(Lang.SR, "Верзија: %s"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Vertaling: %s")));
		Translater.set("server_login",
				new Translation(new TranslatedMsg(Lang.EN, "Left click to connect"),
						new TranslatedMsg(Lang.FR, "Clic gauche pour se connecter"),
						new TranslatedMsg(Lang.ES, "Clic izquierdo para conectarse"),
						new TranslatedMsg(Lang.DE, "Links Klicken um zu verbinden"),
						new TranslatedMsg(Lang.SR, "Леви клик да се повезати"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Linker klik om te verbinden")));
		Translater.set("server_bookmark",
				new Translation(new TranslatedMsg(Lang.EN, "Right click to add to bookmarks"),
						new TranslatedMsg(Lang.FR, "Clic droit pour ajouter aux favoris"),
						new TranslatedMsg(Lang.ES, "Clic derecho para añadir a sus favoritos"),
						new TranslatedMsg(Lang.DE, "Rechts Klicken um das Server als Favoriten zu speichern"),
						new TranslatedMsg(Lang.SR, "Десни клик да се дода у фаворити"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Klik met de rechtermuisknop om toe te voegen aan favorieten")));
		Translater.set("server_bookmark2",
				new Translation(new TranslatedMsg(Lang.EN, "Right click to remove of bookmarks"),
						new TranslatedMsg(Lang.FR, "Clic droit pour retirer des favoris"),
						new TranslatedMsg(Lang.ES, "Clic derecho para quitar de sus favoritos"),
						new TranslatedMsg(Lang.DE, "Rechts Klicken um das Server als Favoriten zu löschen"),
						new TranslatedMsg(Lang.SR, "Десни клик да се скида од фаворити"),
						new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Favorieten verwijderen met de rechtermuisknop")));
		Translater.set("server_login_date", new Translation(new TranslatedMsg(Lang.EN, "Login date: %s"),
				new TranslatedMsg(Lang.FR, "Date de connexion : %s"),
				new TranslatedMsg(Lang.ES, "Fetcha de conexión: %s"),
				new TranslatedMsg(Lang.DE, "Verbindungsdatum: %s"), new TranslatedMsg(Lang.SR, "Датум везе: %s"),
				new TranslatedMsg(Lang.RU, ""), new TranslatedMsg(Lang.NL, "Aansluitingsdatum: %s")));
		Translater.set("cmd_search_usage", new Translation(new TranslatedMsg(Lang.EN, "§c/%s <your search>"),
				new TranslatedMsg(Lang.FR, "§c/%s <votre recherche>"),
				new TranslatedMsg(Lang.ES, "§c/%s <su búsqueda>"), new TranslatedMsg(Lang.DE, "§c/%s <ihre Suche>"),
				new TranslatedMsg(Lang.SR, "§c/%s <ваш истраживан>"), new TranslatedMsg(Lang.RU, ""),
				new TranslatedMsg(Lang.NL, "§c/%s <uw zoekopdracht>")));
		Translater.set("cmd_search_searching",
				new Translation(new TranslatedMsg(Lang.EN, "§aSearching..."),
						new TranslatedMsg(Lang.FR, "§aRecherche en cours..."),
						new TranslatedMsg(Lang.ES, "§aBúsqueda en proceso..."),
						new TranslatedMsg(Lang.DE, "§aDie Suche läuft..."),
						new TranslatedMsg(Lang.SR, "§aистраживање је у току..."), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Onderzoek in uitvoering...")));
		Translater.set("chat_login_wait",
				new Translation(new TranslatedMsg(Lang.EN, "You will be connected to the server in a few seconds..."),
						new TranslatedMsg(Lang.FR, "Vous allez être connecté au serveur dans quelques secondes..."),
						new TranslatedMsg(Lang.ES, "Se conectará al servidor dentro de algunos segundos..."),
						new TranslatedMsg(Lang.DE, "Sie werden in ein paar Sekunden verbunden..."),
						new TranslatedMsg(Lang.SR, "Бичете повезани на серверу за неку секунду..."),
						new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Binnen enkele seconden bent u verbonden met de server...")));
		Translater.set("chat_login_error_premium",
				new Translation(new TranslatedMsg(Lang.EN, "Click-to-connect is unavailable for premiums servers!"),
						new TranslatedMsg(Lang.FR, "Click-to-connect non disponible pour les serveurs premiums !"),
						new TranslatedMsg(Lang.ES, "¡Click-to-connect no es disponible para los servidores premiums!"),
						new TranslatedMsg(Lang.DE, "Click-to-connect ist für Premiums Servers nicht verfügbar!"),
						new TranslatedMsg(Lang.SR, "Клик-то-конект није доспутан за Премиум сервери!"),
						new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Click-to-connect niet beschikbaar voor premium servers!")));
		Translater.set("chat_login_error_offline",
				new Translation(new TranslatedMsg(Lang.EN, "This server is offline !"),
						new TranslatedMsg(Lang.FR, "Ce serveur est hors ligne !"),
						new TranslatedMsg(Lang.ES, "¡Este servidor no está en línea!"),
						new TranslatedMsg(Lang.DE, "Dieses Server ist Offline!"),
						new TranslatedMsg(Lang.SR, "Овај сервер није повезано!"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Deze server is offline!")));
		Translater.set("chat_login_error_doesnt_exists",
				new Translation(new TranslatedMsg(Lang.EN, "This server doesn't exists!"),
						new TranslatedMsg(Lang.FR, "Ce serveur n'existe pas ou plus !"),
						new TranslatedMsg(Lang.ES, "¡Este servidor no existe!"),
						new TranslatedMsg(Lang.DE, "Dieser Server ist nicht vorhanden oder mehr!"),
						new TranslatedMsg(Lang.SR, "Овај сервер не постоји/више!"), new TranslatedMsg(Lang.RU, ""),
						new TranslatedMsg(Lang.NL, "Deze server bestaat niet of bestaat niet meer!")));
		Translater.set("chat_login_error_unknow",
				new Translation(new TranslatedMsg(Lang.EN, "Unable to join this server!"),
						new TranslatedMsg(Lang.FR, "Impossible de rejoindre ce serveur !"),
						new TranslatedMsg(Lang.ES, "¡Es imposible de llegar a este servidor!"),
						new TranslatedMsg(Lang.DE, "Es ist leider unmöglich mit dieses Server zu verbinden!"),
						new TranslatedMsg(Lang.SR, "Не може да се повеза на овај сервер!"),
						new TranslatedMsg(Lang.RU, ""), new TranslatedMsg(Lang.NL, "Kan deze server niet bereiken!")));
		Translater.set("chat_error_fromcs_ctc", new Translation(
				new TranslatedMsg(Lang.EN, "You must be connected from CraftSearch to use Click-To-Connect!"),
				new TranslatedMsg(Lang.FR,
						"Vous devez être connecté depuis CraftSearch pour utiliser Click-To-Connect !"),
				new TranslatedMsg(Lang.ES, "¡Es necesario conectarse con CraftSearch para utilisar Click-To-Connect!"),
				new TranslatedMsg(Lang.DE, "Sie müssen von CraftSearch verbunden sein um Click-To-Connect zu nützen!"),
				new TranslatedMsg(Lang.SR, "Морате да будете повезани од КрафтСерћ да би користили Клик-то-конект!"),
				new TranslatedMsg(Lang.RU, ""),
				new TranslatedMsg(Lang.NL, "Om Click-To-Connect te gebruiken, moet u ingelogd zijn bij CraftSearch!")));

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
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Plugin:invalidToken()");
		query(data);
		players.clear();
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
			Map<String, Object> data = new HashMap<String, Object>();
			boolean setup = true;
			data.put("method", "Plugin:update()");
			data.put("adding", "plugin:" + getDescription().getName() + ":" + getDescription().getVersion());
			data.put("port", Bukkit.getPort() + "");
			data.put("name", name);
			data.put("online", Bukkit.getOnlineMode());
			if (Bukkit.getOnlineMode()) {
				setup = false;
				getLogger().warning("WARNING! Your server is running on a online-mode enabled server."
						+ " Please disable it in your server.properties files and enable PremiumGuard in the config.yml file in the Plugins/CraftSearch directory.");
			}
			data.put("premiumguard", isPremiumGuard());
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
			if (ip != null && !ip.isEmpty()) {
				data.put("ip", ip);
			}
			data.put("whitelist", Bukkit.hasWhitelist());
			data.put("owner", owner);
			data.put("setup", setup);
			Map<String, Object> response = query(data);
			if (response.containsKey("success")) {
				if ((boolean) response.get("success")) {
					if (response.containsKey("token")) {
						token = (String) response.get("token");
					}
				} else if (response.get("error").equals("unreachable")) {
					getLogger().warning(
							"ERROR! Your server is unreachable from CraftSearch. Please check the server port is open. If you need help, please contact the craftsearch support.");
				} else if (response.get("error").equals("invalid_token")) {
					getLogger().warning("ERROR! The token is invalid.");
				} else {
					getLogger().warning("UNKNOWN ERROR! (code=" + response.get("error") + ")");
				}
			} else {
				getLogger().warning("UNKNOWN ERROR! (response=" + response + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, Object> query(Map<String, Object> data) {
		try {
			data.put("token", token);

			if (debug) {
				System.out.println("QUERY=" + data);
			}

			URL object = new URL(API);

			HttpURLConnection con = (HttpURLConnection) object.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestMethod("POST");

			JSONObject json = new JSONObject(data);

			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(json.toString());
			wr.flush();

			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				String raw = sb.toString();
				if (debug) {
					System.out.println("RESPONSE=" + raw);
				}
				JSONObject response = new JSONObject(raw);
				Map<String, Object> map = new TreeMap<String, Object>(new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						try {
							Integer i1 = Integer.parseInt(o1);
							Integer i2 = Integer.parseInt(o2);
							return i1.compareTo(i2);
						} catch (NumberFormatException e) {
							return o1.compareTo(o2);
						}
					}
				});
				map.putAll(response.toMap());
				return map;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, Object> searchServers(String search, String order_by, int start, int limit) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Public:searchServers()");
		data.put("search", search);
		data.put("order_by", order_by);
		data.put("start", start + "");
		data.put("limit", limit + "");
		return query(data);
	}

	public Map<String, Object> searchServers(String search, String order_by, int pagination) {
		int start = 0;
		int limit = 28;
		if (pagination > 0) {
			start = ((pagination - 1) * 27);
		}
		return searchServers(search, order_by, start, limit);
	}

	public String getServerID() {
		String id = "";
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("method", "Plugin:getServerDetails()");
		Map<String, Object> response = query(data);
		if (response.containsKey("success") && (boolean) response.get("success") && response.containsKey("id")) {
			id = (String) response.get("id");
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
