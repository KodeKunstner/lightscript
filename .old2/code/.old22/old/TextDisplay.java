/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.microedition.lcdui.*;
/**
 *
 * @author voel
 */
public class TextDisplay implements Drawable {
    int x, y, w, h;
    int color;
    String strs[];
    Font f;
    int lineheight;
    TextDisplay(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w;
        f = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        lineheight = f.getHeight();
        strs = new String[h/lineheight];
        this.h = strs.length*lineheight;
        color = 0;
    }
    public void println(String s) {
        int i;
        for(i = 0; i< strs.length - 1; i++) {
            strs[i] = strs[i+1];
        }
        strs[i] = s;
    }
    public void draw(Graphics g) {
        g.setColor(color);
        for(int i = 0;i < strs.length; i++) {
            if(strs[i] != null) {
                g.drawString(strs[i], x, y+lineheight*i, Graphics.LEFT|Graphics.TOP);
            }
        }
    }
}
