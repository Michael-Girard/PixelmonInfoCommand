package com.sylcharin.pixelmoninfocommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pixelmon {
    private String id;
    private Object[] spawnInfos;
    private List<HashMap<String, ArrayList<Object>>> information = new ArrayList<>();

    protected void init(){
        for (HashMap<String, ArrayList<Object>> informationSet : JSONHelper.parseSpawnInfos(spawnInfos)){
            information.add(informationSet);
        }
    }

    public String printInfo(){
        StringBuilder sb = new StringBuilder();
        sb.append(("\n-----------\n" + this.getId() + "\n-----------\n"));
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

            if (minLevel != null && maxLevel != null){
                sb.append("Levels: ");
                sb.append(minLevel != null ? minLevel.toString().substring(1, minLevel.toString().length() - 1) + " - " : "? - ");
                sb.append(maxLevel != null ? maxLevel.toString().substring(1, maxLevel.toString().length() - 1) + "\n" : "?\n");
            }
            else{
                sb.append("Level: ");
                sb.append(level != null ? level : "?");
            }

            sb.append(biomes != null ? "Biomes: " + biomes.toString().substring(1, biomes.toString().length() - 1) + "\n": "Biomes: All Biomes\n");
            sb.append(locations != null ? "Locations: " + locations.toString().substring(1, locations.toString().length() - 1) + "\n": "");
            sb.append(nearbyBlocks != null ? "Near: " + nearbyBlocks.toString().substring(1, nearbyBlocks.toString().length() - 1) + "\n": "");
            sb.append(times != null ? "Times: " + times.toString().substring(1, times.toString().length() - 1) + "\n": "");
            sb.append(weathers != null ? "Weathers: " + weathers.toString().substring(1, weathers.toString().length() - 1) + "\n": "");
            sb.append(temperature != null ? "Temperature: " + temperature.toString().substring(1, temperature.toString().length() - 1) + "\n": "");
            sb.append(minY != null ? "Min Height: " + minY.toString().substring(1, minY.toString().length() - 1) + "\n": "");
            sb.append(maxY != null ? "Max Height: " + maxY.toString().substring(1, maxY.toString().length() - 1) + "\n": "");
            sb.append(rarity != null ? "Rarity: " + rarity.toString().substring(1, rarity.toString().length() - 1) + "\n": "");
            sb.append(anticondition != null ? "Anti-Conditions: " + anticondition.toString().substring(1, anticondition.toString().length() - 1) + "\n": "");
            sb.append("\n");
        });

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
