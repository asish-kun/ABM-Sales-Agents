package model;

import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.weights.WeightInitDistribution;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * A manager to build, train, and store a MultiLayerNetwork (DL4J)
 * for each sales agent.
 */
public class NeuralNetworkManager {

    private MultiLayerNetwork model;
    private NormalizerMinMaxScaler scaler = null;
    private int inputSize;

    public NeuralNetworkManager(int inputSize) {
        this.inputSize = inputSize;
        this.model = buildNetwork(inputSize);
    }

    private MultiLayerNetwork buildNetwork(int numInputs) {

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .weightInit(new WeightInitDistribution(new NormalDistribution(0, 0.01)))
                .updater(new Adam(0.001))
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(numInputs)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(64)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .nIn(32)
                        .nOut(1)
                        .activation(Activation.SIGMOID)
                        .build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(config);
        net.init();
        return net;
    }


    /**
     * Train this agent's network on a subset of data.
     */
    public void train(DataSet dataSet, int epochs, NormalizerMinMaxScaler givenScaler) {
        this.scaler = givenScaler; // store the same scaler you used for training
        for (int i = 0; i < epochs; i++) {
            model.fit(dataSet);

            System.out.println("Epoch " + (i+1) + " loss: " + model.score());
        }
    }

    /**
     * Predict the (probConversion, probFallOff).
     */
    public float[] predictINDArray(INDArray alreadyScaledInput) {
        INDArray output = model.output(alreadyScaledInput);
        return output.toFloatVector();
    }

    public float[] predictScaled(float[] rawFeatures) {
        if (scaler == null) {
            // If for some reason you never stored a scaler, bail or do raw predict
            System.err.println("WARNING: No scaler set - returning raw predict. This may break thresholds!");
            return predictRaw(rawFeatures);
        }

        // Convert float[] to INDArray
        INDArray rawND = Nd4j.create(rawFeatures, new int[]{1, inputSize});
        // Scale
        INDArray scaled = rawND.dup(); // Make a copy first
        scaler.transform(scaled);      // Apply scaling in-place
        // Predict
        return predictINDArray(scaled);
    }

    public float[] predictRaw(float[] features) {
        INDArray input = Nd4j.create(features, new int[]{1, inputSize});
        INDArray output = model.output(input);
        return output.toFloatVector();
    }

    public MultiLayerNetwork getModel() {
        return model;
    }
    public NormalizerMinMaxScaler getScaler() {
        return scaler;
    }

    public void setScaler(NormalizerMinMaxScaler scaler) {
        this.scaler = scaler;
    }
}
