/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.microedition.lcdui.*;
/**
 *
 * @author voel
 */
final class GUI extends Canvas {
    public Object drawables[];
    public Yolan keydown[];
    public Yolan keyup[];
    public Yolan keyrepeat[];

    public GUI() {
        keydown = new Yolan[13];
        keyup = new Yolan[13];
        keyrepeat = new Yolan[13];
        drawables = new Object[0];
    }
    
    private void keycall(Yolan fn[], int i) {
        if(Canvas.KEY_NUM0 <= i && i <= Canvas.KEY_NUM9) {
            i -= Canvas.KEY_NUM0;
        } else if(i == Canvas.KEY_POUND) {
            i = 10;
        } else if(i == Canvas.KEY_STAR) {
            i = 11;
        } else {
            i = 12;
        }
        if(fn[i] != null) {
            fn[i].e();
        }
    }
    protected void keyPressed(int i) {
        keycall(keydown, i);
    }
    protected void keyReleased(int i) {
        keycall(keyup, i);
    }
    protected void keyRepeated(int i) {
        keycall(keyrepeat, i);
    }
   
    protected void paint(Graphics g) {
        g.setColor(0xffffff);
        g.fillRect(0, 0, getWidth(), getHeight());
        for(int i = 0; i < drawables.length; i++) {
            Object o = drawables[i];
            if(o != null && o instanceof Drawable) {
                ((Drawable)o).draw(g);
            }
        }
    }
}
