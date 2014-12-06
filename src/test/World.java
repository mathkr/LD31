package test;

import java.util.ArrayList;

public class World {
        public static enum TerrainType {
                DEFAULT,
                COPPER,
                SILVER,
                GLASS
        }

        public Vector2i bounds = new Vector2i(160, 90);
        public ArrayList<Structure> structures;
        public TerrainType[][] terrain;


        public World() {
                structures = new ArrayList<Structure>();
                terrain = new TerrainType[bounds.x][bounds.y];
        }
}
