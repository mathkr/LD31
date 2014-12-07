package test;

import test.resources.Resource;
import test.resources.ResourceTable;
import test.structures.*;
import test.structures.Structure;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
        public Structure[][] structureGrid;
        public ResourceTable resources;
        public ResourceTable resourceCapacity;

        public World() {
                bounds = new Vector2i(80, 45);
                structureGrid = new Structure[bounds.x][bounds.y];
                structures = new ArrayList<Structure>();
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

        public Structure getCPU(){
                //TODO: implement
                return null;
        }

        //sollte bei der CPU starten
        //die CPU hat immer maximalen (GLASS) road access zu sich selbst
        public void revalidateRoadAccess(){
                for(Structure structure : structures)
                        structure.roadAccess = RoadAccess.NONE;
                getCPU().setRoadAccess(RoadAccess.GLASS);
                //CPU.adjacentRoads.setRoadAccess
                //forAll roads r : r.adjacentRoads.setRoadAccess...
        }
}
