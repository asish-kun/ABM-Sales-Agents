# -*- coding: utf-8 -*-
"""
Created on Tue Apr  6 10:56:29 2021

Function to plot SA for 4 strategies

@author: mchs
"""

import numpy as np
import pandas as pd
from matplotlib import pyplot as plt


######################################################    
def plotSAOnVariableTwoPlots (fig, keySA, experiment_name, experimentLabel, xAxisLabel):
        
    cmap = ['#d7191c', '#fdae61', '#abd9e9', '#2c7bb6', '#8c73b6']
            
    # files in-out
    file_data = '../logs/SummaryMCruns_' + experiment_name + '.txt'
      
    keySAvariable = keySA + 'Value'
        
    numColVar1 = 2
    numColFinalConvLeads = 4
    numColFinalConvLeadsByMag = 7
    #numColFinalFallOffs = 10
    numColFinalPay = 13
    numColFinalWorkExtra = 16    
    numColFinalWithBonus = 19
    
       
    # load files using ; as separator
    initial_dataset = pd.read_csv(file_data, sep=';', header=None) 
            
    dataset_scenario1 = pd.DataFrame.from_dict(np.array([initial_dataset.iloc[:, numColVar1], 
                                                         initial_dataset.iloc[:, numColFinalConvLeads], 
                                                         initial_dataset.iloc[:, numColFinalConvLeadsByMag], 
                                                         initial_dataset.iloc[:, numColFinalWorkExtra], 
                                                         initial_dataset.iloc[:, numColFinalWithBonus], 
                                                         initial_dataset.iloc[:, numColFinalPay]]).T)
    
    dataset_scenario1.columns = [keySAvariable,'CLs','CLsByMag','PeopleWorkExtra','PeopleWithBonus','Pay']
    
    dataset_scenario1['CLs'] = pd.to_numeric(dataset_scenario1['CLs'])    
    dataset_scenario1['CLsByMag'] = pd.to_numeric(dataset_scenario1['CLsByMag'])        
   # dataset_scenario1['FLs'] = pd.to_numeric(dataset_scenario1['FLs'])         
    dataset_scenario1['PeopleWithBonus'] = pd.to_numeric(dataset_scenario1['PeopleWithBonus'])    
    dataset_scenario1['Pay'] = pd.to_numeric(dataset_scenario1['Pay'])    
    dataset_scenario1['PeopleWorkExtra'] = pd.to_numeric(dataset_scenario1['PeopleWorkExtra'])    
    
    dataset_scenario1['PeopleWithBonus'] = dataset_scenario1['PeopleWithBonus'] / 100
    dataset_scenario1['PeopleWorkExtra'] = dataset_scenario1['PeopleWorkExtra'] / 100
    
    #dataset_scenario1['avgMagByLead'] = dataset_scenario1['CLsByMag'] / dataset_scenario1['CLs']

    
    ######################################################    
    # once we have the averaged values, we plot them in grid of plots
    
    # to see all the styles:  print(plt.style.available)
    plt.style.use('seaborn-paper')

    
    
    #Scatter the points
      
    ######################################################        
    ax = plt.subplot(1, 2, 1) # (rows, columns, panel number)
        
    ax.set_xlabel(xAxisLabel)
    ax.set_ylabel('Number of leads')
    #ax.set_title('Leads & sales', fontweight='bold');


    ax.plot(dataset_scenario1[keySAvariable], dataset_scenario1['CLs'],
                label = 'Converted leads ' + experimentLabel, 
                linewidth = 3, 
                color = cmap[2],
                linestyle = ':',
                alpha = 0.9)
    
    
    
    # =============================================================================
    """ ax.plot(dataset_scenario1[keySAvariable], dataset_scenario1['Pay'],
                 label = 'Pay (including bonuses) ' + experimentLabel, 
                 linewidth = 2, 
                 color = cmap[3],
                 linestyle = ':',
                 alpha = 1)
    """
    # =============================================================================
    ax.plot(dataset_scenario1[keySAvariable], dataset_scenario1['CLsByMag'],
                 label = 'Sales revenue ' + experimentLabel, 
                 linewidth = 2, 
                 color = cmap[1],
                 linestyle = '-',
                 alpha = 1)
    
    # =============================================================================
    # to show the legend 
    ax.legend(loc= 'best')
 
    # set range for X
    #ax.set_ylim([400, 420])
    
    
    ax = plt.subplot(1, 2, 2) # (rows, columns, panel number)

    ax.set_xlabel(xAxisLabel)
    ax.set_ylabel('Rate of salespeople')
    #ax.set_title('Salespeople', fontweight='bold');
        
# =============================================================================
#     
#     
# # =============================================================================
    ax.plot(dataset_scenario1[keySAvariable], dataset_scenario1['PeopleWithBonus'],
                    label = 'Obtained bonus ' + experimentLabel, 
                    linewidth = 3, 
                    color = cmap[2],
                    linestyle = '-',
                    alpha = 0.5)
#      
# =============================================================================
# =============================================================================
    ax.plot(dataset_scenario1[keySAvariable], dataset_scenario1['PeopleWorkExtra'],
                 label = 'Working extra hours ' + experimentLabel, 
                 linewidth = 2, 
                 color = cmap[4],
                 linestyle = '-.',
                alpha = 0.8)
    
# =============================================================================
    # to show the legend 
    ax.legend(loc= 'best')
    
    # set range for X
    #ax.set_xlim([0, 400])

    
    
    ######################################################      
    
