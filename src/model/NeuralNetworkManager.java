package model;

import java.io.File;

import org.joone.engine.FullSynapse;
import org.joone.engine.LinearLayer;
import org.joone.engine.Monitor;
import org.joone.engine.NeuralNet;
import org.joone.engine.SigmoidLayer;
import org.joone.engine.TeachingSynapse;
import org.joone.io.FileInputSynapse;

public class NeuralNetworkManager {

 private NeuralNet nnet;

 public void buildAndTrainNetwork(String trainingFilePath) {
  // 1) Create layers
  LinearLayer input = new LinearLayer();
  SigmoidLayer hidden = new SigmoidLayer();
  SigmoidLayer output = new SigmoidLayer();

  input.setRows(2);   // XOR input: 2 values
  hidden.setRows(3);  // Hidden neurons
  output.setRows(1);  // XOR output: 1 value

  // 2) Create synapses
  FullSynapse synapseIH = new FullSynapse(); // input -> hidden
  FullSynapse synapseHO = new FullSynapse(); // hidden -> output

  // 3) Connect layers through synapses
  input.addOutputSynapse(synapseIH);
  hidden.addInputSynapse(synapseIH);

  hidden.addOutputSynapse(synapseHO);
  output.addInputSynapse(synapseHO);

  // 4) Create neural net and add layers
  nnet = new NeuralNet();
  nnet.addLayer(input, NeuralNet.INPUT_LAYER);
  nnet.addLayer(hidden, NeuralNet.HIDDEN_LAYER);
  nnet.addLayer(output, NeuralNet.OUTPUT_LAYER);

  // 5) Setup monitor
  Monitor monitor = nnet.getMonitor();
  monitor.setLearningRate(0.7);
  monitor.setMomentum(0.5);
  monitor.setTrainingPatterns(4);     // XOR dataset has 4 rows
  monitor.setTotCicles(2000);
  monitor.setLearning(true);

  // 6) Hook up input file
  FileInputSynapse inputStream = new FileInputSynapse();
  inputStream.setInputFile(new File(trainingFilePath));
  inputStream.setAdvancedColumnSelector("1,2"); // Inputs in columns 1 & 2
  input.addInputSynapse(inputStream);

  // 7) Hook up output (expected values)
  FileInputSynapse targetStream = new FileInputSynapse();
  targetStream.setInputFile(new File(trainingFilePath));
  targetStream.setAdvancedColumnSelector("3"); // Output in column 3

  TeachingSynapse trainer = new TeachingSynapse();
  trainer.setDesired(targetStream);
  nnet.setTeacher(trainer);
  output.addOutputSynapse(trainer);

  // 8) Start training
  nnet.go();
 }
}
