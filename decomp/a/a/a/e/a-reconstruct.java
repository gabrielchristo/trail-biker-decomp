package a.a.a.e;

import java.io.InputStream;

public class BinaryAsset
{
    public static final int FORMAT_VERSION = 1;
    private String resourcePath;
    public byte[] data;
    
    public BinaryAsset(final String if1) {
        super();
        this.loadFromResource(this.resourcePath = if1);
    }
    
    public int getUnsignedByte(final int n) {
        final byte b = this.data[n];
        return (b < 0) ? (256 + b) : b;
    }
    
    public int getUInt16(final int n) {
        return 256 * this.getUnsignedByte(n) + this.getUnsignedByte(n + 1);
    }
    
    private void loadFromResource(final String s) {
        try {
            final InputStream resourceAsStream = this.getClass().getResourceAsStream(this.resourcePath);
            final int n = (resourceAsStream.read() << 8) + resourceAsStream.read();
            if (resourceAsStream.read() == 1) {
                this.data = new byte[n];
                for (int i = 0; i < n; ++i) {
                    this.data[i] = (byte)resourceAsStream.read();
                }
            }
            resourceAsStream.close();
        }
        catch (Exception ex) {
            throw new RuntimeException("load failed");
        }
    }
}
