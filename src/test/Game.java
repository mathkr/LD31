package test;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.*;

public class Game extends BasicGame
{
        public static final int WIN_WIDTH = 800;
        public static final int WIN_HEIGHT = 600;

        public static World world;
        public static Renderer renderer;

        public Game(String gamename) {
                super(gamename);
        }

        @Override
        public void init(GameContainer gc) throws SlickException {
                world = new World();
                renderer = new Renderer();
        }

        @Override
        public void update(GameContainer gc, int i) throws SlickException {
        }

        @Override
        public void render(GameContainer gc, Graphics g) throws SlickException {
                renderer.render(gc, g);
        }

        public static void main(String[] args) {
                try {
                        AppGameContainer appgc;
                        appgc = new AppGameContainer(new Game("Simple Slick Game"));
                        appgc.setDisplayMode(WIN_WIDTH, WIN_HEIGHT, false);
                        appgc.setTargetFrameRate(60);
                        appgc.start();
                } catch (SlickException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
}
