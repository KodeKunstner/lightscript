/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.microedition.lcdui.*;
import com.nokia.mid.ui.FullCanvas;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Zui extends FullCanvas implements CommandListener, Runnable /*, Order */ {
    
    BitmapFont f;
    int w = 96;
    int h = 65;
    
    Display display;
    public Zui(Display disp) {
        try {
            InputStream is = this.getClass().getResourceAsStream("font8.png");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int i;
            while((i = is.read()) >= 0) {
                bos.write(i);
            }
            is.close();
            is = this.getClass().getResourceAsStream("font8.txt");
            int lengths[] = new int[256];
            int pos = 0;
            while((i = is.read()) >= 0) {
                if(i >= '0') {
                    lengths[pos] = i - '0';
                    ++pos;
                }
            }
            if(pos != 256) {
                throw new Error("wrong number of lengths in fonttext");
            }
            
            f = new BitmapFont(bos.toByteArray(), lengths);
            
            // Set up this canvas to listen to command events
	    setCommandListener(this);
        } catch(Exception e) {
            e.printStackTrace();
        }
        disp.setCurrent(this);
        this.display = disp;
    } 
    String names1[] = {
        "Timelog",
        "Notes",
        "People",
        "Email",
        "Sms",
        "Phone",
        "Calendar",
        "Reference",
        "Dictaphone"
    };
    String names2[] = {
        "Rasmus",
        "MariAnne",
        "Kirsten",
        "Niels",
        "Katrine",
        "Johan",
        "Jakob",
        "Anne",
        "Karen",
    };
    String names[] = names1;
    int time = 0;
    int quadrant = 3;
    int rootcolor = 0;
    private void drawBoxes(Graphics g, String[] labels, int x0, int y0, int w0, int h0, boolean highcolor) {
        int w = w0 / 4;
        int h = h0 / 4;
        int dx = (w0 / 3 - w) / 2;
        int dy = (h0 / 3 - h) / 2;
        for(int i = 0; i < 9 ; i++) {
            int x = x0 + (i % 3) * w0 / 3 + dx; 
            int y = y0 + (i / 3) * h0 / 3 + dy;
                g.setColor(labels[i].hashCode() | 0xc0c0c0);
            g.fillRect(x, y, w, h);
        }
    }
    public void paint(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setClip(0,0, 96, 65);
        int x0, y0, xpos, ypos, w, h;
        int deltatime = (int) System.currentTimeMillis() - time;
        if(time != 0 && deltatime > 512) {
            time = 0;
            rootcolor = names[quadrant].hashCode();
            names = names2;
            names2 = names1;
            names1 = names;
            
        }
        if(time != 0) {
            w = 96 + deltatime * (3 * 96 * 2 - 96) / 512;
            h = 65 + deltatime * (3 * 2 * 65 - 65) / 512;
            x0 = deltatime * ((quadrant % 3) * 2 * -96 - 95 * 6 / 12) / 512;
            y0 = deltatime * ((quadrant / 3) * 2 * -65 - 65 * 6 / 12) / 512;
        } else {
            x0 = 0;
            y0 = 0;
            w = 96;
            h = 65;
        }
        g.setColor(rootcolor| 0xc0c0c0);
            
        g.fillRect(0, 0, 96, 65);

        this.drawBoxes(g, names, x0, y0, w, h, time != 0);
        if(time != 0) {
            int qx = x0 + (quadrant % 3) * w/3 + w/12;
            int qy = y0 + (quadrant / 3) * h/3 + h/12;
            this.drawBoxes(g, names2, qx, qy, w/6, h/6, false);
        }
        
        ypos = y0 + 1;
        xpos = x0;
        if(time==0) {
        for(int i = 0; i< 9; i++) {
            ypos = y0 + 1 + (1 + 3*(i/3)) * (h - 3) / 9;
            xpos = x0 + (i%3) * w/3 - w/3  + (w - f.length(names[i])) / 2;
            f.draw(g, xpos, ypos, names[i]);
        }
        
        } else {
            display.callSerially(this);
        }
                                   
        
    }
    
    /**
     * Called when a key is pressed.
     */
    protected  void keyPressed(int keyCode) {
        
        switch(keyCode) {
            default:
                quadrant = keyCode - Zui.KEY_NUM1;
                time = (int) System.currentTimeMillis();
                break;
        }
        this.repaint();
        /*
        switch(keyCode) {
            case Zui.KEY_NUM1:
                scale = (scale * 5) / 4 + 1;
                break;
            case Zui.KEY_NUM3:
                scale = (scale * 4) / 5 + 1;
                break;
            case Zui.KEY_NUM2:
                y0  += (1<< 19) / scale + 1;
                break;
            case Zui.KEY_NUM5:
                y0  -= (1<< 19) / scale + 1;
                break;
            case Zui.KEY_NUM4:
                x0  += (1<< 19) / scale + 1;
                break;
            case Zui.KEY_NUM6:
                x0  -= (1<< 19) / scale + 1;
                break;
        }
         */
        this.repaint();
    }
    
    /**
     * Called when a key is released.
     */
    protected  void keyReleased(int keyCode) {
    }

    /**
     * Called when a key is repeated (held down).
     */
    protected  void keyRepeated(int keyCode) {
    }
    
    /**
     * Called when the pointer is dragged.
     */
    protected  void pointerDragged(int x, int y) {
    }

    /**
     * Called when the pointer is pressed.
     */
    protected  void pointerPressed(int x, int y) {
    }

    /**
     * Called when the pointer is released.
     */
    protected  void pointerReleased(int x, int y) {
    }
    
    /**
     * Called when action should be handled
     */
    public void commandAction(Command command, Displayable displayable) {
    }

    public void run() {
        this.repaint();
    }
}
