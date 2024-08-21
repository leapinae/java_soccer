package com.ygames.ysoccer.match;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.ygames.ysoccer.framework.Assets;
import com.ygames.ysoccer.framework.EMath;
import com.ygames.ysoccer.framework.Font;
import com.ygames.ysoccer.framework.GLGame;
import com.ygames.ysoccer.framework.GLShapeRenderer;
import com.ygames.ysoccer.framework.InputDevice;
import com.ygames.ysoccer.framework.Settings;

import static com.ygames.ysoccer.framework.Assets.gettext;
import static com.ygames.ysoccer.match.MatchFsm.STATE_END;
import static com.ygames.ysoccer.match.MatchFsm.STATE_HIGHLIGHTS;
import static com.ygames.ysoccer.match.SceneFsm.ActionType.NEW_FOREGROUND;
import static com.ygames.ysoccer.match.SceneRenderer.guiAlpha;

class MatchStateHighlights extends MatchState {

    private int subframe0;
    private boolean paused;
    private boolean showCurrentRecord;
    private boolean slowMotion;
    private boolean keySlow;
    private boolean keyPause;
    private int position;
    private InputDevice inputDevice;

    MatchStateHighlights(MatchFsm fsm) {
        super(fsm);

        displayWindVane = true;

        checkReplayKey = false;
        checkPauseKey = false;
        checkHelpKey = false;
        checkBenchCall = false;
    }

    @Override
    void entryActions() {
        super.entryActions();

        // store initial frame
        subframe0 = match.subframe;

        paused = false;
        showCurrentRecord = true;
        slowMotion = false;

        // control keys
        keySlow = Gdx.input.isKeyPressed(Input.Keys.R);
        keyPause = Gdx.input.isKeyPressed(Input.Keys.P);

        // position of current frame in the highlights vector
        position = 0;

        inputDevice = null;

        match.recorder.loadHighlight(sceneRenderer);
    }

    @Override
    void doActions(float deltaTime) {
        super.doActions(deltaTime);

        // toggle pause
        if (Gdx.input.isKeyPressed(Input.Keys.P) && !keyPause) {
            paused = !paused;
        }
        keyPause = Gdx.input.isKeyPressed(Input.Keys.P);

        // toggle slow-motion
        if (Gdx.input.isKeyPressed(Input.Keys.R) && !keySlow) {
            slowMotion = !slowMotion;
        }
        keySlow = Gdx.input.isKeyPressed(Input.Keys.R);

        // set/unset controlling device
        if (inputDevice == null) {
            for (InputDevice d : match.game.inputDevices) {
                if (d.fire2Down()) {
                    inputDevice = d;
                }
            }
        } else {
            if (inputDevice.fire2Down()) {
                inputDevice = null;
            }
        }

        // set speed
        int speed;
        if (inputDevice != null) {
            speed = 12 * inputDevice.x1 - 4 * inputDevice.y1 + 8 * Math.abs(inputDevice.x1) * inputDevice.y1;
        } else if (slowMotion) {
            speed = GLGame.SUBFRAMES / 2;
        } else {
            speed = GLGame.SUBFRAMES;
        }

        // set position
        if (!paused) {
            position = EMath.slide(position, GLGame.SUBFRAMES / 2, Const.REPLAY_SUBFRAMES, speed);

            match.subframe = (subframe0 + position) % Const.REPLAY_SUBFRAMES;
        }
    }

    @Override
    SceneFsm.Action[] checkConditions() {

        // quit on fire button
        for (InputDevice d : match.game.inputDevices) {
            if (d.fire1Down()) {
                showCurrentRecord = false;
                return newFadedAction(NEW_FOREGROUND, STATE_END);
            }
        }

        // quit on finish
        if (position == Const.REPLAY_SUBFRAMES) {
            match.recorder.nextHighlight();
            showCurrentRecord = false;
            if (match.recorder.hasEnded()) {
                return newFadedAction(NEW_FOREGROUND, STATE_END);
            } else {
                return newFadedAction(NEW_FOREGROUND, STATE_HIGHLIGHTS);
            }
        }

        return checkCommonConditions();
    }

    @Override
    void render() {
        super.render();

        int f = Math.round(1f * match.subframe / GLGame.SUBFRAMES) % 32;
        if (showCurrentRecord && f < 16) {
            Assets.font10.draw(sceneRenderer.batch, (match.recorder.getCurrent() + 1) + "/" + match.recorder.getRecorded(), 30, 22, Font.Align.LEFT);
        }
        if (Settings.showDevelopmentInfo) {
            Assets.font10.draw(sceneRenderer.batch, "FRAME: " + (match.subframe / 8) + " / " + Const.REPLAY_FRAMES, 30, 42, Font.Align.LEFT);
            Assets.font10.draw(sceneRenderer.batch, "SUBFRAME: " + match.subframe + " / " + Const.REPLAY_SUBFRAMES, 30, 62, Font.Align.LEFT);
        }

        sceneRenderer.batch.end();
        float a = position * 360f / Const.REPLAY_SUBFRAMES;
        GLShapeRenderer shapeRenderer = sceneRenderer.shapeRenderer;
        shapeRenderer.setProjectionMatrix(sceneRenderer.camera.combined);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0x242424, guiAlpha);
        shapeRenderer.arc(20, 32, 6, 270 + a, 360 - a);
        shapeRenderer.setColor(0xFF0000, guiAlpha);
        shapeRenderer.arc(18, 30, 6, 270 + a, 360 - a);
        shapeRenderer.end();
        sceneRenderer.batch.begin();

        if (inputDevice != null) {
            int frameX = 1 + inputDevice.x1;
            int frameY = 1 + inputDevice.y1;
            sceneRenderer.batch.draw(Assets.replaySpeed[frameX][frameY], sceneRenderer.guiWidth - 50, sceneRenderer.guiHeight - 50);
        }

        if (paused) {
            Assets.font10.draw(sceneRenderer.batch, gettext("PAUSE"), sceneRenderer.guiWidth / 2, 22, Font.Align.CENTER);
        }
    }
}