package test;

import java.util.Random;

import test.World.*;

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


    static {
        //Legt nur die maximale anzahl fest
        creationHintLoadCount = 10;
        creationHintLoadSize = 5;
        creationHintMapSizeDefaultX = 160;
        creationHintMapSizeDefaultY = 80;

    }

    public static void createWorld(World world){
        Long seed = new Random().nextLong();
        createWorld(world, seed);
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
