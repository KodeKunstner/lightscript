import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author voel
 */
/*
public class ZuiObject {
    public Image img;
    public short x;
    public short y;
    public short z;
    private ZuiObject() {
    }
    
    Font font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public ZuiObject(String str, int x, int y, int z) {
        this.x = (short)x;
        this.y = (short)y;
        this.z = (short)z;
        int width = font.stringWidth(str) + 5;
        int height = font.getHeight() + 5;
        img = Image.createImage(width, height);
        Graphics g = img.getGraphics();
        g.setFont(font);
        g.setColor(192, 192, 192);
        g.drawRect(0, 0, width-1, height-1);
        g.setColor(0, 0, 0);
        g.drawString(str, 3, 3, g.TOP|g.LEFT);
    }

}
*/