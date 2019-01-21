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

public enum Grade {

	PLAYER(false), PARTNER(false), ADS(true), TRANSLATER(true), BUILDER(true), ADMIN(true);

	public static Grade get(String id) {
		for (Grade s : values()) {
			if (s.toString().equalsIgnoreCase(id)) {
				return s;
			}
		}
		return Grade.PLAYER;
	}
	
	private boolean staff;
	
	private Grade(boolean staff) {
		this.staff = staff;
	}
	
	public boolean isStaff(){
		return staff;
	}

}
