#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""

Script to generate evolution plots for ABM4Sales

Created on Wed Aug 23 17:00:46 2017

@author: mchica
"""

from matplotlib import pyplot as plt

from functionPlotTimeConversionsFallOffs import plotEvolutionConvFallOffs


plt.rcParams.update({'font.size': 6})
plt.rcParams.update({'font.family': 'Arial'})


######################################################   

numberOfAgents = 1
totalSteps = 1600

expString1= 'simple' #'simple_window_5'
#expString2= 'simple_r_0_5' #'simple_window_10'
#expString3= 'simple_r_0_9' #'simple_window_20'
#expString4= 'simple' #'simple_window_40'

titleString = 'Temporal evolution of leads and salespeople'

fileNameString = 'evolution_sales_model.png'


# files for scenarios
file_scenario1 = '../logs/TimeSeriesMCruns_' + expString1 + '.txt'
#file_scenario2 = '../logs/TimeSeriesMCruns_' + expString2 + '.txt'
#file_scenario3 = '../logs/TimeSeriesMCruns_' + expString3 + '.txt'
#file_scenario4 = '../logs/TimeSeriesMCruns_' + expString4 + '.txt'


######################################################    
# once we have the averaged values, we plot them in grid of plots

# to see all the styles:  print(plt.style.available)
plt.style.use('seaborn-paper')

# size of the panel
fig = plt.figure(num=None, dpi=500, facecolor='w', edgecolor='k')

# add additional space between subplots
fig.subplots_adjust(hspace=.4)


  
######################################################        
ax = plt.subplot(1, 1, 1) # (rows, columns, panel number)


plotEvolutionConvFallOffs (ax, file_scenario1, '', numberOfAgents, ':', 'noRange', 4, 1)
#plotEvolutionConvFallOffs (ax, file_scenario2, '(r=0.5)', numberOfAgents, '-', 'noRange', 4, 1)
#plotEvolutionConvFallOffs (ax, file_scenario3, '(r=0.9)', numberOfAgents, '-.', 'noRange', 4, 1)
#plotEvolutionConvFallOffs (ax, file_scenario4, '(d=0.1)', numberOfAgents, '--', 'noRange', 4, 1)


# to show the legend for both scatters
ax.legend()

# set labels and title of the subplot
ax.set_ylabel('Number of leads', fontname="Arial", fontsize=8)
ax.set_xlabel('Time-steps (days)', fontname="Arial", fontsize=8)
ax.set_title(titleString, fontsize=10);

ax.set_ylim([0, 100]);

#ax.set_xlim([0, totalSteps]);

######################################################      

# save figure to file
fig.savefig('../plots/' + fileNameString)


