package test.structures;

import org.newdawn.slick.Image;
import test.Game;
import test.Vector2i;
import test.World;
import test.resources.Resource;
import test.resources.ResourceTable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class StructureLoader {
        public static interface Updater {
                void update(Structure structure);
        }

        public static HashMap<StructureType, Properties> propertiesMap = new HashMap<>();
        public static HashMap<StructureType, Updater> updaterMap = new HashMap<>();
        public static HashMap<StructureType, ArrayList<Vector2i>> occupiedTilesMap = new HashMap<>();

        static {
//                updaterMap.put(StructureType.CopperMine, (structure) -> {
//                        Integer value = structure.getNearResources(World.TerrainType.COPPER, 1);
//                        structure.productionOutPerSec.put(Resource.COPPER, structure.productionFactor * value);
//                });
//
//                updaterMap.put(StructureType.GlassMine, (structure) -> {
//                        Integer value = structure.getNearResources(World.TerrainType.GLASS, 1);
//                        structure.productionOutPerSec.put(Resource.GLASS, structure.productionFactor * value);
//                        System.out.println("value: " + value);
//                });
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

                Image image;
                if (properties.getProperty("image") != null) {
                        image = Game.renderer.getImage("resources/" + properties.getProperty("image"));
                } else {
                        image = null;
                }

//                structure.productionFactor = Float.parseFloat(properties.getProperty("productionFactor", "1f"));

                ArrayList<Vector2i> occupiedTiles = getOccupiedTiles(type);
                structure.occupiedTiles = occupiedTiles;
                structure.image = image;
                structure.updater = updaterMap.get(type);

                return structure;
        }
}
