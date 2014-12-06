package test.structures;

import test.Game;
import test.resources.Resource;
import test.resources.ResourceTable;
import test.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Structure {
        public Vector2i position;
        public List<Vector2i> occupiedTiles;
        public ResourceTable buildCost;
        public ResourceTable productionInDelta;
        public ResourceTable productionOutDelta;
        public ResourceTable productionInPerSec;
        public ResourceTable productionOutPerSec;

        public Structure(Vector2i pos) {
                position = pos;
                occupiedTiles = new ArrayList<Vector2i>();
                buildCost = new ResourceTable();
                productionInDelta = new ResourceTable();
                productionOutDelta = new ResourceTable();
                productionInPerSec = new ResourceTable();
                productionOutPerSec = new ResourceTable();
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
                ResourceTable resources = Game.world.resources;
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
                //ziehe eingangsressourcen ab
                productionInDelta.resources.forEach((res, val) -> {
                        float rDelta = val.intValue();
                        if (rDelta >= 1.0f) {
                                productionInDelta.change(res, -rDelta);
                                resources.change(res, -rDelta);
                        }
                });
                //addiere ausgangsressourcen
                //TODO: capacity-check
                productionOutDelta.resources.forEach((res, val) -> {
                        float rDelta = (int)(float)val;
                        if(rDelta >= 1.0f){
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

        public void actuallyPlace(){
                Game.world.structures.add(this);
                Game.world.resources.subtract(this.buildCost);
        }
}
