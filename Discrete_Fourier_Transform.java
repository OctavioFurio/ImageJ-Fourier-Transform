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
    int M;                  // Quantia dos M primeiros coeficientes
    int k;                  // Número de vizinhos mais próximos    
    int id = 0;             // Indice da imagem de referência; 

    public int setup(String arg, ImagePlus imp) 
    {
        reference = imp;
        ImageConverter ic = new ImageConverter(imp);
        ic.convertToGray8();
        return DOES_ALL;
    }

    public void run(ImageProcessor img) 
    {
        GenericDialog gd;
        SaveDialog sd;
        String dir;
        ImageAccess[] base;
        double[][] features;
        double[][] distances;
        
        // Solicita o valor de M
        gd = new GenericDialog("Transformada discreta de Fourier (DFT)", IJ.getInstance());
        gd.addNumericField("Quantia M de fatores a serem registrados: ", 100, 0);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        M = (int) gd.getNextNumber();

        // Solicita o diretório das imagens
        sd = new SaveDialog("Selecione seu diretório.", "Selecione seu diretório.", "");
        if (sd.getFileName() == null) 
            return;
        dir = sd.getDirectory();

        base = varrerDiretorio(dir);        // Retorna as imagens do diretório
        if(base == null)
            return;
        features = getFeatures(base);       // Retorna os vetores de características

        // Solicita o valor de k
        gd = new GenericDialog("Transformada discreta de Fourier (DFT)", IJ.getInstance());
        gd.addNumericField("Quantia k de vizinhos a serem buscados: ", 5, 0);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        k = (int) gd.getNextNumber();

        distances = distance(features, id); // Calcula as distâncias até a imagem de referência
        
        // Mostra as k primeiras imagens
        for(int i = 0; i < distances.length; i++)
        {
            double[] image = distances[i];

            if(i > k)     break;
            if(i == 0)    base[(int) image[0]].show("Imagem original");
            else          base[(int) image[0]].show("Imagem semelhante nro. " + i);
        }  
    }

    public double[][] getFeatures(ImageAccess[] base) 
    {
        double[][] features;
        
        IJ.log("");
        IJ.log("Aplicando DFT...");

        features = new double[base.length][1 + M]; // Guardará os M valores extraídos de N imagens, + um identificador

        for (int i = 0; i < base.length; i++) {
            double[] output;
            ImageAccess img;

            img = base[i];

            IJ.showStatus(i+"/"+base.length);               // mostra na interface
            IJ.showProgress((double)i / base.length);       // barra de progresso 
            IJ.log("Analisando imagem " + (i + 1) + "...");

            output = DFT.applyDTF(img, M);
            features[i] = createFeatureVector(i, output); // identificador está sendo do tipo double para facilitar. Deverá ser o String: list[i].
        }

        saveThe(features, "features.csv");

        IJ.log("");
        IJ.showProgress(1.0);
        IJ.showStatus("");
        
        return features; 
    }

    public ImageAccess[] varrerDiretorio(String dir) 
    {
        String[] list;
        ImageAccess[] base;
        
        IJ.log("");
        IJ.log("Vasculhando imagens...");
        
        if (!dir.endsWith(File.separator)) dir += File.separator;
        
        list = new File(dir).list(); // lista de arquivos
        if (list==null) return null;

        base = new ImageAccess[list.length];
        id = 0;

        for (int i = 0; i < list.length; i++) {
            File f;
            ImagePlus image;
            ImageAccess input;
            int nx, ny;

            IJ.showStatus(i+"/"+list.length+": "+list[i]);  // mostra na interface
            IJ.showProgress((double)i / list.length);       // barra de progresso 
            
            f = new File(dir+list[i]);    
            if (f.isDirectory()) continue;
            
            image = new Opener().openImage(dir, list[i]); // abre a imagem
            if (image == null) continue;        
            if (image.getTitle().equals(reference.getTitle())) id = i;

            input = new ImageAccess(image.getProcessor());
            nx = input.getWidth(); 
            ny = input.getHeight();

            base[i] = input;
        }

        IJ.log("");
        IJ.showProgress(1.0);
        IJ.showStatus("");
        
        return base; 
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
    
    public static double[][] distance(double[][] features, int id) 
    {
        double[][] distances = new double[features.length][2];

        for (int i = 0; i < features.length; i++) {
            double[] image = features[i];

            // Salvamos o identificador junto com as distâncias
            distances[i][0] = i;
            distances[i][1] = eDistance(image, features[id]);
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