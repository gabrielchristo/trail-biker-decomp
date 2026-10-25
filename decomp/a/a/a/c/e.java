package a.a.a.c;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import a.a.a.e.a;
import a.a.a.b.c;

public class e
{
    public int c;
    private int if;
    public boolean a;
    private c d;
    public int b;
    public int void;
    public int try;
    public int g;
    public int null;
    public int char;
    public int e;
    public int f;
    public int long;
    public int goto;
    public int for;
    public boolean case;
    public int byte;
    public int new;
    public int int;
    public byte[][] else;
    private a do;
    
    public e(final int b, final int void1, final a do1) {
        super();
        this.b = b;
        this.void = void1;
        this.do = do1;
        try {
            this.d = new c(Image.createImage("/res/bikes.png"), (int)this.do.a[16], (int)this.do.a[17], true);
        }
        catch (Exception ex) {
            System.out.println("Failed to load bikes.png in Motorbike() constructor.");
        }
        this.char = this.do.a[12];
        this.e = this.do.a[13];
        if (a.a.a.c.f.a != 0) {
            --this.char;
            --this.e;
        }
        this.try = this.d.if;
        this.g = this.d.do;
        this.int();
        this.a();
    }
    
    public void for() {
        if (this.c != 5) {
            if (this.long < this.e) {
                this.long += 10;
            }
            this.c = 1;
            this.if = 0;
        }
    }
    
    public void do() {
        if (this.c != 5) {
            this.c = 0;
            this.if = 0;
        }
    }
    
    public void try() {
        if (this.c != 5) {
            if (this.long > this.char) {
                this.long -= 10;
            }
            this.c = 2;
            this.if = 0;
        }
    }
    
    public void if() {
        this.c = 5;
        this.new();
        this.if = 0;
    }
    
    public void new() {
        ++this.if;
        switch (this.c) {
            case 0: {
                if (this.if > this.do.a[68]) {
                    if (this.long > this.char) {
                        --this.long;
                    }
                    this.if = 0;
                    break;
                }
                break;
            }
            case 1: {
                if (this.if > this.do.a[67]) {
                    if (this.long < this.e) {
                        ++this.long;
                    }
                    this.if = 0;
                    break;
                }
                break;
            }
            case 2: {
                if (this.if > this.do.a[70]) {
                    if (this.long > this.char) {
                        --this.long;
                    }
                    this.if = 0;
                    break;
                }
                break;
            }
            case 5: {
                this.null = 21;
                this.f = 0;
                this.long = 0;
                break;
            }
        }
    }
    
    public void a(final Graphics graphics) {
        int byte1;
        if (this.c == 5) {
            byte1 = 21;
        }
        else if (this.case) {
            byte1 = this.byte;
        }
        else {
            byte1 = this.null + 3;
        }
        this.d.a(byte1, graphics, this.b, this.void - this.g);
    }
    
    public void a(final int for1, final boolean b) {
        if (!this.case) {
            this.for = for1;
            this.case = true;
            if (b) {
                this.byte = 0;
            }
            else {
                this.byte = this.null + this.f;
            }
        }
        else if (this.byte > 0) {
            --this.byte;
        }
    }
    
    public void a() {
        this.long = this.char;
        this.goto = 0;
        this.c = 0;
        this.a = true;
        this.if = 0;
        this.null = 7;
        this.f = 3;
        this.for = 0;
        this.case = false;
    }
    
    private void int() {
        final int n = 21;
        this.else = new byte[2][n];
        for (int i = 0; i < n; ++i) {
            this.else[0][i] = this.do.a[19 + i];
            this.else[1][i] = this.do.a[40 + i];
        }
    }
    
    public int a(final int n) {
        byte b;
        if (this.case) {
            b = this.else[n][this.byte];
        }
        else {
            b = this.else[n][this.null + this.f];
        }
        return b;
    }
}
