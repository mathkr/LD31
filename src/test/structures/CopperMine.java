package test.structures;

import test.Game;
import test.Vector2i;
import test.World;
import test.resources.Resource;

import java.util.ArrayList;

/**
 * Created by msk on 06.12.14.
 */
public class CopperMine extends Structure {

    private Float prodfactor = 0.2F;

    public CopperMine(Vector2i pos) {
        super(pos);
        occupiedTiles.add(new Vector2i(-1, 0));
        occupiedTiles.add(new Vector2i(0, -1));
        occupiedTiles.add(new Vector2i(0, 0));
        occupiedTiles.add(new Vector2i(0, 1));
        occupiedTiles.add(new Vector2i(1, 0));

        buildCost.put(Resource.SILICON, 0F);



    }

    @Override
    public void update(float d) {
        Float factor = 0F;
        for (int i = Math.max(this.position.x - 2, 0); i < Math.min(5, Game.world.bounds.x - this.position.x); i++) {
            for (int j = Math.max(this.position.y - 2, 0); j < Math.min(5, Game.world.bounds.y - this.position.y); j++) {
                if(Game.world.terrain[i][j] == World.TerrainType.COPPER){
                    factor += prodfactor;
                }
            }
        }
        productionOutPerSec.put(Resource.COPPER, factor);
        super.update(d);
    }
}
