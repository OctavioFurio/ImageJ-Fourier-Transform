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
    int id = 0;             // Indice da imagem de referência
    String[] imageNames;    // Imagens do diretório

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
        double choice;
        double[][] features;
        double[][] distances;
        
        // Interface para definição de parâmetros
        gd = new GenericDialog("Transformada discreta de Fourier (DFT)", IJ.getInstance());
        gd.addNumericField("Quantia M de fatores a serem registrados: ", 100, 0);
        gd.addNumericField("Quantia k de vizinhos a serem buscados: ", 5, 0);
        gd.addNumericField("Utilizar a distancia L (0 = Infinity):", 2, 2);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        M = (int) gd.getNextNumber();
        k = (int) gd.getNextNumber();
        choice = (double) gd.getNextNumber();

        // Solicita o diretório das imagens
        sd = new SaveDialog("Selecione seu diretório.", "Selecione seu diretório.", "");
        if (sd.getFileName() == null) return;
        dir = sd.getDirectory();

        base = varrerDiretorio(dir); // Retorna as imagens do diretório
        if(base == null) return;
        features = getFeatures(base);            // Retorna os vetores de características

        saveThe(features, "features.csv");

        distances = distance(features, id, choice); // Calcula as distâncias até a imagem de referência
        
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

        features = new double[base.length][M]; // Guardará os M valores extraídos de N imagens

        for (int i = 0; i < base.length; i++) {
            ImageAccess img;

            img = base[i];

            IJ.showStatus(i+"/"+base.length);               // mostra na interface
            IJ.showProgress((double)i / base.length);       // barra de progresso 
            IJ.log("Analisando imagem " + (i + 1) + "...");

            features[i] = DFT.applyDTF(img, M); // identificador está sendo do tipo double para facilitar. Deverá ser o String: list[i].
        }

        IJ.log("");
        IJ.showProgress(1.0);
        IJ.showStatus("");
        
        return features; 
    }

    public ImageAccess[] varrerDiretorio(String dir) 
    {
        ImageAccess[] base;
        
        IJ.log("");
        IJ.log("Vasculhando imagens...");
        
        if (!dir.endsWith(File.separator)) dir += File.separator;
        
        imageNames = new File(dir).list(); // lista de arquivos
        if (imageNames==null) return null;

        base = new ImageAccess[imageNames.length];
        id = 0;

        for (int i = 0; i < imageNames.length; i++) {
            File f;
            ImagePlus image;
            ImageAccess input;
            int nx, ny;

            IJ.showStatus(i + "/" + imageNames.length + ": " + imageNames[i]);  // mostra na interface
            IJ.showProgress((double)i / imageNames.length);       // barra de progresso 
            
            f = new File(dir + imageNames[i]);    
            if (f.isDirectory()) continue;
            
            image = new Opener().openImage(dir, imageNames[i]); // abre a imagem
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
    
    public double[][] distance(double[][] features, int id, double c) 
    {
        double[][] distances = new double[features.length][2];

        double[] referenceFeature = features[id];
        double[] normalizedReferenceFeature = normalizeFeatureVector(referenceFeature);

        for (int i = 0; i < features.length; i++) 
        {
            double[] imageFeature = features[i];
            double[] normalizedImageFeature = normalizeFeatureVector(imageFeature);

            distances[i][0] = i;
            distances[i][1] = eDistance(normalizedImageFeature, normalizedReferenceFeature, c);
        }

        // Sort by distance
        Arrays.sort(distances, Comparator.comparingDouble(row -> row[1]));
        return distances; // O primeiro item sempre será a própria imagem. Podemos usar isso.
    }

    private double eDistance(double[] ref, double[] other, double c) 
    {
        assert ref.length == other.length : "Erro: Base de dados inconsistente.";

        if (c > 0)
        {
            double sum = 0;

            // Começando de 1 para ignorar o identificador.
            for (int index = 1; index < ref.length; index++) 
                sum += Math.pow(Math.abs(ref[index] - other[index]), c);
            
            return Math.pow(sum, 1./c);
        }

        double max = 0;

        // Começando de 1 para ignorar o identificador.
        for (int index = 1; index < ref.length; index++) {
            double diff = Math.abs(ref[index] - other[index]);
            if(diff > max)
                max = diff;
        }

        return max;
    }

    private double[] normalizeFeatureVector(double[] featureVector) 
    {
        double[] normalized = new double[featureVector.length];
        double mean = 0;
        double variance = 0;

        for (int i = 1; i < featureVector.length; i++) 
            mean += featureVector[i];

        mean /= (featureVector.length - 1);

        for (int i = 1; i < featureVector.length; i++) 
            variance += Math.pow(featureVector[i] - mean, 2);

        variance /= (featureVector.length - 2);

        double stdDev = Math.sqrt(variance);

        normalized[0] = featureVector[0]; // Preserve the identifier
        for (int i = 1; i < featureVector.length; i++) 
            normalized[i] = (featureVector[i] - mean) / stdDev;

        return normalized;
    }

    private void saveThe(double[][] output, String fileName) 
    {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) 
        {
            for (int j = 0; j < output.length; j++) {
                double[] row = output[j];

                writer.print(imageNames[j] + ",");
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