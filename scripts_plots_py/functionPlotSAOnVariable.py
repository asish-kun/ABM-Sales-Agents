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
def plotSAOnVariable (keySA, experiment_name, title_experiment):
        
    cmap = ['#d7191c', '#fdae61', '#abd9e9', '#2c7bb6', '#8c73b6']

    plt.style.use('default')
            
    # files in-out
    file_data = '../logs/SummaryMCruns_' + experiment_name + '.txt'
    file_plot = '../plots/plots_' + experiment_name + '.png'
      
    keySAvariable = keySA + 'Value'
        
    numColVar1 = 2
    numColFinalConvLeads = 4
    numColFinalConvLeadsByMag = 7
    numColFinalFallOffs = 10
    numColFinalSalesRevenue = 13
    numColFinalWorkExtra = 16    
    numColFinalWithBonus = 19
    
       
    # load files using ; as separator
    initial_dataset = pd.read_csv(file_data, sep=';', header=None) 
            
    dataset_scenario1 = pd.DataFrame.from_dict(np.array([initial_dataset.iloc[:, numColVar1], 
                                                         initial_dataset.iloc[:, numColFinalConvLeads], 
                                                         initial_dataset.iloc[:, numColFinalConvLeadsByMag], 
                                                         initial_dataset.iloc[:, numColFinalWorkExtra], 
                                                         initial_dataset.iloc[:, numColFinalWithBonus], 
                                                         initial_dataset.iloc[:, numColFinalSalesRevenue]]).T)
    
    dataset_scenario1.columns = [keySAvariable,'CLs','CLsByMag','PeopleWorkExtra','PeopleWithBonus','SalesRev']
    
    dataset_scenario1['CLs'] = pd.to_numeric(dataset_scenario1['CLs'])    
    dataset_scenario1['CLsByMag'] = pd.to_numeric(dataset_scenario1['CLsByMag'])        
   # dataset_scenario1['FLs'] = pd.to_numeric(dataset_scenario1['FLs'])         
    dataset_scenario1['PeopleWithBonus'] = pd.to_numeric(dataset_scenario1['PeopleWithBonus'])    
    dataset_scenario1['SalesRev'] = pd.to_numeric(dataset_scenario1['SalesRev'])    
    dataset_scenario1['PeopleWorkExtra'] = pd.to_numeric(dataset_scenario1['PeopleWorkExtra'])    
    
    #dataset_scenario1['avgMagByLead'] = dataset_scenario1['CLsByMag'] / dataset_scenario1['CLs']

    
    ######################################################    
    # once we have the averaged values, we plot them in grid of plots
    
    # to see all the styles:  print(plt.style.available)
    plt.style.use('seaborn-paper')
    
    # size of the panel
    fig = plt.figure(num=None, dpi=100, facecolor='w', edgecolor='k')
     
    #fig.suptitle(title_experiment, fontsize=14)
    
    # add additional space between subplots
    fig.subplots_adjust(hspace=.3)
    
    
    #Scatter the points
      
    ######################################################        
    ax = plt.subplot(1, 1, 1) # (rows, columns, panel number)
        
    ax.plot(dataset_scenario1[keySAvariable], dataset_scenario1['CLs'],
                label = 'Converted leads', 
                linewidth = 2, 
                color = cmap[2],
                linestyle = ':',
                alpha = 0.5)
# =============================================================================
#     
#     
# # =============================================================================
    ax.plot(dataset_scenario1[keySAvariable], dataset_scenario1['PeopleWithBonus'],
                    label = 'PeopleWithBonus', 
                    linewidth = 2, 
                    color = cmap[3],
                    linestyle = '-',
                    alpha = 0.5)
#      
# =============================================================================
# =============================================================================
    ax.plot(dataset_scenario1[keySAvariable], dataset_scenario1['PeopleWorkExtra'],
                 label = 'Rate of people working extra', 
                 linewidth = 2, 
                 color = cmap[4],
                 linestyle = '-.',
                alpha = 0.5)
    
# =============================================================================
    ax.plot(dataset_scenario1[keySAvariable], dataset_scenario1['CLsByMag'],
                 label = 'total sales mult. mag', 
                 linewidth = 2, 
                 color = cmap[1],
                 linestyle = '-',
                 alpha = 1)
 
# =============================================================================
    # to show the legend for both scatters
    ax.legend(loc= 'center right')
    
    # set labels and title of the subplot
    ax.set_ylabel('Indicator')
    ax.set_title(title_experiment, fontweight='bold');
    ax.set_xlabel(keySA)
        
    # set range for X
    #ax.set_xlim([0, 400])

    # set range for X
    #ax.set_ylim([100, 800])
    
    
    ######################################################      
    
    # save figure to file
    fig.savefig(file_plot)

    
    ######################################################      
    
