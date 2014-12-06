package test.structures;

import test.Vector2i;
import test.World;
import test.resources.Resource;

/**
 * Created by msk on 06.12.14.
 * Einfache Glas Mine
 */
public class GlasMine extends Structure{

    private Float prodfactor = 0.05F;

    public GlasMine(Vector2i pos) {
        super(pos);
        occupiedTiles.add(new Vector2i(0,0));
        occupiedTiles.add(new Vector2i(0,1));
        occupiedTiles.add(new Vector2i(1,0));
        occupiedTiles.add(new Vector2i(1,1));
    }

    @Override
    public void update(float d) {
        Integer value = getNearResources(World.TerrainType.GLASS, 1);
        productionOutPerSec.put(Resource.GLASS, value * prodfactor);
        super.update(d);
    }
}
