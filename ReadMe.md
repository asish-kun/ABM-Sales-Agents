# Salesforce Agent-Based Simulation with Neural Network Decision Making

**Exploring the Impact of Machine Learning on Salesperson Performance Dynamics**

---

## 📌 Overview

This project enhances a traditional agent-based model (ABM) of salesforce decision-making by integrating neural networks (NNs) into agent logic. Originally, salespeople relied on equation-based utility functions to prioritize leads, work hours, and social comparisons. In this twist, each salesperson agent employs a **unique neural network** to predict lead conversion probabilities, fall-off risks, and other dynamic factors. The goal is to compare how NN-driven agents adapt their strategies over weeks versus the original equation-based approach, uncovering insights into performance, risk tolerance, and policy impacts.

---

## 🚀 Key Features

### Original Equation-Based Model
- **Utility-driven decisions**: Agents use risk-aversion-weighted equations to prioritize leads.
- **Social comparison**: Work-hour decisions depend on peer compensation thresholds.
- **Dynamic lead states**: Conversion/fall-off probabilities updated via fixed decay rates and certainty parameters.

### Neural Network Enhanced Model
- **NN predictions**: Each agent’s NN predicts lead conversion/fall-off probabilities using:
  - Lead attributes (magnitude, certainty).
  - Agent’s historical performance (converted leads, hours worked).
  - Social comparison factors (peer bonuses, pay ratios).
- **Adaptive learning**: NNs retrain weekly with new data, enabling agents to refine strategies over time.
- **Scenario testing**: Compare NN agents against equation-based agents under identical firm policies (quotas, bonuses).

---

## 🧠 Implementation Details

### Neural Network Architecture
- **Inputs**: Lead magnitude, certainty, agent’s risk aversion, recent performance, social comparison metrics.
- **Outputs**: Predicted conversion probability (`p_c`), fall-off probability (`p_f`), and "utility score" for lead prioritization.
- **Framework**: Built with [Deeplearning4j](https://deeplearning4j.org/) (Java-based ML library), integrated into the `Salesperson` and `Lead` classes.

### Key Code Changes
- **`Salesperson.java`**: Added NN initialization, training, and prediction methods.
- **`Lead.java`**: Enhanced with methods to feed data into the agent’s NN.
- **`ModelParameters.java`**: New parameters to toggle NN usage, set training epochs, and learning rates.

---

## 📈 Expected Results

- **Performance divergence**: NN agents may outperform equation-based agents in dynamic environments by adapting to hidden patterns (e.g., lead decay trends).
- **Risk tolerance evolution**: NNs could uncover non-linear relationships between risk aversion and lead magnitude, altering long-term strategies.
- **Policy sensitivity**: Quota-based bonuses might have amplified effects on NN agents if social comparison factors are weighted differently.

---

## ⚙️ Installation

1. **Prerequisites**:
   - Java SE 21
   - intall all dependencies in pom.xml file in root directory
   - Deeplearning4j libraries (included in `/libraries`)

2. **Run with Neural Networks**:
   ```bash
  # Salesforce Agent-Based Simulation with Neural Network Decision Making

**Exploring the Impact of Machine Learning on Salesperson Performance Dynamics**

---

## 📌 Overview

This project enhances a traditional agent-based model (ABM) of salesforce decision-making by integrating neural networks (NNs) into agent logic. Originally, salespeople relied on equation-based utility functions to prioritize leads, work hours, and social comparisons. In this twist, each salesperson agent employs a **unique neural network** to predict lead conversion probabilities, fall-off risks, and other dynamic factors. The goal is to compare how NN-driven agents adapt their strategies over weeks versus the original equation-based approach, uncovering insights into performance, risk tolerance, and policy impacts.

---

## 🚀 Key Features

### Original Equation-Based Model
- **Utility-driven decisions**: Agents use risk-aversion-weighted equations to prioritize leads.
- **Social comparison**: Work-hour decisions depend on peer compensation thresholds.
- **Dynamic lead states**: Conversion/fall-off probabilities updated via fixed decay rates and certainty parameters.

### Neural Network Enhanced Model
- **NN predictions**: Each agent’s NN predicts lead conversion/fall-off probabilities using:
  - Lead attributes (magnitude, certainty).
  - Agent’s historical performance (converted leads, hours worked).
  - Social comparison factors (peer bonuses, pay ratios).
- **Adaptive learning**: NNs retrain weekly with new data, enabling agents to refine strategies over time.
- **Scenario testing**: Compare NN agents against equation-based agents under identical firm policies (quotas, bonuses).

---

## 🧠 Implementation Details

### Neural Network Architecture
- **Inputs**: Lead magnitude, certainty, agent’s risk aversion, recent performance, social comparison metrics.
- **Outputs**: Predicted conversion probability (`p_c`), fall-off probability (`p_f`), and "utility score" for lead prioritization.
- **Framework**: Built with [Deeplearning4j](https://deeplearning4j.org/) (Java-based ML library), integrated into the `Salesperson` and `Lead` classes.

### Key Code Changes
- **`Salesperson.java`**: Added NN initialization, training, and prediction methods.
- **`Lead.java`**: Enhanced with methods to feed data into the agent’s NN.
- **`ModelParameters.java`**: New parameters to toggle NN usage, set training epochs, and learning rates.

---

## 📈 Expected Results

- **Performance divergence**: NN agents may outperform equation-based agents in dynamic environments by adapting to hidden patterns (e.g., lead decay trends).
- **Risk tolerance evolution**: NNs could uncover non-linear relationships between risk aversion and lead magnitude, altering long-term strategies.
- **Policy sensitivity**: Quota-based bonuses might have amplified effects on NN agents if social comparison factors are weighted differently.

---

## ⚙️ Installation

1. **Prerequisites**:
   - Java SE 17+
   - Python 3.8+ (for analysis scripts)
   - Deeplearning4j libraries (included in `/libraries`)

2. **Run with Neural Networks**:
   ```bash
   # Salesforce Agent-Based Simulation with Neural Network Decision Making

**Exploring the Impact of Machine Learning on Salesperson Performance Dynamics**

---

## 📌 Overview

This project enhances a traditional agent-based model (ABM) of salesforce decision-making by integrating neural networks (NNs) into agent logic. Originally, salespeople relied on equation-based utility functions to prioritize leads, work hours, and social comparisons. In this twist, each salesperson agent employs a **unique neural network** to predict lead conversion probabilities, fall-off risks, and other dynamic factors. The goal is to compare how NN-driven agents adapt their strategies over weeks versus the original equation-based approach, uncovering insights into performance, risk tolerance, and policy impacts.

---

## 🚀 Key Features

### Original Equation-Based Model
- **Utility-driven decisions**: Agents use risk-aversion-weighted equations to prioritize leads.
- **Social comparison**: Work-hour decisions depend on peer compensation thresholds.
- **Dynamic lead states**: Conversion/fall-off probabilities updated via fixed decay rates and certainty parameters.

### Neural Network Enhanced Model
- **NN predictions**: Each agent’s NN predicts lead conversion/fall-off probabilities using:
  - Lead attributes (magnitude, certainty).
  - Agent’s historical performance (converted leads, hours worked).
  - Social comparison factors (peer bonuses, pay ratios).
- **Adaptive learning**: NNs retrain weekly with new data, enabling agents to refine strategies over time.
- **Scenario testing**: Compare NN agents against equation-based agents under identical firm policies (quotas, bonuses).

---

## 🧠 Implementation Details

### Neural Network Architecture
- **Inputs**: Lead magnitude, certainty, agent’s risk aversion, recent performance, social comparison metrics.
- **Outputs**: Predicted conversion probability (`p_c`), fall-off probability (`p_f`), and "utility score" for lead prioritization.
- **Framework**: Built with [Deeplearning4j](https://deeplearning4j.org/) (Java-based ML library), integrated into the `Salesperson` and `Lead` classes.

### Key Code Changes
- **`Salesperson.java`**: Added NN initialization, training, and prediction methods.
- **`Lead.java`**: Enhanced with methods to feed data into the agent’s NN.
- **`ModelParameters.java`**: New parameters to toggle NN usage, set training epochs, and learning rates.

---

## 📈 Expected Results

- **Performance divergence**: NN agents may outperform equation-based agents in dynamic environments by adapting to hidden patterns (e.g., lead decay trends).
- **Risk tolerance evolution**: NNs could uncover non-linear relationships between risk aversion and lead magnitude, altering long-term strategies.
- **Policy sensitivity**: Quota-based bonuses might have amplified effects on NN agents if social comparison factors are weighted differently.

---

## ⚙️ Installation

1. **Prerequisites**:
   - Java SE 17+
   - Python 3.8+ (for analysis scripts)
   - Deeplearning4j libraries (included in `/libraries`)

2. **Run with Neural Networks**:
   run this command in the Terminal within your root directory
   ```bash
   java -jar ABM4Sales_FixedExpectedValues.jar
     -paramsFile config/ABM4Sales.properties
     -outputFile simpleTest_WithExpectedSales_A
     -portfolioSize 5
     -avgRiskAversion 1
     -stdevRiskAversion 0
     -decayRateLeads 0.011
     -thresholdForConversion 0.99
     -MCRuns 30
     -maxSteps 70
     -nrAgents 163
     -quota 0
     -rateForBonus 0
     -fileForLeads data_injection/parsed_filtered_Dataset_ABM_28APR2024_withAmountNorm.csv
