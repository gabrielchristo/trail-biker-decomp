package a.a.a.e;

import java.io.InputStream;

public class a
{
    public static final int do = 1;
    private String if;
    public byte[] a;
    
    public a(final String if1) {
        super();
        this.a(this.if = if1);
    }
    
    public int a(final int n) {
        final byte b = this.a[n];
        return (b < 0) ? (256 + b) : b;
    }
    
    public int if(final int n) {
        return 256 * this.a(n) + this.a(n + 1);
    }
    
    private void a(final String s) {
        try {
            final InputStream resourceAsStream = this.getClass().getResourceAsStream(this.if);
            final int n = (resourceAsStream.read() << 8) + resourceAsStream.read();
            if (resourceAsStream.read() == 1) {
                this.a = new byte[n];
                for (int i = 0; i < n; ++i) {
                    this.a[i] = (byte)resourceAsStream.read();
                }
            }
            resourceAsStream.close();
        }
        catch (Exception ex) {
            throw new RuntimeException("load failed");
        }
    }
}
