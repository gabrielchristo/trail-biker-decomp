package a.a.a.c;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import a.a.a.e.BinaryAsset;
import a.a.a.b.SpriteSheet;

public class Motorbike
{
    public int bikeState;
    private int animationTick;
    public boolean isActive;
    private SpriteSheet bikeSprites;
    public int screenX;
    public int screenY;
    public int spriteWidth;
    public int spriteHeight;
    public int poseFrameIndex;
    public int minLean;
    public int maxLean;
    public int currentLean;
    public int pitchOffset;
    public int trickDriftX;
    public boolean inAirTrick;
    public int trickFrameIndex;
    public int unusedNew;
    public int poseVariant;
    public byte[][] wheelProbeTable;
    private BinaryAsset gameMetadata;
    
    public Motorbike(final int screenX, final int screenY, final BinaryAsset gameMetadata) {
        super();
        this.screenX = screenX;
        this.screenY = screenY;
        this.gameMetadata = gameMetadata;
        try {
            this.bikeSprites = new SpriteSheet(Image.createImage("/res/bikes.png"), (int)this.gameMetadata.data[16], (int)this.gameMetadata.data[17], true);
        }
        catch (Exception ex) {
            System.out.println("Failed to load bikes.png in Motorbike() constructor.");
        }
        this.minLean = this.gameMetadata.data[12];
        this.maxLean = this.gameMetadata.data[13];
        if (TrailBikerMidlet.deviceProfileId != 0) {
            --this.minLean;
            --this.maxLean;
        }
        this.spriteWidth = this.bikeSprites.frameWidth;
        this.spriteHeight = this.bikeSprites.frameHeight;
        this.loadWheelProbeTable();
        this.resetBike();
    }
    
    public void leanForward() {
        if (this.bikeState != 5) {
            if (this.currentLean < this.maxLean) {
                this.currentLean += 10;
            }
            this.bikeState = 1;
            this.animationTick = 0;
        }
    }
    
    public void setNeutral() {
        if (this.bikeState != 5) {
            this.bikeState = 0;
            this.animationTick = 0;
        }
    }
    
    public void leanBack() {
        if (this.bikeState != 5) {
            if (this.currentLean > this.minLean) {
                this.currentLean -= 10;
            }
            this.bikeState = 2;
            this.animationTick = 0;
        }
    }
    
    public void enterCrash() {
        this.bikeState = 5;
        this.tickAnimation();
        this.animationTick = 0;
    }
    
    public void tickAnimation() {
        ++this.animationTick;
        switch (this.bikeState) {
            case 0: {
                if (this.animationTick > this.gameMetadata.data[68]) {
                    if (this.currentLean > this.minLean) {
                        --this.currentLean;
                    }
                    this.animationTick = 0;
                    break;
                }
                break;
            }
            case 1: {
                if (this.animationTick > this.gameMetadata.data[67]) {
                    if (this.currentLean < this.maxLean) {
                        ++this.currentLean;
                    }
                    this.animationTick = 0;
                    break;
                }
                break;
            }
            case 2: {
                if (this.animationTick > this.gameMetadata.data[70]) {
                    if (this.currentLean > this.minLean) {
                        --this.currentLean;
                    }
                    this.animationTick = 0;
                    break;
                }
                break;
            }
            case 5: {
                this.poseFrameIndex = 21;
                this.poseVariant = 0;
                this.currentLean = 0;
                break;
            }
        }
    }
    
    public void paint(final Graphics graphics) {
        int spriteFrameIndex;
        if (this.bikeState == 5) {
            spriteFrameIndex = 21;
        }
        else if (this.inAirTrick) {
            spriteFrameIndex = this.trickFrameIndex;
        }
        else {
            spriteFrameIndex = this.poseFrameIndex + 3;
        }
        this.bikeSprites.drawFrame(spriteFrameIndex, graphics, this.screenX, this.screenY - this.spriteHeight);
    }
    
    public void advanceAirTrick(final int hopImpulse, final boolean resetTrickFrame) {
        if (!this.inAirTrick) {
            this.trickDriftX = hopImpulse;
            this.inAirTrick = true;
            if (resetTrickFrame) {
                this.trickFrameIndex = 0;
            }
            else {
                this.trickFrameIndex = this.poseFrameIndex + this.poseVariant;
            }
        }
        else if (this.trickFrameIndex > 0) {
            --this.trickFrameIndex;
        }
    }
    
    public void resetBike() {
        this.currentLean = this.minLean;
        this.pitchOffset = 0;
        this.bikeState = 0;
        this.isActive = true;
        this.animationTick = 0;
        this.poseFrameIndex = 7;
        this.poseVariant = 3;
        this.trickDriftX = 0;
        this.inAirTrick = false;
    }
    
    private void loadWheelProbeTable() {
        final int n = 21;
        this.wheelProbeTable = new byte[2][n];
        for (int i = 0; i < n; ++i) {
            this.wheelProbeTable[0][i] = this.gameMetadata.data[19 + i];
            this.wheelProbeTable[1][i] = this.gameMetadata.data[40 + i];
        }
    }
    
    public int getWheelProbeOffset(final int n) {
        byte b;
        if (this.inAirTrick) {
            b = this.wheelProbeTable[n][this.trickFrameIndex];
        }
        else {
            b = this.wheelProbeTable[n][this.poseFrameIndex + this.poseVariant];
        }
        return b;
    }
}
