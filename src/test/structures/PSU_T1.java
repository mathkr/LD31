package test.structures;

import test.Vector2i;
import test.resources.Resource;

public class PSU_T1 extends Structure{

    public PSU_T1(Vector2i pos){
        super(pos);
        occupiedTiles.add(new Vector2i(0,0));
        occupiedTiles.add(new Vector2i(0,1));
        occupiedTiles.add(new Vector2i(0,2));
        occupiedTiles.add(new Vector2i(1,0));
        occupiedTiles.add(new Vector2i(1,1));
        occupiedTiles.add(new Vector2i(1,2));
        occupiedTiles.add(new Vector2i(2,0));
        occupiedTiles.add(new Vector2i(2,1));
        occupiedTiles.add(new Vector2i(2,2));
        buildCost.put(Resource.SILICON, 80.0f);
        productionOutPerSec.put(Resource.ENERGY, 0.333f);
    }
}
