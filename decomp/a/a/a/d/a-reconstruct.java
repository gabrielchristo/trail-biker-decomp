package a.a.a.d;

import com.nokia.mid.sound.Sound;
import a.a.a.e.BinaryAsset;

public class SoundManager
{
    Sound nokiaSound;
    protected byte[][] sampleData;
    private final int reservedZero;
    private final int soundDataType;
    
    public SoundManager(final String s) {
        super();
        this.reservedZero = 0;
        this.soundDataType = 1;
        this.loadSoundsFromResource(s);
        this.nokiaSound = null;
    }
    
    protected void loadSoundsFromResource(final String s) {
        final BinaryAsset a = new BinaryAsset(s);
        final byte b = a.data[0];
        this.sampleData = new byte[b][];
        for (byte b2 = 0; b2 < b; ++b2) {
            final int if1 = a.getUInt16(1 + b2 * 2);
            int n;
            if (b2 == b - 1) {
                n = a.data.length;
            }
            else {
                n = a.getUInt16(1 + (b2 + 1) * 2);
            }
            final int n2 = n - if1;
            this.sampleData[b2] = new byte[n2];
            for (int i = 0; i < n2; ++i) {
                this.sampleData[b2][i] = a.data[if1 + i];
            }
        }
    }
    
    public void playSound(final byte b) {
        if (this.nokiaSound == null) {
            this.nokiaSound = new Sound(this.sampleData[b], 1);
        }
        else {
            this.nokiaSound.init(this.sampleData[b], 1);
        }
        this.nokiaSound.setGain(255);
        this.nokiaSound.play(1);
    }
}
