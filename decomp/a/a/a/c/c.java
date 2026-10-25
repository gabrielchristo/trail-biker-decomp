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
import a.a.a.e.a;
import javax.microedition.lcdui.CommandListener;

public class c implements CommandListener
{
    private int[] c;
    private long[] if;
    public int d;
    public int new;
    a byte;
    private c null;
    public boolean e;
    private List b;
    public int long;
    public int a;
    public byte goto;
    public f for;
    public a.a.a.c.a int;
    private Command do;
    private Command void;
    private Command f;
    protected boolean else;
    public boolean try;
    public int case;
    public boolean char;
    
    public c(final f for1) {
        super();
        this.for = for1;
        this.int = new a.a.a.c.a(for1, this);
        this.byte = new a("/res/levels.dat");
        this.for();
        this.byte();
        this.do = new Command("OK", 4, 0);
        this.void = new Command("Back", 2, 0);
        this.f = new Command("Exit", 7, 0);
        this.else = true;
        this.long = 1;
        this.b = null;
        this.int.goto();
        this.a();
    }
    
    private void for() {
        int n = 0;
        final byte b = this.byte.a[n++];
        this.c = new int[b];
        for (byte b2 = 0; b2 < b; ++b2) {
            this.c[b2] = n;
            n += 6 + (this.byte.a[n + 2] * 100 + this.byte.a[n + 2 + 1]) * (this.byte.a[n + 4] * 100 + this.byte.a[n + 4 + 1]);
        }
    }
    
    public int do(final int n) {
        final int n2 = this.c[n];
        return this.byte.a[n2 + 2] * 100 + this.byte.a[n2 + 2 + 1];
    }
    
    public int a(final int n) {
        final int n2 = this.c[n];
        return this.byte.a[n2 + 4] * 100 + this.byte.a[n2 + 4 + 1];
    }
    
    public int case() {
        return this.byte.a[0];
    }
    
    public int if() {
        return this.d;
    }
    
    public long if(final int n) {
        if (this.a == 2) {
            return this.int(n);
        }
        return this.for(n);
    }
    
    public long char() {
        return this.if(this.new);
    }
    
    public long int(final int n) {
        final int n2 = this.c[n];
        return (this.byte.a[n2 + 0] * 60 + this.byte.a[n2 + 0 + 1]) * 1000;
    }
    
    public long for(final int n) {
        return this.if[n];
    }
    
    public byte[][] do() {
        final int do1 = this.do(this.new);
        final int a = this.a(this.new);
        int n = this.c[this.new] + 2;
        final byte[][] array = new byte[a + 1][];
        array[0] = new byte[4];
        for (int i = 0; i < 4; ++i) {
            array[0][i] = this.byte.a[n++];
        }
        for (int j = 1; j <= a; ++j) {
            array[j] = new byte[do1];
            for (int k = 0; k < do1; ++k) {
                array[j][k] = this.byte.a[n++];
            }
        }
        return array;
    }
    
    public void a(final long n) {
        boolean b = false;
        if (n != -1L && n < this.if[this.new]) {
            this.if[this.new] = n;
            b = true;
        }
        if (this.a == 2 && this.new < 7 && this.int.x < this.int.I && n > 0L) {
            if (this.new == this.d - 1) {
                ++this.d;
            }
            b = true;
        }
        if (b) {
            this.goto();
        }
        this.else = true;
        this.int();
        if (this.a == 2 && this.int.x < this.int.I) {
            if (this.new < 7) {
                if (n > 0L) {
                    ++this.new;
                    this.int.new();
                }
                this.new();
            }
            else if (n > 0L) {
                this.long = 6;
                this.else();
            }
            else {
                this.new();
            }
        }
        else {
            this.long = 3;
            this.else();
        }
    }
    
    private void byte() {
        final byte b = this.byte.a[0];
        this.if = new long[b];
        this.d = 2;
        this.goto = 0;
        this.try = true;
        this.case = 100;
        for (byte b2 = 0; b2 < b; ++b2) {
            this.if[b2] = this.int(b2);
        }
        try {
            final RecordStore openRecordStore = RecordStore.openRecordStore("times", false);
            final byte[] array = new byte[b * 3 + 4];
            final byte[] nextRecord = openRecordStore.enumerateRecords((RecordFilter)null, (RecordComparator)null, false).nextRecord();
            openRecordStore.closeRecordStore();
            int n = 0;
            this.d = nextRecord[n++];
            if (this.d <= 0) {
                this.d = 1;
            }
            this.goto = nextRecord[n++];
            this.try = (nextRecord[n++] != 0);
            this.case = nextRecord[n++] * 100;
            for (byte b3 = 0; b3 < b; ++b3) {
                this.if[b3] = nextRecord[n++] * 1000 * 60;
                final long[] if1 = this.if;
                final byte b4 = b3;
                if1[b4] += nextRecord[n++] * 1000;
                final long[] if2 = this.if;
                final byte b5 = b3;
                if2[b5] += nextRecord[n++] * 10;
            }
        }
        catch (Exception ex) {}
    }
    
    private void goto() {
        try {
            RecordStore.deleteRecordStore("times");
        }
        catch (Exception ex) {}
        try {
            final byte b = this.byte.a[0];
            final RecordStore openRecordStore = RecordStore.openRecordStore("times", true);
            final byte[] array = new byte[b * 3 + 4];
            int n = 0;
            array[n++] = (byte)this.d;
            array[n++] = this.goto;
            array[n++] = (byte)(this.try ? 1 : 0);
            array[n++] = (byte)((this.case == 100) ? 1 : 0);
            for (byte b2 = 0; b2 < b; ++b2) {
                array[n++] = (byte)(this.if[b2] / 1000L / 60L);
                array[n++] = (byte)(this.if[b2] / 1000L % 60L);
                array[n++] = (byte)(this.if[b2] / 10L % 100L);
            }
            openRecordStore.addRecord(array, 0, n);
            openRecordStore.closeRecordStore();
        }
        catch (Exception ex2) {}
    }
    
    public void else() {
        this.b = null;
        System.gc();
        String str;
        if (a.a.a.c.f.a == 0) {
            str = "\r\n";
        }
        else {
            str = "\n";
        }
        switch (this.long) {
            case 1: {
                (this.b = new List("", 3)).append("Practice", (Image)null);
                this.b.append("Time Attack", (Image)null);
                this.b.append("Stunt Cup", (Image)null);
                this.b.append("High Scores", (Image)null);
                this.b.addCommand(this.f);
                break;
            }
            case 2: {
                this.b = new List("Select Course", 3);
                for (int if1 = this.if(), i = 0; i < if1; ++i) {
                    this.b.append("Course " + (i + 1), (Image)null);
                }
                this.b.addCommand(this.void);
                break;
            }
            case 3: {
                this.b = new List("", 3);
                if (!this.else) {
                    this.b.append("Continue", (Image)null);
                }
                this.b.append("New Game", (Image)null);
                this.b.append("Instructions", (Image)null);
                this.b.addCommand(this.void);
                break;
            }
            case 4: {
                final Form current = new Form("High Scores");
                final StringBuffer sb = new StringBuffer();
                if (this.goto != 0) {
                    sb.append("Best Cup:");
                    sb.append(str);
                    sb.append("    ");
                    switch (this.goto) {
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
                for (int if2 = this.if(), j = 0; j < if2; ++j) {
                    sb.setLength(0);
                    sb.append("Course");
                    sb.append(" ");
                    sb.append(j + 1);
                    sb.append(str);
                    sb.append("      ");
                    final int n = (int)(this.if[j] / 1000L / 60L);
                    sb.append(n / 10);
                    sb.append(n % 10);
                    sb.append(":");
                    final int n2 = (int)(this.if[j] / 1000L % 60L);
                    sb.append(n2 / 10);
                    sb.append(n2 % 10);
                    sb.append("'");
                    final int n3 = (int)(this.if[j] / 10L % 100L);
                    sb.append(n3 / 10);
                    sb.append(n3 % 10);
                    sb.append(str);
                    current.append(sb.toString());
                }
                current.addCommand(this.void);
                Display.getDisplay((MIDlet)this.for).setCurrent((Displayable)current);
                current.setCommandListener((CommandListener)this);
                break;
            }
            case 5: {
                final Form current2 = new Form("");
                String replace;
                if (a.a.a.c.f.a != 0) {
                    replace = "2 - Accelerate\r\n5 - Brake\r\n3 or 1 - Bunny hop/lift front wheel in the air\r\nDon't accelerate on logs and always land on two wheels or your back wheel to avoid losing your balance. \r\nCup Mode - Beat the set time on each course to advance to the next. You have four lives.\r\nPractice - Attempt to set a new record for a course. You have infinite lives.\r\nTime Attack Mode - Attempt to beat your best time for a course. You have infinite lives.\r\n".replace('\r', ' ');
                }
                else {
                    replace = "2 - Accelerate\r\n5 - Brake\r\n3 or 1 - Bunny hop/lift front wheel in the air\r\nDon't accelerate on logs and always land on two wheels or your back wheel to avoid losing your balance. \r\nCup Mode - Beat the set time on each course to advance to the next. You have four lives.\r\nPractice - Attempt to set a new record for a course. You have infinite lives.\r\nTime Attack Mode - Attempt to beat your best time for a course. You have infinite lives.\r\n";
                }
                current2.append(replace);
                current2.addCommand(this.void);
                Display.getDisplay((MIDlet)this.for).setCurrent((Displayable)current2);
                current2.setCommandListener((CommandListener)this);
                break;
            }
            case 6: {
                boolean b = false;
                final Form current3 = new Form("");
                String str2 = null;
                String str3 = null;
                Label_1078: {
                    switch (this.int.x) {
                        case 0: {
                            if (this.goto != 3) {
                                this.goto = 3;
                                b = true;
                            }
                            str2 = "Gold Cup Awarded!";
                            if (this.d < 11) {
                                switch (this.d) {
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
                                this.d = 11;
                                break;
                            }
                            break;
                        }
                        case 1:
                        case 2: {
                            if (this.goto != 3 && this.goto != 2) {
                                this.goto = 2;
                                b = true;
                            }
                            str2 = "Silver Cup Awarded!";
                            if (this.d < 10) {
                                switch (this.d) {
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
                                this.d = 10;
                                break;
                            }
                            break;
                        }
                        case 3:
                        case 4: {
                            if (this.goto == 0) {
                                this.goto = 1;
                                b = true;
                            }
                            str2 = "Bronze Cup Awarded!";
                            if (this.d >= 9) {
                                break;
                            }
                            switch (this.d) {
                                default: {
                                    str3 = "Course 9 unlocked.";
                                    b = true;
                                    this.d = 9;
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
                Display.getDisplay((MIDlet)this.for).setCurrent((Displayable)current3);
                if (b) {
                    this.goto();
                }
                current3.addCommand(this.void);
                current3.setCommandListener((CommandListener)this);
                break;
            }
        }
        if (this.b != null) {
            Display.getDisplay((MIDlet)this.for).setCurrent((Displayable)this.b);
            this.b.setCommandListener((CommandListener)this);
        }
    }
    
    public void commandAction(final Command command, final Displayable displayable) {
        if ((command == this.do || command == List.SELECT_COMMAND) && this.b != null) {
            int selectedIndex = this.b.getSelectedIndex();
            switch (this.long) {
                case 1: {
                    switch (selectedIndex) {
                        case 0: {
                            this.long = 2;
                            this.a = 0;
                            break;
                        }
                        case 1: {
                            this.long = 2;
                            this.a = 1;
                            break;
                        }
                        case 2: {
                            this.new = 0;
                            this.long = 3;
                            this.a = 2;
                            break;
                        }
                        case 3: {
                            this.long = 4;
                            break;
                        }
                    }
                    break;
                }
                case 2: {
                    this.new = selectedIndex;
                    this.long = 3;
                    break;
                }
                case 3: {
                    if (!this.else) {
                        --selectedIndex;
                    }
                    switch (selectedIndex) {
                        case 0: {
                            this.else = true;
                            this.int.if();
                            if (this.a == 2) {
                                this.new = 0;
                            }
                        }
                        case -1: {
                            this.new();
                            break;
                        }
                        case 1: {
                            this.long = 5;
                            break;
                        }
                    }
                }
            }
            this.else();
        }
        else if (command == this.void) {
            switch (this.long) {
                case 2: {
                    this.long = 1;
                    break;
                }
                case 3: {
                    if (this.a == 0 || this.a == 1) {
                        this.long = 2;
                    }
                    else {
                        this.long = 1;
                    }
                    this.else = true;
                    break;
                }
                case 5: {
                    this.long = 3;
                    break;
                }
                case 4:
                case 6:
                case 7: {
                    this.long = 1;
                    break;
                }
                default: {
                    this.int();
                    break;
                }
            }
            this.else();
        }
        else if (command == this.f) {
            this.for.getClass().getName();
            this.for.destroyApp(false);
        }
    }
    
    private void new() {
        this.long = 0;
        if (this.else) {
            this.int.a();
        }
        else {
            this.int.goto();
        }
        this.char = false;
        this.a();
        this.else = false;
    }
    
    protected void int() {
        this.int.do();
        if (this.long == 0) {
            this.long = 3;
        }
    }
    
    private void a() {
        Display.getDisplay((MIDlet)this.for).setCurrent((Displayable)this.int);
        this.b = null;
    }
    
    public void try() {
        if (!this.char) {
            this.char = true;
            this.int();
            this.else();
        }
    }
}
