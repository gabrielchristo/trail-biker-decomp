package a.a.a.b;

import a.a.a.c.f;
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
    
    public SpriteSheet(final Image new1, final int case1, final int byte1, final boolean try1) {
        super();
        this.clippedMode = try1;
        this.sheetWidth = new1.getWidth();
        this.sheetHeight = new1.getHeight();
        this.gridColumns = case1;
        this.gridRows = byte1;
        this.frameCount = this.gridRows * this.gridColumns;
        this.frameHeight = this.sheetHeight / this.gridRows;
        this.frameWidth = this.sheetWidth / this.gridColumns;
        if (!this.clippedMode) {
            this.slicedFrames = new Image[this.frameCount];
            int n = 0;
            for (int i = 0; i < this.gridColumns; ++i) {
                for (int j = 0; j < this.gridRows; ++j) {
                    this.slicedFrames[n] = Image.createImage(this.frameWidth, this.frameHeight);
                    this.slicedFrames[n].getGraphics().drawImage(new1, -i * this.frameWidth, -j * this.frameHeight, 20);
                    ++n;
                }
            }
        }
        else {
            this.sourceImage = new1;
        }
    }
    
    public void drawFrame(final int n, final Graphics graphics, final int n2, final int n3) {
        if (!this.clippedMode) {
            graphics.drawImage(this.slicedFrames[n], n2, n3, 20);
        }
        else {
            int n4 = n2;
            final int n5 = n2 - n / this.gridRows * this.frameWidth;
            int n6 = n3;
            final int n7 = n3 - this.frameHeight * (n % this.gridRows);
            final int clipX = graphics.getClipX();
            final int clipY = graphics.getClipY();
            final int clipWidth = graphics.getClipWidth();
            final int clipHeight = graphics.getClipHeight();
            int if1 = this.frameWidth;
            int do1 = this.frameHeight;
            if (n4 < 0) {
                if1 += n4;
                n4 = 0;
            }
            if (n6 < 0) {
                do1 += n6;
                n6 = 0;
            }
            if (f.a == 1) {
                ++if1;
                ++do1;
            }
            graphics.setClip(n4, n6, if1, do1);
            graphics.drawImage(this.sourceImage, n5, n7, 20);
            graphics.setClip(clipX, clipY, clipWidth, clipHeight);
        }
    }
}
