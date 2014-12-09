package test;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.GUIContext;
import org.newdawn.slick.util.FastTrig;
import slick.MouseOverArea;
import test.resources.Resource;
import test.structures.*;

import java.util.*;

public class UserInterface {
        enum InterfaceState {
                OFF,
                PLACING,
                SELECTING,
                REMOVING,
        }

        public static Resource[] base = { Resource.COPPER, Resource.SILVER, Resource.GLASS, Resource.SILICON };
        public static Resource[] energy = { Resource.ENERGY };
        public static Resource[] population = { Resource.ELECTRON, Resource.PHOTON, Resource.QUANTUM };
        public static Resource[] data = { Resource.DATA, Resource.SOUND, Resource.GRAPHICS, Resource.BITCOINS};

        public List<MyButton> buttons;
        private static Stack<MyButton> overlayButtons;

        public SideMenu menu;
        public InterfaceState guiState;
        public Structure structureToPlace;
        public Structure selectedStructure;

        Color selectionColor = new Color(0xFF, 0xFF, 0xFF);
        float selectionColorTime = 0f;

        public int buttonMargins = 20;
        public int buttonSize = Renderer.HEADER_HEIGHT - buttonMargins;

        public UserInterface(GameContainer gc) {
                menu = new SideMenu();
                buttons = new ArrayList<>();
                overlayButtons = new Stack<>();

                structureToPlace = null;
                guiState = InterfaceState.OFF;
                selectedStructure = null;

                // Add a button per StructureType
                Vector2i buttonPos = new Vector2i(buttonMargins / 2, buttonMargins / 2);
                for (StructureType structureType : StructureType.values()) {
                        String name = StructureLoader.getProperties(structureType).getProperty("name", "no name");
                        String desc = StructureLoader.getProperties(structureType).getProperty("desc", "no description");
                        String imgPath = StructureLoader.getProperties(structureType).getProperty("image");

                        Rectangle shape = new Rectangle(buttonPos.x, buttonPos.y, buttonSize, buttonSize);

                        Image image = null;

                        if (imgPath != null) {
                                image = Game.renderer.getImage("resources/" + imgPath).copy();

                                Color filterColor;
                                switch (structureType) {
                                        case CopperRoad:
                                                filterColor = Game.renderer.TERRAIN_COPPER_COLOR;
                                                break;
                                        case SilverRoad:
                                                filterColor = Game.renderer.TERRAIN_SILVER_COLOR;
                                                break;
                                        case GlassRoad:
                                                filterColor = Game.renderer.TERRAIN_GLASS_COLOR;
                                                break;
                                        default:
                                                filterColor = Color.white;
                                                break;
                                }

                                image.setImageColor(filterColor.r, filterColor.g, filterColor.b, filterColor.a);
                        }

                        MyButton button = new MyButton(name + ": " + desc, gc, image, shape);

                        button.addListener((component) -> {
                                        structureToPlace = StructureLoader.getInstance(structureType,
                                                0,
                                                0
                                        );
                                        guiState = InterfaceState.PLACING;
                                });

                        buttons.add(button);

                        buttonPos.x += buttonSize + buttonMargins;
                }

                gc.getInput().addMouseListener(new MouseListener() {
                        @Override
                        public void mouseWheelMoved(int change) { }

                        @Override
                        public void mouseClicked(int button, int x, int y, int clickCount) { }

                        @Override
                        public void mousePressed(int button, int x, int y) {
                        }

                        @Override
                        public void mouseReleased(int button, int x, int y) {
                                if (button == Input.MOUSE_LEFT_BUTTON) {
                                        int worldX = (Game.appgc.getInput().getAbsoluteMouseX() - Game.renderer.stagePosition.x) / Game.renderer.tileSize;
                                        int worldY = (Game.appgc.getInput().getAbsoluteMouseY() - Game.renderer.stagePosition.y) / Game.renderer.tileSize;
                                        Vector2i clickedPosition = new Vector2i(worldX, worldY);

                                        if (worldX >= 0 && worldX < Game.world.WORLD_DIMENSIONS.x && worldY >= 0 && worldY < Game.world.WORLD_DIMENSIONS.y) {
                                                // Hier haben wir auf ein gueltiges tile geclickt

                                                if (guiState == InterfaceState.REMOVING) {
                                                        if (Game.world.structureGrid[worldX][worldY] != null) {
                                                                Game.world.structureGrid[worldX][worldY].remove();
                                                        }
                                                } else if (structureToPlace != null) {
                                                        // can it be placed? roads get handled in mouse dragged callback
                                                        structureToPlace.position.x = worldX;
                                                        structureToPlace.position.y = worldY;

                                                        structureToPlace.position.x = structureToPlace.position.x + structureToPlace.dimensions.x >= World.WORLD_DIMENSIONS.x ?
                                                                World.WORLD_DIMENSIONS.x - structureToPlace.dimensions.x : structureToPlace.position.x;

                                                        structureToPlace.position.y = structureToPlace.position.y + structureToPlace.dimensions.y >= World.WORLD_DIMENSIONS.y ?
                                                                World.WORLD_DIMENSIONS.y - structureToPlace.dimensions.y : structureToPlace.position.y;

                                                        if (structureToPlace.canBePlaced()) {
                                                                structureToPlace.actuallyPlace();

                                                                int particleX = x;
                                                                int particleW = structureToPlace.dimensions.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                                int particleY = y;
                                                                int particleH = structureToPlace.dimensions.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;

                                                                Game.renderer.spawnParticlesInArea(
                                                                        particleX, particleY,
                                                                        particleW, particleH,
                                                                        40, 1, 20,
                                                                        Game.renderer.TERRAIN_DEFAULT_COLOR.brighter(0.7f), 1f);

                                                                if (!structureToPlace.isRoad()) { // Only stop placing if we arent placing a wire
                                                                        structureToPlace = null;
                                                                        guiState = InterfaceState.OFF;
                                                                } else { // If we are, make a new wire piece to place
                                                                        structureToPlace = StructureLoader.getInstance(structureToPlace.type, structureToPlace.position.x,
                                                                                structureToPlace.position.y);
                                                                }
                                                        }
                                                } else {
                                                        // See if we clicked on a structure
                                                        Structure clickedStructure = null;
                                                        int structureIndex = 0;
                                                        while (clickedStructure == null && structureIndex < Game.world.structures.size()) {
                                                                Structure current = Game.world.structures.get(structureIndex);

                                                                for (Vector2i occupiedTile : current.occupiedTiles) {
                                                                        if (Vector2i.add(current.position, occupiedTile).equals(clickedPosition)) {
                                                                                clickedStructure = current;
                                                                                break;
                                                                        }
                                                                }

                                                                ++structureIndex;
                                                        }

                                                        if (clickedStructure != null) {
                                                                // We clicked on a structure
                                                                selectedStructure = clickedStructure;
                                                                guiState = InterfaceState.SELECTING;
                                                                selectionColorTime = 0f;
                                                        } else {
                                                                // We clicked on nothing
                                                                selectedStructure = null;
                                                                guiState = InterfaceState.OFF;
                                                        }
                                                }
                                        }
                                }

                                if (button == Input.MOUSE_RIGHT_BUTTON) {
                                        structureToPlace = null;
                                        selectedStructure = null;
                                        guiState = InterfaceState.OFF;
                                }
                        }

                        @Override
                        public void mouseMoved(int oldx, int oldy, int newx, int newy) { }

                        @Override
                        public void mouseDragged(int oldx, int oldy, int newx, int newy) {
                                if (Game.appgc.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
                                        if (       oldx >  Game.renderer.stagePosition.x
                                                && oldx <= Game.renderer.stagePosition.x + Game.renderer.stageDimensions.x
                                                && oldy >  Game.renderer.stagePosition.y
                                                && oldy <= Game.renderer.stagePosition.y + Game.renderer.stageDimensions.y)
                                        {
                                                int startx = Game.getWorldX(oldx);
                                                int starty = Game.getWorldY(oldy);

                                                int endx = Game.getWorldX(newx);
                                                int endy = Game.getWorldY(newy);

                                                int deltax = endx - startx;
                                                int deltay = endy - starty;

                                                int signx = (int)Math.signum(deltax);
                                                int signy = (int)Math.signum(deltay);

                                                boolean stepX = Math.abs(deltax) > Math.abs(deltay);

                                                int currentx = startx;
                                                int currenty = starty;

                                                while (true) {
                                                        if (structureToPlace != null && guiState == InterfaceState.PLACING && structureToPlace.isRoad()) {
                                                                // Placing roads
                                                                structureToPlace.position.x = currentx;
                                                                structureToPlace.position.y = currenty;

                                                                if (structureToPlace.canBePlaced()) {
                                                                        structureToPlace.actuallyPlace();

                                                                        int particleX = newx;
                                                                        int particleW = structureToPlace.dimensions.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                                        int particleY = newy;
                                                                        int particleH = structureToPlace.dimensions.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;

                                                                        Game.renderer.spawnParticlesInArea(
                                                                                particleX, particleY,
                                                                                particleW, particleH,
                                                                                40, 1, 20,
                                                                                Game.renderer.TERRAIN_DEFAULT_COLOR.brighter(0.7f), 1f);

                                                                        structureToPlace = StructureLoader.getInstance(structureToPlace.type, structureToPlace.position.x,
                                                                                structureToPlace.position.y);
                                                                }
                                                        } if (guiState == InterfaceState.REMOVING) {
                                                                if (Game.world.structureGrid[currentx][currenty] != null) {
                                                                        Structure remStruct = Game.world.structureGrid[currentx][currenty];

                                                                        int particleX = Game.renderer.stagePosition.x + remStruct.position.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                                        int particleW = remStruct.dimensions.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                                        int particleY = Game.renderer.stagePosition.y + remStruct.position.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                                        int particleH = remStruct.dimensions.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;

                                                                        Game.renderer.spawnParticlesInArea(
                                                                                particleX, particleY,
                                                                                particleW, particleH,
                                                                                40, 1, 20,
                                                                                Color.darkGray, 1f);

                                                                        Game.renderer.spawnParticlesInArea(
                                                                                particleX, particleY,
                                                                                particleW, particleH,
                                                                                40, 1, 20,
                                                                                Color.lightGray, 1f);

                                                                        remStruct.remove();
                                                                }
                                                        }

                                                        if (currentx == endx && currenty == endy) {
                                                                break;
                                                        }

                                                        if (currentx == endx) {
                                                                stepX = false;
                                                        }

                                                        if (currenty == endy) {
                                                                stepX = true;
                                                        }

                                                        if (stepX) {
                                                                currentx += signx;
                                                                stepX = !stepX;
                                                        } else {
                                                                currenty += signy;
                                                                stepX = !stepX;
                                                        }
                                                }
                                        }
                                }
                        }

                        @Override
                        public void setInput(Input input) { }

                        @Override
                        public boolean isAcceptingInput() {
                                return true;
                        }

                        @Override
                        public void inputEnded() { }

                        @Override
                        public void inputStarted() { }
                });
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
                if (buttonList != null) {
                        buttonList.add(button);
                }
                button.addListener(listener);
                position.x += length + buttonMargins;
        }

        public void render(GameContainer gc, Graphics g) {
                for (MyButton button : buttons) {
                        button.render(gc, g);
                }

                g.setColor(Color.white);

                int textY = (Game.WIN_HEIGHT - Renderer.FOOTER_HEIGHT) + buttonMargins / 2;
                int textX = buttonMargins / 2;

                for (Resource resource : base) {
                        String name = resource.name() + ":";
                        String res = Integer.toString(Math.round(Game.world.resources.get(resource)));
                        String div = "/";
                        String of = Integer.toString(Math.round(Game.world.resourceCapacity.get(resource)));

                        g.drawString(name, textX, textY);
                        textX += Game.renderer.font.getWidth(name) + Game.renderer.font.getWidth("#");

                        float resourceVal = Game.world.resources.get(resource);
                        float resourceCap = Game.world.resourceCapacity.get(resource);

                        boolean atCap = resourceVal == resourceCap;
                        boolean overCap = resourceVal > resourceCap;

                        if (atCap) {
                                g.setColor(Color.yellow);
                        } else if (overCap) {
                                g.setColor(Color.red);
                        }
                        g.drawString(res, textX, textY);
                        textX += Game.renderer.font.getWidth(res);

                        g.setColor(Color.white);
                        g.drawString(div, textX, textY);
                        textX += Game.renderer.font.getWidth(div);

                        g.drawString(of, textX, textY);
                        textX += Game.renderer.font.getWidth(of) + Game.renderer.font.getWidth("#");
                }

                textX = (Game.WIN_WIDTH - buttonMargins / 2);
                for (Resource resource : energy) {
                        String name = resource.name() + ":";
                        String res = Integer.toString(Math.round(Game.world.resources.get(resource)));
                        String div = "/";
                        String of = Integer.toString(Math.round(Game.world.resourceCapacity.get(resource)));

                        textX -= Game.renderer.font.getWidth(of);
                        g.drawString(of, textX, textY);

                        textX -= Game.renderer.font.getWidth(div);
                        g.drawString(div, textX, textY);


                        float resourceVal = Game.world.resources.get(resource);
                        float resourceCap = Game.world.resourceCapacity.get(resource);

                        boolean atCap = resourceVal == resourceCap;
                        boolean overCap = resourceVal > resourceCap;

                        if (atCap) {
                                g.setColor(Color.yellow);
                        } else if (overCap) {
                                g.setColor(Color.red);
                        }
                        textX -= Game.renderer.font.getWidth(res);
                        g.drawString(res, textX, textY);

                        g.setColor(Color.white);
                        textX -= Game.renderer.font.getWidth(name) + Game.renderer.font.getWidth("#");
                        g.drawString(name, textX, textY);
                }

                textX = buttonMargins / 2;
                textY += buttonMargins / 2 + Game.renderer.font.getLineHeight();
                for (Resource resource : population) {
                        String name = resource.name() + ":";
                        String res = Integer.toString(Math.round(Game.world.resources.get(resource)));
                        String div = "/";
                        String of = Integer.toString(Math.round(Game.world.resourceCapacity.get(resource)));

                        g.drawString(name, textX, textY);
                        textX += Game.renderer.font.getWidth(name) + Game.renderer.font.getWidth("#");

                        float resourceVal = Game.world.resources.get(resource);
                        float resourceCap = Game.world.resourceCapacity.get(resource);

                        boolean atCap = resourceVal == resourceCap;
                        boolean overCap = resourceVal > resourceCap;

                        if (atCap) {
                                g.setColor(Color.yellow);
                        } else if (overCap) {
                                g.setColor(Color.red);
                        }
                        g.drawString(res, textX, textY);
                        textX += Game.renderer.font.getWidth(res);

                        g.setColor(Color.white);
                        g.drawString(div, textX, textY);
                        textX += Game.renderer.font.getWidth(div);

                        g.drawString(of, textX, textY);
                        textX += Game.renderer.font.getWidth(of) + Game.renderer.font.getWidth("#");
                }

                textX = (Game.WIN_WIDTH - buttonMargins / 2);
                int index = 0;
                for (Resource resource : data) {
                        String name = resource.name() + ":";
                        String res = Integer.toString(Math.round(Game.world.resources.get(resource)));
                        String div = "/";
                        String of = Integer.toString(Math.round(Game.world.resourceCapacity.get(resource)));

                        if (index == 0) {
                                textX -= Game.renderer.font.getWidth(of);
                        } else {
                                textX -= Game.renderer.font.getWidth(of) + Game.renderer.font.getWidth("#");
                        }
                        g.drawString(of, textX, textY);

                        textX -= Game.renderer.font.getWidth(div);
                        g.drawString(div, textX, textY);


                        float resourceVal = Game.world.resources.get(resource);
                        float resourceCap = Game.world.resourceCapacity.get(resource);

                        boolean atCap = resourceVal == resourceCap;
                        boolean overCap = resourceVal > resourceCap;

                        if (atCap) {
                                g.setColor(Color.yellow);
                        } else if (overCap) {
                                g.setColor(Color.red);
                        }
                        textX -= Game.renderer.font.getWidth(res);
                        g.drawString(res, textX, textY);

                        g.setColor(Color.white);
                        textX -= Game.renderer.font.getWidth(name) + Game.renderer.font.getWidth("#");
                        g.drawString(name, textX, textY);
                        ++index;
                }

                menu.render(g);

                while (!overlayButtons.empty()) {
                        MyButton button = overlayButtons.pop();
                        g.setColor(Color.white);

                        int boxW = Game.renderer.font.getWidth(button.buttonDescription) + buttonMargins;
                        int boxH = Game.renderer.font.getHeight(button.buttonDescription) + buttonMargins;

                        int ySign = button.getY() > Game.WIN_HEIGHT / 2 ? -1 : 1;
                        int yOffset = 50;

                        int boxX = button.getX();
                        if (boxX + boxW > Game.WIN_WIDTH) {
                                boxX = Game.WIN_WIDTH - (boxW + buttonMargins / 2);
                        }

                        int boxY = button.getY() + ySign * yOffset;

                        g.setColor(Game.renderer.TERRAIN_DEFAULT_COLOR);
                        g.fillRect(boxX, boxY, boxW, boxH);

                        g.setColor(Color.white);
                        g.drawString(button.buttonDescription, boxX + buttonMargins / 2, boxY + buttonMargins / 2);

                        g.setColor(Game.renderer.TERRAIN_GLASS_COLOR);
                        g.drawRect(boxX, boxY, boxW, boxH);
                }
        }

        public void update(GameContainer gc, float delta) {
                if (structureToPlace != null) {
                        int newPosX = Game.getWorldMouseX();
                        int newPosY = Game.getWorldMouseY();

                        newPosX = newPosX < 0 ? 0 : newPosX;
                        newPosX = newPosX >= World.WORLD_DIMENSIONS.x - structureToPlace.dimensions.x
                                ? (World.WORLD_DIMENSIONS.x - structureToPlace.dimensions.x) : newPosX;
                        
                        newPosY = newPosY < 0 ? 0 : newPosY;
                        newPosY = newPosY >= World.WORLD_DIMENSIONS.y - structureToPlace.dimensions.y
                                ? (World.WORLD_DIMENSIONS.y - structureToPlace.dimensions.y) : newPosY;

                        structureToPlace.position.x = newPosX;
                        structureToPlace.position.y = newPosY;
                }

                selectionColorTime += 5 * delta;
                float alpha = Math.abs((float) FastTrig.sin(selectionColorTime));
                selectionColor = new Color(0xFF, 0xFF, 0xFF, alpha);
        }

        public static class MyButton extends MouseOverArea {
                public String buttonDescription;
                public Rectangle shape;

                public MyButton(String desc, GUIContext container, Image image, int x, int y, int width, int height) {
                        super(container, image, x, y, width, height);
                        buttonDescription = desc;
                }

                public MyButton(String desc, GUIContext container, Image image, Rectangle shape) {
                        super(container, image, shape);
                        this.shape = shape;
                        buttonDescription = desc;
                }

                @Override
                public void render(GUIContext container, Graphics g) {
                        g.setColor(Game.renderer.TERRAIN_DEFAULT_COLOR);
                        if (shape != null) {
                                g.fill(shape);
                        }

                        if (isMouseOver()) {
                                overlayButtons.push(this);
                        }

                        Rectangle clip = g.getClip();
                        g.setClip(getX(), getY(), getWidth(), getHeight());

                        super.render(container, g);

                        g.setClip(clip);

                        if (isMouseOver()) {
                                g.setColor(Game.renderer.TERRAIN_GLASS_COLOR);
                        } else {
                                g.setColor(Game.renderer.TERRAIN_SILVER_COLOR);
                        }

                        if (shape != null) {
                                g.drawRect(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
                        }
                }
        }

        public static String addLineBreaks(String string, int width) {
                int w = 0;
                int lastSpaceIndex = -1;

                StringBuilder sb = new StringBuilder(string);

                for (int i = 0; i < string.length(); ++i) {
                        if (string.charAt(i) == ' ') {
                                lastSpaceIndex = i;
                        }

                        w += Game.renderer.font.getWidth(Character.toString(string.charAt(i)));

                        if (w > width) {
                                if (lastSpaceIndex != -1) {
                                        sb.setCharAt(lastSpaceIndex, '\n');

                                        int newWidth = 0;
                                        for (int j = lastSpaceIndex + 1; j <= i; ++j) {
                                                newWidth += Game.renderer.font.getWidth(Character.toString(string.charAt(j)));
                                        }

                                        lastSpaceIndex = -1;
                                        w = newWidth;
                                } else {
                                        sb.insert(i, '\n');
                                        lastSpaceIndex = -1;
                                        w = 0;
                                }
                        }
                }

                return sb.toString();
        }

        class SideMenu {
                public MyButton removeButton;
                public MyButton standbyButton;
                public MyButton demolitionButton;

                public Vector2i menuPos;
                public Vector2i menuDim;

                public SideMenu(){
                        try {
                                String demolition = "Demolition mode";
                                Image demolitionButtonImage = new Image(Game.renderer.font.getWidth(demolition) + 10, Game.renderer.font.getHeight(demolition) + 10);
                                demolitionButtonImage.getGraphics().setFont(Game.renderer.font);
                                demolitionButtonImage.getGraphics().drawString(demolition, 5, 5);
                                demolitionButtonImage.getGraphics().flush();
                                Rectangle demolitionRect = new Rectangle(0, 0, demolitionButtonImage.getWidth(), demolitionButtonImage.getHeight());

                                demolitionButton = new MyButton(
                                        "Demolition mode allows you to demolish placed structures",
                                        Game.appgc,
                                        demolitionButtonImage,
                                        demolitionRect);

                                demolitionButton.addListener(
                                        (comp) -> {
                                                if (guiState == InterfaceState.REMOVING) {
                                                        guiState = InterfaceState.OFF;
                                                } else {
                                                        guiState = InterfaceState.REMOVING;
                                                        selectedStructure = null;
                                                        structureToPlace = null;
                                                }
                                        });

                                String standby = "Toggle standby";
                                Image standbyButtonImage = new Image(Game.renderer.font.getWidth(standby) + 10, Game.renderer.font.getHeight(standby) + 10);
                                standbyButtonImage.getGraphics().setFont(Game.renderer.font);
                                standbyButtonImage.getGraphics().drawString(standby, 5, 5);
                                standbyButtonImage.getGraphics().flush();
                                Rectangle standbyRect = new Rectangle(0, 0, standbyButtonImage.getWidth(), standbyButtonImage.getHeight());

                                standbyButton = new MyButton(
                                        "Toggle the standby state of the structure",
                                        Game.appgc,
                                        standbyButtonImage,
                                        standbyRect);

                                standbyButton.addListener(
                                        (comp) -> {
                                                if (guiState == InterfaceState.SELECTING && !selectedStructure.isRoad() && selectedStructure.type != StructureType.Cpu_t1) {
                                                        if (selectedStructure.state != StructureState.Standby) {
                                                                selectedStructure.setState(StructureState.Standby);
                                                        } else {
                                                                selectedStructure.setState(StructureState.Active);
                                                        }
                                                }
                                        });

                                String remove = "Remove Structure";
                                Image removeButtonImage = new Image(Game.renderer.font.getWidth(remove) + 10, Game.renderer.font.getHeight(remove) + 10);
                                removeButtonImage.getGraphics().setFont(Game.renderer.font);
                                removeButtonImage.getGraphics().drawString(remove, 5, 5);
                                removeButtonImage.getGraphics().flush();
                                Rectangle removeRect = new Rectangle(0, 0, removeButtonImage.getWidth(), removeButtonImage.getHeight());

                                removeButton = new MyButton(
                                        "Remove selected structure",
                                        Game.appgc,
                                        removeButtonImage,
                                        removeRect);

                                removeButton.addListener(
                                        (comp) -> {
                                                if (guiState == InterfaceState.SELECTING) {
                                                        int particleX = Game.renderer.stagePosition.x + selectedStructure.position.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                        int particleW = selectedStructure.dimensions.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                        int particleY = Game.renderer.stagePosition.y + selectedStructure.position.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                        int particleH = selectedStructure.dimensions.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;

                                                        selectedStructure.remove();
                                                        selectedStructure = null;
                                                        guiState = InterfaceState.OFF;

                                                        Game.renderer.spawnParticlesInArea(
                                                                particleX, particleY,
                                                                particleW, particleH,
                                                                40, 1, 20,
                                                                Color.darkGray, 1f);

                                                        Game.renderer.spawnParticlesInArea(
                                                                particleX, particleY,
                                                                particleW, particleH,
                                                                40, 1, 20,
                                                                Color.lightGray, 1f);
                                                }
                                        });
                        } catch (SlickException e) {
                                e.printStackTrace();
                        }

                        menuPos = new Vector2i(Game.WIN_WIDTH - Renderer.MENU_WIDTH, Renderer.HEADER_HEIGHT);
                        menuDim = new Vector2i(Renderer.MENU_WIDTH, Game.WIN_HEIGHT - (Renderer.HEADER_HEIGHT + Renderer.FOOTER_HEIGHT));
                }

                public void render(Graphics g) {
                        Structure structure = null;

                        int margin = 10;
                        int lineHeight = Game.renderer.font.getLineHeight() + margin;
                        int lineWidth = menuDim.x - 2 * margin;
                        int leftX = menuPos.x + margin;
                        int lineY = menuPos.y;

                        demolitionButton.setX(Game.WIN_WIDTH / 2 - (demolitionButton.getWidth() / 2));
                        demolitionButton.setY((Game.WIN_HEIGHT - Renderer.FOOTER_HEIGHT) + Renderer.FOOTER_HEIGHT / 2 - demolitionButton.getHeight() / 2);
                        demolitionButton.render(Game.appgc, g);

                        switch (guiState) {
                                case OFF:
                                        return;
                                case REMOVING:
                                        g.drawString("Removing structures...", leftX, lineY);

                                        int x = Game.getWorldMouseX();
                                        int y = Game.getWorldMouseY();

                                        if (Game.world.structureGrid[x][y] != null) {
                                                // draw red rectangle
                                                Structure over = Game.world.structureGrid[x][y];

                                                g.setColor(Color.red);
                                                int selectedX = Game.renderer.stagePosition.x + over.position.x * Game.renderer.tileSize;
                                                int selectedY = Game.renderer.stagePosition.y + over.position.y * Game.renderer.tileSize;

                                                g.drawRect(selectedX - 5, selectedY - 5,
                                                        over.dimensions.x * Game.PIXEL_SCALE * Game.PIXELS_PER_TILE + 10,
                                                        over.dimensions.y * Game.PIXEL_SCALE * Game.PIXELS_PER_TILE + 10);
                                        } else {
                                                g.setColor(Color.magenta);
                                                int selectedX = Game.renderer.stagePosition.x + x * Game.renderer.tileSize;
                                                int selectedY = Game.renderer.stagePosition.y + y * Game.renderer.tileSize;

                                                g.drawRect(selectedX - 5, selectedY - 5,
                                                        Game.PIXEL_SCALE * Game.PIXELS_PER_TILE + 10,
                                                        Game.PIXEL_SCALE * Game.PIXELS_PER_TILE + 10);
                                        }
                                        return;
                                case PLACING:
                                        structure = structureToPlace;
                                        break;
                                case SELECTING:
                                        structure = selectedStructure;
                                        break;
                                default:
                                        System.err.println("Unknown menu state in side menu renderer.");
                                        System.exit(-1);
                        }

                        Properties props = StructureLoader.getProperties(structure.type);

                        int thumbSize = 50;
                        Image image = structure.image;
                        if (image != null) {
                                g.setColor(Game.renderer.TERRAIN_DEFAULT_COLOR);
                                g.fillRect(leftX, lineY, thumbSize, thumbSize);
                                g.setColor(Color.white);

                                // Draw the image clipped
                                Rectangle clip = g.getClip();
                                g.setClip(leftX, lineY, thumbSize, thumbSize);
                                if (image.getWidth() < thumbSize && image.getHeight() < thumbSize) {
                                        image.drawCentered(leftX + thumbSize / 2, lineY + thumbSize / 2);
                                } else {
                                        image.draw(leftX + margin, lineY + margin);
                                }
                                g.setClip(clip);

                                g.drawRect(leftX, lineY, thumbSize, thumbSize);
                        }
                        lineY += thumbSize + margin;

                        String name = addLineBreaks(props.getProperty("name"), lineWidth);
                        g.drawString(name, leftX, lineY);
                        lineY += lineHeight;

                        String description = addLineBreaks(props.getProperty("desc"), lineWidth);
                        g.drawString(description, leftX, lineY);
                        lineY += Game.renderer.font.getHeight(description) + margin;

                        g.drawLine(leftX, lineY, leftX + lineWidth, lineY);
                        lineY += margin;

                        if (guiState == InterfaceState.SELECTING) {
                                boolean isOnStandby = structure.state == StructureState.Standby;
                                if (isOnStandby) {
                                        String standby = "On standby!";
                                        g.drawString(standby, leftX, lineY);
                                        lineY += lineHeight;
                                }

                                if (!structure.isRoad() && structure.type != StructureType.Cpu_t1) {
                                        g.drawString("Populationfactor: " + structure.populationFactor, leftX, lineY);
                                        lineY += lineHeight;
                                        g.drawString("Wirefactor: " + structure.roadFactor, leftX, lineY);
                                        lineY += lineHeight;
                                }
                        }

                        if (guiState == InterfaceState.PLACING) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("Build costs:\n");
                                boolean hasCosts = false;
                                for (Resource resource : Resource.values()) {
                                        float value = structure.buildCost.get(resource);
                                        if (value != 0) {
                                                sb.append(resource.name() + ": " + value + "\n");
                                                hasCosts = true;
                                        }
                                }

                                if (hasCosts) {
                                        // Delete last newline char
                                        sb.deleteCharAt(sb.length() - 1);
                                        g.drawString(sb.toString(), leftX, lineY);
                                        lineY += Game.renderer.font.getHeight(sb.toString()) + margin;
                                }
                        }

                        { // producing
                                StringBuilder sb = new StringBuilder();

                                if (guiState == InterfaceState.PLACING && structure.usesTerrain()) {
                                        sb.append("Produces (units per sec\nper resource in range):\n");
                                } else {
                                        sb.append("Produces (units per sec):\n");
                                }

                                for (Resource resource : Resource.values()) {
                                        float value = structure.productionOutPerSec.get(resource);
                                        if (value != 0) {
                                                sb.append(resource.name() + ": " + (value * structure.getProductionFactor()) + "\n");
                                        }
                                }

                                if (structure.isProducer) {
                                        // Delete last newline char
                                        sb.deleteCharAt(sb.length() - 1);
                                        g.drawString(sb.toString(), leftX, lineY);
                                        lineY += Game.renderer.font.getHeight(sb.toString()) + margin;
                                }
                        }

                        { // raising cap
                                StringBuilder sb = new StringBuilder();
                                sb.append("Raises capacity:\n");
                                boolean raises = false;
                                for (Resource resource : Resource.values()) {
                                        float value = structure.capacityIncrease.get(resource);
                                        if (value != 0) {
                                                sb.append(resource.name() + ": " + value + "\n");
                                                raises = true;
                                        }
                                }

                                if (raises) {
                                        // Delete last newline char
                                        sb.deleteCharAt(sb.length() - 1);
                                        g.drawString(sb.toString(), leftX, lineY);
                                        lineY += Game.renderer.font.getHeight(sb.toString()) + margin;
                                }
                        }

                        { // consumes
                                StringBuilder sb = new StringBuilder();
                                sb.append("Consumes (units per sec):\n");
                                for (Resource resource : Resource.values()) {
                                        float value = structure.productionInPerSec.get(resource);
                                        if (value != 0) {
                                                sb.append(resource.name() + ": " + (value * (structure.isProducer ? structure.getProductionFactor() : 1.0f)) + "\n");
                                        }
                                }

                                if (structure.isConsumer) {
                                        // Delete last newline char
                                        sb.deleteCharAt(sb.length() - 1);
                                        g.drawString(sb.toString(), leftX, lineY);
                                        lineY += Game.renderer.font.getHeight(sb.toString()) + margin;
                                }
                        }

                        if (guiState == InterfaceState.SELECTING) {
                                { // refund
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("Refunds when demolished:\n");
                                        boolean refunds = false;
                                        for (Resource resource : Resource.values()) {
                                                float value = structure.refundResources.get(resource);
                                                if (value != 0) {
                                                        sb.append(resource.name() + ": " + value + "\n");
                                                        refunds = true;
                                                }
                                        }

                                        if (refunds) {
                                                // Delete last newline char
                                                sb.deleteCharAt(sb.length() - 1);
                                                g.drawString(sb.toString(), leftX, lineY);
                                                lineY += Game.renderer.font.getHeight(sb.toString()) + margin;
                                        }
                                }

                                g.drawLine(leftX, lineY, leftX + lineWidth, lineY);
                                lineY += margin;

                                // show failure states
                                List<String> failureReasons = new ArrayList<>();
                                switch (structure.state) {
                                        case NoRoadAccess:
                                                failureReasons.add("Pathfinding failure! Not connected to the CPU!");
                                                break;
                                        case NoInputResources:
                                                failureReasons.add("No input signal! Not enough input resources!");
                                                break;
                                        case NoSpareCapacity:
                                                failureReasons.add("Stack overflow error! No spare capacity!");
                                                break;
                                }

                                for (String failure : failureReasons) {
                                        String text = addLineBreaks(failure, lineWidth);
                                        g.drawString(text, leftX, lineY);
                                        lineY += Game.renderer.font.getHeight(text) + margin;
                                }

                                {
                                        if (structure.getRoadAccess() != RoadAccess.NONE && structure.type != StructureType.Cpu_t1) {
                                                String roadAccess = "Quality of wiring to CPU:\n" + structure.getRoadAccess().name();
                                                g.drawString(roadAccess, leftX, lineY);
                                                lineY += Game.renderer.font.getHeight(roadAccess) + margin;
                                        }
                                }

                                lineY = menuPos.y + menuDim.y - (Game.PIXEL_SCALE + removeButton.getHeight());

                                removeButton.setX(leftX);
                                removeButton.setY(lineY);
                                removeButton.render(Game.appgc, g);

                                lineY -= removeButton.getHeight() + margin;

                                if (!structure.isRoad() && structure.type != StructureType.Cpu_t1) {
                                        standbyButton.setX(leftX);
                                        standbyButton.setY(lineY);
                                        standbyButton.render(Game.appgc, g);
                                }

                                { // rect around selected struct
                                        g.setColor(selectionColor);
                                        int selectedX = Game.renderer.stagePosition.x + structure.position.x * Game.renderer.tileSize;
                                        int selectedY = Game.renderer.stagePosition.y + structure.position.y * Game.renderer.tileSize;

                                        g.drawRect(selectedX - 5, selectedY - 5,
                                                structure.dimensions.x * Game.PIXEL_SCALE * Game.PIXELS_PER_TILE + 10,
                                                structure.dimensions.y * Game.PIXEL_SCALE * Game.PIXELS_PER_TILE + 10);
                                }
                        }
                }
        }
}
