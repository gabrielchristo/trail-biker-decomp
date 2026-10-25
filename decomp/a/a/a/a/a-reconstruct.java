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
    
    public void paint(final Graphics graphics, final int n, final int n2) {
        final int n3 = (int)(this.displayTimeMs / 1000L) % 60;
        final int n4 = (int)(this.displayTimeMs / 1000L) / 60;
        final int n5 = 3;
        this.digitSprites.a(n4, graphics, n, n2);
        this.digitSprites.a(10, graphics, n + n5, n2);
        this.digitSprites.a(n3 / 10, graphics, n + n5 * 2, n2);
        this.digitSprites.a(n3 % 10, graphics, n + n5 * 3, n2);
        if (this.showCentiseconds) {
            final int n6 = (int)(this.displayTimeMs / 10L) % 100;
            this.digitSprites.a(10, graphics, n + n5 * 4, n2);
            this.digitSprites.a(n6 / 10, graphics, n + n5 * 5, n2);
            this.digitSprites.a(n6 % 10, graphics, n + n5 * 6, n2);
        }
    }
    
    public int getPaintWidth() {
        final int if1 = this.digitSprites.if;
        final int n = 3;
        int n2 = 4 * (if1 + n);
        if (this.showCentiseconds) {
            n2 += 3 * (if1 + n);
        }
        return n2;
    }
    
    public int getPaintHeight() {
        return this.digitSprites.do;
    }
}
