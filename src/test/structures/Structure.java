package test.structures;

import org.newdawn.slick.Image;
import test.Game;
import test.Vector2i;
import test.World;
import test.resources.Resource;
import test.resources.ResourceTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Structure {
        public Vector2i position;
        public Vector2i dimensions;
        public List<Vector2i> occupiedTiles;
        public ResourceTable buildCost;
        public ResourceTable productionInDelta;
        public ResourceTable productionOutDelta;
        public ResourceTable productionInPerSec;
        public ResourceTable productionOutPerSec;
        public ResourceTable capacityIncrease;
        public ResourceTable refundResources;
        public StructureLoader.Updater updater;
        public StructureType type;
        public RoadAccess roadAccess;
        public StructureState state;
        public boolean isProducer;
        public boolean wasPlaced;
        public int resourceRadius;
        public float productionFactor;

        public Image image;

        public Structure(Vector2i pos, StructureType t) {
                position = pos;
                type = t;
                occupiedTiles = new ArrayList<>();
                buildCost = new ResourceTable();
                productionInDelta = new ResourceTable();
                productionOutDelta = new ResourceTable();
                productionInPerSec = new ResourceTable();
                productionOutPerSec = new ResourceTable();
                capacityIncrease = new ResourceTable();
                roadAccess = RoadAccess.NONE;
                wasPlaced = false;
                productionFactor = 1.0F;
        }

        public boolean collidesWith(Structure other){
                for(Vector2i thisPos : this.occupiedTiles)
                        for(Vector2i otherPos : other.occupiedTiles)
                                if(this.position.x+thisPos.x == other.position.x+otherPos.x
                                        && this.position.y+thisPos.y == other.position.y+otherPos.y)
                                        return true;
                return false;
        }

        public void setState(StructureState setState){
                if(state == StructureState.Active && setState != StructureState.Active)
                        Game.world.resourceCapacity.subtract(capacityIncrease);
                else if(state != StructureState.Active && setState == StructureState.Active)
                        Game.world.resourceCapacity.add(capacityIncrease);
                state = setState;
        }

        public void update(float d){
                if (updater != null) {
                        updater.update(this);
                }

                if(roadAccess == RoadAccess.NONE){
                        //keine strasse :(
                        setState(StructureState.NoRoadAccess);
                        return;
                }
                if(isRoad())
                        return;

                ResourceTable resources = Game.world.resources;
                ResourceTable cap = Game.world.resourceCapacity;
                //buffere aenderungen, solange unter 1.0f
                productionInPerSec.resources.forEach((res, val) -> {
                        if (productionInDelta.get(res) < 1.0f)
                                productionInDelta.change(res, productionInPerSec.get(res) * d * productionFactor);
                });
                productionOutPerSec.resources.forEach((res, val) -> {
                        if (productionOutDelta.get(res) < 1.0f)
                                productionOutDelta.change(res, productionOutPerSec.get(res) * d * productionFactor);
                });
                //pruefe, ob eingangsressourcen vorhanden
                for(Map.Entry<Resource, Float> e : productionInDelta.resources.entrySet()){
                        float rDelta = e.getValue().intValue();
                        if (rDelta >= 1.0f && !resources.canSubtract(e.getKey(), rDelta)) {
                                //kein saft :(
                                setState(StructureState.NoInputResources);
                                return;
                        }
                }
                //pruefe, ob fuer mindestens eine der produzierten ressourcen kapazitaet vorhanden ist
                if(isProducer) {
                        boolean hasCapacity = false;
                        for (Map.Entry<Resource, Float> e : productionOutPerSec.resources.entrySet()) {
                                if (e.getValue() > 0.0f && resources.get(e.getKey()) < cap.get(e.getKey())) {
                                        hasCapacity = true;
                                        break;
                                }
                        }
                        if (!hasCapacity) {
                                //lager voll :(
                                //vielleicht hier was anderes machen
                                setState(StructureState.NoSpareCapacity);
                                return;
                        }
                }
                setState(StructureState.Active);
                //ziehe eingangsressourcen ab
                productionInDelta.resources.forEach((res, val) -> {
                        float rDelta = val.intValue();
                        if (rDelta >= 1.0f) {
                                productionInDelta.change(res, -rDelta);
                                resources.change(res, -rDelta);
                        }
                });
                //addiere ausgangsressourcen
                productionOutDelta.resources.forEach((res, val) -> {
                        float rDelta = val.intValue();
                        if(rDelta >= 1.0f){
                                float currentRes = resources.get(res);
                                float resourceCap = cap.get(res);
                                rDelta = rDelta + currentRes <= resourceCap ? rDelta : resourceCap - currentRes;
                                productionOutDelta.change(res, -rDelta);
                                resources.change(res, rDelta);
                        }
                });
        }

        public boolean canBePlaced(){
                if(type == StructureType.CPU_T1 && Game.world.cpu != null)
                        //schon eine CPU vorhanden - mehr geht nicht
                        return false;
                for(Structure other : Game.world.structures) {
                        if (collidesWith(other)) {
                                //kein Platz :(
                                return false;
                        }
                }

                switch (type) {
                        case CopperMine:
                                if (getNearResources(World.TerrainType.COPPER, resourceRadius) == 0) {
                                        return false;
                                }
                                break;
                        case SilverMine:
                                if (getNearResources(World.TerrainType.SILVER, resourceRadius) == 0) {
                                        return false;
                                }
                                break;
                        case GlassMine:
                                if (getNearResources(World.TerrainType.GLASS, resourceRadius) == 0) {
                                        return false;
                                }
                                break;
                }

                if(!Game.world.resources.greaterOrEqual(buildCost)) {
                        //zu teuer :(
                        return false;
                }

                return true;
        }

        public Integer getNearResources(World.TerrainType searchType, Integer area){
                Vector2i min = new Vector2i(occupiedTiles.get(0).x, occupiedTiles.get(0).y);
                Vector2i max = new Vector2i(occupiedTiles.get(0).x, occupiedTiles.get(0).y);
                for (Vector2i occupiedTile : occupiedTiles) {
                        if(min.x > occupiedTile.x)
                                min.x = occupiedTile.x;
                        if(min.y > occupiedTile.y)
                                min.y = occupiedTile.y;
                        if(max.x < occupiedTile.x)
                                max.x = occupiedTile.x;
                        if(max.y < occupiedTile.y)
                                max.y = occupiedTile.y;
                }
                min.x = this.position.x + min.x - area > 0 ? min.x - area : 0;
                min.y = this.position.y + min.y - area > 0 ? min.y - area : 0;
                max.x = this.position.x + max.x + area < Game.world.WORLD_DIMENSIONS.x   ? max.x + area : max.x;
                max.y = this.position.y + max.y + area < Game.world.WORLD_DIMENSIONS.y  ? max.y + area : max.y;

                Integer count = 0;
                for (int i = min.x + this.position.x; i <= max.x + this.position.x; i++) {
                        for (int j = min.y + this.position.y; j <= max.y + this.position.y; j++) {
                                if(Game.world.terrain[i][j] == searchType){
                                        ++count;
                                }
                        }
                }
                return count;
        }

        public void actuallyPlace(){
                switch(type){
                        case SilverMine : productionOutPerSec.multiply(Resource.SILVER, getNearResources(World.TerrainType.SILVER, resourceRadius)); break;
                        case CopperMine : productionOutPerSec.multiply(Resource.COPPER, getNearResources(World.TerrainType.COPPER, resourceRadius)); break;
                        case GlassMine : productionOutPerSec.multiply(Resource.GLASS, getNearResources(World.TerrainType.GLASS, resourceRadius)); break;
                        case CPU_T1 : Game.world.cpu = this;
                }
                isProducer = false;
                for(Resource resource : Resource.values())
                        if(productionOutPerSec.get(resource) > 0.0f) {
                                isProducer = true;
                                break;
                        }
                for(Vector2i v : occupiedTiles)
                        Game.world.structureGrid[position.x+v.x][position.y+v.y] = this;
                Game.world.structures.add(this);
                Game.world.resources.subtract(this.buildCost);
                Game.world.revalidateRoadAccess();
                wasPlaced = true;
        }

        public void remove(){
                if(type == StructureType.CPU_T1)
                        Game.world.cpu = null;

                Game.world.structures.remove(this);

                for(Vector2i v : occupiedTiles)
                        Game.world.structureGrid[position.x+v.x][position.y+v.y] = null;

                setState(StructureState.NoRoadAccess);

                if(isRoad() || type == StructureType.CPU_T1)
                        Game.world.revalidateRoadAccess();

                Game.world.resources.add(this.refundResources);
        }

        public RoadAccess getRoadAccess(){
                return roadAccess;
        }

        //tries to improve a structure's road access
        //returns true if a road's roadAccess improved
        public boolean improveRoadAccess(RoadAccess road){
                switch(type){
                        case CopperRoad :
                                if(roadAccess.compareTo(road) < 0){
                                        roadAccess = RoadAccess.COPPER;
                                        setState(StructureState.Active);
                                        return true;
                                }
                                return false;
                        case SilverRoad :
                                switch(road){
                                        case COPPER :
                                                if(roadAccess.compareTo(road) < 0){
                                                        roadAccess = RoadAccess.COPPER;
                                                        setState(StructureState.Active);
                                                        return true;
                                                }
                                                return false;
                                        case SILVER :
                                        case GLASS :
                                                if(roadAccess.compareTo(road) < 0){
                                                        roadAccess = RoadAccess.SILVER;
                                                        setState(StructureState.Active);
                                                        return true;
                                                }
                                                return false;
                                }
                        case GlassRoad :
                                if(roadAccess.compareTo(road) < 0){
                                        roadAccess = road;
                                        setState(StructureState.Active);
                                        return true;
                                }
                                return false;
                }
                if(roadAccess.compareTo(road) < 0){
                        roadAccess = road;
                        if (road == RoadAccess.COPPER) this.productionFactor = 1.0F;
                        if (road == RoadAccess.SILVER) this.productionFactor = 1.25F;
                        if (road == RoadAccess.GLASS) this.productionFactor = 1.75F;
                }
                return false;
        }

        public boolean isRoad(){
                switch(type){
                        case CopperRoad :
                        case SilverRoad :
                        case GlassRoad : return true;
                        default : return false;
                }
        }
}
