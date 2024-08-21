package com.ygames.ysoccer.gui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Gui {

    public final int WIDTH = 1280;
    public final int HEIGHT = 720;
    public int screenWidth;
    public int screenHeight;
    public int originX;
    public int originY;

    public TextureRegion logo;
    public final TextureRegion[] lightIcons = new TextureRegion[2];
    public final TextureRegion[] pitchIcons = new TextureRegion[9];
    public final TextureRegion[] weatherIcons = new TextureRegion[10];
    public final TextureRegion[][] controls = new TextureRegion[2][3];
    public final TextureRegion[] penaltyCards = new TextureRegion[3];

    public void resize(int width, int height) {
        float wZoom = (float) width / WIDTH;
        float hZoom = (float) height / HEIGHT;
        float zoom = Math.min(wZoom, hZoom);
        screenWidth = (int) (width / zoom);
        screenHeight = (int) (height / zoom);
        originX = (screenWidth - WIDTH) / 2;
        originY = (screenHeight - HEIGHT) / 2;
    }

    public void setTextures(TextureAtlas guiAtlas) {
        logo = guiAtlas.findRegion("logo");
        logo.flip(false, true);

        TextureAtlas.AtlasRegion region;
        region = guiAtlas.findRegion("light");
        for (int i = 0; i < 2; i++) {
            lightIcons[i] = new TextureRegion(region, 47 * i, 0, 46, 46);
            lightIcons[i].flip(false, true);
        }

        region = guiAtlas.findRegion("pitches");
        for (int i = 0; i < 9; i++) {
            pitchIcons[i] = new TextureRegion(region, 47 * i, 0, 46, 46);
            pitchIcons[i].flip(false, true);
        }

        region = guiAtlas.findRegion("weather");
        for (int i = 0; i < 10; i++) {
            weatherIcons[i] = new TextureRegion(region, 47 * i, 0, 46, 46);
            weatherIcons[i].flip(false, true);
        }

        region = guiAtlas.findRegion("controls");
        for (int i = 0; i < 3; i++) {
            controls[0][i] = new TextureRegion(region, 36 * i, 0, 36, 36);
            controls[0][i].flip(false, true);
            controls[1][i] = new TextureRegion(region, 18 * i, 36, 18, 18);
            controls[1][i].flip(false, true);
        }

        region = guiAtlas.findRegion("penalty_cards");
        penaltyCards[0] = new TextureRegion(region, 0, 0, 9, 14);
        penaltyCards[0].flip(false, true);
        penaltyCards[1] = new TextureRegion(region, 10, 0, 9, 14);
        penaltyCards[1].flip(false, true);
        penaltyCards[2] = new TextureRegion(region, 20, 0, 14, 14);
        penaltyCards[2].flip(false, true);
    }
}