package a.a.a.a;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import a.a.a.b.c;

public class a
{
    boolean do;
    boolean case;
    public long int;
    public long else;
    long byte;
    c a;
    private long char;
    private boolean new;
    public static final int goto = 10;
    public static final int if = 11;
    public static final int try = 3;
    public static final long for = 599990L;
    
    public a(final boolean b, final long n, final boolean b2) {
        super();
        try {
            this.a = new c(Image.createImage("/res/numbers.png"), 1, 11, true);
        }
        catch (Exception ex) {
            System.out.println("Failed to load numbers.png in Clock() constructor.");
        }
        this.a(b, n, b2);
    }
    
    public void do() {
        if (!this.new) {
            final long n = System.currentTimeMillis() - this.byte;
            if (this.do && this.int > 0L) {
                this.int = this.else - n;
            }
            else {
                this.int = this.else + n;
                if (this.int > 599990L) {
                    this.int = 599990L;
                }
            }
            if (this.int < 0L) {
                this.int = 0L;
            }
        }
    }
    
    public void a(final boolean do1, final long n, final boolean case1) {
        this.else = n;
        this.int = n;
        this.byte = System.currentTimeMillis();
        this.do = do1;
        this.case = case1;
        this.new = false;
    }
    
    public void a(final boolean new1) {
        if (new1) {
            if (!this.new) {
                this.char = System.currentTimeMillis();
            }
        }
        else if (this.new) {
            this.byte += System.currentTimeMillis() - this.char;
        }
        this.new = new1;
    }
    
    public void a(final Graphics graphics, final int n, final int n2) {
        final int n3 = (int)(this.int / 1000L) % 60;
        final int n4 = (int)(this.int / 1000L) / 60;
        final int n5 = 3;
        this.a.a(n4, graphics, n, n2);
        this.a.a(10, graphics, n + n5, n2);
        this.a.a(n3 / 10, graphics, n + n5 * 2, n2);
        this.a.a(n3 % 10, graphics, n + n5 * 3, n2);
        if (this.case) {
            final int n6 = (int)(this.int / 10L) % 100;
            this.a.a(10, graphics, n + n5 * 4, n2);
            this.a.a(n6 / 10, graphics, n + n5 * 5, n2);
            this.a.a(n6 % 10, graphics, n + n5 * 6, n2);
        }
    }
    
    public int a() {
        final int if1 = this.a.if;
        final int n = 3;
        int n2 = 4 * (if1 + n);
        if (this.case) {
            n2 += 3 * (if1 + n);
        }
        return n2;
    }
    
    public int if() {
        return this.a.do;
    }
}
