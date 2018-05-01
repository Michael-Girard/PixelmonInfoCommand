# PixelmonInfoCommand
Command for displaying pixelmon information in chat.

Known Issues:
1) Minecraft with the pixelmon mod must be run once without the command jar in the mods folder. This is to have the pixelmon mod create BetterSpawnerConfig.json in Minecraft's config folder. If BetterSpawnerConfig.json does not exist, the mod will throw an error during minecraft's startup and the game will not start.

2) (Potentially Fixed on 1.2 - Needs Testing) The command does not work with technic because PixelmonInfoCommand looks for files in the vanilla minecraft folder. This will be fixed, but for now the workaround involves copying a couple of files into the vanilla minecraft folders. You'll need to run the mod at least once first so BetterSpawnerConfig.json is created, and then...

		a) Copy BetterSpawnerConfig.json from "%appdata%\roaming\.technic\modpacks\pixelmon-reforged\config\pixelmon" into "%appdata%\roaming\.minecraft\config\pixelmon". If the folders inside .minecraft do not exist, create them. Creating a shortcut instead may work, but it's untested.

		b) Copy the Pixelmon JAR file from "%appdata%\roaming\.technic\modpacks\pixelmon-reforged\mods" into "%appdata%\roaming\.minecraft\mods". If the mods folder inside .minecraft does not exist, create it. Creating a shortcut instead may work, but it's untested.
