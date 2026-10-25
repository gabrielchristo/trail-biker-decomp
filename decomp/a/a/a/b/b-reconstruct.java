package a.a.a.b;

import javax.microedition.lcdui.Graphics;

public class TerrainLevel
{
    public static final int RESERVED_ZERO_A = 0;
    public static final int RESERVED_ZERO_B = 0;
    ScrollingTileLayer scrollLayer;
    int slopeIndexStride;
    int slopeTableHeight;
    public int tileSizePx;
    int tilePieceCount;
    public int cameraGroundOffsetY;
    public int bikeWorldX;
    private int minBikeWorldX;
    public byte surfaceTileId;
    public byte composedTileId;
    public int levelWidthPx;
    byte[][] slopeLookup;
    byte[][][] tilePieceDefs;
    byte[][] expandedTileMap;
    int expandedMapWidthTiles;
    int expandedMapHeightTiles;
    int sourceLevelWidth;
    int sourceLevelHeight;
    int passThroughTileCount;
    byte[] passThroughTileIds;
    public static final byte PROBE_TILE_ADJUST = Byte.MIN_VALUE;
    
    public TerrainLevel(final int n, final int n2, final int n3, final int n4, final SpriteSheet c, final int goto1, final byte[][] array, final boolean b, final byte[] array2, final byte[] array3, final int n5, final int n6) {
        super();
        this.tileSizePx = goto1;
        this.parseSlopeMetadata(array2);
        this.parseTilePieceCatalog(array3);
        this.expandLevelGrid(array);
        this.bikeWorldX = n5;
        this.minBikeWorldX = n5;
        this.surfaceTileId = 0;
        this.composedTileId = 0;
        this.scrollLayer = new ScrollingTileLayer(n, n2, n3, n4, c, goto1, this.expandedTileMap, this.expandedMapWidthTiles, this.expandedMapHeightTiles, b, n6);
    }
    
    private void parseSlopeMetadata(final byte[] array) {
        int n = 0;
        final byte b = array[n++];
        this.slopeIndexStride = this.tileSizePx / b;
        this.slopeTableHeight = array[n++];
        this.slopeLookup = new byte[this.slopeTableHeight][b];
        for (int i = 0; i < this.slopeTableHeight; ++i) {
            for (byte b2 = 0; b2 < b; ++b2) {
                this.slopeLookup[i][b2] = array[n++];
            }
        }
    }
    
    private void parseTilePieceCatalog(final byte[] array) {
        int n = 0;
        this.tilePieceCount = array[n++];
        final byte b = array[n++];
        this.tilePieceDefs = new byte[this.tilePieceCount][][];
        for (int i = 0; i < this.tilePieceCount; ++i) {
            final byte b2 = array[n++];
            final byte b3 = array[n++];
            this.tilePieceDefs[i] = new byte[b3 + 1][];
            (this.tilePieceDefs[i][0] = new byte[2])[0] = b2;
            this.tilePieceDefs[i][0][1] = b3;
            for (byte b4 = 1; b4 <= b3; ++b4) {
                this.tilePieceDefs[i][b4] = new byte[b2];
                for (byte b5 = 0; b5 < b2; ++b5) {
                    this.tilePieceDefs[i][b4][b5] = array[n++];
                }
            }
        }
        this.passThroughTileCount = array[n++];
        this.passThroughTileIds = new byte[this.passThroughTileCount];
        for (int j = 0; j < this.passThroughTileCount; ++j) {
            this.passThroughTileIds[j] = array[n++];
        }
    }
    
    void expandLevelGrid(final byte[][] array) {
        this.expandedMapWidthTiles = 0;
        this.expandedMapHeightTiles = 0;
        this.sourceLevelWidth = 0;
        this.sourceLevelHeight = 0;
        this.expandedTileMap = null;
        System.gc();
        if (array != null) {
            this.sourceLevelWidth = array[0][0] * 100 + array[0][1];
            this.sourceLevelHeight = array[0][2] * 100 + array[0][3];
            for (int i = 0; i < this.sourceLevelWidth; ++i) {
                int long1 = 0;
                for (int j = 1; j < this.sourceLevelHeight + 1; ++j) {
                    if (array[j][i] != 0) {
                        long1 += this.tilePieceDefs[array[j][i] - 1][0][1];
                    }
                }
                if (long1 > this.expandedMapHeightTiles) {
                    this.expandedMapHeightTiles = long1;
                }
            }
            for (int k = 1; k < this.sourceLevelHeight + 1; ++k) {
                int c = 0;
                for (int l = 0; l < this.sourceLevelWidth; ++l) {
                    if (array[k][l] != 0) {
                        c += this.tilePieceDefs[array[k][l] - 1][0][0];
                    }
                }
                if (c > this.expandedMapWidthTiles) {
                    this.expandedMapWidthTiles = c;
                }
            }
            this.expandedTileMap = new byte[this.expandedMapHeightTiles][this.expandedMapWidthTiles];
            int n = 0;
            final int n2 = this.expandedMapHeightTiles - 1;
            for (int n3 = 0; n3 < this.sourceLevelWidth; ++n3) {
                int n4 = this.expandedMapHeightTiles - 1;
                int n5 = 0;
                for (int n6 = this.sourceLevelHeight - 1; n6 >= 0; --n6) {
                    final byte b = array[n6 + 1][n3];
                    if (b != 0) {
                        final byte b2 = this.tilePieceDefs[b - 1][0][0];
                        final byte b3 = this.tilePieceDefs[b - 1][0][1];
                        for (byte b4 = 0; b4 < b3; ++b4) {
                            for (byte b5 = 0; b5 < b2; ++b5) {
                                this.expandedTileMap[n4 - b4][n + b5] = this.tilePieceDefs[b - 1][b3 - b4][b5];
                            }
                        }
                        n4 -= b3;
                        n5 = b2;
                    }
                }
                n += n5;
            }
            this.levelWidthPx = this.expandedMapHeightTiles * this.tileSizePx;
        }
    }
    
    public byte getTileAtPixels(final int n, final int n2) {
        return this.scrollLayer.getTileAt(this.scrollLayer.tileColumnFromPixelX(n), this.scrollLayer.tileRowFromPixelY(n2));
    }
    
    public byte matchTilePieceId(final byte b, final byte b2, final byte b3) {
        byte b4 = 0;
        if (b2 != 0) {
            for (int i = 0; i < this.tilePieceCount; i = (byte)(i + 1)) {
                final byte b5 = this.tilePieceDefs[i][0][0];
                for (byte b6 = this.tilePieceDefs[i][0][1], b7 = 1; b7 <= b6; ++b7) {
                    for (byte b8 = 0; b8 < b5; ++b8) {
                        boolean b9 = false;
                        boolean b10 = false;
                        boolean b11 = false;
                        if (b8 == 0 || this.tilePieceDefs[i][b7][b8 - 1] == b) {
                            b9 = true;
                        }
                        if (this.tilePieceDefs[i][b7][b8] == b2) {
                            b10 = true;
                        }
                        if (b8 == b5 - 1 || this.tilePieceDefs[i][b7][b8 + 1] == b3) {
                            b11 = true;
                        }
                        if (b9 && b10 && b11) {
                            b4 = (byte)(i + 1);
                            break;
                        }
                    }
                    if (b4 != 0) {
                        break;
                    }
                }
                if (b4 != 0) {
                    break;
                }
            }
        }
        return b4;
    }
    
    public int getSlopeForTile(final int n, final byte b) {
        int n2 = 0;
        if (b != 0) {
            n2 = this.slopeLookup[b - 1][n % this.tileSizePx / this.slopeIndexStride];
        }
        return n2;
    }
    
    public void scrollBy(int n) {
        final int f = this.bikeWorldX;
        final int e = this.cameraGroundOffsetY;
        this.cameraGroundOffsetY = 0;
        this.surfaceTileId = 0;
        if (this.bikeWorldX + n < this.minBikeWorldX) {
            n = this.minBikeWorldX - this.bikeWorldX;
        }
        this.bikeWorldX += n;
        while (this.surfaceTileId == 0 && this.cameraGroundOffsetY < this.levelWidthPx) {
            this.surfaceTileId = this.getTileAtPixels(this.bikeWorldX, this.cameraGroundOffsetY);
            if (this.isPassThroughTile(this.surfaceTileId)) {
                this.surfaceTileId = 0;
            }
            if (this.surfaceTileId == 0) {
                this.cameraGroundOffsetY += this.tileSizePx;
            }
        }
        if (this.surfaceTileId != 0) {
            this.composedTileId = this.matchTilePieceId(this.getTileAtPixels(this.bikeWorldX - this.tileSizePx, this.cameraGroundOffsetY), this.surfaceTileId, this.getTileAtPixels(this.bikeWorldX + this.tileSizePx, this.cameraGroundOffsetY));
            this.cameraGroundOffsetY = this.scrollLayer.tileRowFromPixelY(this.cameraGroundOffsetY) * this.tileSizePx + this.getSlopeForTile(this.bikeWorldX, this.surfaceTileId);
        }
        this.scrollLayer.updateScrollForward(0, n);
        this.scrollLayer.updateScrollReverse(0, n);
    }
    
    public void paint(final Graphics graphics) {
        this.scrollLayer.paint(graphics);
    }
    
    public byte probeTerrainAt(final int n, final int n2, final boolean b, final boolean b2) {
        byte a = 0;
        boolean b3 = false;
        int n3 = 0;
        if (n >= 0 && n2 >= 0) {
            byte a2 = 0;
            while (a2 == 0 && n3 < this.levelWidthPx) {
                a2 = this.getTileAtPixels(n, n3);
                if (b && this.isPassThroughTile(a2)) {
                    a2 = 0;
                }
                if (a2 == 0) {
                    n3 += this.tileSizePx;
                }
                else {
                    if (n2 <= n3 + this.tileSizePx) {
                        continue;
                    }
                    n3 += this.tileSizePx;
                    a2 = 0;
                }
            }
            if (a2 != 0) {
                final int n4 = this.scrollLayer.tileRowFromPixelY(n3) * this.tileSizePx + this.getSlopeForTile(n, a2);
                if (n2 >= n4 || b2) {
                    a = this.matchTilePieceId(this.getTileAtPixels(n - this.tileSizePx, n4), a2, this.getTileAtPixels(n + this.tileSizePx, n4));
                    if (n2 > n4 && !b2) {
                        b3 = true;
                    }
                }
            }
            if (b3) {
                a -= 128;
            }
        }
        return a;
    }
    
    public void loadLevel(final byte[][] array) {
        this.expandLevelGrid(array);
        this.scrollLayer.setTileMapData(this.expandedTileMap, this.expandedMapWidthTiles, this.expandedMapHeightTiles);
    }
    
    public void setBikeWorldX(final int f) {
        this.bikeWorldX = f;
        this.surfaceTileId = 0;
        this.composedTileId = 0;
        this.scrollLayer.initScrollAt(f - this.minBikeWorldX, 0);
    }
    
    private boolean isPassThroughTile(final byte b) {
        boolean b2 = false;
        if (b != 0) {
            for (int i = 0; i < this.passThroughTileCount; ++i) {
                if (b == this.passThroughTileIds[i]) {
                    b2 = true;
                    break;
                }
            }
        }
        return b2;
    }
    
    public int findFlatGroundX(int n, final byte b, final int n2) {
        byte b2;
        byte b3;
        byte b4;
        for (b2 = this.probeTerrainAt(n - n2, 0, true, true), b3 = this.probeTerrainAt(n - n2 / 2, 0, true, true), b4 = this.probeTerrainAt(n, 0, true, true); (b2 == 0 || b3 == 0 || b4 == 0) && n > 0; --n, b2 = this.probeTerrainAt(n - n2, 0, true, true), b3 = this.probeTerrainAt(n - n2 / 2, 0, true, true), b4 = this.probeTerrainAt(n, 0, true, true)) {}
        if (b2 != 0 && b3 != 0 && b4 != 0) {
            int n3;
            for (n3 = n - this.tilePieceDefs[b2][0][0]; (b2 != b || b3 != b || b4 != b) && n3 > 0; n3 -= this.tileSizePx, b2 = this.probeTerrainAt(n3 - n2, 0, true, true), b3 = this.probeTerrainAt(n3 - n2 / 2, 0, true, true), b4 = this.probeTerrainAt(n3, 0, true, true)) {}
            int n4 = n3 - n2;
            if (n4 < 0) {
                n4 = 0;
            }
            return n4;
        }
        return (n - n2 > 0) ? (n - n2) : 0;
    }
}
