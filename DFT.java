/*
 * DFT.java
 */

import java.util.Arrays;

public class DFT { 

    public DFT() {}
    
	public static double[] applyDTF(ImageAccess img, int M) 
	{	
		double[] output = calculateDFT(img, M);
		return output;
	}

    private static double[] calculateDFT(ImageAccess img, int M) 
	{
        int[] imageVector = imageToPixelVector(img);
		int N = imageVector.length;

        assert N >= M : "Erro: M muito grande.";

        double[] coefs = new double[M];
        
		for (int pos = 0; pos < M; pos++) 
		{
            ComplexNumber sum = new ComplexNumber(0, 0);
        
			for (int i = 0; i < N; i++) 
			{
                double angle = -2 * Math.PI * pos * i / N;

				// Aplicando a fórmula de Euler
                ComplexNumber exp = new ComplexNumber(Math.cos(angle), Math.sin(angle));
				
                sum = sum.add(new ComplexNumber(
					imageVector[i] * exp.real, imageVector[i] * exp.image
				));
            }

			ComplexNumber res = new ComplexNumber(sum.real / N, sum.image / N);
            coefs[pos] = res.modulo;
        }

        return coefs;
    }


	/* imageToPixelVector: Transforma a imagem em um vetor binário, onde 0 
	 * representa um pixel preto, e 1 representa um pixel branco. 
	 * A imagem é percorrida a esquerda para a direita, de cima para baixo.
	 */
	private static int[] imageToPixelVector(ImageAccess img) 
	{
		int nx, ny;
		int[] pixelVec;

		nx = img.getWidth();
		ny = img.getHeight();
		pixelVec = new int[nx * ny];

		for (int i = 0; i < nx * ny; i ++)
		{
			int x, y;
			x = i % nx;
			y = i / nx;
			pixelVec[i] = img.getPixel(x, y) > 0 ? 1 : 0;
		}

		return pixelVec;
	}

	/* pixelVectorToImage: Transforma um vetor de pixels em uma imagem,
	 * de modo que 0 representa um pixel preto e 1, um pixel branco.
	 * A imagem é construida da esquerda para a direita, de cima para baixo, 
	 * respeitando a resolução fornecida.
	 */
	private static ImageAccess pixelVectorToImage(int[] pixelVec, int nx, int ny) 
	{
		ImageAccess img;

		img = new ImageAccess(nx, ny);

		for (int i = 0; i < nx * ny; i ++)
		{
			int x, y;
			x = i % nx;
			y = i / nx;
			img.putPixel(x, y, pixelVec[i] * 255);
		}

		return img;
	}

	static <T> T[] concatArray(T[] array1, T[] array2) 
	{
		T[] res;
		res = Arrays.copyOf(array1, array1.length + array2.length);
		System.arraycopy(array2, 0, res, array1.length, array2.length);
		return res;
	}
}