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
    boolean showImages;     // Exibir ou não as imagens escolhidas

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
        String fdist;
        ImageAccess[] base;
        double choice;
        double[][] features;
        double[][] distances;
        
        // Interface para definição de parâmetros
        gd = new GenericDialog("Transformada discreta de Fourier (DFT)", IJ.getInstance());
        gd.addNumericField("Quantia M de fatores a serem registrados: ", 100, 0);
        gd.addNumericField("Quantia k de vizinhos a serem buscados: ", 5, 0);
        gd.addChoice("Funcao de distancia: ", new String[] { "Manhattan ", "Euclidiana", "Chebyshev"}, "Euclidiana");
        gd.addCheckbox("Exibir os K vizinhos escolhidos", false);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        M = (int) gd.getNextNumber();
        k = (int) gd.getNextNumber();
        fdist = gd.getNextChoice();

        showImages = gd.getNextBoolean();
        choice = fdist.equals("Manhattan")  ? (double) 1 :
                 fdist.equals("Euclidiana") ? (double) 2 : (double) 0;

        // Solicita o diretório das imagens
        sd = new SaveDialog("Selecione seu diretorio.", "Selecione seu diretorio.", "");
        if (sd.getFileName() == null) return;
        dir = sd.getDirectory();
        
        try
        { 
            base = varrerDiretorio(dir); // Retorna as imagens do diretório
            features = getFeatures(base); // Retorna os vetores de características
            saveThe(features, "features.csv");
            distances = distance(features, id, choice); // Calcula as distâncias até a imagem de referência
        }
        catch(Exception e)
        { 
            IJ.log("\n" + e.getMessage());
            return; 
        }
        
        double precision = 0;
        double recall = 0;
        String originalClass = imageNames[id].replaceAll("[^a-z]","");;
        String currentClass = "";

        IJ.log("Utilizando a funcao de distancia " + fdist);
        IJ.log(">> " + imageNames[(int) distances[0][0]] + " [Imagem de referencia]");
        IJ.log("");
        // Mostra as k primeiras imagens
        for(int i = 1; i <= k; i++)
        {
            double[] image;
            int imageId;
            int imageDist;

            image = distances[i];
            imageId = (int) image[0];
            imageDist = (int )image[1];

            if(showImages)  base[imageId].show("Imagem semelhante nro. " + i);
            
            IJ.log(imageNames[imageId] + " -> Escolha nro. " + i + " (Distancia = " + imageDist + "u)");

            currentClass = imageNames[imageId].replaceAll("[^a-z]","");
            if(originalClass.equals(currentClass)) { precision += 1; recall += 1; }
        }
        precision = precision / k;
        recall = recall / 9.;

        IJ.log("");
        IJ.log("Precisao: " + precision + ", Revocacao: " + recall);
    }

    public ImageAccess[] varrerDiretorio(String dir) throws Exception
    {
        ImageAccess[] base;
        
        IJ.log("");
        IJ.log("Vasculhando imagens...");
        
        if (!dir.endsWith(File.separator)) dir += File.separator;
        
        imageNames = new File(dir).list(); // lista de arquivos
        if (imageNames == null) throw new Exception("Erro: diretorio vazio.");

        base = new ImageAccess[imageNames.length];
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

    public double[][] getFeatures(ImageAccess[] base) throws Exception
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

            try{ features[i] = DFT.applyDTF(img, M); }
            catch(Exception e){ throw e; }
        }

        IJ.log("");
        IJ.showProgress(1.0);
        IJ.showStatus("");
        
        return features; 
    }
    
    public double[][] distance(double[][] features, int id, double c) throws Exception
    {
        double[][] distances = new double[features.length][2];

        double[] referenceFeature = features[id];
        double[] normalizedReferenceFeature = normalizeFeatureVector(referenceFeature);

        for (int i = 0; i < features.length; i++) 
        {
            double[] normalizedImageFeature = normalizeFeatureVector(features[i]);

            try
            {
                distances[i][0] = i;
                distances[i][1] = eDistance(normalizedImageFeature, normalizedReferenceFeature, c);
            }
            catch(Exception e){ throw e; }
        }

        // Sort by distance
        Arrays.sort(distances, Comparator.comparingDouble(row -> row[1]));
        return distances; // O primeiro item sempre será a própria imagem.
    }

    private double eDistance(double[] ref, double[] other, double c) throws Exception
    {
        if(ref.length != other.length) throw new Exception("Erro: Base de dados inconsistente.");

        if (c > 0)
        {
            double sum = 0;

            for (int index = 0; index < ref.length; index++) 
                sum += Math.pow(Math.abs(ref[index] - other[index]), c);
            
            return Math.pow(sum, 1./c);
        }

        double max = 0;

        for (int index = 0; index < ref.length; index++) {
            double diff = Math.abs(ref[index] - other[index]);
            if(diff > max)
                max = diff;
        }

        return max;
    }

    private double[] normalizeFeatureVector(double[] featureVector) 
    {
        double[] normalized = new double[featureVector.length];
        double N = featureVector.length;
        double mean = 0;
        double variance = 0;
        double stdDev;

        for (int i = 0; i < N; i++) 
            mean += featureVector[i];
        mean /= N;

        for (int i = 0; i < N; i++) 
            variance += Math.pow(featureVector[i] - mean, 2);
        variance /= (N - 1);
        stdDev = Math.sqrt(variance);

        for (int i = 0; i < N; i++) 
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
            writer.close();
        } 
        catch (IOException e) 
        {
            IJ.log("\n" + e.getMessage());
        }
    }
}