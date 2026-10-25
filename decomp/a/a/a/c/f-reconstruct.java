package a.a.a.c;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Display;
import a.a.a.e.BinaryAsset;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

public class TrailBikerMidlet extends MIDlet
{
    private List unusedList;
    private LevelManager levelManager;
    public BinaryAsset gameMetadata;
    public static int deviceProfileId;
    
    public TrailBikerMidlet() {
        super();
        this.gameMetadata = new BinaryAsset("/res/metadata_mono.dat");
        try {
            final String property = System.getProperty("microedition.platform");
            if (property.startsWith("Nokia6310i")) {
                TrailBikerMidlet.deviceProfileId = 0;
            }
            else if (property.startsWith("Nokia3410")) {
                TrailBikerMidlet.deviceProfileId = 2;
            }
        }
        catch (Exception ex) {}
        this.levelManager = new LevelManager(this);
    }
    
    public void startApp() {
    }
    
    public void pauseApp() {
        this.levelManager.try();
        this.notifyPaused();
    }
    
    public void destroyApp(final boolean b) {
        this.levelManager.gameCanvas.do();
        Display.getDisplay((MIDlet)this).setCurrent((Displayable)null);
        this.levelManager = null;
        this.gameMetadata = null;
        System.gc();
        this.notifyDestroyed();
    }
    
    public static void noop(final String s) {
    }
    
    static {
        TrailBikerMidlet.deviceProfileId = 4;
    }
}
