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
        public List<Vector2i> occupiedTiles;
        public ResourceTable buildCost;
        public ResourceTable productionInDelta;
        public ResourceTable productionOutDelta;
        public ResourceTable productionInPerSec;
        public ResourceTable productionOutPerSec;
        public ResourceTable capacityIncrease;
        public float productionFactor;
        public StructureLoader.Updater updater;

        public Image image;

        public Structure(Vector2i pos) {
                position = pos;
                occupiedTiles = new ArrayList<>();
                buildCost = new ResourceTable();
                productionInDelta = new ResourceTable();
                productionOutDelta = new ResourceTable();
                productionInPerSec = new ResourceTable();
                productionOutPerSec = new ResourceTable();
                capacityIncrease = new ResourceTable();
        }

        public boolean collidesWith(Structure other){
                for(Vector2i thisPos : this.occupiedTiles)
                        for(Vector2i otherPos : other.occupiedTiles)
                                if(this.position.x+thisPos.x == other.position.x+otherPos.x
                                        && this.position.y+thisPos.y == other.position.y+otherPos.y)
                                        return true;
                return false;
        }

        public void update(float d){
                if (updater != null) {
                        updater.update(this);
                }

                ResourceTable resources = Game.world.resources;
                ResourceTable cap = Game.world.resourceCapacity;
                //buffere aenderungen, solange unter 1.0f
                productionInPerSec.resources.forEach((res, val) -> {
                        if (productionInDelta.get(res) < 1.0f)
                                productionInDelta.change(res, productionInPerSec.get(res) * d);
                });
                productionOutPerSec.resources.forEach((res, val) -> {
                        if (productionOutDelta.get(res) < 1.0f)
                                productionOutDelta.change(res, productionOutPerSec.get(res) * d);
                });
                //pruefe, ob eingangsressourcen vorhanden
                for(Map.Entry<Resource, Float> e : productionInDelta.resources.entrySet()){
                        float rDelta = e.getValue().intValue();
                        if (rDelta >= 1.0f && !resources.canSubstract(e.getKey(), rDelta)) {
                                //kein saft :(
                                return;
                        }
                }
                //pruefe, ob fuer mindestens eine der produzierten ressourcen kapazitaet vorhanden ist
                boolean hasCapacity = false;
                for(Map.Entry<Resource, Float> e : productionOutPerSec.resources.entrySet()){
                        if(resources.get(e.getKey()) < cap.get(e.getKey())) {
                                hasCapacity = true;
                                break;
                        }
                }
                if(!hasCapacity)
                        //lager voll :(
                        return;
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
                for(Structure other : Game.world.structures) {
                        if (collidesWith(other)) {
                                //kein Platz :(
                                return false;
                        }
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
                max.x = this.position.x + max.x + area < Game.world.bounds.x   ? max.x + area : max.x;
                max.y = this.position.y + max.y + area < Game.world.bounds.y  ? max.y + area : max.y;

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
                Game.world.structures.add(this);
                Game.world.resources.subtract(this.buildCost);
                Game.world.resourceCapacity.add(this.capacityIncrease);
        }

        public void remove(){
                Game.world.resourceCapacity.subtract(this.capacityIncrease);
                Game.world.trimResourcesToCap();
        }
}
