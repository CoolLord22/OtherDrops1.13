# OtherDrops [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Discord](https://img.shields.io/discord/418432278113550337.svg?logo=discord&logoWidth=18&colorB=7289DA)](https://discordapp.com/invite/eHBxk5q)
OtherDrops is a plugin for the Minecraft Bukkit API that lets you completely customize what blocks and dead mobs drop when they are destroyed. Apples from leaves, no more broken glass, and much, much more.

* Please check the [Wiki](https://github.com/CoolLord22/OtherDrops1.13/wiki) for advanced usage information
* See the [Spigot Page](https://www.spigotmc.org/resources/otherdrops-updated.51793/) for more download information
* Use the [Legacy branch](https://github.com/CoolLord22/OtherDrops) of the plugin for versions < 1.13
---

### Donation Link
If you appreciate our plugins and support, consider donating and showin' us some love <3 

[![ko-fi](https://www.ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/O4O425D12)

---

### Building from GitHub (Maven)

These instructions assume you have already forked and/or cloned the project and have Java and Maven installed.

#### Prerequisites
- Java 17+
- Maven 3.8+
- Git

No manual dependency downloads are required. All dependencies are resolved automatically via Maven.

#### 1. Clone the repository
```
git clone https://github.com/<your-fork>/OtherDrops.git
cd OtherDrops
```
#### 2. Plugin versioning

The plugin version is generated automatically during the Maven build and injected into `plugin.yml`.
Example format: `3.2.8-b579`

To change the base version (for forks or local testing), **please** update the version in `pom.xml`:
```
<version>3.2.8</version>
```
The build number will be appended automatically. No manual edits to `plugin.yml` are required.

#### 3. Build the plugin
Run: `mvn clean package`

This will:
- Compile the plugin
- Generate `plugin.yml` with the correct version
- Build all special event jars
- Package event jars into the final plugin

#### 4. Output

After a successful build, the final plugin jar will be located at: `target/OtherDrops-<version>.jar`
and is ready to be placed directly into your server's `/plugins` folder.

#### Notes:
- Ant is no longer used and is not required
- No `lib/` or `dependencies/` folder is needed
- Spigot/Paper APIs are resolved automatically via Maven
- Event jars are generated during the build and should not be edited manually

---

### Contact Us
If you have a problem please create a ticket and include the error (if there was one). Feel free to join the Discord Server linked above! I'm super active there and tend to respond faster on it.

---

### Metrics
[![](https://bstats.org/signatures/bukkit/OtherDrops.svg?sanitize=true)](https://bstats.org/plugin/bukkit/OtherDrops/3708)
Powered by [bStats](https://bstats.org/)
