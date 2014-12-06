package test;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.GUIContext;
import org.newdawn.slick.gui.MouseOverArea;
import test.structures.CopperMill;
import test.structures.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UserInterface {
        public List<MyButton> buttons;
        private static Stack<MyButton> overlayButtons;

        Structure structureToPlace;

        public Vector2i guiTopLeft = new Vector2i(0, 0);
        public int buttonSize = 40;
        public int buttonMargins = 10;

        public UserInterface(GameContainer gc) {
                buttons = new ArrayList<>();
                overlayButtons = new Stack<>();
                structureToPlace = null;

                // add Buttons
                try {
                        Vector2i currentButtonPosition = new Vector2i(guiTopLeft.x + buttonMargins,
                                guiTopLeft.y + buttonMargins);

                        addButton(
                                buttons,
                                "Place copper mill",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = new CopperMill(
                                                new Vector2i(
                                                        Game.getWorldMouseX(),
                                                        Game.getWorldMouseY()
                                                ));
                                }
                        );
                } catch (SlickException e) {
                        e.printStackTrace();
                }
        }

        public void addButton(List<MyButton> buttonList, String description, GUIContext context,
                              Image image, Vector2i position, int length, ComponentListener listener)
        {
                final MyButton button = new MyButton(
                        description,
                        context,
                        image,
                        position.x,
                        position.y,
                        length,
                        length);
                button.addListener(listener);
                buttonList.add(button);
                position.x += length + buttonMargins;
        }

        public void render(GameContainer gc, Graphics g) {
                for (MyButton button : buttons) {
                        button.render(gc, g);
                }

                g.setColor(Color.white);
                g.drawString(Game.world.resources.toString(), 10, Game.WIN_HEIGHT - 40);

                while (!overlayButtons.empty()) {
                        MyButton button = overlayButtons.pop();
                        g.setColor(Color.white);
                        g.drawString(button.buttonDescription, button.getX() + 10, button.getY() + 45);
                }
        }

        public void update(GameContainer gc) {
                if (structureToPlace != null) {
                        structureToPlace.position.x = Game.getWorldMouseX();
                        structureToPlace.position.y = Game.getWorldMouseY();
                }
        }

        public static class MyButton extends MouseOverArea {
                public String buttonDescription;

                public MyButton(String desc, GUIContext container, Image image, int x, int y, ComponentListener listener) {
                        super(container, image, x, y, listener);
                        buttonDescription = desc;
                }

                public MyButton(String desc, GUIContext container, Image image, int x, int y) {
                        super(container, image, x, y);
                        buttonDescription = desc;
                }

                public MyButton(String desc, GUIContext container, Image image, int x, int y, int width, int height, ComponentListener listener) {
                        super(container, image, x, y, width, height, listener);
                        buttonDescription = desc;
                }

                public MyButton(String desc, GUIContext container, Image image, int x, int y, int width, int height) {
                        super(container, image, x, y, width, height);
                        buttonDescription = desc;
                }

                public MyButton(String desc, GUIContext container, Image image, Shape shape) {
                        super(container, image, shape);
                        buttonDescription = desc;
                }

                @Override
                public void render(GUIContext container, Graphics g) {
                        super.render(container, g);
                        if (isMouseOver()) {
                                overlayButtons.push(this);
                        }
                }
        }
}
