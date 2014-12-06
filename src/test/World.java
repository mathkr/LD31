package test;

import test.resources.ResourceTable;
import test.structures.*;

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

                structures.add(new CopperMine(new Vector2i(33, 33)));
                structures.add(new SilverMine(new Vector2i(77, 43)));
                structures.add(new GlasMine(new Vector2i(73, 37)));
        }

        public void update(float delta) {
                for (Structure structure : structures) {
                        structure.update(delta);
                }
                resources.resources.forEach((r, f) -> System.out.println("res: " + r + ", value: " + f));
        }

        public void createWorld(Long seed){
                WorldGenerator.createWorld(this,seed);
        }
}
