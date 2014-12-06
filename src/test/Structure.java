package test;

import java.util.ArrayList;
import java.util.List;

public abstract class Structure {
        public Vector2i position;
        public List<Vector2i> occupiedTiles;

        public Structure() {
                occupiedTiles = new ArrayList<Vector2i>();
        }

        public abstract void update();

        public boolean colidesWith(Structure other){
                for(Vector2i thisPos : this.occupiedTiles)
                        for(Vector2i otherPos : other.occupiedTiles)
                                if(this.position.x+thisPos.x == other.position.x+otherPos.x
                                        && this.position.y+thisPos.y == other.position.y+otherPos.y)
                                        return true;
                return false;
        }

        public abstract ResourceTable getCost();
}
