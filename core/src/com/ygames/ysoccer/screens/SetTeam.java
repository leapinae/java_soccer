package com.ygames.ysoccer.screens;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.ygames.ysoccer.competitions.Cup;
import com.ygames.ysoccer.competitions.League;
import com.ygames.ysoccer.competitions.tournament.Tournament;
import com.ygames.ysoccer.framework.Assets;
import com.ygames.ysoccer.framework.Font;
import com.ygames.ysoccer.framework.GLGame;
import com.ygames.ysoccer.framework.GLScreen;
import com.ygames.ysoccer.framework.InputDevice;
import com.ygames.ysoccer.framework.RgbPair;
import com.ygames.ysoccer.gui.Button;
import com.ygames.ysoccer.gui.Label;
import com.ygames.ysoccer.gui.Picture;
import com.ygames.ysoccer.gui.TacticsBoard;
import com.ygames.ysoccer.gui.Widget;
import com.ygames.ysoccer.match.Const;
import com.ygames.ysoccer.match.Match;
import com.ygames.ysoccer.match.Player;
import com.ygames.ysoccer.match.Tactics;
import com.ygames.ysoccer.match.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ygames.ysoccer.framework.Assets.font10;
import static com.ygames.ysoccer.framework.Assets.font14;
import static com.ygames.ysoccer.framework.Assets.gettext;
import static com.ygames.ysoccer.framework.Font.Align.CENTER;
import static com.ygames.ysoccer.framework.Font.Align.LEFT;
import static com.ygames.ysoccer.match.Match.AWAY;
import static com.ygames.ysoccer.match.Match.HOME;

class SetTeam extends GLScreen {

    private Team ownTeam;
    private Team opponentTeam;
    private Team shownTeam;
    private int reservedInputDevices;
    private int selectedPos;
    private boolean compareTactics;
    private Font font10yellow;

    private List<Widget> playerButtons = new ArrayList<>();
    private TacticsBoard tacticsBoard;
    private Widget[] tacticsButtons = new Widget[18];
    private Widget teamInputDeviceButton;
    private Widget playMatchButton;

    SetTeam(GLGame game) {
        super(game);
        playMenuMusic = false;

        Match match = navigation.competition.getMatch();

        if (navigation.team.index == HOME) {
            ownTeam = match.team[HOME];
            opponentTeam = match.team[AWAY];
            reservedInputDevices = (ownTeam.controlMode != Team.ControlMode.COMPUTER) && (opponentTeam.controlMode != Team.ControlMode.COMPUTER) ? 1 : 0;
        } else {
            ownTeam = match.team[AWAY];
            opponentTeam = match.team[HOME];
            reservedInputDevices = 0;
        }

        // default input device
        if (ownTeam.inputDevice == null && ownTeam.nonAiInputDevicesCount() == 0) {
            ownTeam.inputDevice = game.inputDevices.assignFirstAvailable();
        }

        // set sanctions
        for (Player player : ownTeam.players) {
            player.getSanctionsFromCompetition(navigation.competition);
        }
        // set sanctions
        for (Player player : opponentTeam.players) {
            player.getSanctionsFromCompetition(navigation.competition);
        }

        shownTeam = ownTeam;
        selectedPos = -1;
        compareTactics = false;

        background = new Texture("images/backgrounds/menu_set_team.jpg");

        font10yellow = new Font(10, 13, 17, 12, 16, new RgbPair(0xFCFCFC, 0xFCFC00));
        font10yellow.load();

        ownTeam.loadImage();
        opponentTeam.loadImage();

        Widget w;

        // players
        for (int pos = 0; pos < Const.FULL_TEAM; pos++) {

            if (!game.competition.viewResult) {
                w = new PlayerInputDeviceButton(pos);
                playerButtons.add(w);
                widgets.add(w);
            }

            w = new PlayerSanctions(pos);
            playerButtons.add(w);
            widgets.add(w);

            w = new PlayerFaceButton(pos);
            playerButtons.add(w);
            widgets.add(w);

            w = new PlayerNumberButton(pos);
            playerButtons.add(w);
            widgets.add(w);

            w = new PlayerNameButton(pos);
            playerButtons.add(w);
            widgets.add(w);

            int x = 550;
            if (shownTeam.type != Team.Type.NATIONAL) {
                if (game.settings.useFlags) {
                    w = new PlayerNationalityFlagButton(pos);
                    playerButtons.add(w);
                    widgets.add(w);
                    x += 26;
                } else {
                    w = new PlayerNationalityCodeButton(pos);
                    playerButtons.add(w);
                    widgets.add(w);
                    x += 58;
                }
            }

            w = new PlayerRoleButton(x, pos);
            playerButtons.add(w);
            widgets.add(w);
            x += 34;

            for (int skillIndex = 0; skillIndex < 3; skillIndex++) {
                w = new PlayerSkillButton(pos, skillIndex, x);
                playerButtons.add(w);
                widgets.add(w);
                x += 13;
            }
            x += 4;

            w = new PlayerStarsButton(pos, x);
            playerButtons.add(w);
            widgets.add(w);
        }

        tacticsBoard = new TacticsBoard(ownTeam, opponentTeam);
        tacticsBoard.setPosition(game.gui.WIDTH / 2 + 140, 120);
        widgets.add(tacticsBoard);

        for (int t = 0; t < 18; t++) {
            w = new TacticsButton(t);
            tacticsButtons[t] = w;
            widgets.add(w);
        }

        w = new TacticsComparisonButton();
        widgets.add(w);

        w = new OpponentTeamButton();
        widgets.add(w);

        if (game.competition.viewResult) {
            w = new CoachLabel();
            widgets.add(w);
        } else {
            w = new TeamInputDeviceButton();
            teamInputDeviceButton = w;
            widgets.add(w);

            w = new ControlModeButton();
            widgets.add(w);
        }

        w = new EditTacticsButton();
        widgets.add(w);

        w = new CoachNameLabel();
        widgets.add(w);

        w = new TeamPicture();
        widgets.add(w);

        w = new TeamNameButton();
        widgets.add(w);

        w = new PlayMatchButton();
        playMatchButton = w;
        widgets.add(w);

        setSelectedWidget(w);

        w = new ExitButton();
        widgets.add(w);

        if (ownTeam.hasUnavailablePlayers()) {
            setSelectedWidget(w);
        }
    }

    private class PlayerInputDeviceButton extends Button {
        int pos;

        PlayerInputDeviceButton(int pos) {
            this.pos = pos;
            setGeometry(60, 120 + 22 * pos, 42, 20);
            setText("", CENTER, font10);
            setImagePosition(17, -1);
            setAddShadow(true);
            textOffsetX = -11;
        }

        @Override
        public void refresh() {
            setText("");
            textureRegion = null;
            setVisible(false);

            if (pos < Math.min(Const.TEAM_SIZE + navigation.competition.benchSize, shownTeam.players.size())) {
                Player player = shownTeam.playerAtPosition(pos);
                if (player.suspensions == 0) {
                    if (player.inputDevice.type == InputDevice.Type.COMPUTER) {
                        setText("");
                    } else {
                        setText(player.inputDevice.port + 1);
                    }
                    textureRegion = gui.controls[1][player.inputDevice.type.ordinal()];
                    setVisible(true);
                }
            }
            setActive((shownTeam == ownTeam) && (ownTeam.controlMode == Team.ControlMode.PLAYER));
        }

        @Override
        public void onFire1Down() {
            Player player = shownTeam.playerAtPosition(pos);
            switch (player.inputDevice.type) {
                case COMPUTER:
                    // move from team to player
                    if (ownTeam.inputDevice != null) {
                        player.setInputDevice(ownTeam.inputDevice);
                        ownTeam.setInputDevice(null);
                    }
                    // get first available
                    else if (game.inputDevices.getAvailabilityCount() > reservedInputDevices) {
                        player.setInputDevice(game.inputDevices.assignFirstAvailable());
                    }
                    break;

                default:
                    InputDevice d = game.inputDevices.assignNextAvailable(player.inputDevice);
                    if (d != null) {
                        player.setInputDevice(d);
                    } else {
                        // back to ai
                        player.inputDevice.setAvailable(true);
                        player.setInputDevice(player.ai);
                        if (ownTeam.nonAiInputDevicesCount() == 0) {
                            ownTeam.setInputDevice(game.inputDevices.assignFirstAvailable());
                        }
                    }
                    break;
            }
            updatePlayerButtons();
            teamInputDeviceButton.setDirty(true);
        }
    }

    private class PlayerSanctions extends Button {
        int pos;

        PlayerSanctions(int pos) {
            this.pos = pos;
            setGeometry(102, 120 + 22 * pos, 20, 20);
            setAddShadow(true);
            setActive(false);
        }

        @Override
        public void refresh() {
            Player player = shownTeam.playerAtPosition(pos);
            textureRegion = null;
            if (player != null) {
                if (player.suspensions == 2) {
                    textureRegion = gui.penaltyCards[2];
                    setImagePosition(0, 1);
                } else if (player.suspensions == 1) {
                    textureRegion = gui.penaltyCards[1];
                    setImagePosition(2, 1);
                } else if (player.isCautioned) {
                    textureRegion = gui.penaltyCards[0];
                    setImagePosition(2, 1);
                }
            }
        }
    }

    private class PlayerFaceButton extends Button {

        int pos;

        PlayerFaceButton(int pos) {
            this.pos = pos;
            setGeometry(122, 120 + 22 * pos, 24, 20);
            setImagePosition(2, -2);
            setActive(false);
            setAddShadow(true);
        }

        @Override
        public void refresh() {
            setPlayerWidgetColor(this, pos);
            Player player = shownTeam.playerAtPosition(pos);
            if (player == null) {
                textureRegion = null;
            } else {
                textureRegion = player.createFace();
            }
        }
    }

    private class PlayerNumberButton extends Button {

        int pos;

        PlayerNumberButton(int pos) {
            this.pos = pos;
            setGeometry(148, 120 + 22 * pos, 34, 20);
            setText("", CENTER, font10);
            setActive(false);
        }

        @Override
        public void refresh() {
            Player player = shownTeam.playerAtPosition(pos);
            if (player == null) {
                setText("");
            } else {
                setText(player.number);
            }
        }
    }

    private class PlayerNameButton extends Button {

        int pos;

        PlayerNameButton(int pos) {
            this.pos = pos;
            setGeometry(184, 120 + 22 * pos, 364, 20);
            setText("", LEFT, font10);
        }

        @Override
        public void refresh() {
            setPlayerWidgetColor(this, pos);
            Player player = shownTeam.playerAtPosition(pos);
            if (player == null) {
                setText("");
                setActive(false);
            } else {
                setText(player.name);
                setActive(shownTeam == ownTeam);
            }
        }

        @Override
        public void onFire1Down() {
            // select
            if (selectedPos == -1) {
                selectedPos = pos;
            }
            // deselect
            else if (selectedPos == pos) {
                selectedPos = -1;
            }
            // swap
            else {
                int ply1 = ownTeam.playerIndexAtPosition(selectedPos);
                int ply2 = ownTeam.playerIndexAtPosition(pos);

                Collections.swap(ownTeam.players, ply1, ply2);
                playMatchButton.setDirty(true);

                selectedPos = -1;
            }
            updatePlayerButtons();
        }
    }

    private class PlayerNationalityFlagButton extends Button {

        int pos;

        PlayerNationalityFlagButton(int pos) {
            this.pos = pos;
            setGeometry(550, 120 + 22 * pos, 26, 20);
            setImagePosition(1, 3);
            setActive(false);
            setAddShadow(true);
        }

        @Override
        public void refresh() {
            Player player = shownTeam.playerAtPosition(pos);
            if (player == null) {
                textureRegion = null;
            } else {
                textureRegion = Assets.getNationalityFlag(player.nationality);
            }
        }
    }

    private class PlayerNationalityCodeButton extends Button {

        int pos;

        PlayerNationalityCodeButton(int pos) {
            this.pos = pos;
            setGeometry(550, 120 + 22 * pos, 58, 20);
            setText("", CENTER, font10);
            setActive(false);
        }

        @Override
        public void refresh() {
            Player player = shownTeam.playerAtPosition(pos);
            if (player == null) {
                setText("");
            } else {
                setText("(" + player.nationality + ")");
            }
        }
    }

    private class PlayerRoleButton extends Button {

        int pos;

        PlayerRoleButton(int x, int pos) {
            this.pos = pos;
            setGeometry(x, 120 + 22 * pos, 34, 20);
            setText("", CENTER, font10);
            setActive(false);
        }

        @Override
        public void refresh() {
            Player player = shownTeam.playerAtPosition(pos);
            if (player == null) {
                setText("");
            } else {
                setText(gettext(player.getRoleLabel()));
            }
        }
    }

    private class PlayerSkillButton extends Button {

        int pos;
        int skillIndex;

        PlayerSkillButton(int pos, int skillIndex, int x) {
            this.pos = pos;
            this.skillIndex = skillIndex;
            setGeometry(x, 120 + 22 * pos, 13, 20);
            setText("", CENTER, font10yellow);
            setActive(false);
        }

        @Override
        public void refresh() {
            Player player = shownTeam.playerAtPosition(pos);
            if (player == null) {
                setText("");
            } else {
                Player.Skill[] skills = player.getOrderedSkills();
                if (skills == null) {
                    setText("");
                } else {
                    setText(gettext(Player.getSkillLabel(skills[skillIndex])));
                }
            }
        }
    }

    private class PlayerStarsButton extends Button {

        int pos;

        PlayerStarsButton(int pos, int x) {
            this.pos = pos;
            setGeometry(x, 120 + 22 * pos, 64, 20);
            setActive(false);
        }

        @Override
        public void refresh() {
            Player player = shownTeam.playerAtPosition(pos);
            if (player == null) {
                textureRegion = null;
            } else {
                textureRegion = Assets.stars[player.getStars()];
            }
        }
    }

    private class TacticsButton extends Button {

        int t;

        TacticsButton(int t) {
            this.t = t;
            setGeometry(game.gui.WIDTH - 90 - 110, 120 + 23 * t, 110, 21);
            setText(Tactics.codes[t], CENTER, font10);
        }

        @Override
        public void refresh() {
            if (shownTeam.tactics == t) {
                setColors(0x9D7B03, 0xE2B004, 0x675103);
            } else {
                setColors(0xE2B004, 0xFCCE30, 0x9D7B03);
            }
            setActive(shownTeam == ownTeam);
        }

        @Override
        public void onFire1Down() {
            if (shownTeam.tactics != t) {
                shownTeam.tactics = t;
                updateTacticsButtons();
                updatePlayerButtons();
            }
        }
    }

    private class TacticsComparisonButton extends Button {

        TacticsComparisonButton() {
            setGeometry(game.gui.WIDTH / 2 + 140, 439, 264, 36);
            setColors(0x824200, 0xB46A00, 0x4C2600);
            setText("", CENTER, font10);
        }

        @Override
        public void refresh() {
            setVisible(shownTeam == ownTeam);
            if (compareTactics) {
                setText(gettext("TEAM TACTICS"));
            } else {
                setText(gettext("TACTICS COMPARISON"));
            }
        }

        @Override
        public void onFire1Down() {
            compareTactics = !compareTactics;
            tacticsBoard.setCompareTactics(compareTactics);
            setDirty(true);
        }
    }

    private class OpponentTeamButton extends Button {

        OpponentTeamButton() {
            setGeometry(game.gui.WIDTH / 2 + 140, 483, 264, 36);
            setColors(0x8B2323, 0xBF4531, 0x571717);
            setText("", CENTER, font10);
        }

        @Override
        public void refresh() {
            setText(gettext(shownTeam == ownTeam ? "VIEW OPPONENT" : "VIEW TEAM"));
        }

        @Override
        public void onFire1Down() {
            if (shownTeam == ownTeam) {
                shownTeam = opponentTeam;
                tacticsBoard.setViewOpponent(true);
            } else {
                shownTeam = ownTeam;
                tacticsBoard.setViewOpponent(false);
            }
            refreshAllWidgets();
        }
    }

    private class TeamInputDeviceButton extends Button {

        TeamInputDeviceButton() {
            setGeometry(game.gui.WIDTH / 2 + 140, 536, 202, 42);
            setText("", LEFT, font10);
            textOffsetX = 50;
            setImagePosition(8, 1);
            setAddShadow(true);
        }

        @Override
        public void refresh() {
            if (shownTeam.inputDevice != null) {
                setVisible(shownTeam == ownTeam);
                switch (shownTeam.inputDevice.type) {
                    case COMPUTER:
                        setText("");
                        break;

                    case KEYBOARD:
                        setText(gettext("KEYBOARD") + " " + (shownTeam.inputDevice.port + 1));
                        break;

                    case JOYSTICK:
                        setText(gettext("JOYSTICK") + " " + (shownTeam.inputDevice.port + 1));
                        break;
                }
                textureRegion = gui.controls[0][shownTeam.inputDevice.type.ordinal()];
            } else {
                setVisible(false);
            }
        }

        @Override
        public void onFire1Down() {
            updateTeamInputDevice(1);
        }

        @Override
        public void onFire2Down() {
            updateTeamInputDevice(-1);
        }

        private void updateTeamInputDevice(int n) {
            shownTeam.inputDevice = game.inputDevices.rotateAvailable(shownTeam.inputDevice, n);
            setDirty(true);
        }
    }

    private class EditTacticsButton extends Button {

        EditTacticsButton() {
            setGeometry(game.gui.WIDTH - 90 - 110, 540, 110, 34);
            setColors(0xBA9206, 0xE9B607, 0x6A5304);
            setText(gettext("TACTICS.EDIT"), CENTER, font10);
        }

        @Override
        public void refresh() {
            setVisible(shownTeam == ownTeam);
        }

        @Override
        public void onFire1Down() {
            game.tacticsTeam = ownTeam;
            game.setScreen(new SelectTactics(game));
        }
    }

    private class ControlModeButton extends Button {

        ControlModeButton() {
            setGeometry(game.gui.WIDTH / 2 + 140, 586, 155, 40);
            setText("", CENTER, font10);
        }

        @Override
        public void refresh() {
            switch (shownTeam.controlMode) {
                case COMPUTER:
                    setText(gettext("CONTROL MODE.COMPUTER") + ":");
                    setColors(0x981E1E, 0xC72929, 0x640000);
                    break;

                case PLAYER:
                    setText(gettext("CONTROL MODE.PLAYER") + ":");
                    setColors(0x0000C8, 0x1919FF, 0x000078);
                    break;

                case COACH:
                    setText(gettext("CONTROL MODE.COACH") + ":");
                    setColors(0x009BDC, 0x19BBFF, 0x0071A0);
                    break;
            }
            setActive(shownTeam == ownTeam);
        }

        @Override
        public void onFire1Down() {
            switch (ownTeam.controlMode) {
                case PLAYER:
                    ownTeam.controlMode = Team.ControlMode.COACH;
                    if (ownTeam.inputDevice == null) {
                        ownTeam.releaseNonAiInputDevices();
                        ownTeam.inputDevice = game.inputDevices.assignFirstAvailable();
                    }
                    break;

                case COACH:
                    ownTeam.controlMode = Team.ControlMode.PLAYER;
                    break;
            }
            setDirty(true);
            updatePlayerButtons();
            teamInputDeviceButton.setDirty(true);
        }
    }

    private class CoachLabel extends Label {
        CoachLabel() {
            setGeometry(game.gui.WIDTH / 2 + 140, 586, 155, 40);
            setText(gettext("CONTROL MODE.COACH") + ":", CENTER, font10);
        }
    }

    private class CoachNameLabel extends Label {

        CoachNameLabel() {
            setPosition(game.gui.WIDTH / 2 + 140 + 155 + 10, 586 + 20);
            setText("", LEFT, font10);
        }

        @Override
        public void refresh() {
            setText(shownTeam.coach.name);
        }
    }

    private class TeamNameButton extends Button {

        TeamNameButton() {
            setGeometry((game.gui.WIDTH - 780) / 2, 40, 780, 41);
            setText("", CENTER, font14);
            setActive(false);
        }

        @Override
        public void refresh() {
            setText(shownTeam.name);
            if (shownTeam == ownTeam) {
                setColor(0x005DDE);
            } else {
                setColor(0xAC1A1A);
            }
        }
    }

    private class TeamPicture extends Picture {

        TeamPicture() {
            setPosition(135, 60);
            setAddShadow(true);
        }

        @Override
        public void refresh() {
            setTextureRegion(shownTeam.image);
            limitToSize(100, 70);
        }
    }

    private class PlayMatchButton extends Button {

        PlayMatchButton() {
            setGeometry(game.gui.WIDTH / 2 + 140, game.gui.HEIGHT - 30 - 42, 240, 42);
            setText(gettext(game.competition.viewResult ? "VIEW RESULT" : "PLAY MATCH"), CENTER, font14);
        }

        @Override
        public void refresh() {
            if (shownTeam == ownTeam) {
                setVisible(true);
                if (ownTeam.hasUnavailablePlayers()) {
                    setColor(0x666666);
                    setActive(false);
                } else {
                    setColor(0xDC0000);
                    setActive(true);
                }
            } else {
                setVisible(false);
            }
        }

        @Override
        public void onFire1Down() {
            if (navigation.team.index == HOME && opponentTeam.controlMode != Team.ControlMode.COMPUTER) {
                navigation.team = navigation.competition.getMatch().team[AWAY];
                game.setScreen(new SetTeam(game));
            } else {
                if (game.competition.viewResult) {
                    switch (navigation.competition.type) {
                        case LEAGUE:
                            ((League) game.competition).generateResult();
                            game.setScreen(new PlayLeague(game));
                            break;

                        case CUP:
                            ((Cup) game.competition).generateResult();
                            game.setScreen(new PlayCup(game));
                            break;

                        case TOURNAMENT:
                            ((Tournament) game.competition).generateResult();
                            game.setScreen(new PlayTournament(game));
                            break;
                    }
                } else {
                    game.setScreen(new MatchSetup(game));
                }
            }
        }
    }

    private class ExitButton extends Button {

        ExitButton() {
            setGeometry(game.gui.WIDTH - 150 - 90, game.gui.HEIGHT - 30 - 38 - 2, 150, 38);
            setColor(0xC84200);
            setText(gettext("EXIT"), CENTER, font14);
        }

        @Override
        public void refresh() {
            setVisible(shownTeam == ownTeam);
        }

        @Override
        public void onFire1Down() {
            switch (navigation.competition.type) {
                case FRIENDLY:
                    if (navigation.folder.equals(Assets.favouritesFile)) {
                        game.setScreen(new SelectFavourites(game));
                    } else {
                        FileHandle[] teamFileHandles = navigation.folder.list(Assets.teamFilenameFilter);
                        if (teamFileHandles.length > 0) {
                            game.setScreen(new SelectTeams(game));
                        } else {
                            game.setScreen(new SelectFolder(game));
                        }
                    }
                    break;

                case LEAGUE:
                    game.setScreen(new PlayLeague(game));
                    break;

                case CUP:
                    game.setScreen(new PlayCup(game));
                    break;

                case TOURNAMENT:
                    game.setScreen(new PlayTournament(game));
                    break;
            }
        }
    }

    private void updatePlayerButtons() {
        for (Widget w : playerButtons) {
            w.setDirty(true);
        }
    }

    private void updateTacticsButtons() {
        for (Widget w : tacticsButtons) {
            w.setDirty(true);
        }
    }

    private void setPlayerWidgetColor(Widget w, int pos) {
        if (shownTeam == ownTeam) {
            // goalkeeper
            if (pos == 0) {
                w.setColor(0x0094DE);
            }
            // other player
            else if (pos < Const.TEAM_SIZE) {
                w.setColor(0x005DDE);
            }
            // bench / out
            else if (pos < shownTeam.players.size()) {
                // bench
                if (pos < Const.TEAM_SIZE + navigation.competition.benchSize) {
                    w.setColor(0x0046A6);
                }
                // out
                else {
                    w.setColor(0x303030);
                }
            }
            // void
            else {
                w.setColor(0x101010);
            }

            // selected
            if (selectedPos == pos) {
                w.setColor(0x993333);
            }
        }
        // opponent
        else {
            // goalkeeper
            if (pos == 0) {
                w.setColor(0xEB4141);
            }
            // other player
            else if (pos < Const.TEAM_SIZE) {
                w.setColor(0xAC1A1A);
            }
            // bench / out
            else if (pos < shownTeam.players.size()) {
                // bench
                if (pos < Const.TEAM_SIZE + navigation.competition.benchSize) {
                    w.setColor(0x851F1F);
                }
                // out
                else {
                    w.setColor(0x303030);
                }
            }
            // void
            else {
                w.setColor(0x101010);
            }
        }
    }
}
