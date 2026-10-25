package a.a.a.c;

import java.util.Timer;
import java.util.TimerTask;

protected class a extends TimerTask
{
    a.a.a.c.a a;
    Timer if;
    private final /* synthetic */ a.a.a.c.a this$0;
    
    public a(final a.a.a.c.a this$0, final a.a.a.c.a a) {
        super();
        this.this$0 = this$0;
        this.a = a;
        (this.if = new Timer()).schedule(this, 0L, 100L);
    }
    
    public void run() {
        this.a.else();
    }
    
    void a() {
        if (this.if == null) {
            return;
        }
        this.cancel();
        this.if.cancel();
        this.if = null;
    }
}
