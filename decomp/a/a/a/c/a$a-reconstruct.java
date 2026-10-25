package a.a.a.c;

import java.util.Timer;
import java.util.TimerTask;

protected class CanvasGameLoopTask extends TimerTask
{
    GameCanvas canvas;
    Timer timer;
    private final GameCanvas this$0;
    
    public CanvasGameLoopTask(final GameCanvas this$0, final GameCanvas canvas) {
        super();
        this.this$0 = this$0;
        this.canvas = canvas;
        (this.timer = new Timer()).schedule(this, 0L, 100L);
    }
    
    public void run() {
        this.canvas.onTimerTick();
    }
    
    void stop() {
        if (this.timer == null) {
            return;
        }
        this.cancel();
        this.timer.cancel();
        this.timer = null;
    }
}
