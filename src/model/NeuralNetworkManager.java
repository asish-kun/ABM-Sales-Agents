package model;

import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.weights.WeightInitDistribution;
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
    private int inputSize;

    public NeuralNetworkManager(int inputSize) {
        this.inputSize = inputSize;
        this.model = buildNetwork(inputSize);
    }

    private MultiLayerNetwork buildNetwork(int numInputs) {
        int hiddenSize = 16; // example

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .weightInit(new WeightInitDistribution(new NormalDistribution(0, 0.01)))
                .updater(new Sgd(0.01))
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(numInputs)
                        .nOut(hiddenSize)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(hiddenSize)
                        .nOut(hiddenSize)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)  // <---- cross-entropy
                        .nIn(hiddenSize)
                        .nOut(2)
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
    public void train(DataSet dataSet, int epochs) {
        for (int i = 0; i < epochs; i++) {
            model.fit(dataSet);
        }
    }

    /**
     * Predict the (probConversion, probFallOff).
     */
    public float[] predict(float[] features) {
        INDArray input = Nd4j.create(features, new int[]{1, inputSize});
        INDArray output = model.output(input);
        return output.toFloatVector();
    }

    public MultiLayerNetwork getModel() {
        return model;
    }
}
