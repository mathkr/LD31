package test.structures;

import test.Game;
import test.Vector2i;
import test.World;
import test.resources.Resource;

/**
 * Created by msk on 06.12.14.
 * Einfache Silber Mine
 */
public class SilverMine extends Structure {

    private final Float prodfactor = 0.1F;

    public SilverMine(Vector2i pos) {
        super(pos);
        image = Game.renderer.getImage("resources/silver_mine.png");

        occupiedTiles.add(new Vector2i(0, 0));
        occupiedTiles.add(new Vector2i(0, 1));
        occupiedTiles.add(new Vector2i(1, 0));
        occupiedTiles.add(new Vector2i(1, 3));
        productionOutPerSec.put(Resource.SILVER, prodfactor * getNearResources(World.TerrainType.SILVER, 1));
    }
}
