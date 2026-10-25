package a.a.a.b;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class a
{
    private int goto;
    private int char;
    private int m;
    private int l;
    private int do;
    private int j;
    private int byte;
    private boolean if;
    private int b;
    private Image g;
    private Graphics long;
    private int a;
    private int f;
    private int d;
    private int null;
    private int new;
    private int int;
    private int o;
    private int n;
    private int k;
    private int else;
    private int h;
    private int void;
    public static final int case = 128;
    public static final byte for = -1;
    public byte[][] i;
    private c e;
    private Image p;
    private boolean try;
    private int c;
    
    public a(final int o, final int n, final int k, final int n2, final c e, final int else1, final byte[][] i, final int h, final int void1, final boolean try1, final int c) {
        super();
        this.o = o;
        this.n = n;
        this.k = k;
        this.e = e;
        this.i = i;
        this.h = h;
        this.void = void1;
        this.else = else1;
        this.try = try1;
        this.new = this.k / this.else + 2;
        if (this.k % this.else > 0) {
            ++this.new;
        }
        this.a = this.new * this.else;
        this.int = n2 / this.else;
        this.f = this.int * this.else;
        this.m = this.new;
        this.l = this.int;
        this.c = c;
        this.do(0, 0);
    }
    
    private void a(final int n, final int n2, final int n3, final int n4) {
        if (this.i != null) {
            switch (this.i[n2][n]) {
                case 0: {
                    this.long.setColor(this.c);
                    this.long.fillRect(n3, n4, this.else, this.else);
                    break;
                }
                default: {
                    this.e.a(this.i[n2][n] - 1, this.long, n3, n4);
                    break;
                }
            }
        }
    }
    
    private void a() {
        for (int i = 0; i < this.new; ++i) {
            for (int j = 0; j < this.int; ++j) {
                this.a(this.goto + i, this.char + j, i * this.else, j * this.else);
            }
        }
    }
    
    private void a(final boolean b) {
        if (this.i != null) {
            int goto1 = this.goto;
            int n = this.char;
            for (int i = 0; i < this.new; ++i) {
                if (i * this.else >= this.do && goto1 >= this.goto) {
                    goto1 = this.m - this.new;
                }
                for (int j = 0; j < this.int; ++j) {
                    if ((this.i[n][goto1] & 0x80) != 0x0) {
                        final byte[] array = this.i[n];
                        final int n2 = goto1;
                        array[n2] -= 128;
                        if (b) {
                            this.a(goto1, n, i * this.else, j * this.else);
                        }
                    }
                    ++n;
                }
                n = this.char;
                ++goto1;
            }
        }
    }
    
    public boolean if(final int n, final int n2) {
        if (this.j < 0) {
            this.j += this.a;
        }
        if (this.j > this.do && this.j <= this.do + this.else) {
            if (this.goto + this.do / this.else >= this.h && this.try) {
                this.goto = 0;
                this.m = this.new;
                this.do = this.a;
            }
            if (this.goto + this.do / this.else >= this.h) {
                if (this.if) {
                    this.if = false;
                    this.b = n;
                }
                this.byte -= n2;
                this.j -= n2;
                if (this.j < 0) {
                    this.j += this.a;
                }
            }
            else {
                if (this.do >= this.a) {
                    this.do = 0;
                    this.goto += this.new;
                }
                if (this.j >= this.a) {
                    this.j -= this.a;
                }
                final int do1 = this.do;
                this.do += this.else;
                ++this.m;
                for (int i = 0; i < this.int; ++i) {
                    this.a(this.goto + do1 / this.else, this.char + i, do1, i * this.else);
                }
            }
        }
        if (this.byte >= this.a) {
            this.byte -= this.a;
        }
        if (this.byte < 0) {
            this.byte += this.a;
        }
        if (this.byte < this.do && this.byte >= this.do - this.else) {
            if (this.goto - (this.new - this.do / this.else) <= 0) {
                if (this.if) {
                    this.if = false;
                    this.b = n;
                }
                this.byte -= n2;
                this.j -= n2;
                if (this.byte >= this.a) {
                    this.byte -= this.a;
                }
            }
            else {
                this.do -= this.else;
                final int do2 = this.do;
                --this.m;
                if (this.do <= 0) {
                    this.do = this.a;
                    this.goto -= this.new;
                }
                for (int j = 0; j < this.int; ++j) {
                    this.a(this.m - this.new, this.char + j, do2, j * this.else);
                }
            }
        }
        return this.if;
    }
    
    public boolean a(final int n, final int n2) {
        if (!this.if) {
            if (this.goto - (this.new - this.do / this.else) <= 0 && n >= this.b && n < this.b + 5) {
                this.if = true;
            }
            if (this.goto + this.do / this.else >= this.h && n <= this.b && n > this.b - 5) {
                this.if = true;
            }
        }
        if (this.if) {
            this.byte += n2;
            this.j += n2;
        }
        return this.if;
    }
    
    public void if(final boolean b) {
        this.a(false);
        if (b) {
            this.char -= this.int;
            this.l -= this.int;
        }
        else {
            this.char += this.int;
            this.l += this.int;
        }
        if (!this.if && this.goto - (this.new - this.do / this.else) == 0) {
            this.goto = 0;
            this.byte = 0;
            this.j = this.k;
            this.b = this.k / 2;
        }
        else if (!this.if) {
            this.goto = this.h - this.new;
            this.byte = this.a - this.k;
            this.j = this.a;
            this.b = this.a - this.k / 2;
        }
        else {
            this.byte = this.d;
            this.j = this.null;
        }
        this.m = this.goto + this.new;
        this.do = this.a;
        this.a();
    }
    
    public void a(final Graphics graphics) {
        if (this.byte < this.j) {
            graphics.drawImage(this.g, this.o - this.byte, this.n, 20);
        }
        else {
            graphics.drawImage(this.g, this.o - this.byte, this.n, 20);
            graphics.drawImage(this.g, this.o + this.a - this.byte, this.n, 20);
        }
    }
    
    public byte for(final int n, final int n2) {
        byte b = -1;
        if (n != -1 && n2 != -1 && this.i != null) {
            b = this.i[n2][n];
        }
        return b;
    }
    
    public int if(final int n) {
        int n2 = n / this.else;
        if (n2 < 0 || n2 >= this.h) {
            n2 = -1;
        }
        return n2;
    }
    
    public int a(final int n) {
        int n2 = n / this.else;
        if (n2 < 0 || n2 >= this.void) {
            n2 = -1;
        }
        return n2;
    }
    
    public void a(final byte[][] i, final int h, final int void1) {
        this.i = null;
        System.gc();
        if (this.g == null) {
            this.g = Image.createImage(this.a, this.f);
            this.long = this.g.getGraphics();
        }
        else {
            this.long.setColor(16777215);
            this.long.fillRect(0, 0, this.a, this.f);
        }
        this.i = i;
        this.h = h;
        this.void = void1;
        this.m = this.new;
        this.l = this.int;
        this.do(0, 0);
    }
    
    public void do(final int d, final int n) {
        this.d = d;
        this.null = this.a - this.else;
        this.if = true;
        this.do = this.a;
        this.j = this.null;
        this.byte = this.d;
        this.goto = d / this.else;
        this.char = n / this.else;
        this.a();
    }
}
