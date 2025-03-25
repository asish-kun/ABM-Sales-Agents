#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""

Script to generate heatmaps 

Created on Wed Aug 27 17:00:46 2018

@author: mchica
"""

from functionPlot4HeatmapOptionsSAOn2Variables import plotHeatmapSAOn2Variables
            

######################################################  

SA_1 = 'thresholdForConversion'
SA_2 = 'decayRateLeads'
    

title = 'SA on ' + SA_1 + ' and '+ SA_1 

experimentName = 'simple_SA_' + SA_1 + '_' + SA_2 

fileName1 = '..\logs\SummaryMCruns_' + experimentName + '.txt'

fileName2 = '..\logs\SummaryMCruns_' + experimentName + '.txt'


compareOption='noCompare'

variable1 ={
        "key": SA_1,
        "label": SA_1
        }

variable2 ={
        "key": SA_2,
        "label": SA_2
        }

# options 'compare' 
plotHeatmapSAOn2Variables (fileName1, fileName2, experimentName, title, variable1, variable2, compareOption)


