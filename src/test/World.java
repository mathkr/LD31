package test;

import test.resources.ResourceTable;
import test.structures.CopperMill;
import test.structures.CopperMine;
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

                structures.add(new CopperMill(new Vector2i(10, 10)));
                structures.add(new CopperMill(new Vector2i(50, 30)));
                structures.add(new CopperMine(new Vector2i(30, 30)));
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
