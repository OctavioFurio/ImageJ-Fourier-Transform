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
		int width = image.length;
        int height = image[0].length;
        double[][] border = new double[width][height];

		/* Percorre sequencialmente a imagem, da esquerda para a direita, 
		* ignorando os pixels da borda da matriz.
		*/
		for(int i = 0; i < (height - 2) * (width - 2); i++)
		{
			int x = i % (width - 2) + 1;
			int y = i / (width - 2) + 1;
			double val = image[x][y]; // Valor do pixel atual 

			/* Se existe ao menos 1 pixel preto no vizinho vertical ou horizontal
			* o valor é multiplicado por 1, caso contrário, 0. */
			val *= 1 - (
				(image[x + 1][y]/255) *
				(image[x - 1][y]/255) *
				(image[x][y + 1]/255) *
				(image[x][y - 1]/255));
		
			border[x][y] = val;
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