#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""

Script to generate evolution plots

Created on Wed Aug 23 17:00:46 2017

@author: mchica
"""

import pandas as pd
from matplotlib import pyplot as plt
from scipy.stats import norm

import numpy as np

######################################################   

# details for the experiment

initMC = 0
noMCs = 10

maxValue = 350
minValue = 250
binwidth = 10



colorKey = 0

keyStringExperiment = 'simple_0bonus_0quota'

fileNameString = keyStringExperiment + '.png'
experiment_name = '../logs/agents/SalespeopleMicrOutput_' + keyStringExperiment 

titlePlot = "Histogram with the aggregated sales (no bonus, no quota)"

            

######################################################    
    
# to see all the styles:  print(plt.style.available)
plt.style.use('seaborn-paper')

# size of the panel
fig = plt.figure(num=None, figsize=(8, 5), dpi=300, facecolor='w', edgecolor='k')
fig.suptitle(titlePlot, fontsize=10)

# add additional space between subplots
fig.subplots_adjust(hspace=.2)

cmap = ['#B72E24',   '#6633FF',   '#11C42F', '#fd8d3c']
cmapGreys = ['#f7f7f7', '#cccccc', '#969696', '#636363', '#252525']
    


for numberOfMC in range(initMC, (initMC + noMCs)):
           
    print('Loading MC run ' + str(numberOfMC))
      
    # 0.05
    file_data = '../logs/' + experiment_name + '.' + str(numberOfMC) + '.txt'
 
    
    # load files using ; as separator
    dataset = pd.read_csv(file_data, sep=';') 
        
    # from https://stackoverflow.com/questions/6986986/bin-size-in-matplotlib-histogram
    n, bins = np.histogram(dataset['aggrConvertedLeads'], bins=np.arange(minValue, maxValue + binwidth, binwidth))
     
    # n returns the values for the numBins. And bins, the cut value of each bin
    #n, bins = np.histogram(dataset['opinion'], bins = numBins)
      
    
    
    if numberOfMC == 0 :
        accCounts = n
    else:
        accCounts = accCounts + n

 
# average by MC runs  
accCounts = accCounts / noMCs

# after all the MC runs, plot the histogram with the accumulated


######################################################        
ax = plt.subplot(1, 1, 1) # (rows, columns, panel number)

# to show the legend for both scatters
#ax.legend()

# set labels and title of the subplot
ax.set_ylabel('Number of salespeople (size of $100$)')
ax.set_xlabel('Bins of aggregated converted leads (from ' + str(minValue) + ' to ' + str(maxValue) + ' sales, bindwidth of ' + str(binwidth) + ')')
   
 
    
xValues = np.arange(0, len(accCounts))

#for k in range(0, len(xValues)):
#    xValues[k] = (bins[k] + bins[k+1]) /2
    
#bars = ax.bar(xValues, accCounts, linewidth = 2, 
bars = ax.bar(xValues, accCounts, linewidth = 2, 
           color = cmap[0],
           linestyle = '-',
           alpha = 0.5)  
 

#pdf = norm.pdf(accCounts, 10, 1)
#ax.plot(xValues, pdf, color='r')

# to show the values of the bar         
ax.bar_label(bars, fontsize=5)

# set range for Y
# ax.set_ylim([0, 30])

# set range for X
# ax.set_xlim([0, 20])

    

######################################################      

# save figure to file
fig.savefig('../plots/' + fileNameString)



