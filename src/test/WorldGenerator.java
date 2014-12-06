package test;

import java.util.Random;
import test.World.*;

/**
 * Created by msk on 06.12.14.
 * Klasse erstellt um eine Welt zu generieren.
 *
 */
public class WorldGenerator {

    public static void createWorld(World world){
        Long seed = new Random().nextLong();
        createWorld(world, seed);
    }

    public static void createWorld(World world, Long seed){


        Random r = new Random(seed);
        for (TerrainType terrainType : TerrainType.values()) {
            if(terrainType != TerrainType.DEFAULT){

                Integer count = r.nextInt(4 ) + 1;
                for (int i = 0; i < count; i++) {
                    boolean vainSet = true;
                    do {

                        Integer sizeLoad = r.nextInt(3) + 1;
                        // dammit die gesamte ader aufgebaut werden
                        // kann muss das maximum verringert werden
                        Integer x = r.nextInt(world.bounds.x - (1 - sizeLoad )) + 1;
                        Integer y = r.nextInt(world.bounds.y - (1 - sizeLoad )) + 1;

                        // suche nach störenden Feldern
                        for (int j = 0; j < sizeLoad; j++) {
                            if(world.terrain[x][y] != TerrainType.DEFAULT){
                                vainSet = false;
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
                    }while(!vainSet);
                }

            }
        }
    }

    public static void resetWorld( World world){

        //clear Terrain from changes
        for (int x = 0; x < world.bounds.x; x++) {
            for (int y = 0; y < world.bounds.y; y++) {
                world.terrain[x][y] = TerrainType.DEFAULT;
            }
        }
    }
}
