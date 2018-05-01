package com.sylcharin.pixelmoninfocommands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sylcharin
 */
@Mod(modid = "pixelmoninfocommands")
public class PixelmonSpawnInfo extends CommandBase implements IClientCommand{
    private final List<String> ALIASES;
    
    public PixelmonSpawnInfo() throws URISyntaxException {
		JSONHelper.getInstance();
    	ALIASES = new ArrayList<>();
		ALIASES.add("spawns");
    }
    
    public static void main(String[] args) throws URISyntaxException {
//        JSONHelper.getInstance();
//        HashMap<String, Pixelmon> monmap = JSONHelper.getPixelmon();
//        for (String name : monmap.keySet()){
//        	System.out.println(monmap.get(name).printInfo());
//		}
    }

	@Override
	public int compareTo(ICommand arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "spawninfo";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/spawninfo <Pokemon>";
	}

	@Override
	public List<String> getAliases() {
		// TODO Auto-generated method stub
		return this.ALIASES;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(args.length == 1)) {
			sender.sendMessage(new TextComponentString("Usage: " + this.getUsage(null)));
			return;
		}
		//If there was an error parsing BetterSpawnerConfig, try again. If it fails again, return an error message.
		if (JSONHelper.betterSpawnerConfigError != null){
			try {
				JSONHelper.getInstance().parseBetterSpawnerConfig();
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}
			if (JSONHelper.betterSpawnerConfigError != null){
				sender.sendMessage(new TextComponentString("Unable to parse BetterSpawnerConfig.json: " + JSONHelper.betterSpawnerConfigError));
				return;
			}
		}
		//If there was an using the Pixelmon JAR, try again. If it fails again, return an error message.
		if (JSONHelper.pixelmonJarError != null){
			try {
				JSONHelper.getInstance().getPixelmonJar();
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}
			if (JSONHelper.pixelmonJarError != null){
				sender.sendMessage(new TextComponentString("Unable to find or read the Pixelmon JAR file: " + JSONHelper.pixelmonJarError));
				return;
			}
		}

		String pixelmonName = JSONHelper.formatTitleCase(args[0]);
		if (pixelmonName.equals("Mimejr")) pixelmonName = "MimeJr";	//Capitalize the second J in the MimeJr edgecase
		Pixelmon pixelmon = JSONHelper.getPixelmon().get(pixelmonName);
		if (pixelmon == null) {
			sender.sendMessage(new TextComponentString("Unable to find pokemon " + args[0] + "!"));
			return;
		}
		sender.sendMessage(new TextComponentString(pixelmon.printInfo()));
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		final List<String> results = new ArrayList<>();
		final String argument = args[0].substring(0, 1).toUpperCase() + args[0].substring(1).toLowerCase();
		JSONHelper.getPixelmon().keySet().stream().forEach(key ->{
			Pixelmon pixelmon = JSONHelper.getPixelmon().get(key);
			if (pixelmon.getId().startsWith(argument)){
				results.add(pixelmon.getId());
			}
		});
		return results;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
	
	@EventHandler
	public void init(FMLPostInitializationEvent event) throws URISyntaxException {
		ClientCommandHandler.instance.registerCommand(new PixelmonSpawnInfo());
	}

	@Override
	public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
		return false;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
}

