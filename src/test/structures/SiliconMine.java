package test.structures;

import test.Vector2i;
import test.resources.Resource;

public class SiliconMine extends Structure{

    public SiliconMine(Vector2i pos){
        super(pos);
        occupiedTiles.add(new Vector2i(0,0));
        occupiedTiles.add(new Vector2i(0,1));
        occupiedTiles.add(new Vector2i(0,2));
        occupiedTiles.add(new Vector2i(1,0));
        occupiedTiles.add(new Vector2i(1,2));
        buildCost.put(Resource.ENERGY, 60.0f);
        productionInPerSec.put(Resource.ENERGY, 0.10f);
        productionOutPerSec.put(Resource.SILICON, 0.6f);
    }
}
