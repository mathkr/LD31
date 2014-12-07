package test;

public class Vector2i {
        public int x;
        public int y;

        public Vector2i(int x, int y) {
                this.x = x;
                this.y = y;
        }

        public static Vector2i add(Vector2i left, Vector2i right) {
                Vector2i res = new Vector2i(0, 0);

                res.x = left.x + right.x;
                res.y = left.y + right.y;

                return res;
        }

        @Override
        public boolean equals(Object obj) {
                Vector2i other = (Vector2i)obj;

                return     x == other.x
                        && y == other.y;
        }

        @Override
        public String toString() {
                return "x: " + x + ", y: " + y;
        }
}
