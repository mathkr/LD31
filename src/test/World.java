package test;

import test.resources.Resource;
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

        public static final Vector2i WORLD_DIMENSIONS = new Vector2i(80, 45);

        public ArrayList<Structure> structures;
        public TerrainType[][] terrain;
        public ResourceTable resources;
        public ResourceTable resourceCapacity;

        public World() {
                structures = new ArrayList<>();
                resources = new ResourceTable(){{
                        put(Resource.COPPER, 100.0f);
                        put(Resource.ENERGY, 250.0f);
                        put(Resource.SILICON, 500.0f);
                }};
                resourceCapacity = new ResourceTable(){{
                        put(Resource.COPPER, 500.0f);
                        put(Resource.SILVER, 250.0f);
                        put(Resource.GLASS, 100.0f);
                        put(Resource.ENERGY, 750.0f);
                        put(Resource.SILICON, 1000.0f);
                        put(Resource.ELECTRON, 2000.0f);
                        //TODO: balance numbers
                }};

                createWorld(null);
        }

        public void update(float delta) {
                for (Structure structure : structures) {
                        structure.update(delta);
                }
        }

        public void createWorld(Long seed){
                if(seed != null){
                        WorldGenerator.createWorld(this,seed);
                } else{
                        WorldGenerator.createWorld(this);
                }
        }

        public void trimResourcesToCap(){
                resources.resources.forEach((r, v) ->{
                        float rCap = resourceCapacity.get(r);
                        if(v > rCap)
                                resourceCapacity.put(r, rCap);
                });
        }
}
