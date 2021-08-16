# MultiLoaderPluginDownloader
Plugin that downloads/updates plugins from bungeecord

SpigotMC: https://www.spigotmc.org/resources/multiloaderplugindownloader.95247/

Depends: https://www.spigotmc.org/resources/coserverapi.95244/

Please join our discord server https://discord.gg/TWEy37Frh2


### How does it work?
You have a lot of servers (~100) and plugins like ViaVersion, ProtocolLib, etc. which updates very often.
You don't want to manually update the plugin on all your servers after every update. What to do?
MultiLoaderPluginDownloader is one of solutions (maybe only one).
You probably have a bungeecord. You can put this plugin(VV, PL, HDB) on a special folder in your bungeecord server and download/update it from bukkit(spigot/paper, etc) while the server is enabling.


### How to use it?

Bungeecord side:
create folder 'MLPD_plugins' and put your plugin here

Bukkit/Spigot side:
in configuration file specify the path to the plugin

### Configuration be like:

```
folder1:
- plugin1
- plugin2
- plugin3
- plugin4
folder2:
- plugin5
- plugin6
folder3: [plugin7, plugin8, plugin9]
```

### Easy API:

```java
//Automatically checks new version of the plugin and in case if old updates it
MLPD.from(folder).using(plugin);
```
