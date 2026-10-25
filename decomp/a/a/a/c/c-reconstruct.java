package a.a.a.c;

import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordFilter;
import javax.microedition.rms.RecordStore;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;
import a.a.a.e.BinaryAsset;
import javax.microedition.lcdui.CommandListener;

public class LevelManager implements CommandListener
{
    private int[] levelRecordOffsets;
    private long[] bestTimesMs;
    public int unlockedCourseCount;
    public int selectedCourseIndex;
    BinaryAsset levelsAsset;
    private LevelManager unusedLevelRef;
    public boolean unusedFlagE;
    private List menuList;
    public int menuScreenId;
    public int gameMode;
    public byte bestCupTier;
    public TrailBikerMidlet midlet;
    public GameCanvas gameCanvas;
    private Command cmdOk;
    private Command cmdBack;
    private Command cmdExit;
    protected boolean hideContinueItem;
    public boolean soundEnabled;
    public int volumePercent;
    public boolean pauseOverlayActive;
    
    public LevelManager(final TrailBikerMidlet midlet) {
        super();
        this.midlet = midlet;
        this.gameCanvas = new GameCanvas(midlet, this);
        this.levelsAsset = new BinaryAsset("/res/levels.dat");
        this.parseLevelOffsets();
        this.loadProgressFromRms();
        this.cmdOk = new Command("OK", 4, 0);
        this.cmdBack = new Command("Back", 2, 0);
        this.cmdExit = new Command("Exit", 7, 0);
        this.hideContinueItem = true;
        this.menuScreenId = 1;
        this.menuList = null;
        this.gameCanvas.startGameLoopTimer();
        this.showCanvas();
    }
    
    private void parseLevelOffsets() {
        int n = 0;
        final byte b = this.levelsAsset.data[n++];
        this.levelRecordOffsets = new int[b];
        for (byte b2 = 0; b2 < b; ++b2) {
            this.levelRecordOffsets[b2] = n;
            n += 6 + (this.levelsAsset.data[n + 2] * 100 + this.levelsAsset.data[n + 2 + 1]) * (this.levelsAsset.data[n + 4] * 100 + this.levelsAsset.data[n + 4 + 1]);
        }
    }
    
    public int getLevelColumnCount(final int n) {
        final int n2 = this.levelRecordOffsets[n];
        return this.levelsAsset.data[n2 + 2] * 100 + this.levelsAsset.data[n2 + 2 + 1];
    }
    
    public int getLevelRowCount(final int n) {
        final int n2 = this.levelRecordOffsets[n];
        return this.levelsAsset.data[n2 + 4] * 100 + this.levelsAsset.data[n2 + 4 + 1];
    }
    
    public int getLevelSlotCount() {
        return this.levelsAsset.data[0];
    }
    
    public int getUnlockedLevelCount() {
        return this.unlockedCourseCount;
    }
    
    public long getTargetTimeMs(final int n) {
        if (this.gameMode == 2) {
            return this.getParTimeFromDat(n);
        }
        return this.getBestTimeMs(n);
    }
    
    public long getCurrentTargetTimeMs() {
        return this.getTargetTimeMs(this.selectedCourseIndex);
    }
    
    public long getParTimeFromDat(final int n) {
        final int n2 = this.levelRecordOffsets[n];
        return (this.levelsAsset.data[n2 + 0] * 60 + this.levelsAsset.data[n2 + 0 + 1]) * 1000;
    }
    
    public long getBestTimeMs(final int n) {
        return this.bestTimesMs[n];
    }
    
    public byte[][] buildLevelGrid() {
        final int do1 = this.getLevelColumnCount(this.selectedCourseIndex);
        final int a = this.getLevelRowCount(this.selectedCourseIndex);
        int n = this.levelRecordOffsets[this.selectedCourseIndex] + 2;
        final byte[][] array = new byte[a + 1][];
        array[0] = new byte[4];
        for (int i = 0; i < 4; ++i) {
            array[0][i] = this.levelsAsset.data[n++];
        }
        for (int j = 1; j <= a; ++j) {
            array[j] = new byte[do1];
            for (int k = 0; k < do1; ++k) {
                array[j][k] = this.levelsAsset.data[n++];
            }
        }
        return array;
    }
    
    public void onRaceFinished(final long n) {
        boolean b = false;
        if (n != -1L && n < this.bestTimesMs[this.selectedCourseIndex]) {
            this.bestTimesMs[this.selectedCourseIndex] = n;
            b = true;
        }
        if (this.gameMode == 2 && this.selectedCourseIndex < 7 && this.gameCanvas.stuntLivesLost < this.gameCanvas.stuntLivesTotal && n > 0L) {
            if (this.selectedCourseIndex == this.unlockedCourseCount - 1) {
                ++this.unlockedCourseCount;
            }
            b = true;
        }
        if (b) {
            this.saveProgressToRms();
        }
        this.hideContinueItem = true;
        this.pauseGameCanvas();
        if (this.gameMode == 2 && this.gameCanvas.stuntLivesLost < this.gameCanvas.stuntLivesTotal) {
            if (this.selectedCourseIndex < 7) {
                if (n > 0L) {
                    ++this.selectedCourseIndex;
                    this.gameCanvas.tickStuntCupLife();
                }
                this.enterPlayMode();
            }
            else if (n > 0L) {
                this.menuScreenId = 6;
                this.showMenuScreen();
            }
            else {
                this.enterPlayMode();
            }
        }
        else {
            this.menuScreenId = 3;
            this.showMenuScreen();
        }
    }
    
    private void loadProgressFromRms() {
        final byte b = this.levelsAsset.data[0];
        this.bestTimesMs = new long[b];
        this.unlockedCourseCount = 2;
        this.bestCupTier = 0;
        this.soundEnabled = true;
        this.volumePercent = 100;
        for (byte b2 = 0; b2 < b; ++b2) {
            this.bestTimesMs[b2] = this.getParTimeFromDat(b2);
        }
        try {
            final RecordStore openRecordStore = RecordStore.openRecordStore("times", false);
            final byte[] array = new byte[b * 3 + 4];
            final byte[] nextRecord = openRecordStore.enumerateRecords((RecordFilter)null, (RecordComparator)null, false).nextRecord();
            openRecordStore.closeRecordStore();
            int n = 0;
            this.unlockedCourseCount = nextRecord[n++];
            if (this.unlockedCourseCount <= 0) {
                this.unlockedCourseCount = 1;
            }
            this.bestCupTier = nextRecord[n++];
            this.soundEnabled = (nextRecord[n++] != 0);
            this.volumePercent = nextRecord[n++] * 100;
            for (byte b3 = 0; b3 < b; ++b3) {
                this.bestTimesMs[b3] = nextRecord[n++] * 1000 * 60;
                final long[] if1 = this.bestTimesMs;
                final byte b4 = b3;
                if1[b4] += nextRecord[n++] * 1000;
                final long[] if2 = this.bestTimesMs;
                final byte b5 = b3;
                if2[b5] += nextRecord[n++] * 10;
            }
        }
        catch (Exception ex) {}
    }
    
    private void saveProgressToRms() {
        try {
            RecordStore.deleteRecordStore("times");
        }
        catch (Exception ex) {}
        try {
            final byte b = this.levelsAsset.data[0];
            final RecordStore openRecordStore = RecordStore.openRecordStore("times", true);
            final byte[] array = new byte[b * 3 + 4];
            int n = 0;
            array[n++] = (byte)this.unlockedCourseCount;
            array[n++] = this.bestCupTier;
            array[n++] = (byte)(this.soundEnabled ? 1 : 0);
            array[n++] = (byte)((this.volumePercent == 100) ? 1 : 0);
            for (byte b2 = 0; b2 < b; ++b2) {
                array[n++] = (byte)(this.bestTimesMs[b2] / 1000L / 60L);
                array[n++] = (byte)(this.bestTimesMs[b2] / 1000L % 60L);
                array[n++] = (byte)(this.bestTimesMs[b2] / 10L % 100L);
            }
            openRecordStore.addRecord(array, 0, n);
            openRecordStore.closeRecordStore();
        }
        catch (Exception ex2) {}
    }
    
    public void showMenuScreen() {
        this.menuList = null;
        System.gc();
        String str;
        if (TrailBikerMidlet.deviceProfileId == 0) {
            str = "\r\n";
        }
        else {
            str = "\n";
        }
        switch (this.menuScreenId) {
            case 1: {
                (this.menuList = new List("", 3)).append("Practice", (Image)null);
                this.menuList.append("Time Attack", (Image)null);
                this.menuList.append("Stunt Cup", (Image)null);
                this.menuList.append("High Scores", (Image)null);
                this.menuList.addCommand(this.cmdExit);
                break;
            }
            case 2: {
                this.menuList = new List("Select Course", 3);
                for (int if1 = this.getUnlockedLevelCount(), i = 0; i < if1; ++i) {
                    this.menuList.append("Course " + (i + 1), (Image)null);
                }
                this.menuList.addCommand(this.cmdBack);
                break;
            }
            case 3: {
                this.menuList = new List("", 3);
                if (!this.hideContinueItem) {
                    this.menuList.append("Continue", (Image)null);
                }
                this.menuList.append("New Game", (Image)null);
                this.menuList.append("Instructions", (Image)null);
                this.menuList.addCommand(this.cmdBack);
                break;
            }
            case 4: {
                final Form current = new Form("High Scores");
                final StringBuffer sb = new StringBuffer();
                if (this.bestCupTier != 0) {
                    sb.append("Best Cup:");
                    sb.append(str);
                    sb.append("    ");
                    switch (this.bestCupTier) {
                        case 1: {
                            sb.append("Bronze");
                            break;
                        }
                        case 2: {
                            sb.append("Silver");
                            break;
                        }
                        case 3: {
                            sb.append("Gold");
                            break;
                        }
                    }
                    sb.append(str);
                    sb.append(str);
                    current.append(sb.toString());
                }
                for (int if2 = this.getUnlockedLevelCount(), j = 0; j < if2; ++j) {
                    sb.setLength(0);
                    sb.append("Course");
                    sb.append(" ");
                    sb.append(j + 1);
                    sb.append(str);
                    sb.append("      ");
                    final int n = (int)(this.bestTimesMs[j] / 1000L / 60L);
                    sb.append(n / 10);
                    sb.append(n % 10);
                    sb.append(":");
                    final int n2 = (int)(this.bestTimesMs[j] / 1000L % 60L);
                    sb.append(n2 / 10);
                    sb.append(n2 % 10);
                    sb.append("'");
                    final int n3 = (int)(this.bestTimesMs[j] / 10L % 100L);
                    sb.append(n3 / 10);
                    sb.append(n3 % 10);
                    sb.append(str);
                    current.append(sb.toString());
                }
                current.addCommand(this.cmdBack);
                Display.getDisplay((MIDlet)this.midlet).setCurrent((Displayable)current);
                current.setCommandListener((CommandListener)this);
                break;
            }
            case 5: {
                final Form current2 = new Form("");
                String replace;
                if (TrailBikerMidlet.deviceProfileId != 0) {
                    replace = "2 - Accelerate\r\n5 - Brake\r\n3 or 1 - Bunny hop/lift front wheel in the air\r\nDon't accelerate on logs and always land on two wheels or your back wheel to avoid losing your balance. \r\nCup Mode - Beat the set time on each course to advance to the next. You have four lives.\r\nPractice - Attempt to set a new record for a course. You have infinite lives.\r\nTime Attack Mode - Attempt to beat your best time for a course. You have infinite lives.\r\n".replace('\r', ' ');
                }
                else {
                    replace = "2 - Accelerate\r\n5 - Brake\r\n3 or 1 - Bunny hop/lift front wheel in the air\r\nDon't accelerate on logs and always land on two wheels or your back wheel to avoid losing your balance. \r\nCup Mode - Beat the set time on each course to advance to the next. You have four lives.\r\nPractice - Attempt to set a new record for a course. You have infinite lives.\r\nTime Attack Mode - Attempt to beat your best time for a course. You have infinite lives.\r\n";
                }
                current2.append(replace);
                current2.addCommand(this.cmdBack);
                Display.getDisplay((MIDlet)this.midlet).setCurrent((Displayable)current2);
                current2.setCommandListener((CommandListener)this);
                break;
            }
            case 6: {
                boolean b = false;
                final Form current3 = new Form("");
                String str2 = null;
                String str3 = null;
                Label_1078: {
                    switch (this.gameCanvas.stuntLivesLost) {
                        case 0: {
                            if (this.bestCupTier != 3) {
                                this.bestCupTier = 3;
                                b = true;
                            }
                            str2 = "Gold Cup Awarded!";
                            if (this.unlockedCourseCount < 11) {
                                switch (this.unlockedCourseCount) {
                                    default: {
                                        str3 = "Courses 9, 10 and 11 unlocked.";
                                        b = true;
                                        break;
                                    }
                                    case 9: {
                                        str3 = "Courses 10 and 11 unlocked.";
                                        b = true;
                                        break;
                                    }
                                    case 10: {
                                        str3 = "Course 11 unlocked.";
                                        b = true;
                                        break;
                                    }
                                }
                                this.unlockedCourseCount = 11;
                                break;
                            }
                            break;
                        }
                        case 1:
                        case 2: {
                            if (this.bestCupTier != 3 && this.bestCupTier != 2) {
                                this.bestCupTier = 2;
                                b = true;
                            }
                            str2 = "Silver Cup Awarded!";
                            if (this.unlockedCourseCount < 10) {
                                switch (this.unlockedCourseCount) {
                                    default: {
                                        str3 = "Courses 9 and 10 unlocked.";
                                        b = true;
                                        break;
                                    }
                                    case 9: {
                                        str3 = "Course 10 unlocked.";
                                        b = true;
                                        break;
                                    }
                                }
                                this.unlockedCourseCount = 10;
                                break;
                            }
                            break;
                        }
                        case 3:
                        case 4: {
                            if (this.bestCupTier == 0) {
                                this.bestCupTier = 1;
                                b = true;
                            }
                            str2 = "Bronze Cup Awarded!";
                            if (this.unlockedCourseCount >= 9) {
                                break;
                            }
                            switch (this.unlockedCourseCount) {
                                default: {
                                    str3 = "Course 9 unlocked.";
                                    b = true;
                                    this.unlockedCourseCount = 9;
                                    break Label_1078;
                                }
                            }
                            break;
                        }
                    }
                }
                final StringBuffer sb2 = new StringBuffer();
                if (str2 != null) {
                    sb2.append(str2);
                    sb2.append(str);
                    sb2.append(str);
                }
                if (str3 != null) {
                    sb2.append(str3);
                }
                current3.append(sb2.toString());
                Display.getDisplay((MIDlet)this.midlet).setCurrent((Displayable)current3);
                if (b) {
                    this.saveProgressToRms();
                }
                current3.addCommand(this.cmdBack);
                current3.setCommandListener((CommandListener)this);
                break;
            }
        }
        if (this.menuList != null) {
            Display.getDisplay((MIDlet)this.midlet).setCurrent((Displayable)this.menuList);
            this.menuList.setCommandListener((CommandListener)this);
        }
    }
    
    public void commandAction(final Command command, final Displayable displayable) {
        if ((command == this.cmdOk || command == List.SELECT_COMMAND) && this.menuList != null) {
            int selectedIndex = this.menuList.getSelectedIndex();
            switch (this.menuScreenId) {
                case 1: {
                    switch (selectedIndex) {
                        case 0: {
                            this.menuScreenId = 2;
                            this.gameMode = 0;
                            break;
                        }
                        case 1: {
                            this.menuScreenId = 2;
                            this.gameMode = 1;
                            break;
                        }
                        case 2: {
                            this.selectedCourseIndex = 0;
                            this.menuScreenId = 3;
                            this.gameMode = 2;
                            break;
                        }
                        case 3: {
                            this.menuScreenId = 4;
                            break;
                        }
                    }
                    break;
                }
                case 2: {
                    this.selectedCourseIndex = selectedIndex;
                    this.menuScreenId = 3;
                    break;
                }
                case 3: {
                    if (!this.hideContinueItem) {
                        --selectedIndex;
                    }
                    switch (selectedIndex) {
                        case 0: {
                            this.hideContinueItem = true;
                            this.gameCanvas.resetStuntLives();
                            if (this.gameMode == 2) {
                                this.selectedCourseIndex = 0;
                            }
                        }
                        case -1: {
                            this.enterPlayMode();
                            break;
                        }
                        case 1: {
                            this.menuScreenId = 5;
                            break;
                        }
                    }
                }
            }
            this.showMenuScreen();
        }
        else if (command == this.cmdBack) {
            switch (this.menuScreenId) {
                case 2: {
                    this.menuScreenId = 1;
                    break;
                }
                case 3: {
                    if (this.gameMode == 0 || this.gameMode == 1) {
                        this.menuScreenId = 2;
                    }
                    else {
                        this.menuScreenId = 1;
                    }
                    this.hideContinueItem = true;
                    break;
                }
                case 5: {
                    this.menuScreenId = 3;
                    break;
                }
                case 4:
                case 6:
                case 7: {
                    this.menuScreenId = 1;
                    break;
                }
                default: {
                    this.pauseGameCanvas();
                    break;
                }
            }
            this.showMenuScreen();
        }
        else if (command == this.cmdExit) {
            this.midlet.getClass().getName();
            this.midlet.destroyApp(false);
        }
    }
    
    private void enterPlayMode() {
        this.menuScreenId = 0;
        if (this.hideContinueItem) {
            this.gameCanvas.startLevel();
        }
        else {
            this.gameCanvas.startGameLoopTimer();
        }
        this.pauseOverlayActive = false;
        this.showCanvas();
        this.hideContinueItem = false;
    }
    
    protected void pauseGameCanvas() {
        this.gameCanvas.shutdownGameLoopTimer();
        if (this.menuScreenId == 0) {
            this.menuScreenId = 3;
        }
    }
    
    private void showCanvas() {
        Display.getDisplay((MIDlet)this.midlet).setCurrent((Displayable)this.gameCanvas);
        this.menuList = null;
    }
    
    public void handleMidletPause() {
        if (!this.pauseOverlayActive) {
            this.pauseOverlayActive = true;
            this.pauseGameCanvas();
            this.showMenuScreen();
        }
    }
}
