package a.a.a.c;

import com.nokia.mid.ui.DeviceControl;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import a.a.a.b.c;
import javax.microedition.lcdui.Image;
import a.a.a.b.d;
import a.a.a.b.b;
import javax.microedition.lcdui.Command;
import com.nokia.mid.ui.FullCanvas;

public class a extends FullCanvas implements Runnable
{
    public int a;
    protected int do;
    protected int M;
    private int try;
    protected boolean U;
    protected Command e;
    private a.a null;
    private int O;
    private int g;
    private int long;
    private int H;
    private int byte;
    private int k;
    private int S;
    private int i;
    private int G;
    private b L;
    private e p;
    private d K;
    private Image h;
    private boolean j;
    private boolean T;
    private boolean V;
    private boolean if;
    private int char;
    private int void;
    private int l;
    private int t;
    private int c;
    private int D;
    private int X;
    private boolean d;
    private int n;
    private int new;
    private int v;
    private int s;
    private int r;
    private int J;
    private int y;
    private int E;
    private a.a.a.a.a W;
    protected boolean q;
    private c int;
    private int C;
    private int b;
    private int z;
    private a.a.a.e.a P;
    private f B;
    private a.a.a.c.c o;
    private boolean case;
    private boolean A;
    private byte F;
    protected int goto;
    protected boolean R;
    protected int x;
    protected int I;
    private c w;
    private int else;
    private int for;
    private a.a.a.d.a Q;
    private Image N;
    private static final int u = 6;
    private String[] f;
    private int m;
    
    public a(final f b, final a.a.a.c.c o) {
        super();
        this.a = 0;
        this.do = 0;
        this.M = 0;
        this.try = 0;
        this.U = false;
        this.e = new Command("Dismiss", 1, 1);
        this.null = null;
        this.j = true;
        this.f = new String[6];
        this.B = b;
        this.o = o;
        this.P = b.for;
        this.do = super.getWidth();
        this.M = super.getHeight();
        if (a.a.a.c.f.a != 0 && a.a.a.c.f.a != 4) {
            ++this.do;
            ++this.M;
        }
        this.L = null;
        this.W = new a.a.a.a.a(false, 0L, true);
        try {
            this.w = new c(Image.createImage("/res/splash.png"), 1, 2, false);
        }
        catch (Exception ex) {
            System.out.println("Splash image load failed " + ex.toString());
        }
        this.else = 1;
    }
    
    protected void if(final Graphics graphics) {
        final int color = graphics.getColor();
        graphics.setColor(16777215);
        graphics.fillRect(0, 0, this.do, this.M);
        graphics.setColor(color);
    }
    
    protected void else() {
        if (this.try > 0) {
            --this.try;
            if (this.try == 0) {
                this.removeCommand(this.e);
                this.U = true;
                this.repaint();
            }
        }
        this.run();
    }
    
    protected void a(final Graphics graphics, int n, int n2, final int n3, final int n4, final String s, final int try1, final boolean b) {
        final Font font = Font.getFont(0, 0, 8);
        if (n == 0) {
            n = font.stringWidth(s) + 8;
        }
        if (n2 == 0) {
            n2 = font.getHeight() + 8;
        }
        final Image image = Image.createImage(n, n2);
        final Graphics graphics2 = image.getGraphics();
        graphics2.setFont(font);
        graphics2.setColor(0);
        final int n5 = n / 2;
        final int n6 = n2 / 2 - font.getHeight() / 2;
        graphics2.drawRoundRect(0, 0, n - 1, n2 - 1, 4, 4);
        graphics2.drawString(s, n5, n6, 17);
        if (b) {
            this.addCommand(this.e);
        }
        int n7;
        if (n3 == 0) {
            n7 = this.do / 2 - n / 2;
        }
        else {
            n7 = n3 - n / 2;
        }
        int n8;
        if (n4 == 0) {
            n8 = this.M / 2 - n2 / 2;
        }
        else {
            n8 = n4 - n2 / 2;
        }
        graphics.drawImage(image, n7, n8, 20);
        this.try = try1;
    }
    
    private void try() {
        this.p = new e((int)this.P.a[9], 0, this.P);
        try {
            if (a.a.a.c.f.a == 4) {
                this.for = 65280;
            }
            else {
                this.for = 16777215;
            }
            this.int = new c(Image.createImage("/res/tiles.png"), (int)this.P.a[6], (int)this.P.a[7], true);
            this.K = new d("/res/bg.png", (int)this.P.a[1], false);
            this.h = Image.createImage("/res/lives.png");
        }
        catch (Exception ex) {
            System.out.println("Image load failed " + ex.toString());
        }
        final int n = this.P.a[72] * this.P.a[73] + 2;
        int n2 = 72;
        final byte[] array = new byte[n];
        for (int i = 0; i < n; ++i) {
            array[i] = this.P.a[n2++];
        }
        final int n3 = this.P.a[n2 + 1] + 2 * this.P.a[n2] + 2;
        final int n4 = n3 + (this.P.a[n2 + n3] + 1);
        final byte[] array2 = new byte[n4];
        for (int j = 0; j < n4; ++j) {
            array2[j] = this.P.a[n2++];
        }
        this.L = new b(0, (int)this.P.a[4], this.do, (int)this.P.a[3], this.int, (int)this.P.a[5], (byte[][])null, false, array, array2, this.p.try / 2, this.for);
        this.T = true;
        this.Q = new a.a.a.d.a("/res/sound.dat");
    }
    
    private void null() {
        this.C = this.P.a[9];
        this.b = this.P.a[9];
        this.char = 0;
        this.r = 0;
        this.E = 0;
        this.l = 0;
        this.X = 0;
        this.q = false;
        this.F = 0;
        this.d = false;
        this.new = 0;
        this.g = 0;
        this.p.a();
        this.L.a(1);
        this.p.void = this.P.a[4] + this.L.e;
        this.byte = 0;
        this.s = this.P.a[65];
        this.y = this.P.a[66];
        this.void = this.P.a[61];
        this.c = this.P.a[11];
        this.D = this.P.a[10];
        this.t = this.P.a[62];
        if (a.a.a.c.f.a != 0) {
            this.s >>= 1;
            this.y >>= 1;
            this.void >>= 2;
            this.t >>= 2;
        }
        this.a(16);
        this.V = true;
    }
    
    public void run() {
        this.H = 0;
        this.A = false;
        if (this.else != -1) {
            if (this.w == null) {
                this.void();
            }
            else if (this.byte > 30) {
                switch (this.else) {
                    case 1: {
                        this.else = 0;
                        this.repaint();
                        break;
                    }
                    case 0: {
                        this.void();
                        break;
                    }
                }
                this.byte = 0;
            }
            else {
                ++this.byte;
            }
        }
        else {
            if (this.g != 0) {
                this.W.a(true);
                if (this.long > 20) {
                    this.long();
                    this.repaint();
                    if (this.g == 0 && !this.q) {
                        this.W.a(false);
                        this.j = true;
                    }
                }
                else {
                    ++this.long;
                }
            }
            if (this.R) {
                if (this.new > this.P.a[64]) {
                    this.R = false;
                }
                else {
                    ++this.new;
                }
            }
            else {
                this.W.do();
                this.p.new();
                Label_1680: {
                    if (this.case) {
                        if (this.X > this.P.a[63]) {
                            this.X = 0;
                            this.case = false;
                            this.d = true;
                            this.p.a();
                        }
                        else {
                            ++this.X;
                        }
                        if (!this.case && this.d) {
                            this.n = this.L.a(this.L.f - this.L.goto * 4, (byte)1, this.L.goto * 4);
                            this.n += this.P.a[8];
                            this.F = 1;
                        }
                    }
                    else {
                        if (!this.case && !this.q) {
                            if (this.d) {
                                this.v = this.s - 2 * this.P.a[71] / this.s;
                                this.J = this.y - 2 * this.P.a[71] / this.y;
                            }
                            else {
                                this.v = this.s - this.p.long / (5 * this.s);
                                this.J = this.y - this.p.long / (5 * this.y);
                            }
                            if (this.r > this.v) {
                                final int n = this.r - this.v;
                                if (this.d) {
                                    this.L.a(-n);
                                }
                                else {
                                    this.L.a(n);
                                }
                                this.r = 0;
                                this.H += 2;
                            }
                            ++this.r;
                            if (this.E > this.J) {
                                final int n2 = this.E - this.J;
                                if (this.d) {
                                    this.K.a(-n2);
                                }
                                else {
                                    this.K.a(n2);
                                }
                                this.E = 0;
                                ++this.H;
                            }
                            ++this.E;
                        }
                        if (this.d && this.L.f <= this.n) {
                            this.d = false;
                            this.p.void = this.P.a[4] + this.L.e;
                            this.p.case = false;
                        }
                        if (this.L.char != 10 && (this.o.a == 0 || this.W.int > 0L) && (this.o.a != 2 || this.x < this.I)) {
                            final long int1 = this.W.int;
                            final a.a.a.a.a w = this.W;
                            if (int1 < 599990L) {
                                if (!this.case && !this.d) {
                                    this.for();
                                }
                                if (this.A && !this.p.case) {
                                    this.p.a(0, false);
                                }
                                if (this.p.case) {
                                    if (this.l > this.t) {
                                        if (this.p.byte < 20) {
                                            final e p = this.p;
                                            ++p.byte;
                                        }
                                        this.l = 0;
                                    }
                                    if (this.char > this.void) {
                                        final e p2 = this.p;
                                        p2.for -= this.D;
                                        final e p3 = this.p;
                                        p3.void -= this.p.for;
                                        final int void1 = this.P.a[4] + this.L.e;
                                        if (this.p.void >= void1) {
                                            this.p.void = void1;
                                            this.p.case = false;
                                            int n3 = 0;
                                            switch (this.L.char) {
                                                case 3: {
                                                    n3 = 0;
                                                    break;
                                                }
                                                case 4: {
                                                    n3 = 16;
                                                    break;
                                                }
                                                default: {
                                                    n3 = 7;
                                                    break;
                                                }
                                            }
                                            n3 += 4;
                                            if (this.p.byte > n3 && !this.A && this.L.char != 9) {
                                                this.byte();
                                                if (this.o.try) {
                                                    this.Q.a((byte)1);
                                                }
                                            }
                                            else {
                                                switch (this.L.char) {
                                                    case 3: {
                                                        this.p.null = 0;
                                                        break;
                                                    }
                                                    case 4: {
                                                        this.p.null = 16;
                                                        break;
                                                    }
                                                    default: {
                                                        this.p.null = 7;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        this.char = 0;
                                    }
                                    ++this.char;
                                    ++this.l;
                                    break Label_1680;
                                }
                                this.p.void = this.P.a[4] + this.L.e;
                                switch (this.L.char) {
                                    case 1:
                                    case 5:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9: {
                                        this.p.null = 7;
                                        break Label_1680;
                                    }
                                    case 3: {
                                        this.p.null = 0;
                                        break Label_1680;
                                    }
                                    case 4: {
                                        this.p.null = 16;
                                        break Label_1680;
                                    }
                                }
                            }
                        }
                        if (!this.q) {
                            this.q = true;
                            this.W.a(true);
                            if (this.o.a == 2 && this.x >= this.I) {
                                ++this.g;
                                this.o.else = true;
                            }
                            else if (this.o.a != 0) {
                                if (this.W.int == 0L) {
                                    this.a(4);
                                }
                                else {
                                    if ((this.o.a == 1 && this.W.int > 0L) || (this.o.a == 2 && this.o.int(this.o.new) - this.W.int < this.o.for(this.o.new))) {
                                        this.a(2);
                                    }
                                    else {
                                        this.a(8);
                                    }
                                    if (this.o.try) {
                                        if (this.o.a == 2 && this.o.new == 8) {
                                            this.Q.a((byte)3);
                                        }
                                        else {
                                            this.Q.a((byte)2);
                                        }
                                    }
                                }
                            }
                            else {
                                final long int2 = this.W.int;
                                final a.a.a.a.a w2 = this.W;
                                if (int2 >= 599990L) {
                                    this.a(4);
                                }
                                else {
                                    if (this.W.int < this.o.char()) {
                                        this.a(2);
                                    }
                                    this.a(8);
                                    if (this.o.try) {
                                        this.Q.a((byte)2);
                                    }
                                }
                            }
                        }
                        if (this.g == 0) {
                            long int3 = this.W.int;
                            if (this.o.a == 2 && (this.x >= this.I || this.W.int == 0L)) {
                                int3 = -1L;
                            }
                            else if (this.o.a != 0) {
                                int3 = this.o.char() - int3;
                            }
                            this.o.a(int3);
                        }
                    }
                }
                final int n4 = this.p.void - this.p.g - (this.P.a[4] - this.P.a[2]) * 2;
                if (n4 <= this.P.a[4] && (n4 > this.P.a[2] || (this.H & 0x1) == 0x0)) {
                    this.H += 4;
                }
                if (this.H != 0) {
                    if ((this.H & 0x1) != 0x0) {
                        this.repaint(0, 0, this.do, (int)this.P.a[2]);
                    }
                    else {
                        this.repaint(0, 0, this.W.a(), this.W.if());
                    }
                    if ((this.H & 0x2) != 0x0) {
                        this.repaint(0, (int)this.P.a[4], this.do, this.P.a[3] + 1);
                    }
                    if ((this.H & 0x4) != 0x0) {
                        this.repaint(0, 0, this.p.try, (int)this.P.a[4]);
                    }
                }
            }
        }
    }
    
    private void a(final int n) {
        if ((this.g & n) == 0x0) {
            this.g += n;
            this.long = 0;
        }
    }
    
    private void long() {
        if (this.g != 0) {
            this.g -= this.int();
            this.long = 0;
        }
    }
    
    private int int() {
        int n = 0;
        if ((this.g & 0x10) != 0x0) {
            n = 16;
        }
        else if ((this.g & 0x4) != 0x0) {
            n = 4;
        }
        else if ((this.g & 0x8) != 0x0) {
            n = 8;
        }
        else if ((this.g & 0x2) != 0x0) {
            n = 2;
        }
        else if ((this.g & 0x1) != 0x0) {
            n = 1;
        }
        return n;
    }
    
    protected void paint(final Graphics graphics) {
        if (this.else != -1) {
            if (this.w != null) {
                graphics.setColor(0);
                graphics.fillRect(0, 0, this.do, this.M);
                this.w.a(this.else, graphics, 0, this.M / 2 - this.w.do / 2);
            }
        }
        else {
            if (this.j) {
                this.if(graphics);
                this.j = false;
            }
            if (this.V) {
                graphics.setColor(16777215);
                graphics.fillRect((int)this.P.a[9], 0, this.p.try, (int)this.P.a[4]);
                this.K.a(graphics, 0, 0);
                this.L.a(graphics);
                if (!this.d) {
                    this.p.a(graphics);
                }
                this.W.a(graphics, 0, 0);
                if (this.o.a == 2) {
                    this.a(graphics);
                }
                this.do(graphics);
            }
        }
    }
    
    private void do(final Graphics graphics) {
        String string = null;
        switch (this.int()) {
            case 4: {
                string = "Out of Time!";
                break;
            }
            case 2: {
                string = "New Best Time!";
                break;
            }
            case 1: {
                string = "Game Over";
                break;
            }
            case 8: {
                string = "Course Complete!";
                break;
            }
            case 16: {
                string = "Course " + (this.o.new + 1);
                break;
            }
        }
        if (string != null) {
            this.a(graphics, 0, 0, this.do / 2, this.M / 2, string, 0, false);
        }
    }
    
    void a(final Graphics graphics) {
        final int n = this.I - this.x - 1;
        final int n2 = this.h.getWidth() + 2;
        int n3 = this.do - n * n2;
        for (int i = 0; i < n; ++i) {
            graphics.drawImage(this.h, n3, 0, 20);
            n3 += n2;
        }
    }
    
    protected void keyPressed(final int n) {
        if (!this.case && !this.d && this.V) {
            switch (n) {
                case 53: {
                    if (!this.p.case) {
                        this.p.try();
                        break;
                    }
                    break;
                }
                case 50: {
                    if (this.p.case) {
                        break;
                    }
                    if (this.L.char != 5) {
                        this.p.for();
                        break;
                    }
                    this.byte();
                    if (this.o.try) {
                        this.Q.a((byte)0);
                        break;
                    }
                    break;
                }
                case 49:
                case 51: {
                    this.p.a(this.c, true);
                    this.l = 0;
                    break;
                }
                case -7:
                case -6:
                case -5:
                case 48: {
                    this.void();
                    break;
                }
            }
        }
        else {
            switch (n) {
                case -7:
                case -6:
                case -5:
                case 48: {
                    this.void();
                    break;
                }
            }
        }
    }
    
    private void void() {
        if (this.else != -1) {
            this.if = true;
            this.else = -1;
            this.w = null;
            if (!this.T) {
                this.try();
                this.null();
            }
            this.o.int();
            this.o.else();
        }
        else {
            this.o.try();
        }
    }
    
    protected void keyReleased(final int n) {
        if (this.V) {
            this.p.do();
        }
    }
    
    protected void keyRepeated(final int n) {
    }
    
    protected void char() {
    }
    
    private void for() {
        int n = 0;
        final int n2 = this.p.void - this.P.a[4];
        final int a = this.p.a(0);
        final int a2 = this.p.a(1);
        final int n3 = Math.abs(a - a2) / 2;
        final int n4 = this.p.try / 2;
        int n5 = this.L.a(this.L.f + n4, n2 - a, !this.p.case, false);
        if (n5 == 0) {
            n5 = this.L.a(this.L.f + n4, n2 - this.p.g, false, false);
        }
        final boolean b = (n5 & 0xFFFFFF80) != 0x0;
        if (b) {
            n5 = (byte)(n5 + 128);
            if (n5 == 0) {
                n5 = 6;
            }
        }
        byte f = this.L.a(this.L.f, n2 - n3, !this.p.case, false);
        if (f == 0) {
            f = this.L.a(this.L.f, n2 - this.p.g, false, false);
        }
        final boolean b2 = (f & 0xFFFFFF80) != 0x0;
        if (b2) {
            f += 128;
        }
        byte b3 = this.L.a(this.L.f - n4, n2 - a2, !this.p.case, false);
        if (b3 == 0) {
            b3 = this.L.a(this.L.f - n4, n2 - this.p.g, false, false);
        }
        final boolean b4 = (b3 & 0xFFFFFF80) != 0x0;
        if (b4) {
            b3 += 128;
        }
        if (n5 != 2 && n5 != 5 && n5 != 8 && n5 != 6 && (n5 != 4 || b3 != 0) && (n5 != 4 || b3 != 1) && (n5 != 4 || b3 != 5) && (n5 != 4 || b3 != 9)) {
            if (n5 == 11 && this.p.case) {}
        }
        else {
            n = (b ? 1 : 0);
        }
        if (n == 0) {
            if (f != 5 && f != 8 && f != 6) {
                if (f == 11 && this.p.case) {}
            }
            else {
                n = (b2 ? 1 : 0);
            }
        }
        if (n == 0) {
            if (b3 != 8) {
                if (b3 == 11 && this.p.case) {}
            }
            else {
                n = (b4 ? 1 : 0);
            }
        }
        if (n != 0) {
            this.byte();
            if (this.o.try) {
                this.Q.a((byte)1);
            }
        }
        else {
            if (!this.p.case && this.L.a(this.L.f + 2, n2, true, false) == 0 && this.L.a(this.L.f, n2 - n3, true, true) != 4) {
                this.A = true;
            }
            if (!this.p.case && n == 0 && (f == 9 || b3 == 9 || n5 == 9)) {
                this.p.long = this.p.char;
            }
        }
        this.F = f;
    }
    
    void a() {
        this.V = false;
        this.else = -1;
        final byte[][] do1 = this.o.do();
        if (this.L != null) {
            this.L.a(do1);
            this.L.if((int)this.P.a[8]);
        }
        this.case();
        this.R = true;
        this.new = 0;
        this.null();
        try {
            this.goto();
        }
        catch (Exception ex) {}
        this.repaint();
    }
    
    private void case() {
        final boolean b = this.o.a != 0;
        this.W.a(b, b ? this.o.char() : 0L, true);
    }
    
    public void if() {
        this.x = 0;
        this.I = this.P.a[14];
    }
    
    public void new() {
        if (this.I - this.x < this.P.a[15]) {
            --this.x;
        }
    }
    
    public void goto() {
        if (this.null != null) {
            return;
        }
        this.null = new a.a(this, this);
        DeviceControl.setLights(0, this.o.case);
        this.W.a(false);
        this.j = true;
    }
    
    public void do() {
        if (this.null == null) {
            return;
        }
        this.null.a();
        this.null = null;
        this.W.a(true);
    }
    
    private void byte() {
        if (!this.case && !this.d && this.p.c != 5) {
            this.case = true;
            this.p.if();
            ++this.x;
            this.repaint();
        }
    }
    
    protected void hideNotify() {
        if (this.if) {
            this.if = false;
        }
        else {
            this.B.pauseApp();
        }
    }
    
    public void a(final String s) {
    }
}
