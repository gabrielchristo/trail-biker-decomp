package a.a.a.c;

import com.nokia.mid.ui.DeviceControl;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import a.a.a.b.SpriteSheet;
import javax.microedition.lcdui.Image;
import a.a.a.b.ParallaxBackground;
import a.a.a.b.TerrainLevel;
import javax.microedition.lcdui.Command;
import com.nokia.mid.ui.FullCanvas;
import a.a.a.a.GameClock;
import a.a.a.d.SoundManager;
import a.a.a.e.BinaryAsset;

public class GameCanvas extends FullCanvas implements Runnable
{
    public int gameStateFlag;
    protected int screenWidth;
    protected int screenHeight;
    private int bannerTickCount;
    protected boolean bannerDismissed;
    protected Command dismissCommand;
    private CanvasGameLoopTask gameLoopTask;
    private int unusedO;
    private int endGameFlags;
    private int endGameAnimTimer;
    private int repaintFlags;
    private int splashFrameCounter;
    private int scrollCounterK;
    private int scrollCounterS;
    private int scrollCounterI;
    private int scrollCounterG;
    private TerrainLevel terrain;
    private Motorbike motorbike;
    private ParallaxBackground parallax;
    private Image livesImage;
    private boolean needsBackgroundClear;
    private boolean assetsLoaded;
    private boolean playfieldReady;
    private boolean suppressPauseOnce;
    private int airTrickTimer;
    private int stuntAirDelayThreshold;
    private int trickHoldTicks;
    private int trickCooldownMax;
    private int hopImpulse;
    private int trickDriftPerTick;
    private int stuntPhaseTicks;
    private boolean rampLandingActive;
    private int rampStopWorldX;
    private int crashAnimTicks;
    private int terrainScrollThreshold;
    private int terrainScrollPeriod;
    private int terrainScrollAccum;
    private int parallaxScrollThreshold;
    private int parallaxScrollPeriod;
    private int parallaxScrollAccum;
    private GameClock gameClock;
    protected boolean raceEndHandled;
    private SpriteSheet tileSprites;
    private int hudClockX;
    private int hudClockY;
    private int unusedZ;
    private BinaryAsset gameMetadata;
    private TrailBikerMidlet midlet;
    private LevelManager levelManager;
    private boolean stuntActive;
    private boolean wheelieOnLog;
    private byte frontTileSample;
    protected int scrollTickCounter;
    protected boolean crashAnimActive;
    protected int stuntLivesLost;
    protected int stuntLivesTotal;
    private SpriteSheet splashSprites;
    private int splashPhase;
    private int emptyTileColorArgb;
    private SoundManager soundManager;
    private Image unusedBannerImage;
    private static final int u = 6;
    private String[] messageBuffer;
    private int messageIndex;
    
    public GameCanvas(final TrailBikerMidlet b, final LevelManager o) {
        super();
        this.gameStateFlag = 0;
        this.screenWidth = 0;
        this.screenHeight = 0;
        this.bannerTickCount = 0;
        this.bannerDismissed = false;
        this.dismissCommand = new Command("Dismiss", 1, 1);
        this.gameLoopTask = null;
        this.needsBackgroundClear = true;
        this.messageBuffer = new String[6];
        this.midlet = b;
        this.levelManager = o;
        this.gameMetadata = b.gameMetadata;
        this.screenWidth = super.getWidth();
        this.screenHeight = super.getHeight();
        if (TrailBikerMidlet.deviceProfileId != 0 && TrailBikerMidlet.deviceProfileId != 4) {
            ++this.screenWidth;
            ++this.screenHeight;
        }
        this.terrain = null;
        this.gameClock = new GameClock(false, 0L, true);
        try {
            this.splashSprites = new SpriteSheet(Image.createImage("/res/splash.png"), 1, 2, false);
        }
        catch (Exception ex) {
            System.out.println("Splash image load failed " + ex.toString());
        }
        this.splashPhase = 1;
    }
    
    protected void fillBackgroundWhite(final Graphics graphics) {
        final int color = graphics.getColor();
        graphics.setColor(16777215);
        graphics.fillRect(0, 0, this.screenWidth, this.screenHeight);
        graphics.setColor(color);
    }
    
    protected void onTimerTick() {
        if (this.bannerTickCount > 0) {
            --this.bannerTickCount;
            if (this.bannerTickCount == 0) {
                this.removeCommand(this.dismissCommand);
                this.bannerDismissed = true;
                this.repaint();
            }
        }
        this.run();
    }
    
    protected void drawCenteredBanner(final Graphics graphics, int n, int n2, final int n3, final int n4, final String s, final int try1, final boolean b) {
        final Font font = Font.getFont(0, 0, 8);
        if (n == 0) {
            n = font.stringWidth(s) + 8;
        }
        if (n2 == 0) {
            n2 = font.getHeight() + 8;
        }
        final Image image = Image.createImage(n, n2);
        final Graphics graphics2 = image.getGraphics();
        graphics2.setFont(font);
        graphics2.setColor(0);
        final int n5 = n / 2;
        final int n6 = n2 / 2 - font.getHeight() / 2;
        graphics2.drawRoundRect(0, 0, n - 1, n2 - 1, 4, 4);
        graphics2.drawString(s, n5, n6, 17);
        if (b) {
            this.addCommand(this.dismissCommand);
        }
        int n7;
        if (n3 == 0) {
            n7 = this.screenWidth / 2 - n / 2;
        }
        else {
            n7 = n3 - n / 2;
        }
        int n8;
        if (n4 == 0) {
            n8 = this.screenHeight / 2 - n2 / 2;
        }
        else {
            n8 = n4 - n2 / 2;
        }
        graphics.drawImage(image, n7, n8, 20);
        this.bannerTickCount = try1;
    }
    
    private void loadGameResources() {
        this.motorbike = new Motorbike((int)this.gameMetadata.data[9], 0, this.gameMetadata);
        try {
            if (TrailBikerMidlet.deviceProfileId == 4) {
                this.emptyTileColorArgb = 65280;
            }
            else {
                this.emptyTileColorArgb = 16777215;
            }
            this.tileSprites = new SpriteSheet(Image.createImage("/res/tiles.png"), (int)this.gameMetadata.data[6], (int)this.gameMetadata.data[7], true);
            this.parallax = new ParallaxBackground("/res/bg.png", (int)this.gameMetadata.data[1], false);
            this.livesImage = Image.createImage("/res/lives.png");
        }
        catch (Exception ex) {
            System.out.println("Image load failed " + ex.toString());
        }
        final int n = this.gameMetadata.data[72] * this.gameMetadata.data[73] + 2;
        int n2 = 72;
        final byte[] array = new byte[n];
        for (int i = 0; i < n; ++i) {
            array[i] = this.gameMetadata.data[n2++];
        }
        final int n3 = this.gameMetadata.data[n2 + 1] + 2 * this.gameMetadata.data[n2] + 2;
        final int n4 = n3 + (this.gameMetadata.data[n2 + n3] + 1);
        final byte[] array2 = new byte[n4];
        for (int j = 0; j < n4; ++j) {
            array2[j] = this.gameMetadata.data[n2++];
        }
        this.terrain = new TerrainLevel(0, (int)this.gameMetadata.data[4], this.screenWidth, (int)this.gameMetadata.data[3], this.tileSprites, (int)this.gameMetadata.data[5], (byte[][])null, false, array, array2, this.motorbike.spriteWidth / 2, this.emptyTileColorArgb);
        this.assetsLoaded = true;
        this.soundManager = new SoundManager("/res/sound.dat");
    }
    
    private void resetRoundState() {
        this.hudClockX = this.gameMetadata.data[9];
        this.hudClockY = this.gameMetadata.data[9];
        this.airTrickTimer = 0;
        this.terrainScrollAccum = 0;
        this.parallaxScrollAccum = 0;
        this.trickHoldTicks = 0;
        this.stuntPhaseTicks = 0;
        this.raceEndHandled = false;
        this.frontTileSample = 0;
        this.rampLandingActive = false;
        this.crashAnimTicks = 0;
        this.endGameFlags = 0;
        this.motorbike.resetBike();
        this.terrain.scrollBy(1);
        this.motorbike.screenY = this.gameMetadata.data[4] + this.terrain.cameraGroundOffsetY;
        this.splashFrameCounter = 0;
        this.terrainScrollPeriod = this.gameMetadata.data[65];
        this.parallaxScrollPeriod = this.gameMetadata.data[66];
        this.stuntAirDelayThreshold = this.gameMetadata.data[61];
        this.hopImpulse = this.gameMetadata.data[11];
        this.trickDriftPerTick = this.gameMetadata.data[10];
        this.trickCooldownMax = this.gameMetadata.data[62];
        if (TrailBikerMidlet.deviceProfileId != 0) {
            this.terrainScrollPeriod >>= 1;
            this.parallaxScrollPeriod >>= 1;
            this.stuntAirDelayThreshold >>= 2;
            this.trickCooldownMax >>= 2;
        }
        this.addEndGameFlag(16);
        this.playfieldReady = true;
    }
    
    public void run() {
        this.repaintFlags = 0;
        this.wheelieOnLog = false;
        if (this.splashPhase != -1) {
            if (this.splashSprites == null) {
                this.dismissSplashOrOpenPause();
            }
            else if (this.splashFrameCounter > 30) {
                switch (this.splashPhase) {
                    case 1: {
                        this.splashPhase = 0;
                        this.repaint();
                        break;
                    }
                    case 0: {
                        this.dismissSplashOrOpenPause();
                        break;
                    }
                }
                this.splashFrameCounter = 0;
            }
            else {
                ++this.splashFrameCounter;
            }
        }
        else {
            if (this.endGameFlags != 0) {
                this.gameClock.setPaused(true);
                if (this.endGameAnimTimer > 20) {
                    this.tickEndGameSequence();
                    this.repaint();
                    if (this.endGameFlags == 0 && !this.raceEndHandled) {
                        this.gameClock.setPaused(false);
                        this.needsBackgroundClear = true;
                    }
                }
                else {
                    ++this.endGameAnimTimer;
                }
            }
            if (this.crashAnimActive) {
                if (this.crashAnimTicks > this.gameMetadata.data[64]) {
                    this.crashAnimActive = false;
                }
                else {
                    ++this.crashAnimTicks;
                }
            }
            else {
                this.gameClock.tick();
                this.motorbike.tickAnimation();
                Label_1680: {
                    if (this.stuntActive) {
                        if (this.stuntPhaseTicks > this.gameMetadata.data[63]) {
                            this.stuntPhaseTicks = 0;
                            this.stuntActive = false;
                            this.rampLandingActive = true;
                            this.motorbike.resetBike();
                        }
                        else {
                            ++this.stuntPhaseTicks;
                        }
                        if (!this.stuntActive && this.rampLandingActive) {
                            this.rampStopWorldX = this.terrain.findFlatGroundX(this.terrain.bikeWorldX - this.terrain.tileSizePx * 4, (byte)1, this.terrain.tileSizePx * 4);
                            this.rampStopWorldX += this.gameMetadata.data[8];
                            this.frontTileSample = 1;
                        }
                    }
                    else {
                        if (!this.stuntActive && !this.raceEndHandled) {
                            if (this.rampLandingActive) {
                                this.terrainScrollThreshold = this.terrainScrollPeriod - 2 * this.gameMetadata.data[71] / this.terrainScrollPeriod;
                                this.parallaxScrollThreshold = this.parallaxScrollPeriod - 2 * this.gameMetadata.data[71] / this.parallaxScrollPeriod;
                            }
                            else {
                                this.terrainScrollThreshold = this.terrainScrollPeriod - this.motorbike.currentLean / (5 * this.terrainScrollPeriod);
                                this.parallaxScrollThreshold = this.parallaxScrollPeriod - this.motorbike.currentLean / (5 * this.parallaxScrollPeriod);
                            }
                            if (this.terrainScrollAccum > this.terrainScrollThreshold) {
                                final int n = this.terrainScrollAccum - this.terrainScrollThreshold;
                                if (this.rampLandingActive) {
                                    this.terrain.scrollBy(-n);
                                }
                                else {
                                    this.terrain.scrollBy(n);
                                }
                                this.terrainScrollAccum = 0;
                                this.repaintFlags += 2;
                            }
                            ++this.terrainScrollAccum;
                            if (this.parallaxScrollAccum > this.parallaxScrollThreshold) {
                                final int n2 = this.parallaxScrollAccum - this.parallaxScrollThreshold;
                                if (this.rampLandingActive) {
                                    this.parallax.scrollBy(-n2);
                                }
                                else {
                                    this.parallax.scrollBy(n2);
                                }
                                this.parallaxScrollAccum = 0;
                                ++this.repaintFlags;
                            }
                            ++this.parallaxScrollAccum;
                        }
                        if (this.rampLandingActive && this.terrain.bikeWorldX <= this.rampStopWorldX) {
                            this.rampLandingActive = false;
                            this.motorbike.screenY = this.gameMetadata.data[4] + this.terrain.cameraGroundOffsetY;
                            this.motorbike.inAirTrick = false;
                        }
                        if (this.terrain.composedTileId != 10 && (this.levelManager.gameMode == 0 || this.gameClock.displayTimeMs > 0L) && (this.levelManager.gameMode != 2 || this.stuntLivesLost < this.stuntLivesTotal)) {
                            final long int1 = this.gameClock.displayTimeMs;
                            final GameClock w = this.gameClock;
                            if (int1 < 599990L) {
                                if (!this.stuntActive && !this.rampLandingActive) {
                                    this.updateBikeOnTerrain();
                                }
                                if (this.wheelieOnLog && !this.motorbike.inAirTrick) {
                                    this.motorbike.advanceAirTrick(0, false);
                                }
                                if (this.motorbike.inAirTrick) {
                                    if (this.trickHoldTicks > this.trickCooldownMax) {
                                        if (this.motorbike.trickFrameIndex < 20) {
                                            final Motorbike p = this.motorbike;
                                            ++p.trickFrameIndex;
                                        }
                                        this.trickHoldTicks = 0;
                                    }
                                    if (this.airTrickTimer > this.stuntAirDelayThreshold) {
                                        final Motorbike p2 = this.motorbike;
                                        p2.trickDriftX -= this.trickDriftPerTick;
                                        final Motorbike p3 = this.motorbike;
                                        p3.screenY -= this.motorbike.trickDriftX;
                                        final int void1 = this.gameMetadata.data[4] + this.terrain.cameraGroundOffsetY;
                                        if (this.motorbike.screenY >= void1) {
                                            this.motorbike.screenY = void1;
                                            this.motorbike.inAirTrick = false;
                                            int n3 = 0;
                                            switch (this.terrain.composedTileId) {
                                                case 3: {
                                                    n3 = 0;
                                                    break;
                                                }
                                                case 4: {
                                                    n3 = 16;
                                                    break;
                                                }
                                                default: {
                                                    n3 = 7;
                                                    break;
                                                }
                                            }
                                            n3 += 4;
                                            if (this.motorbike.trickFrameIndex > n3 && !this.wheelieOnLog && this.terrain.composedTileId != 9) {
                                                this.beginStunt();
                                                if (this.levelManager.soundEnabled) {
                                                    this.soundManager.playSound((byte)1);
                                                }
                                            }
                                            else {
                                                switch (this.terrain.composedTileId) {
                                                    case 3: {
                                                        this.motorbike.poseFrameIndex = 0;
                                                        break;
                                                    }
                                                    case 4: {
                                                        this.motorbike.poseFrameIndex = 16;
                                                        break;
                                                    }
                                                    default: {
                                                        this.motorbike.poseFrameIndex = 7;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        this.airTrickTimer = 0;
                                    }
                                    ++this.airTrickTimer;
                                    ++this.trickHoldTicks;
                                    break Label_1680;
                                }
                                this.motorbike.screenY = this.gameMetadata.data[4] + this.terrain.cameraGroundOffsetY;
                                switch (this.terrain.composedTileId) {
                                    case 1:
                                    case 5:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9: {
                                        this.motorbike.poseFrameIndex = 7;
                                        break Label_1680;
                                    }
                                    case 3: {
                                        this.motorbike.poseFrameIndex = 0;
                                        break Label_1680;
                                    }
                                    case 4: {
                                        this.motorbike.poseFrameIndex = 16;
                                        break Label_1680;
                                    }
                                }
                            }
                        }
                        if (!this.raceEndHandled) {
                            this.raceEndHandled = true;
                            this.gameClock.setPaused(true);
                            if (this.levelManager.gameMode == 2 && this.stuntLivesLost >= this.stuntLivesTotal) {
                                ++this.endGameFlags;
                                this.levelManager.pauseOverlayActive = true;
                            }
                            else if (this.levelManager.gameMode != 0) {
                                if (this.gameClock.displayTimeMs == 0L) {
                                    this.addEndGameFlag(4);
                                }
                                else {
                                    if ((this.levelManager.gameMode == 1 && this.gameClock.displayTimeMs > 0L) || (this.levelManager.gameMode == 2 && this.levelManager.getBestTimeMs(this.levelManager.selectedCourseIndex) - this.gameClock.displayTimeMs < this.levelManager.getParTimeFromDat(this.levelManager.selectedCourseIndex))) {
                                        this.addEndGameFlag(2);
                                    }
                                    else {
                                        this.addEndGameFlag(8);
                                    }
                                    if (this.levelManager.soundEnabled) {
                                        if (this.levelManager.gameMode == 2 && this.levelManager.selectedCourseIndex == 8) {
                                            this.soundManager.playSound((byte)3);
                                        }
                                        else {
                                            this.soundManager.playSound((byte)2);
                                        }
                                    }
                                }
                            }
                            else {
                                final long int2 = this.gameClock.displayTimeMs;
                                final a.a.a.a.a w2 = this.gameClock;
                                if (int2 >= 599990L) {
                                    this.addEndGameFlag(4);
                                }
                                else {
                                    if (this.gameClock.displayTimeMs < this.levelManager.getCurrentTargetTimeMs()) {
                                        this.addEndGameFlag(2);
                                    }
                                    this.addEndGameFlag(8);
                                    if (this.levelManager.soundEnabled) {
                                        this.soundManager.playSound((byte)2);
                                    }
                                }
                            }
                        }
                        if (this.endGameFlags == 0) {
                            long int3 = this.gameClock.displayTimeMs;
                            if (this.levelManager.gameMode == 2 && (this.stuntLivesLost >= this.stuntLivesTotal || this.gameClock.displayTimeMs == 0L)) {
                                int3 = -1L;
                            }
                            else if (this.levelManager.gameMode != 0) {
                                int3 = this.levelManager.getCurrentTargetTimeMs() - int3;
                            }
                            this.levelManager.onRaceFinished(int3);
                        }
                    }
                }
                final int n4 = this.motorbike.screenY - this.motorbike.spriteHeight - (this.gameMetadata.data[4] - this.gameMetadata.data[2]) * 2;
                if (n4 <= this.gameMetadata.data[4] && (n4 > this.gameMetadata.data[2] || (this.repaintFlags & 0x1) == 0x0)) {
                    this.repaintFlags += 4;
                }
                if (this.repaintFlags != 0) {
                    if ((this.repaintFlags & 0x1) != 0x0) {
                        this.repaint(0, 0, this.screenWidth, (int)this.gameMetadata.data[2]);
                    }
                    else {
                        this.repaint(0, 0, this.gameClock.getPaintWidth(), this.gameClock.getPaintHeight());
                    }
                    if ((this.repaintFlags & 0x2) != 0x0) {
                        this.repaint(0, (int)this.gameMetadata.data[4], this.screenWidth, this.gameMetadata.data[3] + 1);
                    }
                    if ((this.repaintFlags & 0x4) != 0x0) {
                        this.repaint(0, 0, this.motorbike.spriteWidth, (int)this.gameMetadata.data[4]);
                    }
                }
            }
        }
    }
    
    private void addEndGameFlag(final int n) {
        if ((this.endGameFlags & n) == 0x0) {
            this.endGameFlags += n;
            this.endGameAnimTimer = 0;
        }
    }
    
    private void tickEndGameSequence() {
        if (this.endGameFlags != 0) {
            this.endGameFlags -= this.getActiveEndGameFlag();
            this.endGameAnimTimer = 0;
        }
    }
    
    private int getActiveEndGameFlag() {
        int n = 0;
        if ((this.endGameFlags & 0x10) != 0x0) {
            n = 16;
        }
        else if ((this.endGameFlags & 0x4) != 0x0) {
            n = 4;
        }
        else if ((this.endGameFlags & 0x8) != 0x0) {
            n = 8;
        }
        else if ((this.endGameFlags & 0x2) != 0x0) {
            n = 2;
        }
        else if ((this.endGameFlags & 0x1) != 0x0) {
            n = 1;
        }
        return n;
    }
    
    protected void paint(final Graphics graphics) {
        if (this.splashPhase != -1) {
            if (this.splashSprites != null) {
                graphics.setColor(0);
                graphics.fillRect(0, 0, this.screenWidth, this.screenHeight);
                this.splashSprites.drawFrame(this.splashPhase, graphics, 0, this.screenHeight / 2 - this.splashSprites.frameHeight / 2);
            }
        }
        else {
            if (this.needsBackgroundClear) {
                this.fillBackgroundWhite(graphics);
                this.needsBackgroundClear = false;
            }
            if (this.playfieldReady) {
                graphics.setColor(16777215);
                graphics.fillRect((int)this.gameMetadata.data[9], 0, this.motorbike.spriteWidth, (int)this.gameMetadata.data[4]);
                this.parallax.paint(graphics, 0, 0);
                this.terrain.paint(graphics);
                if (!this.rampLandingActive) {
                    this.motorbike.paint(graphics);
                }
                this.gameClock.paint(graphics, 0, 0);
                if (this.levelManager.gameMode == 2) {
                    this.drawStuntLives(graphics);
                }
                this.drawEndGameMessage(graphics);
            }
        }
    }
    
    private void drawEndGameMessage(final Graphics graphics) {
        String string = null;
        switch (this.getActiveEndGameFlag()) {
            case 4: {
                string = "Out of Time!";
                break;
            }
            case 2: {
                string = "New Best Time!";
                break;
            }
            case 1: {
                string = "Game Over";
                break;
            }
            case 8: {
                string = "Course Complete!";
                break;
            }
            case 16: {
                string = "Course " + (this.levelManager.selectedCourseIndex + 1);
                break;
            }
        }
        if (string != null) {
            this.drawCenteredBanner(graphics, 0, 0, this.screenWidth / 2, this.screenHeight / 2, string, 0, false);
        }
    }
    
    void drawStuntLives(final Graphics graphics) {
        final int n = this.stuntLivesTotal - this.stuntLivesLost - 1;
        final int n2 = this.livesImage.getWidth() + 2;
        int n3 = this.screenWidth - n * n2;
        for (int i = 0; i < n; ++i) {
            graphics.drawImage(this.livesImage, n3, 0, 20);
            n3 += n2;
        }
    }
    
    protected void keyPressed(final int n) {
        if (!this.stuntActive && !this.rampLandingActive && this.playfieldReady) {
            switch (n) {
                case 53: {
                    if (!this.motorbike.inAirTrick) {
                        this.motorbike.leanBack();
                        break;
                    }
                    break;
                }
                case 50: {
                    if (this.motorbike.inAirTrick) {
                        break;
                    }
                    if (this.terrain.composedTileId != 5) {
                        this.motorbike.leanForward();
                        break;
                    }
                    this.beginStunt();
                    if (this.levelManager.soundEnabled) {
                        this.soundManager.playSound((byte)0);
                        break;
                    }
                    break;
                }
                case 49:
                case 51: {
                    this.motorbike.advanceAirTrick(this.hopImpulse, true);
                    this.trickHoldTicks = 0;
                    break;
                }
                case -7:
                case -6:
                case -5:
                case 48: {
                    this.dismissSplashOrOpenPause();
                    break;
                }
            }
        }
        else {
            switch (n) {
                case -7:
                case -6:
                case -5:
                case 48: {
                    this.dismissSplashOrOpenPause();
                    break;
                }
            }
        }
    }
    
    private void dismissSplashOrOpenPause() {
        if (this.splashPhase != -1) {
            this.suppressPauseOnce = true;
            this.splashPhase = -1;
            this.splashSprites = null;
            if (!this.assetsLoaded) {
                this.loadGameResources();
                this.resetRoundState();
            }
            this.levelManager.pauseGameCanvas();
            this.levelManager.showMenuScreen();
        }
        else {
            this.levelManager.handleMidletPause();
        }
    }
    
    protected void keyReleased(final int n) {
        if (this.playfieldReady) {
            this.motorbike.setNeutral();
        }
    }
    
    protected void keyRepeated(final int n) {
    }
    
    protected void onCharHook() {
    }
    
    private void updateBikeOnTerrain() {
        int n = 0;
        final int n2 = this.motorbike.screenY - this.gameMetadata.data[4];
        final int a = this.motorbike.getWheelProbeOffset(0);
        final int a2 = this.motorbike.getWheelProbeOffset(1);
        final int n3 = Math.abs(a - a2) / 2;
        final int n4 = this.motorbike.spriteWidth / 2;
        int n5 = this.terrain.probeTerrainAt(this.terrain.bikeWorldX + n4, n2 - a, !this.motorbike.inAirTrick, false);
        if (n5 == 0) {
            n5 = this.terrain.probeTerrainAt(this.terrain.bikeWorldX + n4, n2 - this.motorbike.spriteHeight, false, false);
        }
        final boolean b = (n5 & 0xFFFFFF80) != 0x0;
        if (b) {
            n5 = (byte)(n5 + 128);
            if (n5 == 0) {
                n5 = 6;
            }
        }
        byte f = this.terrain.probeTerrainAt(this.terrain.bikeWorldX, n2 - n3, !this.motorbike.inAirTrick, false);
        if (f == 0) {
            f = this.terrain.probeTerrainAt(this.terrain.bikeWorldX, n2 - this.motorbike.spriteHeight, false, false);
        }
        final boolean b2 = (f & 0xFFFFFF80) != 0x0;
        if (b2) {
            f += 128;
        }
        byte b3 = this.terrain.probeTerrainAt(this.terrain.bikeWorldX - n4, n2 - a2, !this.motorbike.inAirTrick, false);
        if (b3 == 0) {
            b3 = this.terrain.probeTerrainAt(this.terrain.bikeWorldX - n4, n2 - this.motorbike.spriteHeight, false, false);
        }
        final boolean b4 = (b3 & 0xFFFFFF80) != 0x0;
        if (b4) {
            b3 += 128;
        }
        if (n5 != 2 && n5 != 5 && n5 != 8 && n5 != 6 && (n5 != 4 || b3 != 0) && (n5 != 4 || b3 != 1) && (n5 != 4 || b3 != 5) && (n5 != 4 || b3 != 9)) {
            if (n5 == 11 && this.motorbike.inAirTrick) {}
        }
        else {
            n = (b ? 1 : 0);
        }
        if (n == 0) {
            if (f != 5 && f != 8 && f != 6) {
                if (f == 11 && this.motorbike.inAirTrick) {}
            }
            else {
                n = (b2 ? 1 : 0);
            }
        }
        if (n == 0) {
            if (b3 != 8) {
                if (b3 == 11 && this.motorbike.inAirTrick) {}
            }
            else {
                n = (b4 ? 1 : 0);
            }
        }
        if (n != 0) {
            this.beginStunt();
            if (this.levelManager.soundEnabled) {
                this.soundManager.playSound((byte)1);
            }
        }
        else {
            if (!this.motorbike.inAirTrick && this.terrain.probeTerrainAt(this.terrain.bikeWorldX + 2, n2, true, false) == 0 && this.terrain.probeTerrainAt(this.terrain.bikeWorldX, n2 - n3, true, true) != 4) {
                this.wheelieOnLog = true;
            }
            if (!this.motorbike.inAirTrick && n == 0 && (f == 9 || b3 == 9 || n5 == 9)) {
                this.motorbike.currentLean = this.motorbike.minLean;
            }
        }
        this.frontTileSample = f;
    }
    
    void startLevel() {
        this.playfieldReady = false;
        this.splashPhase = -1;
        final byte[][] do1 = this.levelManager.buildLevelGrid();
        if (this.terrain != null) {
            this.terrain.loadLevel(do1);
            this.terrain.setBikeWorldX((int)this.gameMetadata.data[8]);
        }
        this.configureGameClock();
        this.crashAnimActive = true;
        this.crashAnimTicks = 0;
        this.resetRoundState();
        try {
            this.startGameLoopTimer();
        }
        catch (Exception ex) {}
        this.repaint();
    }
    
    private void configureGameClock() {
        final boolean b = this.levelManager.gameMode != 0;
        this.gameClock.reset(b, b ? this.levelManager.getCurrentTargetTimeMs() : 0L, true);
    }
    
    public void resetStuntLives() {
        this.stuntLivesLost = 0;
        this.stuntLivesTotal = this.gameMetadata.data[14];
    }
    
    public void tickStuntCupLife() {
        if (this.stuntLivesTotal - this.stuntLivesLost < this.gameMetadata.data[15]) {
            --this.stuntLivesLost;
        }
    }
    
    public void startGameLoopTimer() {
        if (this.gameLoopTask != null) {
            return;
        }
        this.gameLoopTask = new CanvasGameLoopTask(this, this);
        DeviceControl.setLights(0, this.levelManager.volumePercent);
        this.gameClock.setPaused(false);
        this.needsBackgroundClear = true;
    }
    
    public void shutdownGameLoopTimer() {
        if (this.gameLoopTask == null) {
            return;
        }
        this.gameLoopTask.stop();
        this.gameLoopTask = null;
        this.gameClock.setPaused(true);
    }
    
    private void beginStunt() {
        if (!this.stuntActive && !this.rampLandingActive && this.motorbike.bikeState != 5) {
            this.stuntActive = true;
            this.motorbike.enterCrash();
            ++this.stuntLivesLost;
            this.repaint();
        }
    }
    
    protected void hideNotify() {
        if (this.suppressPauseOnce) {
            this.suppressPauseOnce = false;
        }
        else {
            this.midlet.pauseApp();
        }
    }
    
    public void noop(final String s) {
    }
}
