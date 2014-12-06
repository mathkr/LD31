package test.structures;

import test.Game;
import test.Vector2i;

public class CopperMill extends Structure{

    public CopperMill(Vector2i pos){
        super(pos);
        occupiedTiles.add(new Vector2i(0,0));
        occupiedTiles.add(new Vector2i(0,1));
        occupiedTiles.add(new Vector2i(1,0));
        occupiedTiles.add(new Vector2i(1,1));
        occupiedTiles.add(new Vector2i(2,1));
    }

    public void update(){
        Game.world.resources.copper += 1;
    }
}
