#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""

Created on Wed Aug 23 17:00:46 2017

@author: mchica
"""
from matplotlib import pyplot as plt

from functionPlotSAOnVariableTwoPlots import plotSAOnVariableTwoPlots

######################################################   

# details for the experiment
experiment_name = '10portfolio_0bonus_0quota_NO_ORT'  #'portfolio10_0bonus_0quota'
experimentLabel = ''

#keySA = 'socialComparison'  
#keySA = 'avgRiskAversion'
#keySA = 'quota'
keySA = 'avgLeadsMagnitude'

#xAxisLabel = 'social comparison ($\gamma$ value)'
xAxisLabel = 'leads magnitude (avg. of Normal distr.)'
#xAxisLabel = 'risk aversion (average)'
#xAxisLabel = 'quota value'


title_experiment = ''
 
    
# size of the panel
fig = plt.figure(figsize=(10, 4), num=None, dpi=300, facecolor='w', edgecolor='k')
 
fig.suptitle(title_experiment, fontsize=12)



plotSAOnVariableTwoPlots (fig, "SA_" + keySA, "SA_" + keySA + "_" + experiment_name, experimentLabel, xAxisLabel)


######################################################      
    
file_plot = '../plots/plots_SA' + keySA + '_' + experiment_name + '.png'

# save figure to file
fig.savefig(file_plot)

