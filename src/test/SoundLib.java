package test;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

public class SoundLib {
        public Sound standbyOn;
        public Sound standbyOff;
        public Sound select;
        public Sound place;
        public Sound over;
        public Sound deselect;
        public Sound demolish;
        public Sound cpu;
        public Sound click;
        public Sound cantPlace;

        public Sound music;

        public SoundLib() {
                try {
                        standbyOn = new Sound("resources/sounds/standby_on.ogg");
                        standbyOff = new Sound("resources/sounds/standby_off.ogg");
                        select = new Sound("resources/sounds/select.ogg");
                        place = new Sound("resources/sounds/place.ogg");
                        over = new Sound("resources/sounds/over.ogg");
                        deselect = new Sound("resources/sounds/deselect.ogg");
                        demolish = new Sound("resources/sounds/demolish.ogg");
                        cpu = new Sound("resources/sounds/cpu.ogg");
                        click = new Sound("resources/sounds/click.ogg");
                        cantPlace = new Sound("resources/sounds/cant_place.ogg");

                        music = new Sound("resources/sounds/music.ogg");
                        music.loop();
                } catch (SlickException e) {
                        e.printStackTrace();
                }
        }

        public void play(Sound sound) {
                sound.play(1f, 0.05f);
        }

        public void playSingle(Sound sound) {
                if (!sound.playing()) {
                        sound.play(1f, 0.05f);
                }
        }
}
