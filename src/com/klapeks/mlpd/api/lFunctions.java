package com.klapeks.mlpd.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarFile;

import com.klapeks.coserver.aConfig;
import com.klapeks.coserver.dFunctions;

public class lFunctions {
	
	public static String prefix = "[MLPD] ";
	public static void log(Object obj) {
		dFunctions.log_(prefix + obj);
	}
	
	public static void errorDisable() {
		if (aConfig.shutdownOnError) {
			log("§cServer will be disabled, to prevent further errors");
			dFunctions.shutdown();
		}
	}
	

	public static int getRandom(int from, int to) {
		return getRandom(new Random(), from, to);
	}
	public static int getRandom(Random random, int from, int to) {
        return random.nextInt((to - from) + 1) + from;
	}
	
	public static int toInt(Object obj) {
		if (obj==null) return 0;
		if (obj instanceof Integer) {
			return (Integer) obj;
		}
		String ss = obj+"";
		try {return Integer.parseInt(ss);} catch(Throwable e) {};
		ss = _filter_(ss, "-1234567890");
		if (ss.equals("")) return 0;
		try {return Integer.parseInt(ss);} catch(Throwable e) {return 0;}
	}
	
	private static String _filter_(String string, String filter) {
		String integer = "";
		for (String s : string.split("")) {
			if (filter.contains(s)) integer=integer+s;
		}
		return integer;
	}
	public static long toLong(Object obj) {
		if (obj==null) return 0;
		if (obj instanceof Long) {
			return (Long) obj;
		}
		String ss = obj+"";
		try {return Long.parseLong(ss);} catch(Throwable e) {};
		ss = _filter_(ss, "-1234567890");
		if (ss.equals("")) return 0;
		try {return Long.parseLong(ss);} catch(Throwable e) {return 0;}
	}
	public static Map<String, String> getAllParameters(String txt, String sep) {
		Map<String, String> map = new HashMap<>();
		if (!txt.contains(sep)) return map;
		txt = txt.replace(" "+sep, sep);
		String[] ss = txt.split(sep);
		if (ss.length==1) ss = txt.split("\\"+sep);
		ss[0] = null;
		for (String s : ss) {
			if (s==null) continue;
			if (s.contains(" ")) {
				map.put(s.split(" ")[0], s.substring(s.split(" ")[0].length()+1));
			} else {
				map.put(s, null);
			}
		}
		return map;
	}
	
	public static String getPluginName(String path) {
		return getPluginName(new File(path));
	}
	
	public static String getPluginName(File executable) {
		try {
			JarFile jar = new JarFile(executable);
			InputStream fileInputStreamReader = jar.getInputStream(jar.getJarEntry("plugin.yml"));
			BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStreamReader));
			String s = null;
			String name = null;
			while ((s=br.readLine())!=null) {
				if (s.startsWith("name:")) {
					name = s.substring(5);
					break;
				}
			}
			br.close();
			fileInputStreamReader.close();
			jar.close();
			while (name.startsWith(" ")) name = name.substring(1);
			while (name.startsWith("\"")) name = name.substring(1);
			while (name.endsWith("\"")) name = name.substring(0, name.length()-1);
			while (name.startsWith("'")) name = name.substring(1);
			while (name.endsWith("'")) name = name.substring(0, name.length()-1);
			return name;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
