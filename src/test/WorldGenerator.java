package test;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import test.World.*;
import test.MapCreationHint.*;
import test.resources.Resource;
import test.structures.Structure;

/**
 * Created by msk on 06.12.14.
 * Klasse erstellt um eine Welt zu generieren.
 *
 */
public class WorldGenerator {

    public static Integer creationHintLoadSize;
    public static Integer creationHintLoadCount;
    public static Integer creationHintMapSizeDefaultX;
    public static Integer creationHintMapSizeDefaultY;
    public static Map<TerrainType, MapCreationHint> hints;

    static {
        //Legt nur die maximale anzahl fest
        creationHintLoadCount = 30;
        creationHintLoadSize = 6;
        creationHintMapSizeDefaultX = 160;
        creationHintMapSizeDefaultY = 80;

        hints = new TreeMap<>();
        MapCreationHint defaultHint = new MapCreationHint();
        defaultHint.spawnAreaAVG = 0.75F;
        defaultHint.countMin = 7;
        defaultHint.countMax = 10;
        defaultHint.sizeMin = 3;
        defaultHint.sizeMax = 7;
        hints.put(TerrainType.COPPER, defaultHint);

        defaultHint = new MapCreationHint();
        defaultHint.spawnArea = Direction.Bottom;
        defaultHint.spawnAreaAVG = 0.8F;
        defaultHint.countMin = 5;
        defaultHint.countMax = 9;
        defaultHint.sizeMin = 2;
        defaultHint.sizeMax = 6;
        hints.put(TerrainType.SILVER, defaultHint);

        defaultHint = new MapCreationHint();
        defaultHint.spawnArea = Direction.Left;
        defaultHint.spawnAreaAVG = 0.8F;
        defaultHint.countMin = 5;
        defaultHint.countMax = 8;
        defaultHint.sizeMin = 4;
        defaultHint.sizeMax = 6;
        hints.put(TerrainType.GLASS, defaultHint);

    }

    public static void createWorld(World world){
        Long seed = new Random().nextLong();
        createWorld(world, seed,true);
    }

    public static void createWorld(World world, Long worldSeed, boolean better){
        resetWorld(world);

        Random seed = new Random(worldSeed);
        for (TerrainType terrainType : TerrainType.values()){
            if(terrainType != TerrainType.DEFAULT){
                MapCreationHint hint = hints.get(terrainType);
                if(hint != null){
                    generateTerrain(hint,seed, world, terrainType);
                } else {
                    generateTerrain(seed);
                }
            }
        }

    }

    private static void generateTerrain(MapCreationHint hint, Random seed, World world, TerrainType terrain){
        Vector2i worldSize = World.WORLD_DIMENSIONS;
        // Erzeuge anzahl der Adern
        Integer veinCount = hint.countMin + seed.nextInt(hint.countMax - hint.countMin);
        System.out.println("veinCount = " + veinCount + " Resource: " + terrain);
        Vector2i pos = new Vector2i(0,0);
        for (int i = 0; i < veinCount; i++) {
            boolean hit = false;
            if (hint.spawnAreaAVG != null){
                // random warscheinlichkeit das ores überall gespawnt werden können
                if(seed.nextDouble() >= hint.spawnAreaAVG){
                    hit = true;
                    pos.x = seed.nextInt(worldSize.x);
                    pos.y = seed.nextInt(worldSize.y);
                }
            }
            // nur wenn nicht anders gesetzt
            if(!hit){
                if(hint.spawnArea != null){
                    switch (hint.spawnArea){
                        case Left:
                            pos.x = seed.nextInt(25);
                            pos.y = seed.nextInt(worldSize.y);
                            break;
                        case Right:
                            pos.x = seed.nextInt(worldSize.x - 30);
                            pos.y = seed.nextInt(worldSize.y);
                            break;
                        case top:
                            pos.x = seed.nextInt(worldSize.x);
                            pos.y = seed.nextInt(25);
                            break;
                        case Bottom:
                            pos.x = seed.nextInt(worldSize.x);
                            pos.y = seed.nextInt(worldSize.y - 25);
                            break;
                        default:
                            pos.x = seed.nextInt(World.WORLD_DIMENSIONS.x);
                            pos.y = seed.nextInt(World.WORLD_DIMENSIONS.y);
                            break;
                    }
                } else {
                    pos.x = seed.nextInt(World.WORLD_DIMENSIONS.x);
                    pos.y = seed.nextInt(World.WORLD_DIMENSIONS.y);
                }
            }

            Integer sizeX = hint.sizeMin + seed.nextInt(hint.sizeMax - hint.sizeMin);
            Integer sizeY = hint.sizeMin + seed.nextInt(hint.sizeMax - hint.sizeMin);
            for (int xi = 0; xi < sizeX; xi++) {
                for (int yi = 0; yi < sizeY; yi++) {
                    if(pos.x + xi > 0 && pos.y + yi > 0 &&
                            pos.x + xi < worldSize.x && pos.y + yi  < worldSize.y){
                        world.terrain[pos.x + xi][pos.y + yi] = terrain;
                    }
                }
            }
        }
    }

    private static void generateVein(Random seed, Integer x, Integer y){

    }

    private static void generateTerrain(Random seed){

    }

    public static void createWorld(World world, Long seed){
        resetWorld(world);

        Random r = new Random(seed);
        for (TerrainType terrainType : TerrainType.values()) {
            if(terrainType != TerrainType.DEFAULT){

                Integer count = r.nextInt(creationHintLoadCount - 1 ) + 1;
                for (int i = 0; i < count; i++) {
                    boolean vainSet = true;
                    Integer cancel = 0;
                    do {
                        ++cancel;
                        Integer sizeLoad = r.nextInt(creationHintLoadSize - 1) + 1;

                        Integer x = r.nextInt(world.WORLD_DIMENSIONS.x - (1 + sizeLoad )) + 1;
                        Integer y = r.nextInt(world.WORLD_DIMENSIONS.y - (1 + sizeLoad )) + 1;

                        // suche nach störenden Feldern
                        for (int xi = 0; xi < sizeLoad; xi++) {
                            for (int yi = 0; yi < sizeLoad; yi++) {
                                if(world.terrain[x + xi][y + yi] != TerrainType.DEFAULT){
                                    vainSet = false;
                                }
                            }
                        }

                        if(vainSet){
                            // alles klar Feld ist frei
                            for (int xi = 0; xi < sizeLoad; xi++) {
                                for (int yi = 0; yi < sizeLoad; yi++) {
                                    world.terrain[xi + x][yi + y] = terrainType;
                                }
                            }
                        }

                        //Suche solange eine Freie stelle biss der Arzt kommt
                    }while(!vainSet && cancel < 5);
                }

            }
        }
    }

    public static void resetWorld( World world){
        world.terrain = new TerrainType[world.WORLD_DIMENSIONS.x][world.WORLD_DIMENSIONS.y];
        //clear Terrain from changes
        for (int x = 0; x < world.WORLD_DIMENSIONS.x; x++) {
            for (int y = 0; y < world.WORLD_DIMENSIONS.y; y++) {
                world.terrain[x][y] = TerrainType.DEFAULT;
            }
        }
    }

    private static Integer getCountOfLoads(){
        return null;
    }
}
