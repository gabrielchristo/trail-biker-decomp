package a.a.a.d;

import com.nokia.mid.sound.Sound;

public class a
{
    Sound a;
    protected byte[][] for;
    private final int if;
    private final int do;
    
    public a(final String s) {
        super();
        this.if = 0;
        this.do = 1;
        this.a(s);
        this.a = null;
    }
    
    protected void a(final String s) {
        final a.a.a.e.a a = new a.a.a.e.a(s);
        final byte b = a.a[0];
        this.for = new byte[b][];
        for (byte b2 = 0; b2 < b; ++b2) {
            final int if1 = a.if(1 + b2 * 2);
            int n;
            if (b2 == b - 1) {
                n = a.a.length;
            }
            else {
                n = a.if(1 + (b2 + 1) * 2);
            }
            final int n2 = n - if1;
            this.for[b2] = new byte[n2];
            for (int i = 0; i < n2; ++i) {
                this.for[b2][i] = a.a[if1 + i];
            }
        }
    }
    
    public void a(final byte b) {
        if (this.a == null) {
            this.a = new Sound(this.for[b], 1);
        }
        else {
            this.a.init(this.for[b], 1);
        }
        this.a.setGain(255);
        this.a.play(1);
    }
}
