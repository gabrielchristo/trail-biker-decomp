package a.a.a.b;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ParallaxBackground
{
    private SpriteSheet spriteSheet;
    private int layerCount;
    private int[] layerFrameIndices;
    private int scrollOffsetPx;
    private int segmentWidthPx;
    private int scrollStepSign;
    
    public ParallaxBackground(final String s, final int do1, final boolean b) {
        super();
        this.layerCount = do1;
        try {
            this.spriteSheet = new SpriteSheet(Image.createImage(s), 1, do1, b);
        }
        catch (Exception ex) {
            System.out.println("Image load failed " + ex.toString());
        }
        this.layerFrameIndices = new int[this.layerCount];
        for (int i = 0; i < this.layerCount; ++i) {
            this.layerFrameIndices[i] = i;
        }
        this.scrollOffsetPx = 0;
        this.scrollStepSign = 1;
        this.segmentWidthPx = this.spriteSheet.frameWidth;
    }
    
    public void scrollBy(final int n) {
        this.scrollOffsetPx += n;
        if (this.scrollOffsetPx < 0) {
            this.scrollStepSign = -1;
        }
        else {
            this.scrollStepSign = 1;
        }
        if (this.scrollOffsetPx <= -this.segmentWidthPx || this.scrollOffsetPx >= this.segmentWidthPx) {
            for (int i = 0; i < this.layerCount; ++i) {
                this.layerFrameIndices[i] += this.scrollStepSign;
                if (this.layerFrameIndices[i] < 0) {
                    this.layerFrameIndices[i] = this.layerCount - 1;
                }
                else if (this.layerFrameIndices[i] >= this.layerCount) {
                    this.layerFrameIndices[i] = 0;
                }
            }
            this.scrollOffsetPx = 0;
        }
    }
    
    public void paint(final Graphics graphics, final int n, final int n2) {
        if (this.layerCount > 1) {
            for (int i = 0; i < this.layerCount; ++i) {
                this.spriteSheet.drawFrame(this.layerFrameIndices[i], graphics, n - this.scrollOffsetPx + i * this.scrollStepSign * this.segmentWidthPx, n2);
            }
        }
        else {
            this.spriteSheet.drawFrame(this.layerFrameIndices[0], graphics, n - this.scrollOffsetPx, n2);
            this.spriteSheet.drawFrame(this.layerFrameIndices[0], graphics, n - this.scrollOffsetPx + this.scrollStepSign * this.segmentWidthPx, n2);
        }
    }
}
