package test;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.*;

public class Game extends BasicGame
{
        public static World world;

        public Game(String gamename) {
                super(gamename);
        }

        @Override
        public void init(GameContainer gc) throws SlickException {
                world = new World();
        }

        @Override
        public void update(GameContainer gc, int i) throws SlickException {
        }

        @Override
        public void render(GameContainer gc, Graphics g) throws SlickException {
                g.drawString("Howdy!", 40, 40);

                g.drawRect(10, 10, 200, 150);
        }

        public static void main(String[] args) {
                try {
                        AppGameContainer appgc;
                        appgc = new AppGameContainer(new Game("Simple Slick Game"));
                        appgc.setDisplayMode(640, 480, false);
                        appgc.setTargetFrameRate(60);
                        appgc.start();
                } catch (SlickException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
}
