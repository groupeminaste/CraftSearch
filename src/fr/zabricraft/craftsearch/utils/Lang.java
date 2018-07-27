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

public enum Lang {

	DEFAULT("Default"), EN("English"), FR("Français"), ES("Español"), DE("Deutsch"), SR("Српски - ћирилицом (бета)"), HR(
			"Hrvatski"), RU("Русский"), NL("Nederlands");

	private String name;

	Lang(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static Lang get(String id) {
		for (Lang l : values()) {
			if (l.toString().equalsIgnoreCase(id) || l.getName().equalsIgnoreCase(id)) {
				return l;
			}
		}
		return Lang.EN;
	}

}
