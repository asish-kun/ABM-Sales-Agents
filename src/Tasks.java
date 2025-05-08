public class Tasks {
}

//Neural Network
// Weeks Elapsed is from the start while the weeks_diff_prior is time elapsed from it's last interaction
// Entry by Week, shows the number of time a lead has been processed at any point of time
// TODO: Real Lead Sum: To show how many leads each sales person has in their portfolio, not used as of now
//  { define real_lead_sum set it equal to portfolio sze of this sales person & all their leads linked to them}
// Amount: The current magnitud coming in from excel data set.
// Sector: To Which sector this lead belong to
// Mkt_gen: to see if the lead is from added from marketing
// Product Service: to see if it;s a product or a service
// Conversion Prb Week (That the neural predicts at the end)

// Sales People & Lead
// Accuracy is changed from sales person to sales person
//  { We will get this from input by entering different accuracies to sales people showing their experience}
// chosen leads to be converted to a array so that sales people can choose different number of chosen leads to work on each week
//  { we will be providing this as input}
// array input for portfolio size of each sales person
//  {will be a metric for both leads and sales people }


//Strategies
// In all the strategy only use predicted probability not actual probability

// Strategies Implementation
//  Escalation of Commitment to be implemented properly:
//  (higher number of times worked on the lead... higher weight in choosing this lead)
// FallOffProbability to be constant for all leads and all agents in all weeks
//  (reference like 10%)
//  { Will be given to the model through console }
//  { No Longer being updated along with probability of conversion}
//  { First decide if the leads converts with true probability then generate
//  	a random probability of fall off if it is less than 10% or else fall off happens }

// Random Generation of Leads
//  for a randomly generated lead Weeks Elapsed and weeks_diff_prior are 0.
// Sector to be matching data as in training data.
// remove Process_week metric from leads
// assign probability of conversion by predicting it with neural network (assign it to the predicted probability not the actual probability)

// Adding Lead with LeadData
// assign probability of conversion by predicting it with neural network (assign it to the predicted probability not the actual probability)
//  {When you are in a new week, at the end of that week
//      figure out when it generates new ones at the end of the previous week or the beginning of the next week}

// Logs
// Currently Each MC run is logged into a single .txt file but after running consecutive
//  runs the data is being accumulated in sames files, going forward i want to generate a new file for a new run place it
//  in the same agents folder with in logs folder.

// Design of Experiments to be run
// TODO: 9 scenarios for 9 strategies, and all sales people use same strategy
//  {Run each scenario 5 times}
// Increase SalesPeople to: 10
// Increase Portfolio to: 50 {use same portfolio size for all of them}
// Increase MCRuns to: 25 runs per scenario
// Increase number of weeks to: 70
// TODO: Set Varying accuracy across 25 runs and repeat it to all the other runs (varying between 10%, 20% & 30%)
// TODO: Visualize results data into a single graph
// TODO: set number of chosen leads to 10
//
// [1,1,1,1,1,1,1,1,1,1] - 1 run
// [2,2,2,2,2,2,2,2,2,2]
// [3,3,3,3,3,3,3,3,3,3]
// [4,4,4,4,4,4,4,4,4,4]
// [5,5,5,5,5,5,5,5,5,5]
// [6,6,6,6,6,6,6,6,6,6]
// [7,7,7,7,7,7,7,7,7,7]
// [8,8,8,8,8,8,8,8,8,8]
// [9,9,9,9,9,9,9,9,9,9]


// Visualizing Results
// TODO: Automating the analysis and visualisation Part.

// Actual and predicted prob
// (pro + accu{accuracy * pred prob}, pro - accu) -> get a random from this range
// accu{accuracy * pred prob}
// -agentLeadChoices - [2,4,3,5,1,3,2,4,3,1]
// only chosen leads will be updated for conversion , but probabilities are updated for every lead despite being chosen

// MC RUN
//  1st week, read the data, use the neural network to make the predictions , No converisons, No Fall offs, just initializing week
//  2nd week follow current implementation ( only leads worked on will get to be converted)
//  We will be check conversion first then updating probability for all the leads
//  Update probs happen at the last setp of simulation updating all the leads attribs like weeks elapsed,.. etc



// Test Runs:
// -agentStrategies
// [1,1,1,1,1,1,1,1,1,1] - 3 run
// [2,2,2,2,2,2,2,2,2,2]
// [3,3,3,3,3,3,3,3,3,3]
// [4,4,4,4,4,4,4,4,4,4]
// [5,5,5,5,5,5,5,5,5,5]
// [6,6,6,6,6,6,6,6,6,6]
// [7,7,7,7,7,7,7,7,7,7]
// [8,8,8,8,8,8,8,8,8,8]
// [9,9,9,9,9,9,9,9,9,9]

//\
//-agentAccuracies
// [0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1]
// [0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2]
// [0.3,0.3,0.3,0.3,0.3,0.3,0.3,0.3,0.3,0.3]
// [0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4]
//\
//-agentLeadChoices
//[5,5,5,5,5,5,5,5,5,5]
//\
//-agentPortfolioSizes
//[50,50,50,50,50,50,50,50,50,50]
//\
//-fallOffProbability
//0.10
