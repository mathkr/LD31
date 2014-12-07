package test;

import test.resources.Resource;
import test.resources.ResourceTable;
import test.structures.RoadAccess;
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

        public static final Vector2i WORLD_DIMENSIONS = new Vector2i(60, 35);

        public TerrainType[][] terrain;
        public Structure[][] structureGrid;
        public ArrayList<Structure> structures;
        public Population population;

        public Structure cpu;
        public ResourceTable resources;
        public ResourceTable resourceCapacity;
        public ResourceTable decayDelta;

        public World() {
                structures = new ArrayList<>();
                population = new Population();
                structureGrid = new Structure[WORLD_DIMENSIONS.x][WORLD_DIMENSIONS.y];
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
                decayDelta = new ResourceTable();

                createWorld(null);
        }

        public void update(float delta) {
                for (Structure structure : structures) {
                        structure.update(delta);
                }
                population.update(delta);
                resourceDecay(delta);
        }

        public void createWorld(Long seed){
                if(seed != null){
                        WorldGenerator.createWorld(this,seed);
                } else{
                        WorldGenerator.createWorld(this);
                }
        }

        //deprecated
        public void trimResourcesToCap(){
                resources.resources.forEach((r, v) ->{
                        float rCap = resourceCapacity.get(r);
                        if(v > rCap)
                                resourceCapacity.put(r, rCap);
                });
        }

        public void resourceDecay(float delta){
                for(Resource resource : Resource.values()){
                        float top = resources.get(resource) - resourceCapacity.get(resource);
                        if(top > 0.0f){
                                decayDelta.change(resource, top*0.5f*delta/10.0f);
                                float rDelta = decayDelta.get(resource).intValue();
                                if(rDelta >= 1.0f){
                                        decayDelta.change(resource, -rDelta);
                                        resources.change(resource, -rDelta);
                                }
                        }else{
                                decayDelta.put(resource, 0.0f);
                        }
                }
        }

        //startet bei der CPU
        //die CPU hat immer maximalen (GLASS) road access zu sich selbst
        public void revalidateRoadAccess(){
                for(Structure structure : structures)
                        structure.roadAccess = RoadAccess.NONE;
                if(cpu == null)
                        //keine CPU - kein Strassenzugang!
                        return;
                cpu.improveRoadAccess(RoadAccess.GLASS);
                Queue<Structure> roads = new ConcurrentLinkedQueue<Structure>();
                Structure s;
                if(cpu.position.y-1 >= 0)
                        for(int i=0; i<cpu.dimensions.x; ++i)
                                if((s = structureGrid[cpu.position.x+i][cpu.position.y-1]) != null && s.isRoad() && s.improveRoadAccess(RoadAccess.GLASS))
                                        roads.add(s);
                if(cpu.position.y+cpu.dimensions.y < WORLD_DIMENSIONS.y)
                        for(int i=0; i<cpu.dimensions.x; ++i)
                                if((s = structureGrid[cpu.position.x+i][cpu.position.y+cpu.dimensions.y]) != null && s.isRoad() && s.improveRoadAccess(RoadAccess.GLASS))
                                        roads.add(s);
                if(cpu.position.x-1 >= 0)
                        for(int i=0; i<cpu.dimensions.y; ++i)
                                if((s = structureGrid[cpu.position.x-1][cpu.position.y+i]) != null && s.isRoad() && s.improveRoadAccess(RoadAccess.GLASS))
                                        roads.add(s);
                if(cpu.position.x+cpu.dimensions.x < WORLD_DIMENSIONS.x)
                        for(int i=0; i<cpu.dimensions.y; ++i)
                                if((s = structureGrid[cpu.position.x+cpu.dimensions.x][cpu.position.y+i]) != null && s.isRoad() && s.improveRoadAccess(RoadAccess.GLASS))
                                        roads.add(s);
                while(!roads.isEmpty()){
                        Structure other;
                        s = roads.poll();
                        if(s.position.y-1 >= 0 && (other = structureGrid[s.position.x][s.position.y-1]) != null && other.improveRoadAccess(s.getRoadAccess()))
                                roads.add(other);
                        if(s.position.y+1 < WORLD_DIMENSIONS.y && (other = structureGrid[s.position.x][s.position.y+1]) != null && other.improveRoadAccess(s.getRoadAccess()))
                                roads.add(other);
                        if(s.position.x-1 >= 0 && (other = structureGrid[s.position.x-1][s.position.y]) != null && other.improveRoadAccess(s.getRoadAccess()))
                                roads.add(other);
                        if(s.position.x+1 < WORLD_DIMENSIONS.x && (other = structureGrid[s.position.x+1][s.position.y]) != null && other.improveRoadAccess(s.getRoadAccess()))
                                roads.add(other);
                }
        }
}
