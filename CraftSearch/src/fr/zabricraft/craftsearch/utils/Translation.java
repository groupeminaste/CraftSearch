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

public class Translation {

	private HashMap<Lang, String> messages = new HashMap<Lang, String>();

	public Translation(TranslatedMsg... msgs) {
		for (TranslatedMsg msg : msgs) {
			if (!messages.containsKey(msg.getLang())) {
				messages.put(msg.getLang(), msg.getMessage());
			}
		}
	}

	public String getMessage(Lang lang) {
		if (lang == null) {
			return "§cUnknown language !";
		}
		if (lang.equals(Lang.DEFAULT)) {
			lang = Lang.EN;
		}
		if (messages.containsKey(lang)) {
			return messages.get(lang);
		}
		return messages.get(Lang.EN);
	}

}
