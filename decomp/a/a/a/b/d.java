package a.a.a.b;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class d
{
    private c new;
    private int do;
    private int[] if;
    private int a;
    private int for;
    private int int;
    
    public d(final String s, final int do1, final boolean b) {
        super();
        this.do = do1;
        try {
            this.new = new c(Image.createImage(s), 1, do1, b);
        }
        catch (Exception ex) {
            System.out.println("Image load failed " + ex.toString());
        }
        this.if = new int[this.do];
        for (int i = 0; i < this.do; ++i) {
            this.if[i] = i;
        }
        this.a = 0;
        this.int = 1;
        this.for = this.new.if;
    }
    
    public void a(final int n) {
        this.a += n;
        if (this.a < 0) {
            this.int = -1;
        }
        else {
            this.int = 1;
        }
        if (this.a <= -this.for || this.a >= this.for) {
            for (int i = 0; i < this.do; ++i) {
                this.if[i] += this.int;
                if (this.if[i] < 0) {
                    this.if[i] = this.do - 1;
                }
                else if (this.if[i] >= this.do) {
                    this.if[i] = 0;
                }
            }
            this.a = 0;
        }
    }
    
    public void a(final Graphics graphics, final int n, final int n2) {
        if (this.do > 1) {
            for (int i = 0; i < this.do; ++i) {
                this.new.a(this.if[i], graphics, n - this.a + i * this.int * this.for, n2);
            }
        }
        else {
            this.new.a(this.if[0], graphics, n - this.a, n2);
            this.new.a(this.if[0], graphics, n - this.a + this.int * this.for, n2);
        }
    }
}
