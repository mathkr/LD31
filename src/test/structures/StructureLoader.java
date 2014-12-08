package test.structures;

import org.newdawn.slick.Image;
import test.Game;
import test.Vector2i;
import test.resources.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class StructureLoader {
        public static interface Updater {
                void update(Structure structure);
        }

        public static HashMap<StructureType, Properties> propertiesMap = new HashMap<>();
        public static HashMap<StructureType, Updater> updaterMap = new HashMap<>();
        public static HashMap<StructureType, ArrayList<Vector2i>> occupiedTilesMap = new HashMap<>();

        public static Map<String, String> validPropertyKeys;

        static {
                try {
                        List<String> propertyLines = Files.readAllLines(Paths.get("resources/data/properties_list.txt"));
                        validPropertyKeys = new HashMap<>();

                        for (String propertyLine : propertyLines) {
                                if (propertyLine.contains("RESOURCE")) {
                                        for (Resource resource : Resource.values()) {
                                                String value = propertyLine.replace("RESOURCE", resource.name());
                                                validPropertyKeys.put(value, value);
                                        }
                                } else {
                                        validPropertyKeys.put(propertyLine, propertyLine);
                                }
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }

                // Updaters:
                float spawnFrequency = 0.01f;
                float spawnVelocity = 10f;
                float spawnVariance = 0.4f;
                int spawnNum = 1;
                float spawnLifeTime = 0.4f;

                updaterMap.put(StructureType.CopperRoad, (structure) -> {
                        if (Math.random() < spawnFrequency) {
                                if (structure.state == StructureState.Active) {
                                        Game.renderer.spawnParticlesAtWorldPosition(structure.position.x, structure.position.y,
                                                spawnVelocity, spawnVariance, spawnNum, Game.renderer.TERRAIN_COPPER_COLOR, spawnLifeTime);
                                }
                        }
                });

                updaterMap.put(StructureType.SilverRoad, (structure) -> {
                        if (Math.random() < spawnFrequency) {
                                if (structure.state == StructureState.Active) {
                                        Game.renderer.spawnParticlesAtWorldPosition(structure.position.x, structure.position.y,
                                                spawnVelocity, spawnVariance, spawnNum, Game.renderer.TERRAIN_SILVER_COLOR, spawnLifeTime);
                                }
                        }
                });

                updaterMap.put(StructureType.GlassRoad, (structure) -> {
                        if (Math.random() < spawnFrequency) {
                                if (structure.state == StructureState.Active) {
                                        Game.renderer.spawnParticlesAtWorldPosition(structure.position.x, structure.position.y,
                                                spawnVelocity, spawnVariance, spawnNum, Game.renderer.TERRAIN_GLASS_COLOR, spawnLifeTime);
                                }
                        }
                });
        }

        public static ArrayList<Vector2i> getOccupiedTiles(StructureType type) {
                if (occupiedTilesMap.containsKey(type)) {
                        return occupiedTilesMap.get(type);
                } else {
                        try {
                                ArrayList<Vector2i> res = new ArrayList<>();
                                List<String> lines = Files.readAllLines(Paths.get("resources/data/" + type.toString() + ".occupied"));

                                for (int y = 0; y < lines.size(); ++y) {
                                        for (int x = 0; x < lines.get(y).length(); ++x) {
                                                if (Character.compare(lines.get(y).charAt(x), '#') == 0) {
                                                        res.add(new Vector2i(x, y));
                                                }
                                        }
                                }

                                occupiedTilesMap.put(type, res);
                                return res;
                        } catch (IOException e) {
                                e.printStackTrace();
                        }

                        return null;
                }
        }

        public static Properties getProperties(StructureType type) {
                if (propertiesMap.containsKey(type)) {
                        return propertiesMap.get(type);
                } else {
                        Properties res = readPropertiesFile("resources/data/" + type.toString() + ".properties");
                        propertiesMap.put(type, res);
                        return res;
                }
        }

        private static Properties readPropertiesFile(String file) {
                Properties res = new Properties();
                try {
                        InputStream is = new FileInputStream(file);
                        res.load(is);

                        boolean isValid = true;
                        // Check if properties are valid
                        for (String key : res.stringPropertyNames()) {
                                if (!validPropertyKeys.containsKey(key)) {
                                        isValid = false;
                                        System.err.println("Unknown property key: '" + key + "', in file: " + file);
                                }
                        }

                        if (!isValid) {
                                System.exit(-1);
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                }
                return res;
        }

        public static Structure getInstance(StructureType type, int x, int y) {
                Vector2i pos = new Vector2i(x, y);
                Structure structure = new Structure(pos, type);

                Properties properties = getProperties(type);

                // TODO(Matthis): add debug output
                for (Resource resource : Resource.values()) {
                        structure.buildCost.put(resource, Float.parseFloat(properties.getProperty("buildCost" + resource.name(), "0f")));
                }

                for (Resource resource : Resource.values()) {
                        structure.productionInPerSec.put(resource, Float.parseFloat(properties.getProperty("productionInPerSec" + resource.name(), "0f")));
                }

                for (Resource resource : Resource.values()) {
                        structure.productionOutPerSec.put(resource, Float.parseFloat(properties.getProperty("productionOutPerSec" + resource.name(), "0f")));
                }

                for (Resource resource : Resource.values()) {
                        structure.capacityIncrease.put(resource, Float.parseFloat(properties.getProperty("capacityIncrease" + resource.name(), "0f")));
                }

                structure.refundResources = structure.buildCost.getMultiple(0.5f);
                structure.refundResources.truncateToInt();

                structure.resourceRadius = Integer.parseInt(properties.getProperty("resourceRadius", "1"));

                Image image;
                if (properties.getProperty("image") != null) {
                        image = Game.renderer.getImage("resources/" + properties.getProperty("image"));
                } else {
                        image = null;
                }

                ArrayList<Vector2i> occupiedTiles = getOccupiedTiles(type);

                int maxX = occupiedTiles.get(0).x;
                int maxY = occupiedTiles.get(0).y;
                for (Vector2i occupiedTile : occupiedTiles) {
                        maxX = maxX < occupiedTile.x ? occupiedTile.x : maxX;
                        maxY = maxY < occupiedTile.y ? occupiedTile.y : maxY;
                }
                Vector2i dimensions = new Vector2i(maxX + 1, maxY + 1);
                structure.dimensions = dimensions;

                structure.occupiedTiles = occupiedTiles;
                structure.image = image;
                structure.updater = updaterMap.get(type);

                return structure;
        }
}
