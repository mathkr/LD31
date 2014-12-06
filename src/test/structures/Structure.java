package test.structures;

import test.Game;
import test.resources.Resource;
import test.resources.ResourceTable;
import test.Vector2i;

import java.util.ArrayList;
import java.util.List;

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
                //buffere aenderungen von maximal 1.0f
                for(Resource r : Resource.values()) {
                        if (productionInDelta.get(r) < 1.0f)
                                productionInDelta.change(r, productionInPerSec.get(r) * d);
                        if (productionOutDelta.get(r) < 1.0f)
                                productionOutDelta.change(r, productionOutPerSec.get(r) * d);
                }
                //pruefe, ob eingangsressourcen vorhanden
                for(Resource r : Resource.values()) {
                        float expectedChange = (int)productionInDelta.get(r);
                        if (expectedChange >= 1.0f && !resources.canSubstract(r, expectedChange)) {
                                //kein saft :(
                                return;
                        }
                }
                //ziehe eingangsressourcen ab
                for(Resource r : Resource.values()) {
                        float rDelta = productionInDelta.get(r);
                        float change = (int) rDelta;
                        productionInDelta.change(r, -change);
                        resources.change(r, -change);
                }
                //addiere ausgangsressourcen
                //TODO: capacity-check
                for(Resource r : Resource.values()) {
                        float rDelta = productionOutDelta.get(r);
                        float change = (int) rDelta;
                        productionOutDelta.change(r, -change);
                        resources.change(r, +change);
                }
        }

        public boolean tryToPlace(){
                for(Structure other : Game.world.structures)
                        if(collidesWith(other)) {
                                //kein Platz :(
                                return false;
                        }
                if(!Game.world.resources.greaterOrEqual(buildCost))
                        //zu teuer :(
                        return false;
                Game.world.structures.add(this);
                return true;
        }
}
