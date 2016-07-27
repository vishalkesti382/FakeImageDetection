package com.qburst.ai.fake_image_detection.neural_network.core;

import com.qburst.ai.fake_image_detection.neural_network.thread_sync.NotifyingThread;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javax.imageio.ImageIO;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.imgrec.ImageRecognitionPlugin;

public class neural_net_processor extends NotifyingThread {

    static BufferedImage image;
    public static NeuralNetwork nnet;
    static ImageRecognitionPlugin imageRecognition;
    public static String nNetworkpath = "nnet/CNN2.nnet";
    public static double real = 0, fake = 0;

    public static void main(String[] args) {
        try {
            System.out.println("Loading Image....");
            image = ImageIO.read(new File("/home/afsal/Desktop/Screenshot 2016-07-24 12:55:50.png"));
            System.out.println("Loading NN....");
            File NNetwork = new File("nnet/CNN2.nnet");
            if (!NNetwork.exists()) {
                System.err.println("Cant Find NN");
                return;
            }
            nnet = NeuralNetwork.load(new FileInputStream(NNetwork)); // load trained neural network saved with Neuroph Studio
            System.out.println("Load Image Recog Plugin....");
            imageRecognition = (ImageRecognitionPlugin) nnet.getPlugin(ImageRecognitionPlugin.class); // get the image recognition plugin from neural network
            System.out.println("Recognize Image....");
            HashMap<String, Double> output = imageRecognition.recognizeImage(image);
            System.out.println("Output is....");
            System.out.println(output.toString());
        } catch (IOException ex) {
            Logger.getLogger(neural_net_processor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public neural_net_processor(BufferedImage image) {
        this.image = image;
    }

    void notifyUser() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Neural Network Missing");
                alert.setHeaderText("Cant find network file");
                alert.setContentText("Please make sure that CNN2.nnet is located at nnet/CNN2.nnet");
                alert.showAndWait();
            }
        });
    }

    @Override
    public void doRun() {
        try {
            if (nnet == null) { //Bypass network reload during comeback through home button
                File NNetwork = new File(nNetworkpath);
                System.out.println("Nueral network loaded = " + NNetwork.getAbsolutePath());
                if (!NNetwork.exists()) {
                    notifyUser();
                    return;
                }
                nnet = NeuralNetwork.load(new FileInputStream(NNetwork)); // load trained neural network saved with Neuroph Studio
                System.out.println("Learning Rule = " + nnet.getLearningRule());
                imageRecognition = (ImageRecognitionPlugin) nnet.getPlugin(ImageRecognitionPlugin.class); // get the image recognition plugin from neural network
            }
            HashMap<String, Double> output = imageRecognition.recognizeImage(image);
            if (output == null) {
                System.err.println("Image Recognition Failed");
            }
            real = output.get("real");
            fake = output.get("faked");
            System.out.println(output.toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(neural_net_processor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
