package test;

import java.util.ArrayList;
import java.util.List;

public class Structure {
        public Vector2i position;
        public List<Vector2i> occupiedTiles;

        public Structure() {
                occupiedTiles = new ArrayList<Vector2i>();
        }
}
