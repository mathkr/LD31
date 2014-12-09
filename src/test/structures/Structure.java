package test.structures;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import test.Game;
import test.Vector2i;
import test.World;
import test.resources.Resource;
import test.resources.ResourceTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Structure {

        static final float freezeTime = 1.7f;

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
        public float freezeDelta;
        public boolean isProducer;
        public boolean isConsumer;
        public boolean wasPlaced;
        public float roadFactor;
        public float populationFactor;
        public int influenceRadius;

        public Image image;

        public Structure(Vector2i pos, StructureType t) {
                position = pos;
                type = t;
                state = StructureState.Active;
                occupiedTiles = new ArrayList<>();
                buildCost = new ResourceTable();
                productionInDelta = new ResourceTable();
                productionOutDelta = new ResourceTable();
                productionInPerSec = new ResourceTable();
                productionOutPerSec = new ResourceTable();
                capacityIncrease = new ResourceTable();
                roadAccess = RoadAccess.NONE;
                wasPlaced = false;
                roadFactor = 1.0f;
                populationFactor = 1.0f;
                freezeDelta = 0.0f;
        }

        public boolean collidesWith(Structure other){
                for(Vector2i thisPos : this.occupiedTiles)
                        for(Vector2i otherPos : other.occupiedTiles)
                                if(this.position.x+thisPos.x == other.position.x+otherPos.x
                                        && this.position.y+thisPos.y == other.position.y+otherPos.y)
                                        return true;
                return false;
        }

        public float updatePopulationFactor(){
                ResourceTable resources = Game.world.resources;
                float res = 1.0f;
                res += resources.get(Resource.ELECTRON) * (1.0f / 4000.0f);
                res += resources.get(Resource.PHOTON) * (1.0f / 1000.f);
                res += resources.get(Resource.QUANTUM) * (1.0f / 250.0f);
                return populationFactor = res;
        }

        public float getProductionFactor(){
                return populationFactor * roadFactor;
        }

        public void setState(StructureState setState){
                if(isConsumer && setState == StructureState.NoInputResources || isProducer && setState == StructureState.NoSpareCapacity)
                        freezeDelta = freezeTime;
                if(state == StructureState.Active && setState != StructureState.Active)
                        Game.world.resourceCapacity.subtract(capacityIncrease);
                else if(state != StructureState.Active && setState == StructureState.Active)
                        Game.world.resourceCapacity.add(capacityIncrease);
                state = setState;
        }

        public void update(final float d){
                if (updater != null) {
                        updater.update(this);
                }

                if(isProducer)
                        updatePopulationFactor();

                if (state == StructureState.Standby) {
                        return;
                }

                if(roadAccess == RoadAccess.NONE){
                        setState(StructureState.NoRoadAccess);
                        return;
                }

                if(!isConsumer && !isProducer){
                        setState(StructureState.Active);
                        return;
                }

                if(freezeDelta > 0.0f){
                        freezeDelta -= d;
                        if(freezeDelta > 0.0f)
                                return;
                        freezeDelta = 0.0f;
                        setState(StructureState.Active);
                }

                final ResourceTable resources = Game.world.resources;
                final ResourceTable cap = Game.world.resourceCapacity;

                //pruefe, ob eingagsressourcen vorhanden
                if(isConsumer) {
                        for (Map.Entry<Resource, Float> e : productionInPerSec.resources.entrySet()) {
                                if (e.getValue() > 0.0f && resources.get(e.getKey()) == 0.0f) {
                                        setState(StructureState.NoInputResources);
                                        return;
                                }
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
                                //vielleicht hier was anderes machen
                                setState(StructureState.NoSpareCapacity);
                                return;
                        }
                }

                final float f = getProductionFactor();

                for (Map.Entry<Resource, Float> entry : productionInPerSec.resources.entrySet()) {
                        Resource res = entry.getKey();
                        float val = entry.getValue();
                        productionInDelta.change(res, productionInPerSec.get(res) * d * (isProducer ? f : 1.0f));
                }

                for (Map.Entry<Resource, Float> entry : productionOutPerSec.resources.entrySet()) {
                        Resource res = entry.getKey();
                        float val = entry.getValue();
                        productionOutDelta.change(res, productionOutPerSec.get(res) * d * f);
                }

                setState(StructureState.Active);

                //ziehe eingangsressourcen ab
                //UNSAFE fuer input von mehr als 1.0 unit resource pro frame
                for (Map.Entry<Resource, Float> entry : productionInDelta.resources.entrySet()) {
                        Resource res = entry.getKey();
                        Float val = entry.getValue();

                        float rDelta = val.intValue();
                        if (rDelta > 0.0f) {
                                productionInDelta.change(res, -rDelta);
                                resources.change(res, -rDelta);
                        }
                }

                //addiere ausgangsressourcen (soweit moeglich)
                for (Map.Entry<Resource, Float> entry : productionOutDelta.resources.entrySet()) {
                        Resource res = entry.getKey();
                        Float val = entry.getValue();

                        float rDelta = val.intValue();
                        if (rDelta > 0.0f) {
                                productionOutDelta.change(res, -rDelta);
                                resources.change(res, Math.min(rDelta, cap.get(res) - resources.get(res)));
                        }
                }
        }

        public boolean canBePlaced(){
                if(wasPlaced)
                        return true;

                if(type == StructureType.Cpu_t1 && Game.world.cpu != null)
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
                        case FastCopperMine:
                                if (getNearResources(World.TerrainType.COPPER) == 0) {
                                        return false;
                                }
                                break;
                        case SilverMine:
                                if (getNearResources(World.TerrainType.SILVER) == 0) {
                                        return false;
                                }
                                break;
                        case GlassMine:
                                if (getNearResources(World.TerrainType.GLASS) == 0) {
                                        return false;
                                }
                                break;
                }

                return Game.world.resources.greaterOrEqual((buildCost));
        }

        public List<Vector2i> getInfluencedTiles() {
                ArrayList<Vector2i> res = new ArrayList<>();

                float radius = influenceRadius * (float)Math.sqrt(2.0);

                for (int x = new Float(position.x - radius).intValue(); x < position.x + dimensions.x + radius; ++x) {
                        if (x < 0 || x >= World.WORLD_DIMENSIONS.x) {
                                continue;
                        }

                        for (int y = new Float(position.y - radius).intValue(); y < position.y + dimensions.y + radius; ++y) {
                                if (y < 0 || y >= World.WORLD_DIMENSIONS.y) {
                                        continue;
                                }

                                Vector2i currentTile = new Vector2i(x, y);

                                Vector2f currentTileCenter = new Vector2f(x + 0.5f, y + 0.5f);
                                Vector2f firstOccupiedCenter = new Vector2f(occupiedTiles.get(0).x + position.x + 0.5f, occupiedTiles.get(0).y + position.y + 0.5f);

                                float minDist = currentTileCenter.distance(firstOccupiedCenter);

                                for (Vector2i occupiedTile : occupiedTiles) {
                                        Vector2f v = new Vector2f(occupiedTile.x + position.x + 0.5f, occupiedTile.y + position.y + 0.5f);
                                        float dist = currentTileCenter.distance(v);

                                        minDist = dist < minDist ? dist : minDist;
                                }

                                if (minDist <= radius) {
                                        res.add(currentTile);
                                }
                        }
                }

                return res;
        }

        public int getNearResources(World.TerrainType searchType){
                int count = 0;
                List<Vector2i> influenced = getInfluencedTiles();
                for (Vector2i tile : influenced) {
                        if (Game.world.terrain[tile.x][tile.y] == searchType) {
                                ++count;
                        }
                }
                return count;
        }

        public boolean initIsProducer(){
                isProducer = false;
                for(Resource resource : Resource.values())
                        if(productionOutPerSec.get(resource) > 0.0f) {
                                isProducer = true;
                                break;
                        }
                return isProducer;
        }

        public boolean initIsConsumer(){
                isConsumer = false;
                for(Resource resource : Resource.values())
                        if(productionInPerSec.get(resource) > 0.0f) {
                                isConsumer = true;
                                break;
                        }
                return isConsumer;
        }

        public void actuallyPlace(){
                Game.sound.playSingle(Game.sound.place);

                switch(type){
                        case CopperMine :
                        case FastCopperMine : productionOutPerSec.multiply(Resource.COPPER, getNearResources(World.TerrainType.COPPER)); break;
                        case SilverMine : productionOutPerSec.multiply(Resource.SILVER, getNearResources(World.TerrainType.SILVER)); break;
                        case GlassMine : productionOutPerSec.multiply(Resource.GLASS, getNearResources(World.TerrainType.GLASS)); break;
                        case Cpu_t1:
                                Game.sound.play(Game.sound.cpu);
                                Game.world.cpu = this;
                                break;
                }
                Game.world.resourceCapacity.add(capacityIncrease);
                for(Vector2i v : occupiedTiles)
                        Game.world.structureGrid[position.x+v.x][position.y+v.y] = this;
                Game.world.structures.add(this);
                Game.world.resources.subtract(this.buildCost);
                Game.world.revalidateRoadAccess();
                wasPlaced = true;
        }

        public void remove(){
                Game.sound.playSingle(Game.sound.demolish);

                if(type == StructureType.Cpu_t1)
                        Game.world.cpu = null;

                Game.world.structures.remove(this);

                for(Vector2i v : occupiedTiles)
                        Game.world.structureGrid[position.x+v.x][position.y+v.y] = null;

                setState(StructureState.NoRoadAccess);

                if(isRoad() || type == StructureType.Cpu_t1)
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
                if(roadAccess.compareTo(road) < 0) {
                        roadAccess = road;
                        switch(road){
                                case NONE : roadFactor = 0.0f; break;
                                case COPPER : roadFactor = 1.0f; break;
                                case SILVER : roadFactor = 1.10f; break;
                                case GLASS : roadFactor = 1.25f; break;
                        }
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

        public boolean usesTerrain(){
                switch(type){
                        case CopperMine :
                        case FastCopperMine :
                        case SilverMine :
                        case GlassMine : return true;
                        default : return false;
                }
        }
}
