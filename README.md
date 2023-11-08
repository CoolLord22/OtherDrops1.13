# OtherDrops
OtherDrops is a plugin for the Minecraft Bukkit API that lets you completely customize what blocks and dead mobs drop when they are destroyed. Apples from leaves, no more broken glass, and much, much more.

* Please check the [Wiki](https://github.com/CoolLord22/OtherDrops1.13/wiki) for advanced usage information
* See the [Spigot Page](https://www.spigotmc.org/resources/otherdrops-updated.51793/) for more download information
* Use the [Legacy branch](https://github.com/CoolLord22/OtherDrops) of the plugin for versions < 1.13


## Building from GitHub
These instructions assume you have already forked and/or cloned the project and have on your computer.

Change the plugin.yml (as generated by plugin.yml.template) to fix the version, potentially by adding your name with a dash (`3.1.3-Name`)

OtherDrops comes with most dependencies already stored in the repository (for simplicity) however you need to download Spigot API and server builds and place into the `lib` folder - renamed to `spigot.jar` and `spigot-server.jar`. You'll also need to obtain a copy of `authlib-1.5.21.jar`.

Then build using your IDE or:

    $ ant jar

Use `ant -p` to see a complete list of Ant tasks.


## Contact Us
If you have a problem please create a ticket and include the error (if there was one). Feel free to join the Discord server linked below! I'm super active there and tend to respond faster on it.

[![](https://i.imgur.com/Q9m1B9C.png)](https://discord.com/invite/eHBxk5q) 
[![](https://i.imgur.com/WyauNtT.png)](https://www.spigotmc.org/conversations/add?to=CoolLord22&title=OtherDrops%20Support)

### Metrics
[![](https://bstats.org/signatures/bukkit/OtherDrops.svg?sanitize=true)](https://bstats.org/plugin/bukkit/OtherDrops/3708)
Powered by [bStats](https://bstats.org/)
