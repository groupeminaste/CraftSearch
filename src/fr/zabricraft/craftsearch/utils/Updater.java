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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import fr.zabricraft.craftsearch.CraftSearch;

public class Updater {

	private static boolean updated = false;

	public static void checkForUpdate(Player... receive) {
		if (CraftSearch.getInstance().isUpdater() && !updated) {
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("method", "Updater:checkForUpdate()");
			data.put("version", CraftSearch.getInstance().getDescription().getVersion());
			HashMap<String, String> response = CraftSearch.getInstance().query(data);
			if (response.containsKey("success") && response.get("success").equals("true")) {
				CraftSearch.getInstance().getLogger().warning(response.get("message"));
				try {
					CraftSearch.getInstance().getLogger().info(
							"Trying to download CraftSearch " + response.get("latest") + " from CraftSearch Server...");
					URL download = new URL(response.get("download"));
					URLConnection c = download.openConnection();
					c.setRequestProperty("User-Agent", CraftSearch.getInstance().getDescription().getName() + "/"
							+ CraftSearch.getInstance().getDescription().getVersion());
					ReadableByteChannel rbc = Channels.newChannel(c.getInputStream());
					FileOutputStream fos = new FileOutputStream(
							new File(CraftSearch.getInstance().getFile().getParentFile(),
									"CraftSearch_" + response.get("latest") + ".jar"));
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					fos.close();
					CraftSearch.getInstance().getLogger()
							.info("CraftSearch " + response.get("latest") + " downloaded!");
					CraftSearch.getInstance().getFile().delete();
					updated = true;
					Bukkit.getScheduler().scheduleSyncRepeatingTask(CraftSearch.getInstance(), new Runnable() {
						@Override
						public void run() {
							if (Bukkit.getOnlinePlayers().size() == 0) {
								CraftSearch.getInstance().getLogger().info(
										"No online players! Deleting previous version and restarting with new version...");
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
							}
						}
					}, 0, 20);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (CraftSearch.getInstance().isUpdater()) {
			try {
				String url = "https://www.craftsearch.net/plugin/checkforupdate_pg.php?version=1.0";

				URL obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				con.setRequestMethod("GET");

				con.setRequestProperty("User-Agent", CraftSearch.getInstance().getDescription().getName() + "/"
						+ CraftSearch.getInstance().getDescription().getVersion());

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				String msg = response.toString();

				if (!msg.isEmpty()) {
					msg = ChatColor.translateAlternateColorCodes('&', msg);
					if (receive.length != 0) {
						for (Player p : receive) {
							p.sendMessage(msg);
						}
					} else {
						CraftSearch.getInstance().getLogger().warning(msg);
					}
				}
			} catch (Exception e) {
			}
		}
	}

}
