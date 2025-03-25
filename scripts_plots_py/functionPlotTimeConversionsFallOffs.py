# -*- coding: utf-8 -*-
"""
Created on March 2022 by mchica

PROJECT WASP 

Function to plot temporal evolution for sales conversions

@author: mchica

"""

import pandas as pd


def plotEvolutionConvFallOffs (ax, file_scenario, textLabel, numberOfAgents, linestyleValue, options, cmapGreyIndex, alphaValue):

    cmap = ['#b35806', '#f1a340', '#fee0b6', '#d8daeb', '#998ec3', '#542788']
    
    cmapGreys = ['#f7f7f7', '#cccccc', '#969696', '#636363', '#252525']

    ## 'decimal' is used for using Spanish regional convention of ',' for floats 
    # https://pandas.pydata.org/pandas-docs/stable/reference/api/pandas.read_csv.html
    dataset_scenario = pd.read_csv(file_scenario, sep=';', decimal='.')
    
  
    ### plotting the lines
    
   
    colorKey = 0
    
    # no. of converted leads
    
    ax.plot(dataset_scenario['step'], dataset_scenario['CLsAvg'],
                   label = 'No. of converted leads ' + textLabel, 
                   linewidth = 2, 
                   color = cmap[colorKey],
                   linestyle = linestyleValue,
                   alpha = 1)
     
    if options.find("noRange") == -1:

           #  filling area between lines: https://matplotlib.org/gallery/lines_bars_and_markers/fill_between_demo.html
           ax.fill_between(dataset_scenario['step'], dataset_scenario['CLsMin'],  
                           dataset_scenario['CLsMax'], color = cmap[colorKey], alpha=0.5)

    colorKey = colorKey + 2

     # leads rated by magnitude
     
    ax.plot(dataset_scenario['step'], dataset_scenario['CLsByMagAvg'],
                  label = 'Converted leads rated by magnitude ' + textLabel, 
                  linewidth = 2, 
                  color = cmap[colorKey],
                  linestyle = linestyleValue,
                  alpha = alphaValue)
     
    if options.find("noRange") == -1:

          #  filling area between lines: https://matplotlib.org/gallery/lines_bars_and_markers/fill_between_demo.html
          ax.fill_between(dataset_scenario['step'], dataset_scenario['CLsByMagMin'],  
                          dataset_scenario['CLsByMagMax'], color = cmap[colorKey], alpha=0.5)

    colorKey = colorKey + 1

     # leads rated by magnitude
     
    ax.plot(dataset_scenario['step'], dataset_scenario['PeopleWithBonusAvg'],
                  label = 'People with bonus ' + textLabel, 
                  linewidth = 2, 
                  color = cmap[colorKey],
                  linestyle = linestyleValue,
                  alpha = alphaValue)
     
    if options.find("noRange") == -1:
    
          #  filling area between lines: https://matplotlib.org/gallery/lines_bars_and_markers/fill_between_demo.html
          ax.fill_between(dataset_scenario['step'], dataset_scenario['SalesRevMin'],  
                          dataset_scenario['SalesRevMax'], color = cmap[colorKey], alpha=0.5)
    
    colorKey = colorKey + 1

     # fall off
     
    ax.plot(dataset_scenario['step'], dataset_scenario['PeopleWorkExtraAvg'],
                  label = 'People working extra ' + textLabel, 
                  linewidth = 2, 
                  color = cmap[colorKey],
                  linestyle = linestyleValue,
                  alpha = alphaValue)
     
    if options.find("noRange") == -1:

          #  filling area between lines: https://matplotlib.org/gallery/lines_bars_and_markers/fill_between_demo.html
          ax.fill_between(dataset_scenario['step'], dataset_scenario['FLsMin'],  
                          dataset_scenario['FLsMax'], color = cmap[colorKey], alpha=0.5)




    ######################################################      
    
