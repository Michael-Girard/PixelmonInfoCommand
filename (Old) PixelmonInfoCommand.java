package com.sylcharin.pixelmoninfocommand;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;

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
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
/**
 * @author Sylcharin
 */
@Mod(modid = "pixelmoninfocommand")
public class PixelmonInfoCommand extends CommandBase implements IClientCommand{
    private static final BetterSpawnerConfig CONFIG;
    private static final JarFile PIXELMON_JAR;
    private static final Path BETTERSPAWNERCONFIG_PATH = Paths.get(System.getenv("APPDATA") + "\\.minecraft\\config\\pixelmon\\betterspawnerconfig.json");
    private static final Path MINECRAFT_MODS_PATH = Paths.get(System.getenv("APPDATA") + "\\.minecraft\\mods");
    private static final List<JarEntry> JSON_FILES = new ArrayList<>();
    private static final String JSON_LOCATION_IN_JAR = "assets/pixelmon/spawning/".toUpperCase();
    
    private static final HashMap<String, Pixelmon> PIXELMON = new HashMap<>();
    private static final HashMap<Biome, String> BIOME_MAP = new HashMap<>();
    private static Gson gson = new Gson();

    private final List<String> ALIASES;
        
    static{
        //Static initializer prepares all of the necessary files
        //First we need to prepare the BetterSpawnerConfig.json file.
        if (!Files.exists(BETTERSPAWNERCONFIG_PATH)){
            throw new RuntimeException("ERROR: BetterSpawningConfig.json not found in " + BETTERSPAWNERCONFIG_PATH.toAbsolutePath() + ". Try running Pixelmon once.");
        }
        try(JsonReader configReader = new JsonReader(new InputStreamReader(new FileInputStream(BETTERSPAWNERCONFIG_PATH.toFile()), "UTF-8"))){
            //Use the Json file to create an instance of BetterSpawnerConfig, then
            CONFIG = new Gson().fromJson(configReader, BetterSpawnerConfig.class);            
        } 
        catch (IOException ex) {
            throw new RuntimeException("ERROR: Error converting BetterSpawnerConfig.json into code.");
        }
        
        //Next, we can get the pixelmon jar file
        PIXELMON_JAR = getPixelmonJar(MINECRAFT_MODS_PATH);
        if (PIXELMON_JAR == null){
            throw new RuntimeException("ERROR: No Pixelmon Jar File Found.");
        }
        
        //Use the jar file to build the list of json files
        buildJSONList();
        
        //Build the BIOME_MAP
        buildBiomeMap();
        
        //Build the PIXELMON map
        JSON_FILES.forEach((entry) -> {
            String pokemonName = entry.getName();
            if (!pokemonName.contains("fish")/* && pokemonName.contains("Leafeon")*/) {
                Pixelmon pixelmon = getPixelmon(entry);
                pixelmon.init();
                PIXELMON.put(pixelmon.getId(), pixelmon);
            }
        });
    }
    
    public PixelmonInfoCommand() {
    	ALIASES = new ArrayList<>();
    	ALIASES.add("poke");
    	ALIASES.add("spawn");
    	ALIASES.add("pokemon");
    	ALIASES.add("pixelmon");
    	
    }
    
    public static void main(String[] args) {
        PIXELMON.keySet().forEach((pixelmon) -> {
            PIXELMON.get(pixelmon).printInfo();
        });
    }
    
    private static Pixelmon getPixelmon(JarEntry entry){
        try(JsonReader reader = new JsonReader(new InputStreamReader(PIXELMON_JAR.getInputStream(entry)))){
            Pixelmon pixelmon = gson.fromJson(reader, Pixelmon.class);
            return pixelmon;
        }
        catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
         
    private static JarFile getPixelmonJar(Path modsDirectory){
        try {
            List<Path> jarFileName = Files.list(modsDirectory)
                    .filter(Files::isRegularFile)   //Filters out directories
                    .filter(fileName -> {           //Filters out files that don't start with PIXELMON and end with .JAR
                            String file = fileName.getFileName().toString().toUpperCase();
                            return file.startsWith("PIXELMON") && file.endsWith(".JAR");
                        })
                    .collect(Collectors.toList());  //Collects the remaining files in the list
            if (!jarFileName.isEmpty()){
                //If the stream found a file, return the first one - there should only be one in the directory anyway
                return new JarFile(jarFileName.get(0).toFile());
            }
        } catch (IOException ex) {
            Logger.getLogger(PixelmonInfoCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static void buildJSONList(){
        //Opening the jar file
        //Stream isolates all of the pokemon spawn json files into a list of JarEntries
        PIXELMON_JAR.stream()
            .filter(filePath -> {
                String filePathString = filePath.getName().toUpperCase();
                return filePathString.startsWith(JSON_LOCATION_IN_JAR) && filePathString.endsWith(".JSON");
            })
            .forEach(entry -> JSON_FILES.add(entry));   
    }
    
    private static void buildBiomeMap(){
        BIOME_MAP.put(Biome.beaches, "Beach");
        BIOME_MAP.put(Biome.birch_forest, "Birch Forest");
        BIOME_MAP.put(Biome.birch_forest_hills, "Birch Forest Hills");
        BIOME_MAP.put(Biome.cold_beach, "Cold Beach");
        BIOME_MAP.put(Biome.cold_deep_ocean, "Cold Deep Ocean");
        BIOME_MAP.put(Biome.cold_ocean, "Cold Ocean");
        BIOME_MAP.put(Biome.deep_ocean, "Deep Ocean");
        BIOME_MAP.put(Biome.desert, "Desert");
        BIOME_MAP.put(Biome.desert_hills, "Desert Hills");
        BIOME_MAP.put(Biome.extreme_hills, "Extreme Hills");
        BIOME_MAP.put(Biome.extreme_hills_with_trees, "Extreme Hills +");
        BIOME_MAP.put(Biome.forest, "Forest");
        BIOME_MAP.put(Biome.forest_hills, "Forest Hills");
        BIOME_MAP.put(Biome.frozen_deep_ocean, "Frozen Deep Ocean");
        BIOME_MAP.put(Biome.frozen_ocean, "Frozen Ocean");
        BIOME_MAP.put(Biome.frozen_river, "Frozen River");
        BIOME_MAP.put(Biome.hell, "The Nether");
        BIOME_MAP.put(Biome.ice_flats, "Ice Plains");
        BIOME_MAP.put(Biome.ice_mountains, "Ice Mountains");
        BIOME_MAP.put(Biome.jungle, "Jungle");
        BIOME_MAP.put(Biome.jungle_edge, "Jungle Edge");
        BIOME_MAP.put(Biome.jungle_hills, "Jungle Hills");
        BIOME_MAP.put(Biome.lukewarm_deep_ocean, "Lukewarm Deep Ocean");
        BIOME_MAP.put(Biome.lukewarm_ocean, "Lukewarm Ocean");
        BIOME_MAP.put(Biome.mesa, "Mesa");
        BIOME_MAP.put(Biome.mesa_clear_rock, "Mesa Plateau");
        BIOME_MAP.put(Biome.mesa_rock, "Mesa Plateau F");
        BIOME_MAP.put(Biome.mushroom_island, "Mushroom Island");
        BIOME_MAP.put(Biome.mushroom_island_shore, "Mushroom Island Shore");
        BIOME_MAP.put(Biome.mutated_birch_forest, "Birch Forest M");
        BIOME_MAP.put(Biome.mutated_birch_forest_hills, "Birch Forest Hills M");
        BIOME_MAP.put(Biome.mutated_desert, "Desert M");
        BIOME_MAP.put(Biome.mutated_extreme_hills, "Extreme Hills M");
        BIOME_MAP.put(Biome.mutated_extreme_hills_with_trees, "Extreme Hills + M");
        BIOME_MAP.put(Biome.mutated_forest, "Flower Forest");
        BIOME_MAP.put(Biome.forest_hills, "Forest Hills M");
        BIOME_MAP.put(Biome.mutated_ice_flats, "Ice Spikes Plains");
        BIOME_MAP.put(Biome.mutated_jungle, "Jungle M");
        BIOME_MAP.put(Biome.mutated_jungle_edge, "Jungle Edge M");
        BIOME_MAP.put(Biome.mutated_mesa, "Mesa M");
        BIOME_MAP.put(Biome.mutated_mesa_clear_rock, "Mesa Plateau M");
        BIOME_MAP.put(Biome.mutated_mesa_rock, "Mesa Plateau F M");
        BIOME_MAP.put(Biome.mutated_plains, "Sunflower Plains");
        BIOME_MAP.put(Biome.mutated_redwood_taiga, "Mega Taiga M");
        BIOME_MAP.put(Biome.mutated_redwood_taiga_hills, "Mega Taiga Hills M");
        BIOME_MAP.put(Biome.mutated_roofed_forest, "Roofed Forest M");
        BIOME_MAP.put(Biome.mutated_savanna, "Savanna M");
        BIOME_MAP.put(Biome.mutated_savanna_rock, "Savanna Plateau M");
        BIOME_MAP.put(Biome.mutated_swampland, "Swampland M");
        BIOME_MAP.put(Biome.mutated_taiga, "Taiga M");
        BIOME_MAP.put(Biome.mutated_taiga_cold, "Cold Taiga M");
        BIOME_MAP.put(Biome.ocean, "Ocean");
        BIOME_MAP.put(Biome.plains, "Plains");
        BIOME_MAP.put(Biome.redwood_taiga, "Mega Taiga");
        BIOME_MAP.put(Biome.redwood_taiga_hills, "Mega Taiga Hills");
        BIOME_MAP.put(Biome.river, "River");
        BIOME_MAP.put(Biome.roofed_forest, "Roofed Forest");
        BIOME_MAP.put(Biome.savanna, "Savanna");
        BIOME_MAP.put(Biome.savanna_rock, "Savanna Plateau");
        BIOME_MAP.put(Biome.sky, "The End");
        BIOME_MAP.put(Biome.sky_island_barren, "The End Barren Island");
        BIOME_MAP.put(Biome.sky_island_high, "The End High Island");
        BIOME_MAP.put(Biome.sky_island_low, "The End Low Island");
        BIOME_MAP.put(Biome.sky_island_medium, "The End Medium Island");
        BIOME_MAP.put(Biome.smaller_extreme_hills, "Extreme Hills Edge");
        BIOME_MAP.put(Biome.stone_beach, "Stone Beach");
        BIOME_MAP.put(Biome.swampland, "Swampland");
        BIOME_MAP.put(Biome.taiga, "Taiga");
        BIOME_MAP.put(Biome.taiga_cold, "Cold Taiga");
        BIOME_MAP.put(Biome.taiga_cold_hills, "Cold Taiga Hills");
        BIOME_MAP.put(Biome.taiga_hills, "Taiga Hills");
        BIOME_MAP.put(Biome.the_void, "The Void");
        BIOME_MAP.put(Biome.warm_deep_ocean, "Warm Deep Ocean");
        BIOME_MAP.put(Biome.warm_ocean, "Warm Ocean");
    }
    
    public static class Pixelmon{
        private String id;
        private Object[] spawnInfos;
        private List<HashMap<String, ArrayList<Object>>> information = new ArrayList<>();
        
        private void init(){
            for (Object o : spawnInfos){
                information.add(parseSpawnInfo((LinkedTreeMap) o));
            }
        }
        
        private HashMap<String, ArrayList<Object>> parseSpawnInfo(LinkedTreeMap spawnInfo){
            //This method takes the spawnInfo that is raw from the Json and translates it to something more readable.
            HashMap<String, ArrayList<Object>> results = new HashMap<>();
    
            //Values in the spawnInfo can include LinkedTreeMaps, ArrayList, Integers, Strings, and Doubles. Probably more.
            //We need to do different things depending on what type of object it is.
            spawnInfo.keySet().forEach((key) -> {
                Object value = spawnInfo.get(key);
                //if the value is a String, Double, or Integer, it can be added to the results as-is
                if (value instanceof String || value instanceof Double || value instanceof Integer){
                    ArrayList<Object> values;
                    //Add the value to an existing arraylist if one exists at the key
                    if (results.containsKey(key.toString())){
                        values = results.get(key.toString());
                        if (!values.contains(value.toString())){
                            values.add(value.toString()); 
                        }
                    }
                    //Else create a new one
                    else{
                        values = new ArrayList<>();
                        values.add(value.toString());
                        results.put(key.toString(), values);
                    }
                }
                //If the value is not a String, Double, or Integer, send it to this recursive helper method that will
                //  repeatedly go down levels until a String, Double, or Integer is found and add them to the results
                //  anticonditions are omitted
                else /*if (!key.equals("anticondition"))*/{
                    parseSpawnInfo(null, key, value, results);
                }
            });
            return results;
        }

        private void parseSpawnInfo(Object previousKey, Object key, Object o, HashMap<String, ArrayList<Object>> results){
            //This is the recursive helper method for the other parseSpawnInfo method
            //If the object passed in is a LinkedTreeMap, we get a keyset from it and iterate over the values
            if (o instanceof LinkedTreeMap){
                LinkedTreeMap<Object, Object> ltm = (LinkedTreeMap)o;
                ltm.keySet().forEach((key2) -> {
                    final Object keyForLambda = (key != null && key.equals("anticondition") ? key : key2);
                    Object value = ltm.get(key2);
                    //if the value is a String, Double, or Integer, it can be added to the results as-is
                    if (value instanceof String || value instanceof Double || value instanceof Integer){
                        ArrayList<Object> values;
                        //Add the value to an existing arraylist if one exists at the key
                        if (results.containsKey(keyForLambda.toString())){
                            values = results.get(keyForLambda.toString());
                            if (!values.contains(value.toString())){
                                values.add(value.toString());
                            }
                        }
                        //Else create a new one
                        else{
                            values = new ArrayList<>();
                            values.add(value.toString());
                            results.put(keyForLambda.toString(), values);
                        }
                    }
                    //If the value is not a String, Double, or Integer, recursively call this method with it
                    else{
                        parseSpawnInfo(key, key2, value, results);
                    }
                });
            }
            else if (o instanceof ArrayList){
                ArrayList list = (ArrayList)o;
                final Object keyForLambda = (previousKey != null && previousKey.equals("anticondition") ? previousKey : key);
                //For each value in the ArrayList, we check if it is a biome or list of biomes.
                list.stream().map((value) -> {
                    //If it is a list a biomes, send each biome to the formatBiome method.
                    //  convert them from biome groups and biomeIds into actual in-game names
                    if (key.toString().equals("stringBiomes")){
                        String newValue = formatBiome(value.toString()).toString();
                        newValue = newValue.replaceAll("\\[", "").replaceAll("]", "");
                        value = newValue;
                    }
                    return value;
                }).forEachOrdered((value) -> {
                    if (value.toString().startsWith("pixelmon:")){
                        String temp = value.toString();
                        temp = temp.substring("pixelmon:".length());
                        temp = temp.substring(0, 1).toUpperCase() + temp.substring(1);
                        temp = temp.replaceFirst("rock", " Rock");
                        value = temp;
                    }
                    else if (value.toString().startsWith("minecraft:")){
                        String temp = value.toString();
                        temp = temp.substring("minecraft:".length());
                        temp = temp.substring(0, 1).toUpperCase() + temp.substring(1);
                        temp = temp.replaceFirst("rock", " Rock");
                        value = temp;
                    }
                    else{
                        String temp = value.toString();
                        temp = temp.substring(0, 1).toUpperCase() + temp.substring(1);
                        temp = temp.replaceFirst("grass", "Grass");
                        value = temp;
                    }
                    ArrayList<Object> values;
                    //Add the value to an existing arraylist if one exists at the key
                    if (results.containsKey(keyForLambda.toString())){
                        values = results.get(keyForLambda.toString());
                        values.add(value.toString());
                    }
                    //Else create a new one
                    else{
                        
                        values = new ArrayList<>();
                        values.add(value.toString());
                        results.put(keyForLambda.toString(), values);
                    }
                });
            }
        }

        private static List<String> formatBiome(String biomeName){
            List<String> results = new ArrayList<>();
            if (biomeName != null){
                Object o = ((LinkedTreeMap)CONFIG.biomeCategories).get(biomeName);
                if (o != null){
                    String[] biomeArray = null;
                    String biomeGroup = o.toString();
                    biomeGroup = biomeGroup.substring(1, biomeGroup.length() - 1);
                    biomeArray = biomeGroup.split(", ");
                    for (String b : biomeArray){
                        if (b.startsWith("minecraft:")){
                            String temp = b.substring("minecraft:".length());
                            results.add(BIOME_MAP.get(Biome.valueOf(temp)));
                        }
                    }
                }
                else{
                    if (biomeName.startsWith("minecraft:")){
                        biomeName = biomeName.substring("minecraft:".length());
                    }
                    results.add(BIOME_MAP.get(Biome.valueOf(biomeName)));
                }
            }
            return results;
        }
        
        public String printInfo(){
        	StringBuilder sb = new StringBuilder();
            sb.append(("\n---------------------\n" + this.getId() + "\n---------------------\n"));
            this.getInformation().stream().forEach((spawns) -> {
                Object biomes = ((HashMap) spawns).get("stringBiomes");
                Object nearbyBlocks = ((HashMap) spawns).get("neededNearbyBlocks");
                Object locations = ((HashMap) spawns).get("stringLocationTypes");
                Object times = ((HashMap) spawns).get("times");
                Object weathers = ((HashMap) spawns).get("weathers");
                Object temperature = ((HashMap) spawns).get("temperature");
                Object minY = ((HashMap) spawns).get("minY");
                Object maxY = ((HashMap) spawns).get("maxY");
                Object level =  ((HashMap) spawns).get("level");
                Object minLevel = ((HashMap) spawns).get("minLevel");
                Object maxLevel = ((HashMap) spawns).get("maxLevel");
                Object rarity = ((HashMap) spawns).get("rarity");
                Object anticondition = ((HashMap) spawns).get("anticondition");
                if (level != null){
                    System.out.println("Level: " + level.toString());
                }
                else{
                    if (minLevel != null || maxLevel != null){
                    	sb.append("Levels: ");
                    }
                    sb.append(minLevel != null ? minLevel + " - " : "? - ");
                    sb.append(maxLevel != null ? maxLevel.toString() + "\n" : "?\n");
                }
                sb.append(biomes != null ? "Biomes: " + biomes.toString() + "\n": "Biomes: [All Biomes]\n");
                sb.append(locations != null ? "Locations: " + locations.toString().toUpperCase() + "\n": "");
                sb.append(nearbyBlocks != null ? "Near: " + nearbyBlocks.toString() + "\n": "");
                sb.append(times != null ? "Times: " + times.toString() + "\n": "");
                sb.append(weathers != null ? "Weathers: " + weathers.toString() + "\n": "");
                sb.append(temperature != null ? "Temperature: " + temperature.toString() + "\n": "");
                sb.append(minY != null ? "Min Height: " + minY.toString() + "\n": "");
                sb.append(maxY != null ? "Max Height: " + maxY.toString() + "\n": "");
                sb.append(rarity != null ? "Rarity: " + rarity.toString() + "\n": "");
                sb.append(anticondition != null ? "Anti-Conditions: " + anticondition.toString() + "\n": "");
                sb.append("\n");
            });
            System.out.println("Start" + sb.substring(sb.length() - 1, sb.length()) + "End");
            while (sb.substring(sb.length() - 1, sb.length()).equals("\n")) {
            	sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }
    
        public List<HashMap<String, ArrayList<Object>>> getInformation() {
            return information;
        }
        
        public void setInformation(List<HashMap<String, ArrayList<Object>>> information) {        
            this.information = information;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Object[] getSpawnInfo() {
            return spawnInfos;
        }

        public void setSpawnInfo(Object[] spawnInfos) {
            this.spawnInfos = spawnInfos;
        }
        
        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append(id);
            sb.append("\n");
            for (Object o : spawnInfos){
                sb.append(o.toString());
                sb.append("\n");
            }
            return sb.toString();
        }
    }
    
    private class BetterSpawnerConfig{
        public Object globalCompositeCondition;
        public Object intervalSeconds;
        public Object blockCategories;
        public Object biomeCategories;

        public Object getGlobalCompositeCondition() {
            return globalCompositeCondition;
        }

        public void setGlobalCompositeCondition(Object globalCompositeCondition) {
            this.globalCompositeCondition = globalCompositeCondition;
        }

        public Object getIntervalSeconds() {
            return intervalSeconds;
        }

        public void setIntervalSeconds(Object intervalSeconds) {
            this.intervalSeconds = intervalSeconds;
        }

        public Object getBlockCategories() {
            return blockCategories;
        }

        public void setBlockCategories(Object blockCategories) {
            this.blockCategories = blockCategories;
        }

        public Object getBiomeCategories() {
            return biomeCategories;
        }

        public void setBiomeCategories(Object biomeCategories) {
            this.biomeCategories = biomeCategories;
        }
    }
    
	@Override
	public int compareTo(ICommand arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "spawns";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "/spawns <Pokemon>";
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
		String pixelmonName = args[0];
		pixelmonName = pixelmonName.substring(0, 1).toUpperCase() + pixelmonName.substring(1).toLowerCase();
		Pixelmon pixelmon = PIXELMON.get(pixelmonName);
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
		PIXELMON.keySet().stream().forEach(key ->{
			Pixelmon pixelmon = PIXELMON.get(key);
			if (pixelmon.getId().startsWith(argument)){
				results.add(pixelmon.getId());
			}
		});
		return results;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		// TODO Auto-generated method stub
		return false;
	}
	
//	@Mod.EventHandler
//	public void serverLoad(FMLServerStartingEvent event) {
//		event.registerServerCommand(new PixelmonInfoCommand());
//	}
	
	@Mod.EventHandler
	public void init(FMLPostInitializationEvent event) {
		ClientCommandHandler.instance.registerCommand(new PixelmonInfoCommand());
	}

	@Override
	public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
}

