package test.structures;

import test.Game;
import test.ResourceTable;
import test.Vector2i;

import java.util.ArrayList;
import java.util.List;

public abstract class Structure {
        public Vector2i position;
        public List<Vector2i> occupiedTiles;
        public ResourceTable buildCost;

        public Structure(Vector2i pos) {
                position = pos;
                occupiedTiles = new ArrayList<Vector2i>();
                buildCost = new ResourceTable();
        }

        public boolean collidesWith(Structure other){
                for(Vector2i thisPos : this.occupiedTiles)
                        for(Vector2i otherPos : other.occupiedTiles)
                                if(this.position.x+thisPos.x == other.position.x+otherPos.x
                                        && this.position.y+thisPos.y == other.position.y+otherPos.y)
                                        return true;
                return false;
        }

        public abstract void update();

        public boolean tryToPlaceAt(Vector2i pos){
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
