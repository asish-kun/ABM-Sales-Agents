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

## ⚙️ Setup Guide (Mac & Windows)

### ✅ Prerequisites

| Tool | macOS | Windows |
|------|--------|---------|
| Git | `brew install git` | `winget install --id Git.Git` |
| JDK 21 | `brew install openjdk@21` | `winget install EclipseAdoptium.Temurin.21.JDK` |
| Maven | `brew install maven` | `winget install Apache.Maven` |
| IntelliJ IDEA (Community) | `brew install --cask intellij-idea-ce` | `winget install JetBrains.IntelliJIDEA.Community` |

> **Java Environment Config (macOS)**:
```bash
# Add to ~/.zshrc or ~/.bash_profile
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
source ~/.zshrc  # or source ~/.bash_profile
```

---

### 📥 Clone the Repository

```bash
git clone https://github.com/asish-kun/ABM-Sales-Agents.git
cd ABM-Sales-Agents
```

---

### 🧠 Open in IntelliJ

1. Launch IntelliJ → File → **Open** → select the `ABM-Sales-Agents` folder.
2. IntelliJ detects `pom.xml` → open as **Maven project**.
3. If asked to enable auto-import, select **Yes**.
4. Wait until all Maven indexing and project setup is complete.

---

### 🔨 Build the Project

In IntelliJ:
- Go to **Build → Build Project** or press **Ctrl+F9 / Cmd+F9**

> Ensure the build completes with **"BUILD SUCCESS"**.

---

### ▶️ Run the Simulation

#### Option 1: Use IntelliJ Run Configuration
1. Go to **Run → Edit Configurations...**
2. Click ➕ → **Application**
3. Set:
    - **Name**: `ABM-App`
    - **Main class**: `view.ConsoleSimulation`
    - **Build and run using**: Java 21
    - **Working directory**: `<project_root>`
    - **Program arguments**:

```bash
-paramsFile config/ABM4Sales.properties \
-outputFile simpleTest_WithExpectedSales_A \
-portfolioSize 8 \
-avgRiskAversion 1 \
-stdevRiskAversion 0 \
-decayRateLeads 0.011 \
-thresholdForConversion 0.99 \
-MCRuns 7 \
-maxSteps 5 \
-nrAgents 9 \
-quota 0 \
-rateForBonus 0 \
-fileForLeads data_injection/parsed_filtered_Dataset_ABM_28APR2024_withAmountNorm.csv \
-agentStrategies [1,2,3,4,5,6,7,8,9] \
-agentAccuracies [0.95,0.85,0.90,1.00,0.80,0.88,0.92,0.78,0.96] \
-agentLeadChoices [2,4,3,5,1,3,2,4,3] \
-agentPortfolioSizes [6,10,8,12,4,9,7,11,5] \
-fallOffProbability 0.10
```

4. Click **Run** ▶️

#### Option 2: Run via Terminal
```bash
mvn clean compile
java -cp target/classes:./libraries/* view.ConsoleSimulation <paste above arguments>
```

---

## 🗂️ Project Structure

```
├── config/                     # Simulation parameter configs
├── data_injection/            # Input datasets (leads, training data)
├── libraries/                 # External jars (DL4J dependencies)
├── logs/                      # Run logs (auto-generated)
├── src/                       # Java source code
│   └── model/                 # Core classes (SalesPerson, Lead, etc.)
├── pom.xml                    # Maven build file
└── README.md                  # Setup + project overview
```

> ⚠️ The project uses `<scope>system</scope>` for libraries – **do not rename the `libraries/` folder** or Maven will fail to resolve.

---

## 🧪 Testing Neural Network Setup (Sample Run)

```bash
java -jar ABM4Sales_FixedExpectedValues.jar \
  -paramsFile config/ABM4Sales.properties \
  -outputFile simpleTest_WithExpectedSales_A \
  -portfolioSize 5 \
  -avgRiskAversion 1 \
  -stdevRiskAversion 0 \
  -decayRateLeads 0.011 \
  -thresholdForConversion 0.99 \
  -MCRuns 30 \
  -maxSteps 70 \
  -nrAgents 163 \
  -quota 0 \
  -rateForBonus 0 \
  -fileForLeads data_injection/parsed_filtered_Dataset_ABM_28APR2024_withAmountNorm.csv
```

---

## ⚠️ Troubleshooting

| Problem | Solution |
|---------|----------|
| Missing JDK in IntelliJ | Go to **Project Structure > SDKs**, add JDK 21 manually |
| Build fails on `system` scope jars | Ensure `libraries/` folder exists with the exact expected JARs |
| Dataset not found | Confirm file paths match your CLI args |
| Compilation error | Ensure you're using **Java 21** and Maven is correctly set up |

---

## 📬 Contact

For questions, ideas, or contributions, open an issue or pull request on [GitHub](https://github.com/asish-kun/ABM-Sales-Agents).

---

**Happy Simulating!**

