package test.structures;

import test.Vector2i;
import test.resources.Resource;

public class S_RAM extends Structure{

    public S_RAM(Vector2i pos){
        super(pos);
        occupiedTiles.add(new Vector2i(0,0));
        occupiedTiles.add(new Vector2i(0,1));
        occupiedTiles.add(new Vector2i(1,0));
        occupiedTiles.add(new Vector2i(1,1));
        occupiedTiles.add(new Vector2i(2,0));
        occupiedTiles.add(new Vector2i(2,1));
        occupiedTiles.add(new Vector2i(3,0));
        occupiedTiles.add(new Vector2i(3,1));
        buildCost.put(Resource.SILICON, 160.0f);
        productionInPerSec.put(Resource.ENERGY, 0.2f);
        capacityIncrease.put(Resource.ELECTRON, 500.0f);
    }

}
