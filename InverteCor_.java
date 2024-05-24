/*
 * InverteCor_.java
 *
 * inverte as intesidades da imagem
 */

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;

/*
 * Esse é um exemplo básico sob o qual podemos construir.
 * 
 * Instruções rápidas:
 * 		1. O arquivo fonte Java deve ser colocado na pasta ImageJ>plugins
 *		2. O nome do arquivo deverá conter "_" para ser reconhecido como um plugin
 *		3. Deve-se incluir nesta pasta também o arquivo ImageAccess.class
 * 		4. Execute o ImageJ e abra uma imagem qualquer de 8 bits
 * 		5. Abra o menu Plugins, Compile and Run, e escolha o arquivo java
 * 		6. Será criado o respectivo arquivo .class
 * 		7. Para ele aparecer na lista de plugins do menu Plugin, reinicie o ImageJ
 */

public class InverteCor_ 
{
	public InverteCor_() {
		ImagePlus imp = WindowManager.getCurrentImage();

		if (imp.getType() != ImagePlus.GRAY8) {
			IJ.showMessage("Only process the 8-bit image");
			return;
		}

		ImageAccess input = new ImageAccess(imp.getProcessor());
		int nx = input.getWidth(); 
		int ny = input.getHeight();
		
		ImageAccess output = new ImageAccess(nx, ny);
		//output.show("Imagem inicial");
		
		output = ClassInverteCor.inverte(input);
		output.show("Imagem resultante");
	}
}




