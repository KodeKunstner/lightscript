import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

final class GenPixFont {
	public static void main(String[] args) throws Exception {
		int x, y, charwidth, count, starty, endy, i, j, n, c;
		boolean b;

		if(args.length != 1) {
			System.out.println("Usage: java GenPixFont sourceimage");
			System.exit(0);
		} 

		BufferedImage image = ImageIO.read(new File(args[0]));

		int fontHeight = image.getHeight() - 4;


		i = 0; charwidth = 0;
		count = 0;
		char id=0;
		for(x=0;x<image.getWidth();x++) {
			c = image.getRGB(x, 0) & 0x00ffffff ;
			if(c != 0) {
				i = 0;
			} else {
				if(i == 0) {
					count++;
				}
				i++;
				if(i>charwidth) {
					charwidth = i;
					id = (char)x;
				}
			}
		}
		System.out.println("charwidth: " + charwidth);
		System.out.println("count: " + count);
		System.out.println("longest: " + (int) id);
	}
}
