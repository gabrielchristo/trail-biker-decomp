package a.a.a.b;

import a.a.a.c.TrailBikerMidlet;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class SpriteSheet
{
    private int gridColumns;
    private int gridRows;
    public int frameCount;
    public int frameHeight;
    public int frameWidth;
    public int sheetWidth;
    public int sheetHeight;
    public Image[] slicedFrames;
    public Image sourceImage;
    public boolean clippedMode;
    
    public SpriteSheet(final Image sourceImage, final int gridColumns, final int gridRows, final boolean clippedMode) {
        super();
        this.clippedMode = clippedMode;
        this.sheetWidth = sourceImage.getWidth();
        this.sheetHeight = sourceImage.getHeight();
        this.gridColumns = gridColumns;
        this.gridRows = gridRows;
        this.frameCount = this.gridRows * this.gridColumns;
        this.frameHeight = this.sheetHeight / this.gridRows;
        this.frameWidth = this.sheetWidth / this.gridColumns;
        if (!this.clippedMode) {
            this.slicedFrames = new Image[this.frameCount];
            int n = 0;
            for (int i = 0; i < this.gridColumns; ++i) {
                for (int j = 0; j < this.gridRows; ++j) {
                    this.slicedFrames[n] = Image.createImage(this.frameWidth, this.frameHeight);
                    this.slicedFrames[n].getGraphics().drawImage(sourceImage, -i * this.frameWidth, -j * this.frameHeight, 20);
                    ++n;
                }
            }
        }
        else {
            this.sourceImage = sourceImage;
        }
    }
    
    public void drawFrame(final int frameIndex, final Graphics graphics, final int x, final int y) {
        if (!this.clippedMode) {
            graphics.drawImage(this.slicedFrames[frameIndex], x, y, 20);
        }
        else {
            int clipX = x;
            final int drawX = x - frameIndex / this.gridRows * this.frameWidth;
            int clipY = y;
            final int drawY = y - this.frameHeight * (frameIndex % this.gridRows);
            final int savedClipX = graphics.getClipX();
            final int savedClipY = graphics.getClipY();
            final int savedClipWidth = graphics.getClipWidth();
            final int savedClipHeight = graphics.getClipHeight();
            int clipWidth = this.frameWidth;
            int clipHeight = this.frameHeight;
            if (clipX < 0) {
                clipWidth += clipX;
                clipX = 0;
            }
            if (clipY < 0) {
                clipHeight += clipY;
                clipY = 0;
            }
            if (TrailBikerMidlet.deviceProfileId == 1) {
                ++clipWidth;
                ++clipHeight;
            }
            graphics.setClip(clipX, clipY, clipWidth, clipHeight);
            graphics.drawImage(this.sourceImage, drawX, drawY, 20);
            graphics.setClip(savedClipX, savedClipY, savedClipWidth, savedClipHeight);
        }
    }
}
