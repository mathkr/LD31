package test;

import test.resources.Resource;
import test.resources.ResourceTable;
import test.structures.CopperMill;
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
                bounds = new Vector2i(80, 45);
                structures = new ArrayList<Structure>();
                resources = new ResourceTable();
                createWorld(4L);

                // For debugging
                for (Resource resource : Resource.values()) {
                        resources.put(resource, 1000.0f);
                }
        }

        public void update(float delta) {
                for (Structure structure : structures) {
                        structure.update(delta);
                }
        }

        public void createWorld(Long seed){
                WorldGenerator.createWorld(this,seed);
        }
}
