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

public class Translater {
	
	private static HashMap<String, Translation> list = new HashMap<String, Translation>();
	
	private static Translation get(String id){
		if(list.containsKey(id)){
			return list.get(id);
		}
		return null;
	}
	
	public static void set(String id, Translation t){
		if(!list.containsKey(id)){
			list.put(id, t);
		}else{
			list.replace(id, t);
		}
	}
	
	public static String get(String id, Lang lang, String... args){
		Translation t = get(id);
		if(t != null){
			String result = t.getMessage(lang);
			if(result == null){
				result = "§cUnknow language !";
			}
			for(String arg : args){
				result = result.replaceFirst("%s", arg);
			}
			return result;
		}
		return "§cUnknow message !";
	}

}
