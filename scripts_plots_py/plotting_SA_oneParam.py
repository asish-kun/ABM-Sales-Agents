#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""

Created on Wed Aug 23 17:00:46 2017

@author: mchica
"""


from functionPlotSAOnVariable import plotSAOnVariable

######################################################   

# details for the experiment
experiment_name = '10portfolio_socialComparison0_UniformLeads'

#keySA = 'socialComparison'
#keySA = 'avgRiskAversion'
keySA = 'quota'

title_experiment = 'SA on ' + keySA + ' (' + experiment_name + ')'
 
plotSAOnVariable ("SA_" + keySA, "SA_" + keySA + "_" + experiment_name, title_experiment)

