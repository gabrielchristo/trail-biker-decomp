package a.a.a.c;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Display;
import a.a.a.e.a;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

public class f extends MIDlet
{
    private List do;
    private c if;
    public a for;
    public static int a;
    
    public f() {
        super();
        this.for = new a("/res/metadata_mono.dat");
        try {
            final String property = System.getProperty("microedition.platform");
            if (property.startsWith("Nokia6310i")) {
                f.a = 0;
            }
            else if (property.startsWith("Nokia3410")) {
                f.a = 2;
            }
        }
        catch (Exception ex) {}
        this.if = new c(this);
    }
    
    public void startApp() {
    }
    
    public void pauseApp() {
        this.if.try();
        this.notifyPaused();
    }
    
    public void destroyApp(final boolean b) {
        this.if.int.do();
        Display.getDisplay((MIDlet)this).setCurrent((Displayable)null);
        this.if = null;
        this.for = null;
        System.gc();
        this.notifyDestroyed();
    }
    
    public static void a(final String s) {
    }
    
    static {
        f.a = 4;
    }
}
