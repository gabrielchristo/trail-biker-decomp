package a.a.a.a;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import a.a.a.b.SpriteSheet;

public class GameClock
{
    boolean countdownMode;
    boolean showCentiseconds;
    public long displayTimeMs;
    public long baseTimeMs;
    long wallClockStartMs;
    SpriteSheet digitSprites;
    private long pauseStartedMs;
    private boolean paused;
    public static final int SPRITE_COLON = 10;
    public static final int SPRITE_COLUMN_COUNT = 11;
    public static final int DIGIT_SPACING = 3;
    public static final long MAX_TIME_MS = 599990L;
    
    public GameClock(final boolean b, final long n, final boolean b2) {
        super();
        try {
            this.digitSprites = new SpriteSheet(Image.createImage("/res/numbers.png"), 1, 11, true);
        }
        catch (Exception ex) {
            System.out.println("Failed to load numbers.png in Clock() constructor.");
        }
        this.reset(b, n, b2);
    }
    
    public void tick() {
        if (!this.paused) {
            final long n = System.currentTimeMillis() - this.wallClockStartMs;
            if (this.countdownMode && this.displayTimeMs > 0L) {
                this.displayTimeMs = this.baseTimeMs - n;
            }
            else {
                this.displayTimeMs = this.baseTimeMs + n;
                if (this.displayTimeMs > 599990L) {
                    this.displayTimeMs = 599990L;
                }
            }
            if (this.displayTimeMs < 0L) {
                this.displayTimeMs = 0L;
            }
        }
    }
    
    public void reset(final boolean countdownMode, final long n, final boolean showCentiseconds) {
        this.baseTimeMs = n;
        this.displayTimeMs = n;
        this.wallClockStartMs = System.currentTimeMillis();
        this.countdownMode = countdownMode;
        this.showCentiseconds = showCentiseconds;
        this.paused = false;
    }
    
    public void setPaused(final boolean paused) {
        if (paused) {
            if (!this.paused) {
                this.pauseStartedMs = System.currentTimeMillis();
            }
        }
        else if (this.paused) {
            this.wallClockStartMs += System.currentTimeMillis() - this.pauseStartedMs;
        }
        this.paused = paused;
    }
    
    public void paint(final Graphics graphics, final int x, final int y) {
        final int seconds = (int)(this.displayTimeMs / 1000L) % 60;
        final int minutes = (int)(this.displayTimeMs / 1000L) / 60;
        final int digitSpacing = 3;
        this.digitSprites.drawFrame(minutes, graphics, x, y);
        this.digitSprites.drawFrame(10, graphics, x + digitSpacing, y);
        this.digitSprites.drawFrame(seconds / 10, graphics, x + digitSpacing * 2, y);
        this.digitSprites.drawFrame(seconds % 10, graphics, x + digitSpacing * 3, y);
        if (this.showCentiseconds) {
            final int centiseconds = (int)(this.displayTimeMs / 10L) % 100;
            this.digitSprites.drawFrame(10, graphics, x + digitSpacing * 4, y);
            this.digitSprites.drawFrame(centiseconds / 10, graphics, x + digitSpacing * 5, y);
            this.digitSprites.drawFrame(centiseconds % 10, graphics, x + digitSpacing * 6, y);
        }
    }
    
    public int getPaintWidth() {
        final int digitWidth = this.digitSprites.frameWidth;
        final int digitSpacing = 3;
        int width = 4 * (digitWidth + digitSpacing);
        if (this.showCentiseconds) {
            width += 3 * (digitWidth + digitSpacing);
        }
        return width;
    }
    
    public int getPaintHeight() {
        return this.digitSprites.frameHeight;
    }
}
