package com.klapeks.mlpd.bungee;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.klapeks.coserver.aConfig;
import com.klapeks.coserver.dFunctions;
import com.klapeks.coserver.dRSA;
import com.klapeks.coserver.plugin.bungee.BungeeCoserv;

public class MLPDServer {
	
	private static String[] doArgs(String req) {
		String[] ss = req.split(" ");
		for (int i = 0; i < ss.length; i++) {
			try { 
				ss[i] = dRSA.base64_decode(ss[i]);
			} 
			catch (Throwable r) {}
		}
		return ss;
	}
	
	static final String fs = File.separator;
	static final int iqii = 500;
	static HashMap<String, List<String>> cashdata = new HashMap<>();
	
	static void __init__() {
		Function<String, String> minihandler = request ->{
			String[] args = doArgs(request);
			switch (args[0]) {
			case "checkfolder": {
				try {
					File file = new File(MainBungee.folder + fs + args[1].replace("/", fs));
					dFunctions.debug("§eChecking folder: " + file);
					if (file.exists()) return "true";
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "false";
			}
			case "checkplugin": {
				try {
					File file = new File(MainBungee.folder + fs + args[1].replace("/", fs) + fs + args[2]+".jar");
					dFunctions.debug("§eChecking plugin: " + file);
					if (file.exists()) return "true";
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "false";
			}
			case "getpluginlastmodified": {
				try {
					File file = new File(MainBungee.folder + fs + args[1].replace("/", fs) + fs + args[2]+".jar");
					if (file.exists()) return file.lastModified()+"";
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "-1";
			}
			case "startplugindownloading": {
				File plugin = new File(MainBungee.folder + fs + args[1].replace("/", fs) + fs + args[2]+".jar");
				dFunctions.debug("§eServer tries download plugin: " + plugin);
				if (!plugin.exists()) return "-1";
				try {
					byte[] bytes = Files.readAllBytes(plugin.toPath());
//					MainBungee.log("s: " + s);
					dFunctions.log("§6Encoding... " + bytes.length);
					final int iqii = 500;
					List<String> cash = new ArrayList<>();
					for (int a = 0; a <= bytes.length; a = a + iqii) {
						byte[] na = Arrays.copyOfRange(bytes, a, (a+iqii) > bytes.length ? bytes.length : (a + iqii));
						cash.add(dRSA.base64_encode_byte(na));
					}
					String randpsw = dRSA.generateSecretKey(3);
					cashdata.put(randpsw, cash);
					return cash.size() + " " + randpsw;
				} catch (Throwable t) {
					t.printStackTrace();
					return "-1";
				}
			}
			case "downloadpluginstage": {
				String secretPsw = args[1];
				if (!cashdata.containsKey(secretPsw)) return "null";
				try {
					return cashdata.get(secretPsw).get(dFunctions.toInt(args[2]));
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "null";
			}
			case "mbneedclearcash": {
				String secretPsw = args[1];
				cashdata.remove(secretPsw);
				return "ok";
			}

			default: break;
			}
			return "404error";
		};
		if (aConfig.useSecurity) {
			BungeeCoserv.addSecurityHandler("multiloaderplugindownloader", minihandler);
		} else {
			BungeeCoserv.addHandler("multiloaderplugindownloader", minihandler);
		}
	}
}
