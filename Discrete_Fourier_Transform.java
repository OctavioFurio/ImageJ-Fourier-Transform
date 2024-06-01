import java.io.*;
import ij.*;
import ij.io.*;
import ij.ImagePlus;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;

import java.util.Arrays;
import java.util.Comparator;

public class Discrete_Fourier_Transform implements PlugInFilter 
{
    ImagePlus reference;    // Imagem de referência
    int M;                       

    public int setup(String arg, ImagePlus imp) 
    {
        reference = imp;
        ImageConverter ic = new ImageConverter(imp);
        ic.convertToGray8();
        return DOES_ALL;
    }

    public void run(ImageProcessor img) 
    {
        GenericDialog gd = new GenericDialog("Transformada discreta de Fourier (DFT)", IJ.getInstance());
        gd.addNumericField("Quantia de fatores a serem registrados: ", 5, 0); // Substituir por quantos fatores de DFT salvar

        gd.showDialog();
        if (gd.wasCanceled())
            return;

        M = (int) gd.getNextNumber();

        SaveDialog sd = new SaveDialog("Selecione seu diretório.", "Selecione algum arquivo no diretório.", "");
        if (sd.getFileName() == null) 
            return;

        String dir = sd.getDirectory();
        String ref = sd.getFileName();

        varrerDiretorio(ref, dir, M);
    }

    public void varrerDiretorio(String ref, String dir, int M) 
    {
        IJ.log("");
        IJ.log("Vasculhando imagens...");
        
        if (!dir.endsWith(File.separator)) dir += File.separator;
        
        String[] list = new File(dir).list();  /* lista de arquivos */
        if (list==null) return;

        double[][] features = new double[list.length][1 + M]; // Guardará os M valores extraídos de N imagens, + um identificador
        ImageAccess[] base = new ImageAccess[list.length];
        double[] output = new double[M];
        int id = 0;

        for (int i = 0; i < list.length; i++) {
            IJ.showStatus(i+"/"+list.length+": "+list[i]);  // mostra na interface
            IJ.showProgress((double)i / list.length);       // barra de progresso 
            File f = new File(dir+list[i]);
            
            if (f.isDirectory()) continue;
            
            ImagePlus image = new Opener().openImage(dir, list[i]); // abre a imagem
            
            if (image == null) continue;        

            ImageAccess input = new ImageAccess(image.getProcessor());
            int nx = input.getWidth(); 
            int ny = input.getHeight();

            IJ.log("Analisando " + f.getName() + "...");

            if (f.getName().equals(ref)) id = i;

            base[i] = input;
            output = DFT.applyDTF(input, M);

            features[i] = createFeatureVector(i, output); // identificador está sendo do tipo double para facilitar. Deverá ser o String: list[i].
        }

        double[][] distances = distance(id, features); // id é o identificador da imagem de referência
        
        int pos = 0;
        for(double[] imagem : distances)
        {
            if(pos > 5)     break;
            if(pos == 0)    base[(int) imagem[0]].show("Imagem original");
            else            base[(int) imagem[0]].show("Imagem semelhante nro. " + pos);
            pos++;
        }
 
        saveThe(features, "features.csv");

        IJ.log("");
        IJ.showProgress(1.0);
        IJ.showStatus("");      
    }    
    
    private static double[] createFeatureVector(int i, double[] output) 
    {
        double[] featureVector = new double[output.length + 1];
        featureVector[0] = (double) i; // Idenficador da imagem

        System.arraycopy(
            output, 0,           // de...
            featureVector, 1,   // para...
            output.length               // no tamanho...
        );

        return featureVector;
    }
    
    public static double[][] distance(int ref, double[][] features) 
    {
        double[][] distances = new double[features.length][2];

        for (int i = 0; i < features.length; i++) {
            double[] image = features[i];

            // Salvamos o identificador junto com as distâncias
            distances[i][0] = i;
            distances[i][1] = eDistance(image, features[ref]);
        }

        // Ordenamos a matriz baseado na distância
        Arrays.sort(distances, Comparator.comparingDouble(row -> row[1]));

        return distances; // O primeiro item sempre será a própria imagem. Podemos usar isso.
    }

    private static double eDistance(double[] ref, double[] other) 
    {
        assert ref.length == other.length : "Erro: Base de dados inconsistente.";

        double sum = 0;

        // Começando de 1 para ignorar o identificador.
        for (int index = 1; index < ref.length; index++) {
            sum += Math.pow(ref[index] - other[index], 2);
        }

        return Math.sqrt(sum);
    }

    private static void saveThe(double[][] output, String fileName) 
    {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) 
        {
            for (double[] row : output) {
                for (int i = 0; i < row.length; i++) {
                    writer.print(row[i]);
                    if (i < row.length - 1) writer.print(",");
                }
                writer.println();
            }
        } 
        
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}