# -*- coding: utf-8 -*-
"""
Created on Wed Apr 28 16:55:31 2021

@author: mchs
"""


import numpy as np
from matplotlib import pyplot as plt

import pandas as pd
import seaborn as sns
import math as mt

######################################################      
def plotHeatmapSAOn2Variables (fileName1, fileName2, experimentName, titleString, variable1, variable2, options): 
    
    ######################################################    
    # once we have the averaged values, we plot them in grid of plots
    
    # to see all the styles:  print(plt.style.available)
    plt.style.use('seaborn-pastel')

    
    cmapString = "YlGnBu"
    
    if options == "compare":
        cmapString = "RdBu"
    
    # size of the panel
    fig = plt.figure(figsize=(11, 9), dpi=500, facecolor='w', edgecolor='k')
    fig.suptitle(titleString, fontsize=10)
    
    # add additional space between subplots
    fig.subplots_adjust(hspace=.5)
    
    
    plt.rcParams.update({'font.size': 8})
    plt.rcParams.update({'font.family': 'Arial'})

    numColVar1 = 2
    numColVar2 = 4
    numColFinalConvLeads = 6
    numColFinalConvLeadsByMag = 9
    numColFinalFallOffs = 12
 
    
    sns.set(font_scale=0.8)
    
    # load files using ; as separator
    dataset_SA = pd.read_csv(fileName1, sep=';', header=None)
        
    df = pd.DataFrame.from_dict(np.array([dataset_SA.iloc[:, numColVar1], 
                                          dataset_SA.iloc[:, numColVar2], 
                                          dataset_SA.iloc[:, numColFinalConvLeads],
                                          dataset_SA.iloc[:, numColFinalConvLeadsByMag],
                                          dataset_SA.iloc[:, numColFinalFallOffs] ]).T)
    
    df.columns = [variable1["key"], variable2["key"],'convLeads','convLeadsByMag','fallOffs']
    
    
    if options == "compare":
       
        dataset_SA2 = pd.read_csv(fileName2, sep=';', header=None)
         
        df2 = pd.DataFrame.from_dict(np.array([dataset_SA2.iloc[:, numColVar1], 
                                               dataset_SA2.iloc[:, numColVar2], 
                                               dataset_SA2.iloc[:, numColFinalConvLeads],
                                               dataset_SA2.iloc[:, numColFinalConvLeadsByMag],
                                               dataset_SA2.iloc[:, numColFinalFallOffs]]).T)
         
        df2.columns = [variable1["key"], variable2["key"],'convLeads','convLeadsByMag','fallOffs']
         
         
    listOfOptions = ['convLeads','convLeadsByMag','fallOffs']
     
    numColsSubplot = 2
    numRowsSubplot = mt.ceil(len(listOfOptions) / 2)
    
    plotIndex = 1
    for valueOption in listOfOptions:
        
    
        ##  df[valueOption] = (pd.to_numeric(df[valueOption]) / noOfAgents) * 100 
        if options == "compare":
            ## df2[valueOption] = (pd.to_numeric(df2[valueOption]) / noOfAgents) * 100 
            df[valueOption] = df2[valueOption] - df[valueOption]
        
        ######################################################    
        ### PLOT EVERY OPTION IN THE LIST AS A NEW SUBPLOT
        
        ax = plt.subplot(numRowsSubplot, numColsSubplot, plotIndex) # (rows, columns, panel number)
        plotIndex = plotIndex + 1
    
        pivotted = df.pivot(variable1["key"],variable2["key"], valueOption)
        
        # rename column names
        pivotted.rename(columns={variable1["key"]:variable1["label"], variable2["key"]:variable2["label"], valueOption:valueOption}, inplace=True)


        # https://seaborn.pydata.org/generated/seaborn.heatmap.html
        
    
        if options == "compare":
            ax = sns.heatmap(pivotted, cmap=cmapString, cbar=True, xticklabels=5, yticklabels=3,
                         linewidths=.0, # vmin=0, vmax=100, 
                         center=0.00, 
                         cbar_kws={'label': 'value', "orientation": "vertical", "shrink": .6}) # this is for shrinking and placing the color bar horizontally
        else:
            ax = sns.heatmap(pivotted, cmap=cmapString, cbar=True, xticklabels=5, yticklabels=3,
                         linewidths=.0, # vmin=0, vmax=100, 
                         cbar_kws={'label': 'value', "orientation": "vertical", "shrink": .6}) # this is for shrinking and placing the color bar horizontally
       
        # this is to set 0,0 at the bottom left position https://stackoverflow.com/questions/34232073/seaborn-heatmap-y-axis-reverse-order
        ax.invert_yaxis()  
    
        ax.set_title('Final ' + valueOption, fontsize=8);
          
        ax.get_yaxis().set_visible(True)  
  
        ax.set_ylabel(variable1["label"] + " values", fontname="Arial", fontsize=8)
        ax.set_xlabel(variable2["label"] + " values", fontname="Arial", fontsize=8)
    
    
        # Rotate the tick labels and set their alignment.
        plt.setp(ax.get_xticklabels(), rotation=45, ha="right", rotation_mode="anchor")
  

    
    # save figure to file  
    
    fileNameSAHeatmap = '../plots/heatmap_' + experimentName + '_' +  variable1["key"] + '_' + variable2["key"]  + '.png'  
    fig.savefig(fileNameSAHeatmap)
    
######################################################      
