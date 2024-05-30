import java.io.*;
import ij.*;
import ij.io.*;
import ij.ImagePlus;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;

public class Discrete_Fourier_Transform implements PlugInFilter 
{
    ImagePlus reference;        // Imagem de referência
    int N;                      
    // boolean salvarResultados;   

    public int setup(String arg, ImagePlus imp) {
        reference = imp;
        ImageConverter ic = new ImageConverter(imp);
        ic.convertToGray8();
        return DOES_ALL;
    }

    public void run(ImageProcessor img) {
        GenericDialog gd = new GenericDialog("Transformada discreta de Fourier (DFT)", IJ.getInstance());
        gd.addNumericField("Quantia de fatores a serem registrados: ", 5, 0); // Substituir por quantos fatores de DFT salvar
        // gd.addCheckbox("Salvar mapas de frequência resultantes: ", false); // Podemos selecionar se desejamos ou não salvar os mapas de frequência das imagens

        gd.showDialog();
        if (gd.wasCanceled())
            return;

        N = (int) gd.getNextNumber();
        // salvarResultados = (boolean) gd.getNextNumber();

        SaveDialog sd = new SaveDialog("Selecione seu diretório.", "Selecione algum arquivo no diretório.", "");
        if (sd.getFileName() == null) 
            return;

        String dir = sd.getDirectory();
        varrerDiretorio(dir);
    }

    public void varrerDiretorio(String dir) {
        IJ.log("");
        IJ.log("Vasculhando imagens...");
        if (!dir.endsWith(File.separator))
            dir += File.separator;
        
        String[] list = new File(dir).list();  /* lista de arquivos */
        if (list==null) 
            return;

        for (int i=0; i<list.length; i++) {
            IJ.showStatus(i+"/"+list.length+": "+list[i]);  // mostra na interface
            IJ.showProgress((double)i / list.length);       // barra de progresso 
            File f = new File(dir+list[i]);
            if (!f.isDirectory()) {
                ImagePlus image = new Opener().openImage(dir, list[i]); // abre a imagem
                if (image != null) {                
                    // image.show();

                    ImageAccess input = new ImageAccess(image.getProcessor());
                    int nx = input.getWidth(); 
                    int ny = input.getHeight();

                    // Variável que receberá o mapa de frequências resultante
                    ImageAccess output = new ImageAccess(nx, ny); // TODO: averiguar suas dimensões reais

                    // CODIGO para aplicar nossa função na imagem:

                    // Input é nossa imagem original. Basta, então, aplicar DFT sobre ela.
                    output = DFT.applyDTF(input, 10);

                    // Output, nesse caso, pode ser sobrescrita por seu mapa de frequências
                    if (i == 10 || i == 4) output.show("Imagem resultante"); // Exibir apenas uma imagem do diretório
                    
                    // if (salvarResultados)
                        // ; // TODO: Salvar imagens resultantes da DFT
                }
            }
        }

        IJ.showProgress(1.0);
        IJ.showStatus("");      
    }      
}