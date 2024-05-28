/*
 * DFT.java
 */

public class DFT { 

    public DFT() {}
    
	static public ImageAccess applyDTF(ImageAccess input, int M) {
		input = borderOf(input);

		return input;
	}

	static private ImageAccess borderOf(ImageAccess input) {
		int nx = input.getWidth();
		int ny = input.getHeight();

		double[][] matrix = new double[nx][ny];

		double value = 0.0;

		for (int x=0; x<nx; x++)
			for (int y=0; y<ny; y++)
				matrix[x][y] = input.getPixel(x, y);

		matrix = detectBorder(matrix);

		for (int x=0; x<nx; x++)
			for (int y=0; y<ny; y++) 
				input.putPixel(x, y, matrix[x][y]);

		return input;
	}

	public static double[][] detectBorder(double[][] image) {
        int height = image.length;
        int width = image[0].length;
        double[][] border = new double[height][width];

        for (int x = 1; x < height - 1; x++)
            for (int y = 1; y < width - 1; y++) 
                if (image[x][y] == 0.) 
                    if (image[x + 1][y + 0] == 255. || image[x - 1][y + 0] == 255. ||
						image[x + 0][y + 1] == 255. || image[x + 0][y - 1] == 255. ||
						image[x + 1][y + 1] == 255. || image[x - 1][y - 1] == 255. ||
						image[x + 1][y - 1] == 255. || image[x - 1][y + 1] == 255.  )   
						{
							border[x][y] = 255.;
						}
                    
        return border;
    }

	private int getN(double[][] shape){
		int N = 0;

		for(double[] x : shape)
			for(double y : x)
				N += (int) y;

		return N;
	}
}