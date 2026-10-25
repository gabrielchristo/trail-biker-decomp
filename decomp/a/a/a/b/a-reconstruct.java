package a.a.a.b;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ScrollingTileLayer
{
    private int worldColumnIndex;
    private int worldRowIndex;
    private int columnScanLimit;
    private int rowScanLimit;
    private int ringWriteOffsetPx;
    private int scrollOffsetSecondary;
    private int scrollOffsetPx;
    private boolean wrapScrollActive;
    private int wrapProbeX;
    private Image bufferImage;
    private Graphics bufferGraphics;
    private int bufferWidthPx;
    private int bufferHeightPx;
    private int savedScrollOffsetPx;
    private int savedScrollSecondaryPx;
    private int bufferColumnCount;
    private int bufferRowCount;
    private int drawAnchorX;
    private int drawAnchorY;
    private int trackWidthPx;
    private int levelWidthTiles;
    private int levelHeightTiles;
    public static final int TILE_WRAP_MARKER = 128;
    public static final byte NO_TILE = -1;
    public byte[][] tileMap;
    private SpriteSheet tileSprites;
    private Image unusedImage;
    private boolean wrapAtFarEdge;
    private int emptyTileFillColor;
    private int tileSizePx;
    
    public ScrollingTileLayer(final int o, final int n, final int k, final int n2, final SpriteSheet e, final int else1, final byte[][] i, final int h, final int void1, final boolean try1, final int c) {
        super();
        this.drawAnchorX = o;
        this.drawAnchorY = n;
        this.trackWidthPx = k;
        this.tileSprites = e;
        this.tileMap = i;
        this.levelWidthTiles = h;
        this.levelHeightTiles = void1;
        this.tileSizePx = else1;
        this.wrapAtFarEdge = try1;
        this.bufferColumnCount = this.trackWidthPx / this.tileSizePx + 2;
        if (this.trackWidthPx % this.tileSizePx > 0) {
            ++this.bufferColumnCount;
        }
        this.bufferWidthPx = this.bufferColumnCount * this.tileSizePx;
        this.bufferRowCount = n2 / this.tileSizePx;
        this.bufferHeightPx = this.bufferRowCount * this.tileSizePx;
        this.columnScanLimit = this.bufferColumnCount;
        this.rowScanLimit = this.bufferRowCount;
        this.emptyTileFillColor = c;
        this.initScrollAt(0, 0);
    }
    
    private void drawTileCell(final int n, final int n2, final int n3, final int n4) {
        if (this.tileMap != null) {
            switch (this.tileMap[n2][n]) {
                case 0: {
                    this.bufferGraphics.setColor(this.emptyTileFillColor);
                    this.bufferGraphics.fillRect(n3, n4, this.tileSizePx, this.tileSizePx);
                    break;
                }
                default: {
                    this.tileSprites.drawFrame(this.tileMap[n2][n] - 1, this.bufferGraphics, n3, n4);
                    break;
                }
            }
        }
    }
    
    private void rebuildEntireBuffer() {
        for (int i = 0; i < this.bufferColumnCount; ++i) {
            for (int j = 0; j < this.bufferRowCount; ++j) {
                this.drawTileCell(this.worldColumnIndex + i, this.worldRowIndex + j, i * this.tileSizePx, j * this.tileSizePx);
            }
        }
    }
    
    private void stripWrapMarkers(final boolean b) {
        if (this.tileMap != null) {
            int goto1 = this.worldColumnIndex;
            int n = this.worldRowIndex;
            for (int i = 0; i < this.bufferColumnCount; ++i) {
                if (i * this.tileSizePx >= this.ringWriteOffsetPx && goto1 >= this.worldColumnIndex) {
                    goto1 = this.columnScanLimit - this.bufferColumnCount;
                }
                for (int j = 0; j < this.bufferRowCount; ++j) {
                    if ((this.tileMap[n][goto1] & 0x80) != 0x0) {
                        final byte[] array = this.tileMap[n];
                        final int n2 = goto1;
                        array[n2] -= 128;
                        if (b) {
                            this.drawTileCell(goto1, n, i * this.tileSizePx, j * this.tileSizePx);
                        }
                    }
                    ++n;
                }
                n = this.worldRowIndex;
                ++goto1;
            }
        }
    }
    
    public boolean updateScrollReverse(final int n, final int n2) {
        if (this.scrollOffsetSecondary < 0) {
            this.scrollOffsetSecondary += this.bufferWidthPx;
        }
        if (this.scrollOffsetSecondary > this.ringWriteOffsetPx && this.scrollOffsetSecondary <= this.ringWriteOffsetPx + this.tileSizePx) {
            if (this.worldColumnIndex + this.ringWriteOffsetPx / this.tileSizePx >= this.levelWidthTiles && this.wrapAtFarEdge) {
                this.worldColumnIndex = 0;
                this.columnScanLimit = this.bufferColumnCount;
                this.ringWriteOffsetPx = this.bufferWidthPx;
            }
            if (this.worldColumnIndex + this.ringWriteOffsetPx / this.tileSizePx >= this.levelWidthTiles) {
                if (this.wrapScrollActive) {
                    this.wrapScrollActive = false;
                    this.wrapProbeX = n;
                }
                this.scrollOffsetPx -= n2;
                this.scrollOffsetSecondary -= n2;
                if (this.scrollOffsetSecondary < 0) {
                    this.scrollOffsetSecondary += this.bufferWidthPx;
                }
            }
            else {
                if (this.ringWriteOffsetPx >= this.bufferWidthPx) {
                    this.ringWriteOffsetPx = 0;
                    this.worldColumnIndex += this.bufferColumnCount;
                }
                if (this.scrollOffsetSecondary >= this.bufferWidthPx) {
                    this.scrollOffsetSecondary -= this.bufferWidthPx;
                }
                final int do1 = this.ringWriteOffsetPx;
                this.ringWriteOffsetPx += this.tileSizePx;
                ++this.columnScanLimit;
                for (int i = 0; i < this.bufferRowCount; ++i) {
                    this.drawTileCell(this.worldColumnIndex + do1 / this.tileSizePx, this.worldRowIndex + i, do1, i * this.tileSizePx);
                }
            }
        }
        if (this.scrollOffsetPx >= this.bufferWidthPx) {
            this.scrollOffsetPx -= this.bufferWidthPx;
        }
        if (this.scrollOffsetPx < 0) {
            this.scrollOffsetPx += this.bufferWidthPx;
        }
        if (this.scrollOffsetPx < this.ringWriteOffsetPx && this.scrollOffsetPx >= this.ringWriteOffsetPx - this.tileSizePx) {
            if (this.worldColumnIndex - (this.bufferColumnCount - this.ringWriteOffsetPx / this.tileSizePx) <= 0) {
                if (this.wrapScrollActive) {
                    this.wrapScrollActive = false;
                    this.wrapProbeX = n;
                }
                this.scrollOffsetPx -= n2;
                this.scrollOffsetSecondary -= n2;
                if (this.scrollOffsetPx >= this.bufferWidthPx) {
                    this.scrollOffsetPx -= this.bufferWidthPx;
                }
            }
            else {
                this.ringWriteOffsetPx -= this.tileSizePx;
                final int do2 = this.ringWriteOffsetPx;
                --this.columnScanLimit;
                if (this.ringWriteOffsetPx <= 0) {
                    this.ringWriteOffsetPx = this.bufferWidthPx;
                    this.worldColumnIndex -= this.bufferColumnCount;
                }
                for (int j = 0; j < this.bufferRowCount; ++j) {
                    this.drawTileCell(this.columnScanLimit - this.bufferColumnCount, this.worldRowIndex + j, do2, j * this.tileSizePx);
                }
            }
        }
        return this.wrapScrollActive;
    }
    
    public boolean updateScrollForward(final int n, final int n2) {
        if (!this.wrapScrollActive) {
            if (this.worldColumnIndex - (this.bufferColumnCount - this.ringWriteOffsetPx / this.tileSizePx) <= 0 && n >= this.wrapProbeX && n < this.wrapProbeX + 5) {
                this.wrapScrollActive = true;
            }
            if (this.worldColumnIndex + this.ringWriteOffsetPx / this.tileSizePx >= this.levelWidthTiles && n <= this.wrapProbeX && n > this.wrapProbeX - 5) {
                this.wrapScrollActive = true;
            }
        }
        if (this.wrapScrollActive) {
            this.scrollOffsetPx += n2;
            this.scrollOffsetSecondary += n2;
        }
        return this.wrapScrollActive;
    }
    
    public void shiftVerticalWindow(final boolean b) {
        this.stripWrapMarkers(false);
        if (b) {
            this.worldRowIndex -= this.bufferRowCount;
            this.rowScanLimit -= this.bufferRowCount;
        }
        else {
            this.worldRowIndex += this.bufferRowCount;
            this.rowScanLimit += this.bufferRowCount;
        }
        if (!this.wrapScrollActive && this.worldColumnIndex - (this.bufferColumnCount - this.ringWriteOffsetPx / this.tileSizePx) == 0) {
            this.worldColumnIndex = 0;
            this.scrollOffsetPx = 0;
            this.scrollOffsetSecondary = this.trackWidthPx;
            this.wrapProbeX = this.trackWidthPx / 2;
        }
        else if (!this.wrapScrollActive) {
            this.worldColumnIndex = this.levelWidthTiles - this.bufferColumnCount;
            this.scrollOffsetPx = this.bufferWidthPx - this.trackWidthPx;
            this.scrollOffsetSecondary = this.bufferWidthPx;
            this.wrapProbeX = this.bufferWidthPx - this.trackWidthPx / 2;
        }
        else {
            this.scrollOffsetPx = this.savedScrollOffsetPx;
            this.scrollOffsetSecondary = this.savedScrollSecondaryPx;
        }
        this.columnScanLimit = this.worldColumnIndex + this.bufferColumnCount;
        this.ringWriteOffsetPx = this.bufferWidthPx;
        this.rebuildEntireBuffer();
    }
    
    public void paint(final Graphics graphics) {
        if (this.scrollOffsetPx < this.scrollOffsetSecondary) {
            graphics.drawImage(this.bufferImage, this.drawAnchorX - this.scrollOffsetPx, this.drawAnchorY, 20);
        }
        else {
            graphics.drawImage(this.bufferImage, this.drawAnchorX - this.scrollOffsetPx, this.drawAnchorY, 20);
            graphics.drawImage(this.bufferImage, this.drawAnchorX + this.bufferWidthPx - this.scrollOffsetPx, this.drawAnchorY, 20);
        }
    }
    
    public byte getTileAt(final int n, final int n2) {
        byte b = -1;
        if (n != -1 && n2 != -1 && this.tileMap != null) {
            b = this.tileMap[n2][n];
        }
        return b;
    }
    
    public int tileColumnFromPixelX(final int n) {
        int n2 = n / this.tileSizePx;
        if (n2 < 0 || n2 >= this.levelWidthTiles) {
            n2 = -1;
        }
        return n2;
    }
    
    public int tileRowFromPixelY(final int n) {
        int n2 = n / this.tileSizePx;
        if (n2 < 0 || n2 >= this.levelHeightTiles) {
            n2 = -1;
        }
        return n2;
    }
    
    public void setTileMapData(final byte[][] i, final int h, final int void1) {
        this.tileMap = null;
        System.gc();
        if (this.bufferImage == null) {
            this.bufferImage = Image.createImage(this.bufferWidthPx, this.bufferHeightPx);
            this.bufferGraphics = this.bufferImage.getGraphics();
        }
        else {
            this.bufferGraphics.setColor(16777215);
            this.bufferGraphics.fillRect(0, 0, this.bufferWidthPx, this.bufferHeightPx);
        }
        this.tileMap = i;
        this.levelWidthTiles = h;
        this.levelHeightTiles = void1;
        this.columnScanLimit = this.bufferColumnCount;
        this.rowScanLimit = this.bufferRowCount;
        this.initScrollAt(0, 0);
    }
    
    public void initScrollAt(final int d, final int n) {
        this.savedScrollOffsetPx = d;
        this.savedScrollSecondaryPx = this.bufferWidthPx - this.tileSizePx;
        this.wrapScrollActive = true;
        this.ringWriteOffsetPx = this.bufferWidthPx;
        this.scrollOffsetSecondary = this.savedScrollSecondaryPx;
        this.scrollOffsetPx = this.savedScrollOffsetPx;
        this.worldColumnIndex = d / this.tileSizePx;
        this.worldRowIndex = n / this.tileSizePx;
        this.rebuildEntireBuffer();
    }
}
