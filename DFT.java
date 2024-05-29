/*
 * DFT.java
 */

public class DFT { 

    public DFT() {}
    
	static public ImageAccess applyDTF(ImageAccess input, int M) {
		input = borderOf(input);

		return input;
	}

	static private int[] imageToPixelVector(ImageAccess img) {
		int nx = img.getWidth();
		int ny = img.getHeight();
		int[] vec = new int[nx* ny];

		for (int i = 0; i < nx * ny; i ++)
		{
			int x = i % nx;
			int y = i / nx;
			vec[i] = (int)(img.getPixel(x, y))/255;
		}

		return vec;
	}

	static private ImageAccess pixelVectorToImage(int[] vec, int nx, int ny) {
		ImageAccess img = new ImageAccess(nx, ny);

		for (int i = 0; i < nx * ny; i ++)
		{
			int x = i % nx;
			int y = i / nx;
			img.putPixel(x, y, vec[i] * 255);
		}

		return img;
	}

	static private ImageAccess borderOf(ImageAccess input) {
		int nx = input.getWidth();
		int ny = input.getHeight();
		int[] vec = imageToPixelVector(input);

		vec = detectBorder(vec, nx, ny);
		input = pixelVectorToImage(vec, nx, ny);

		return input;
	}

	public static int[] detectBorder(int[] vec, int nx, int ny) {
        int[] border = new int[nx * ny];

		/* Percorre sequencialmente a imagem, da esquerda para a direita, 
		* ignorando os pixels da borda da matriz.
		*/
		for(int i = 0; i < (nx - 2) * (ny - 2); i++)
		{
			int x = i % (nx - 2) + 1;
			int y = i / (nx - 2) + 1;
			int j = x + y * nx;
			int val = vec[j]; // Valor do pixel atual 

			/* Se existe ao menos 1 pixel preto no vizinho vertical ou horizontal
			* o valor é multiplicado por 1, caso contrário, 0. */
			val *= 1 - (
				vec[(x + 1) + y * nx] *
				vec[(x - 1) + y * nx] *
				vec[x + (y + 1) * nx] *
				vec[x + (y - 1) * nx]);
		
			border[j] = val;
		}
                    
        return border;
    }

	static private ComplexNumber[] borderVectorOf(ImageAccess img) {
		int nx = img.getWidth();
		int ny = img.getHeight();
		int[] vec = imageToPixelVector(img);
		ComplexNumber[] borderVec;

		vec = detectBorder(vec, nx, ny);
		img = pixelVectorToImage(vec, nx, ny);
		borderVec = detectBorderVector(vec, nx, ny);

		return borderVec;
	}

	public static ComplexNumber[] detectBorderVector(int[] vec, int nx, int ny) {
        boolean[] visited = new boolean[nx * ny];
		ComplexNumber[] borderVec = new ComplexNumber[0];
		double val= 0;
		int i = 0;

		// Enquanto não achar um pixel de borda, varre a matriz
		while(val != 255. && i < nx * ny) 
		{
			val = vec[i]; 
		}

		/*TODO: IMPLEMENTAR A BUSCA EM BORDA E PASSAR O RESULTADO PARA VEC*/
                    
        return borderVec;
    }

	private int getN(double[][] shape){
		int N = 0;

		for(double[] x : shape)
			for(double y : x)
				N += (int) y;

		return N;
	}
}