package test;

import test.structures.Structure;

import java.util.ArrayList;

public class World {
        public static enum TerrainType {
                DEFAULT,
                COPPER,
                SILVER,
                GLASS
        }

        public Vector2i bounds;
        public ArrayList<Structure> structures;
        public TerrainType[][] terrain;
        public ResourceTable resources;


        public World() {
                bounds = new Vector2i(160, 90);
                structures = new ArrayList<Structure>();
                resources = new ResourceTable();
                createWorld(4L);
        }

        public void createWorld(Long seed){
                WorldGenerator.createWorld(this,seed);
        }
}
