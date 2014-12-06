package test.structures;

import test.Game;
import test.Vector2i;
import test.World;
import test.resources.Resource;


/**
 * Created by msk on 06.12.14.
 * Einfache Kupfer Mine
 */
public class CopperMine extends Structure {

    private Float prodfactor = 0.2F;

    public CopperMine(Vector2i pos) {
        super(pos);
        image = Game.renderer.getImage("resources/copper_mine.png");

        occupiedTiles.add(new Vector2i(0, 0));
        occupiedTiles.add(new Vector2i(0, 1));
        occupiedTiles.add(new Vector2i(1, 0));
        occupiedTiles.add(new Vector2i(1, 3));
    }

    @Override
    public void update(float d) {
        Integer value = getNearResources(World.TerrainType.COPPER, 1);

        productionOutPerSec.put(Resource.COPPER, prodfactor * value);
        super.update(d);
    }
}
