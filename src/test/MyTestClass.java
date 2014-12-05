package test;

/**
 * Created by msk on 05.12.14.
 */
public class MyTestClass {
        private class Code {
                String[] lots_of_code;
        }

        private class ClubMate {
                int[] tasty_mate;
        }

        Code work(ClubMate[] lots_of_mate) {
                System.out.println("Wurst");
                return new Code();
        }
}
