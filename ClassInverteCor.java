/*
 * ClassInverteCor.java
 */

public class ClassInverteCor { 
    /** Creates a new instance of ClassExemplo */
    public ClassInverteCor() {}
    
	static public ImageAccess inverte(ImageAccess input) {
		int nx = input.getWidth();
		int ny = input.getHeight();

		double value = 0.0;

		for (int x=0; x<nx; x++)
			for (int y=0; y<ny; y++) {
				value = input.getPixel(x, y);
				value = 255 - value;
				input.putPixel(x, y, value);
			}
		return input;	
	}
}