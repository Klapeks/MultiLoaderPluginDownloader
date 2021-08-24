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
import com.klapeks.mlpd.api.lFunctions;

public class MLPDServer {
	
	public static void main(String[] args) {
		String file_with_config = "Citizens";
		File file = new File("H://_KlapoMatia//DeadLight//plugins_MLPD//important//".replace("//", fs)+file_with_config);
		System.out.println(file.isDirectory());
		List<String> list = new ArrayList<>();
		deep(list, file, file.getParentFile().getParent());
		list.forEach(p->System.out.println(p));
	}
	public static void deep(List<String> list, File file, String prefix) {
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				deep(list, f, prefix);
				continue;
			}
			list.add((f+"").substring(prefix.length()+1));
		}
	}
	
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
	static HashMap<String, List<String>> cashdata_config = new HashMap<>();
	
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
			
			//PLUGIN
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
					dFunctions.debug("§6Encoding plugin {plugin}... ".replace("{plugin}", args[2]) + bytes.length);
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
			
			//CONFIGS
			//1 - folder
			//2 - plugin
			//3 - cfg
			case "startdownloadconfig": {
				File config = new File(MainBungee.folder + fs + args[1].replace("/", fs) + fs + args[2].replace("/", fs) + fs + args[3].replace("/", fs));
				dFunctions.debug("§eServer tries download config: " + config);
				if (!config.exists()) return "-1";
				try {
					byte[] bytes = Files.readAllBytes(config.toPath());
//					MainBungee.log("s: " + s);
					dFunctions.debug("§6Encoding config... " + bytes.length);
					final int iqii = 500;
					List<String> cash = new ArrayList<>();
					for (int a = 0; a <= bytes.length; a = a + iqii) {
						byte[] na = Arrays.copyOfRange(bytes, a, (a+iqii) > bytes.length ? bytes.length : (a + iqii));
						cash.add(dRSA.base64_encode_byte(na));
					}
					String randpsw = dRSA.generateSecretKey(3);
					cashdata_config.put(randpsw, cash);
					return cash.size() + " " + randpsw;
				} catch (Throwable t) {
					t.printStackTrace();
					return "-1";
				}
			}
			case "downloadconfigstage": {
				String secretPsw = args[1];
				if (!cashdata_config.containsKey(secretPsw)) return "null";
				try {
					return cashdata_config.get(secretPsw).get(dFunctions.toInt(args[2]));
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "null";
			}
			case "getconfiglistoffolder": {
				try {
					File file = new File(MainBungee.folder + fs + args[1].replace("/", fs) + fs + args[2].replace("/", fs));
					if (!file.exists()) return "null";
					if (!file.isDirectory()) return "null";
					
					String str = ""; int g = (file+"").length()+1;
					List<File> files = getListOfConfigs(file);
					for (File f : files) {
						str += ",,,,," + (f+"").substring(g).replace(fs, "/");
					}
					if (str.startsWith(",,,,,")) str = str.replaceFirst(",,,,,", "");
					return str;
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "null";
			}
			case "checkfolderwithconfigs": {
				try {
					File file = new File(MainBungee.folder + fs + args[1].replace("/", fs) + fs + args[2].replace("/", fs));
					dFunctions.debug("§eChecking folder with configs: " + file);
					if (file.exists()) return "true";
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "false";
			}
			case "checkconfiginfolderwithconfigs": {
				try {
					File file = new File(MainBungee.folder + fs + args[1].replace("/", fs) + fs + args[2].replace("/", fs) + fs + args[3].replace("/", fs));
					dFunctions.debug("§eChecking config in folder: " + file);
					if (file.exists()) return "true";
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "false";
			}
			case "getconfiglastmodified": {
				try {
					File file = new File(MainBungee.folder + fs + args[1].replace("/", fs) + fs + args[2].replace("/", fs) + fs + args[3].replace("/", fs));
					if (file.exists()) return file.lastModified()+"";
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "-1";
			}
			case "mbneedclearconfigcash": {
				String secretPsw = args[1];
				cashdata_config.remove(secretPsw);
				return "ok";
			}

			default: {
				lFunctions.log("Unknown request: " + Arrays.toString(args));
				break;
			}
			}
			return "404error";
		};
		if (aConfig.useSecurity) {
			BungeeCoserv.addSecurityHandler("multiloaderplugindownloader", minihandler);
		} else {
			BungeeCoserv.addHandler("multiloaderplugindownloader", minihandler);
		}
	}
	private static List<File> getListOfConfigs(File file) {
		List<File> f = new ArrayList<>();
		getListOfConfigs(f, file);
		return f;
	}
	private static void getListOfConfigs(List<File> f, File file) {
		for (File fl : file.listFiles()) {
			if (fl.isDirectory()) {
				getListOfConfigs(f, fl);
				continue;
			}
			f.add(fl);
		}
	}
}
