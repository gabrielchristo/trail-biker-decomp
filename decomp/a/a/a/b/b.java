package a.a.a.b;

import javax.microedition.lcdui.Graphics;

public class b
{
    public static final int try = 0;
    public static final int case = 0;
    a d;
    int for;
    int g;
    public int goto;
    int byte;
    public int e;
    public int f;
    private int if;
    public byte void;
    public byte char;
    public int do;
    byte[][] null;
    byte[][][] int;
    byte[][] i;
    int c;
    int long;
    int new;
    int h;
    int else;
    byte[] b;
    public static final byte a = Byte.MIN_VALUE;
    
    public b(final int n, final int n2, final int n3, final int n4, final c c, final int goto1, final byte[][] array, final boolean b, final byte[] array2, final byte[] array3, final int n5, final int n6) {
        super();
        this.goto = goto1;
        this.a(array2);
        this.if(array3);
        this.if(array);
        this.f = n5;
        this.if = n5;
        this.void = 0;
        this.char = 0;
        this.d = new a(n, n2, n3, n4, c, goto1, this.i, this.c, this.long, b, n6);
    }
    
    private void a(final byte[] array) {
        int n = 0;
        final byte b = array[n++];
        this.for = this.goto / b;
        this.g = array[n++];
        this.null = new byte[this.g][b];
        for (int i = 0; i < this.g; ++i) {
            for (byte b2 = 0; b2 < b; ++b2) {
                this.null[i][b2] = array[n++];
            }
        }
    }
    
    private void if(final byte[] array) {
        int n = 0;
        this.byte = array[n++];
        final byte b = array[n++];
        this.int = new byte[this.byte][][];
        for (int i = 0; i < this.byte; ++i) {
            final byte b2 = array[n++];
            final byte b3 = array[n++];
            this.int[i] = new byte[b3 + 1][];
            (this.int[i][0] = new byte[2])[0] = b2;
            this.int[i][0][1] = b3;
            for (byte b4 = 1; b4 <= b3; ++b4) {
                this.int[i][b4] = new byte[b2];
                for (byte b5 = 0; b5 < b2; ++b5) {
                    this.int[i][b4][b5] = array[n++];
                }
            }
        }
        this.else = array[n++];
        this.b = new byte[this.else];
        for (int j = 0; j < this.else; ++j) {
            this.b[j] = array[n++];
        }
    }
    
    void if(final byte[][] array) {
        this.c = 0;
        this.long = 0;
        this.new = 0;
        this.h = 0;
        this.i = null;
        System.gc();
        if (array != null) {
            this.new = array[0][0] * 100 + array[0][1];
            this.h = array[0][2] * 100 + array[0][3];
            for (int i = 0; i < this.new; ++i) {
                int long1 = 0;
                for (int j = 1; j < this.h + 1; ++j) {
                    if (array[j][i] != 0) {
                        long1 += this.int[array[j][i] - 1][0][1];
                    }
                }
                if (long1 > this.long) {
                    this.long = long1;
                }
            }
            for (int k = 1; k < this.h + 1; ++k) {
                int c = 0;
                for (int l = 0; l < this.new; ++l) {
                    if (array[k][l] != 0) {
                        c += this.int[array[k][l] - 1][0][0];
                    }
                }
                if (c > this.c) {
                    this.c = c;
                }
            }
            this.i = new byte[this.long][this.c];
            int n = 0;
            final int n2 = this.long - 1;
            for (int n3 = 0; n3 < this.new; ++n3) {
                int n4 = this.long - 1;
                int n5 = 0;
                for (int n6 = this.h - 1; n6 >= 0; --n6) {
                    final byte b = array[n6 + 1][n3];
                    if (b != 0) {
                        final byte b2 = this.int[b - 1][0][0];
                        final byte b3 = this.int[b - 1][0][1];
                        for (byte b4 = 0; b4 < b3; ++b4) {
                            for (byte b5 = 0; b5 < b2; ++b5) {
                                this.i[n4 - b4][n + b5] = this.int[b - 1][b3 - b4][b5];
                            }
                        }
                        n4 -= b3;
                        n5 = b2;
                    }
                }
                n += n5;
            }
            this.do = this.long * this.goto;
        }
    }
    
    public byte a(final int n, final int n2) {
        return this.d.for(this.d.if(n), this.d.a(n2));
    }
    
    public byte a(final byte b, final byte b2, final byte b3) {
        byte b4 = 0;
        if (b2 != 0) {
            for (int i = 0; i < this.byte; i = (byte)(i + 1)) {
                final byte b5 = this.int[i][0][0];
                for (byte b6 = this.int[i][0][1], b7 = 1; b7 <= b6; ++b7) {
                    for (byte b8 = 0; b8 < b5; ++b8) {
                        boolean b9 = false;
                        boolean b10 = false;
                        boolean b11 = false;
                        if (b8 == 0 || this.int[i][b7][b8 - 1] == b) {
                            b9 = true;
                        }
                        if (this.int[i][b7][b8] == b2) {
                            b10 = true;
                        }
                        if (b8 == b5 - 1 || this.int[i][b7][b8 + 1] == b3) {
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
    
    public int a(final int n, final byte b) {
        int n2 = 0;
        if (b != 0) {
            n2 = this.null[b - 1][n % this.goto / this.for];
        }
        return n2;
    }
    
    public void a(int n) {
        final int f = this.f;
        final int e = this.e;
        this.e = 0;
        this.void = 0;
        if (this.f + n < this.if) {
            n = this.if - this.f;
        }
        this.f += n;
        while (this.void == 0 && this.e < this.do) {
            this.void = this.a(this.f, this.e);
            if (this.a(this.void)) {
                this.void = 0;
            }
            if (this.void == 0) {
                this.e += this.goto;
            }
        }
        if (this.void != 0) {
            this.char = this.a(this.a(this.f - this.goto, this.e), this.void, this.a(this.f + this.goto, this.e));
            this.e = this.d.a(this.e) * this.goto + this.a(this.f, this.void);
        }
        this.d.a(0, n);
        this.d.if(0, n);
    }
    
    public void a(final Graphics graphics) {
        this.d.a(graphics);
    }
    
    public byte a(final int n, final int n2, final boolean b, final boolean b2) {
        byte a = 0;
        boolean b3 = false;
        int n3 = 0;
        if (n >= 0 && n2 >= 0) {
            byte a2 = 0;
            while (a2 == 0 && n3 < this.do) {
                a2 = this.a(n, n3);
                if (b && this.a(a2)) {
                    a2 = 0;
                }
                if (a2 == 0) {
                    n3 += this.goto;
                }
                else {
                    if (n2 <= n3 + this.goto) {
                        continue;
                    }
                    n3 += this.goto;
                    a2 = 0;
                }
            }
            if (a2 != 0) {
                final int n4 = this.d.a(n3) * this.goto + this.a(n, a2);
                if (n2 >= n4 || b2) {
                    a = this.a(this.a(n - this.goto, n4), a2, this.a(n + this.goto, n4));
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
    
    public void a(final byte[][] array) {
        this.if(array);
        this.d.a(this.i, this.c, this.long);
    }
    
    public void if(final int f) {
        this.f = f;
        this.void = 0;
        this.char = 0;
        this.d.do(f - this.if, 0);
    }
    
    private boolean a(final byte b) {
        boolean b2 = false;
        if (b != 0) {
            for (int i = 0; i < this.else; ++i) {
                if (b == this.b[i]) {
                    b2 = true;
                    break;
                }
            }
        }
        return b2;
    }
    
    public int a(int n, final byte b, final int n2) {
        byte b2;
        byte b3;
        byte b4;
        for (b2 = this.a(n - n2, 0, true, true), b3 = this.a(n - n2 / 2, 0, true, true), b4 = this.a(n, 0, true, true); (b2 == 0 || b3 == 0 || b4 == 0) && n > 0; --n, b2 = this.a(n - n2, 0, true, true), b3 = this.a(n - n2 / 2, 0, true, true), b4 = this.a(n, 0, true, true)) {}
        if (b2 != 0 && b3 != 0 && b4 != 0) {
            int n3;
            for (n3 = n - this.int[b2][0][0]; (b2 != b || b3 != b || b4 != b) && n3 > 0; n3 -= this.goto, b2 = this.a(n3 - n2, 0, true, true), b3 = this.a(n3 - n2 / 2, 0, true, true), b4 = this.a(n3, 0, true, true)) {}
            int n4 = n3 - n2;
            if (n4 < 0) {
                n4 = 0;
            }
            return n4;
        }
        return (n - n2 > 0) ? (n - n2) : 0;
    }
}
