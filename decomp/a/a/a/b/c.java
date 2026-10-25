package a.a.a.b;

import a.a.a.c.f;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class c
{
    private int case;
    private int byte;
    public int int;
    public int do;
    public int if;
    public int char;
    public int for;
    public Image[] a;
    public Image new;
    public boolean try;
    
    public c(final Image new1, final int case1, final int byte1, final boolean try1) {
        super();
        this.try = try1;
        this.char = new1.getWidth();
        this.for = new1.getHeight();
        this.case = case1;
        this.byte = byte1;
        this.int = this.byte * this.case;
        this.do = this.for / this.byte;
        this.if = this.char / this.case;
        if (!this.try) {
            this.a = new Image[this.int];
            int n = 0;
            for (int i = 0; i < this.case; ++i) {
                for (int j = 0; j < this.byte; ++j) {
                    this.a[n] = Image.createImage(this.if, this.do);
                    this.a[n].getGraphics().drawImage(new1, -i * this.if, -j * this.do, 20);
                    ++n;
                }
            }
        }
        else {
            this.new = new1;
        }
    }
    
    public void a(final int n, final Graphics graphics, final int n2, final int n3) {
        if (!this.try) {
            graphics.drawImage(this.a[n], n2, n3, 20);
        }
        else {
            int n4 = n2;
            final int n5 = n2 - n / this.byte * this.if;
            int n6 = n3;
            final int n7 = n3 - this.do * (n % this.byte);
            final int clipX = graphics.getClipX();
            final int clipY = graphics.getClipY();
            final int clipWidth = graphics.getClipWidth();
            final int clipHeight = graphics.getClipHeight();
            int if1 = this.if;
            int do1 = this.do;
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
            graphics.drawImage(this.new, n5, n7, 20);
            graphics.setClip(clipX, clipY, clipWidth, clipHeight);
        }
    }
}
