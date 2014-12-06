package test;

import test.resources.ResourceTable;
import test.structures.Structure;

import java.util.ArrayList;

public class World {
        public static enum TerrainType {
                DEFAULT,
                COPPER,
                SILVER,
                GLASS
        }

        public Vector2i bounds = new Vector2i(160, 90);
        public ArrayList<Structure> structures;
        public TerrainType[][] terrain;
        public ResourceTable resources;


        public World() {
                structures = new ArrayList<Structure>();
                
                terrain = new TerrainType[bounds.x][bounds.y];
                // Initialize terrain array with default terrain
                for (int x = 0; x < terrain.length; x++) {
                        for (int y = 0; y < terrain[x].length; y++) {
                                terrain[x][y] = TerrainType.DEFAULT;
                        }
                }

                resources = new ResourceTable();
        }
}
