package ro.devteam.elmandroid;

import org.ejml.simple.SimpleMatrix;

import java.util.Random;

public class ELM {

    public SimpleMatrix train_samples;
    public SimpleMatrix train_labels;

    public SimpleMatrix test_samples;
    public SimpleMatrix test_labels;

    public SimpleMatrix bias;
    public SimpleMatrix inputWeights;
    public SimpleMatrix outputWeights;

    public SimpleMatrix Y;
    public SimpleMatrix T;

    private boolean clasification = true;

    private int numberHiddenNeurons;
    private int numberOutputNeurons;
    private int numberInputNeurons;

    private int numTrainData;
    private int numTestData;

    private double accuracy = 0;
    private String activationFunction = "sign";

    public ELM(int numberHiddenNeurons) {
        this.numberHiddenNeurons = numberHiddenNeurons;
    }

    public ELM(int numberHiddenNeurons, String activationFunction) {
        this.numberHiddenNeurons = numberHiddenNeurons;
        this.activationFunction = activationFunction;
    }

    public ELM(int numberHiddenNeurons, String activationFunction, boolean clasificationProblem) {
        this.numberHiddenNeurons = numberHiddenNeurons;
        this.activationFunction = activationFunction;
        this.clasification = clasificationProblem;
    }

    public void addTrainData(double[][] train_samples, double[][] train_labels) {
        this.train_samples = new SimpleMatrix(train_samples);
        this.train_labels = new SimpleMatrix(train_labels);
    }

    public void addTestData(double[][] test_samples, double[][] test_labels) {
        this.test_samples = new SimpleMatrix(test_samples);
        this.test_labels = new SimpleMatrix(test_labels);
    }

    public void train() {
        numTrainData = train_samples.numCols();
        numberInputNeurons = train_samples.numRows();

        train_samples = train_samples.transpose();

//        inputWeights = SimpleMatrix.random_DDRM(numberHiddenNeurons, numberInputNeurons, -1, 1, new Random());


    }

}
