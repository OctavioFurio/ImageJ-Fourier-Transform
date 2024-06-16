/*
 * DFT.java
 */

import java.util.Arrays;

public class DFT { 

	public DFT() {}
	
	public static double[] applyDTF(ImageAccess img, int M) throws Exception
	{	
		double[] output;
		ComplexNumber[] bordedSeq;

		img = addPaddingTo(img,1);
		img = borderOf(img);
		bordedSeq = borderSequenceOf(img);

		try {
			output = calculateDFT(bordedSeq, M);
			return output;
		} catch (Exception e) { throw e; }
	}

	/* <RESUMO>: Calcula o descritor de Fourier a partir da sequência
	 * de números complexos da fronteira.
	 */
	private static double[] calculateDFT(ComplexNumber[] borderSeq, int M) throws Exception
	{
		int N;
		double oneOverN;
		double angleConst;
		double[] coefs;
		
		ComplexNumber sum;

		N = borderSeq.length;

		if(N < M) throw new Exception("Erro: M muito grande.");

		coefs = new double[M];
		oneOverN = 1.0 / N;
		angleConst = -2.0 * Math.PI / N;
		
		for (int k = 0; k < M; k++) 
		{
			sum = new ComplexNumber(0,0);

			for(int n = 0; n < N; n++)
			{
				double angle = angleConst * k * n;
				
				ComplexNumber exp = new ComplexNumber(
					Math.cos(angle), Math.sin(angle)
				);
				
				sum.add(ComplexNumber.multiply(borderSeq[n], exp));
			}

			sum.multiply(oneOverN);
			coefs[k] = sum.modulo();
		}

		return coefs;
	}

	/* <RESUMO>: Adiciona uma borda extra à imagem. */
	private static ImageAccess addPaddingTo(ImageAccess img, int padding) 
	{
		int nx, ny;
		ImageAccess newImg;

		nx = img.getWidth();
		ny = img.getHeight();
		newImg = new ImageAccess(nx + padding * 2, ny + padding * 2);

		for(int i = 0; i < nx * ny; i++)
		{
			int x, y;
			double val;

			x = i % nx;
			y = i / nx;
			val = img.getPixel(x,y);

			newImg.putPixel(x + padding, y + padding, val);
		}

		return newImg;
	}

	/* <RESUMO>: Transforma a imagem em um vetor binário, onde 0 
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

	/* <RESUMO>: Transforma um vetor de pixels em uma imagem,
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

	/* <RESUMO>: Retorna uma nova imagem contendo apenas a borda da figura. */
	private static ImageAccess borderOf(ImageAccess img) {
		int nx, ny;
		int[] pixelVec;

		nx = img.getWidth();
		ny = img.getHeight();
		pixelVec = imageToPixelVector(img);
		pixelVec = borderOf(pixelVec, nx, ny);
		img = pixelVectorToImage(pixelVec, nx, ny);

		return img;
	}

	/* <RESUMO>: Retorna um novo vetor de pixels contendo apenas a borda da figura. */
	private static int[] borderOf(int[] pixelVec, int nx, int ny) {
        int[] borderVec;
		borderVec = new int[nx * ny];

		/* Percorre sequencialmente a imagem, da esquerda para a direita, 
		* ignorando os pixels da borda da matriz.
		*/
		for(int i = 0; i < (nx - 2) * (ny - 2); i++)
		{
			int x, y, j, val;
			x = i % (nx - 2) + 1;
			y = i / (nx - 2) + 1;
			j = x + y * nx;
			val = pixelVec[j]; // Valor do pixel atual 

			/* Se existe ao menos 1 pixel preto no vizinho vertical ou horizontal
			* o valor é multiplicado por 1, caso contrário, 0. */
			val *= 1 - (
				pixelVec[(x + 1) + (y + 0) * nx] *
				pixelVec[(x + 0) + (y + 1) * nx] *
				pixelVec[(x - 1) + (y + 0) * nx] *
				pixelVec[(x + 0) + (y - 1) * nx]
			);
		
			borderVec[j] = val;
		}
                    
        return borderVec;
    }

	/* <RESUMO>: Retorna uma sequência de números complexos que representam a fronteira
	* da imagem. 
	*/
	private static ComplexNumber[] borderSequenceOf(ImageAccess img) {
		int nx, ny;
		int[] pixelVec;
		ComplexNumber[] borderSeq;

		nx = img.getWidth();
		ny = img.getHeight();
		pixelVec = imageToPixelVector(img);
		borderSeq = borderSequenceOf(pixelVec, nx, ny);

		return borderSeq;
	}

	/* <RESUMO>: Retorna uma sequência de números complexos que representam a fronteira
	* da imagem.
	*/
	private static ComplexNumber[] borderSequenceOf(int[] pixelVec, int nx, int ny) 
	{
        int i, val;
		ComplexNumber[] borderSeq;
		boolean[] visited;

		// Enquanto não achar um pixel de borda, varre o vetor
		i = -1;
		do {
			i++;
			val = pixelVec[i];
		} while(val != 1 && i < nx * ny);

		visited = new boolean[nx * ny];
		borderSeq = longestBorderSequence(i, pixelVec, nx, ny, visited);
                    
        return borderSeq;
    }

	/* <RESUMO>: Busca recursivamente a maior sequência de borda possível. */
	private static ComplexNumber[] longestBorderSequence(
		int current, int[] pixelVec, int nx, int ny, boolean[] visited
	) 
	{
		int x, y;
		int[] dir;
		ComplexNumber[] borderSeq, logestChildSeq, tempSeq;

		if(pixelVec[current] == 0 || visited[current])
			return new ComplexNumber[0];

		visited[current] = true;

		x = current % nx;
		y = current / nx;

		dir = new int[8];
		dir[0] = (x + 1) + (y + 0) * nx; // Leste
		dir[1] = (x + 1) + (y + 1) * nx; // Nordeste
		dir[2] = (x + 0) + (y + 1) * nx; // Norte
		dir[3] = (x - 1) + (y + 1) * nx; // Noroeste
		dir[4] = (x - 1) + (y + 0) * nx; // Oeste
		dir[5] = (x - 1) + (y - 1) * nx; // Sudoeste
		dir[6] = (x + 0) + (y - 1) * nx; // Sul
		dir[7] = (x + 1) + (y - 1) * nx; // Sudeste

		logestChildSeq = longestBorderSequence(dir[0], pixelVec, nx, ny, visited);
		for(int d = 1; d < 8; d++)
		{
			tempSeq = longestBorderSequence(dir[d], pixelVec, nx, ny, visited);
			if(tempSeq.length > logestChildSeq.length)
				logestChildSeq = Arrays.copyOf(tempSeq, tempSeq.length);
		}

		borderSeq = new ComplexNumber[1];
		borderSeq[0] = new ComplexNumber(x, y);
		borderSeq = concatArray(borderSeq,logestChildSeq);
                    
        return borderSeq;
    }

	/* <RESUMO>: Concatena dois arrays. */
	static <T> T[] concatArray(T[] array1, T[] array2) 
	{
		T[] res;
		res = Arrays.copyOf(array1, array1.length + array2.length);
		System.arraycopy(array2, 0, res, array1.length, array2.length);
		return res;
	}

	/* <RESUMO>: Transforma uma sequência de números complexos para uma imagem.
	 * Este método é apenas utilizada para testes.
	 */
	private static ImageAccess borderSequenceToImage(ComplexNumber[] borderSeq, int nx, int ny) {
		ImageAccess img;

		img = new ImageAccess(nx, ny);

		for (int i = 0; i < borderSeq.length; i++)
		{
			ComplexNumber s = borderSeq[i];
			img.putPixel((int)s.real, (int)s.image, i);
		}

		return img;
	}
}