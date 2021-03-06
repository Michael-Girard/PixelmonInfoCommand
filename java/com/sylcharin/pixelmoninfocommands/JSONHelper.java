package com.sylcharin.pixelmoninfocommands;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class JSONHelper {
    private static Gson gson = new Gson();
    private static JSONHelper instance = null;

    private final Path BETTER_SPAWNER_CONFIG_JSON = Paths.get(System.getProperty("user.dir") + "/config/pixelmon/BetterSpawnerConfig.json");
    private static BetterSpawnerConfig betterSpawnerConfig = null;

    private final Path MINECRAFT_MODS_PATH = Paths.get(System.getProperty("user.dir") + "/mods");
    private static final String LOCATION_OF_SPAWN_JSONS_IN_JAR = "assets/pixelmon/spawning/".toUpperCase();
    private static final List<JarEntry> SPAWN_JSON_FILES = new ArrayList<>();
    private static JarFile pixelmonJar;

    protected static String betterSpawnerConfigError;
    protected static String pixelmonJarError;

    private static final HashMap<String, Pixelmon> PIXELMON = new HashMap<>();

    private JSONHelper() throws URISyntaxException {
        parseBetterSpawnerConfig();
        pixelmonJar = getPixelmonJar();
        if (betterSpawnerConfigError == null && pixelmonJarError == null) {
            buildJSONList();
            BiomeList.getInstance();
            buildPixelmonMap();
        }
    }

    public static JSONHelper getInstance() throws URISyntaxException {
        if (instance == null){
            instance = new JSONHelper();
        }
        return instance;
    }

    public static BetterSpawnerConfig getConfig(){
        return betterSpawnerConfig;
    }

    public static HashMap<String, Pixelmon> getPixelmon() { return PIXELMON; }

    protected void parseBetterSpawnerConfig(){
        //If BetterSpawnerConfig.json exists in the config/pixelmon folder, create a BetterSpawnerConfig object from it
        //Otherwise, put an error message inside of the string
        if (Files.exists(BETTER_SPAWNER_CONFIG_JSON)){
            try(JsonReader configReader = new JsonReader(new InputStreamReader(new FileInputStream(BETTER_SPAWNER_CONFIG_JSON.toFile()), "UTF-8"))){
                betterSpawnerConfig = new Gson().fromJson(configReader, BetterSpawnerConfig.class);
            }
            catch (IOException ex) {
                betterSpawnerConfigError = ex.getMessage();
                return;
            }
        }
        else{
            betterSpawnerConfigError = "ERROR: No BetterSpawnerConfig.json found!";
            return;
        }
        betterSpawnerConfigError = null;
    }

    protected JarFile getPixelmonJar(){
        try {
            List<Path> jarFileName = Files.list(MINECRAFT_MODS_PATH)
                    .filter(Files::isRegularFile)   //Filters out directories
                    .filter(file -> {
                        //Filters out small files that don't contain PIXELMON and end with .JAR
                        String fileName = file.getFileName().toString().toUpperCase();
                        return fileName.contains("PIXELMON") && fileName.endsWith(".JAR") && file.toFile().length() > 200000000;
                    })
                    .collect(Collectors.toList());  //Collects the remaining files in the list
            if (!jarFileName.isEmpty()){
                //If the stream found a file, return the first one - there should only be one in the directory anyway
                pixelmonJarError = null;
                return new JarFile(jarFileName.get(0).toFile());
            }
            else{
                pixelmonJarError = "ERROR: No Pixelmon Jar File Found!";
            }
        } catch (IOException ex) {
            pixelmonJarError = ex.getMessage();
        }
        return null;
    }

    public static void buildJSONList(){
        //Opening the jar file
        //Stream isolates all of the pokemon spawn json files into a list of JarEntries
        pixelmonJar.stream()
                .filter(filePath -> {
                    String filePathString = filePath.getName().toUpperCase();
                    return filePathString.startsWith(LOCATION_OF_SPAWN_JSONS_IN_JAR) && filePathString.endsWith(".JSON");
                })
                .forEach(SPAWN_JSON_FILES::add);
    }

    private static void buildPixelmonMap(){
        SPAWN_JSON_FILES.forEach((entry) -> {
            String pokemonName = entry.getName();
            if (!pokemonName.contains("fish")/* && pokemonName.contains("Leafeon")*/) {
                Pixelmon pixelmon;

                try(JsonReader reader = new JsonReader(new InputStreamReader(pixelmonJar.getInputStream(entry)))){
                    pixelmon = gson.fromJson(reader, Pixelmon.class);
                    pixelmon.init();
                    PIXELMON.put(pixelmon.getId(), pixelmon);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    protected static HashMap<String, ArrayList<Object>>[] parseSpawnInfos(Object[] spawnInfos){
        HashMap<String, ArrayList<Object>>[] results = new HashMap[spawnInfos.length];

        for (int index = 0; index < spawnInfos.length; index++){
            results[index] = new HashMap<>();
            parseSpawnInfos(null, "spawnInfos", spawnInfos[index], results[index]);
        }

        return results;
    }

    private static void parseSpawnInfos(Object previousLevelName, Object levelName, Object dataStructure, HashMap<String, ArrayList<Object>> results) {
        //If the object passed in is a LinkedTreeMap, this method calls itself to walk down the structure

        if (dataStructure instanceof AbstractMap){
            AbstractMap<Object, Object> map = (AbstractMap) dataStructure;

            map.keySet().forEach(nextLevelName -> {
                Object nextDataStructure = map.get(nextLevelName);
//                System.out.println(nextDataStructure.getClass());

                //if the next data structure is some sort of map, recursively call this method
                if (nextDataStructure instanceof ArrayList || nextDataStructure instanceof AbstractMap) {
                    parseSpawnInfos(levelName, nextLevelName, nextDataStructure, results);
                }
                //Otherwise, it's probably a data type that can be added right to the results
                else{
                    ArrayList<Object> values;
                    //Add the value to an existing arraylist if one exists at the key
                    if (results.containsKey(nextLevelName.toString())){
                        values = results.get(nextLevelName.toString());
                        values.add(formatTitleCase(nextDataStructure.toString()));
                    }

                    //Else create a new one
                    else{
                        values = new ArrayList<>();
                        values.add(formatTitleCase(nextDataStructure.toString()));
                        results.put(nextLevelName.toString(), values);
                    }
                }
            });
        }

        //If the object passed in is an ArrayList, we can get the data from the list
        else if (dataStructure instanceof List){
            List<Object> list = (List) dataStructure;

            list.stream().map(value -> {
//                System.out.println(levelName.toString());
                if (levelName.toString().equals("stringBiomes")){
                    //Convert a biome category into a set of biomes, changing those biomes to easily readable names
                    String biomeName = BiomeList.formatBiome(value.toString()).toString();
                    biomeName = biomeName.replaceAll("\\[", "").replaceAll("]", "");
                    value = biomeName;
                }
                return value;
            }).forEach(value -> {
                if (value instanceof String) {
                    String valueAsString = value.toString();
                    ArrayList<Object> values;

                    //Time to add the values to the results
                    //If the results already contained the type of value, add the value to the existing list
                    if (results.containsKey(levelName.toString())) {
                        if (previousLevelName.equals("anticondition")) {
                            //Anticonditions are added under their own header
                            //If it's an anticondition, check if that key exists (if so, add to the key, else make a new one)
                            if (results.containsKey(previousLevelName.toString())) {
                                values = results.get(previousLevelName.toString());
                            }
                            else{
                                values = new ArrayList<>();
                                values.add(formatTitleCase(value.toString()));
                                results.put(previousLevelName.toString(), values);
                                return;
                            }
                        } else {
                            //If it was a normal condition and the key existed, get the results
                            values = results.get(levelName.toString());
                        }
                        //Add the value to the existing condition or anticondition
                        values.add(formatTitleCase(valueAsString));
                    } else {
                        values = new ArrayList<>();
                        values.add(formatTitleCase(value.toString()));
                        results.put(levelName.toString(), values);
                    }
                }
            });
        }
    }

    public static String formatTitleCase(String target){
//        System.out.println(target);
        //Remove things like "minecraft:" and "pixelmon:"
        if (target.contains(":")) {
            target = target.substring(target.lastIndexOf(":") + 1);
        }

        target = target.replaceAll("_", " ");                       //Replace underscores with spaces
        target = target.toLowerCase();                                                //Lowercase all letters
        target = target.substring(0, 1).toUpperCase() + target.substring(1);   //Capitalize first letter

        //The following code converts the strings to Title Case
        if (target.contains(" ")) {
            String[] splitBySpace = target.split(" ");
            StringBuilder sb = new StringBuilder();
            for (int index = 0; index < splitBySpace.length; index++) {
                splitBySpace[index] = splitBySpace[index].substring(0, 1).toUpperCase() + splitBySpace[index].substring(1);
                sb.append(splitBySpace[index]);
                if (index + 1 != splitBySpace.length) {
                    sb.append(" ");
                }
            }
            target = sb.toString();
        }
//        System.out.println(target);
        return target;
    }

    protected class BetterSpawnerConfig{
        private Object globalCompositeCondition;
        private Object intervalSeconds;
        private Object blockCategories;
        private Object biomeCategories;

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
}
