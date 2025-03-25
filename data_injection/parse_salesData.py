# -*- coding: utf-8 -*-
"""
Created on Tue Apr 30 14:42:24 2024

@author: Usuario
"""
import numpy as np
import pandas as pd
from matplotlib import pyplot as plt

#####################################
def normData (_data, _min, _max):
    
    normalizedData = (_data - _min) / (_max - _min)
    
    if normalizedData < 0 :
        normalizedData = 0
    
    if normalizedData > 1 :
        normalizedData = 1
    
    return normalizedData

#####################################


file_data='Dataset_ABM_28APR2024.csv'
file_output='parsed_filtered_Dataset_ABM_28APR2024_withAmountNorm_withUnknownFinalState.csv'

# load files using ; as separator
df = pd.read_csv(file_data, sep=';', header=0) 

minValueMagnitude = 1000           ## there are some 34 values of 0 which are also normalized to 0 as magnitude
maxValueMagnitude = 280000   ## there are like 13 values above 1mill, but they are outliers like 30mill.


# Filtra las filas por índice (columna "salesperson ID")
# Supongamos que "salesperson ID" es la columna 0 (cambia el índice según tu archivo)
unique_ids_leads = df.iloc[:, 1].unique()

print ("Hay " + str(len(unique_ids_leads)) + " ID diferentes de leads\n")


grupos_por_id = df.groupby('Local Opportunity ID')

dfNuevo = pd.DataFrame()

# counter to count the leads with no Created week
numNullCreatedWeeks = 0
numTotalSavedLeads = 0
numNullClosedWeeks = 0

# Itera sobre los grupos
for id_grupo, grupo in grupos_por_id:
    # Aquí 'id_grupo' es el valor del 'Local ID'
    # 'grupo' es un DataFrame con las filas correspondientes a ese ID

    rowsWon = grupo.loc[df['Won'] == 1]     
    if rowsWon.empty:
        cumWeekWon = -1
    else:
        cumWeekWon = rowsWon.iloc[0]['Cum. Weeknr.']
                               
    rowsCreated = grupo.loc[df['Created'] == 1]     
    if rowsCreated.empty:
        cumWeekCreated = -1
    else:
        cumWeekCreated = rowsCreated.iloc[0]['Cum. Weeknr.']
       
        
    rowsLost = grupo.loc[df['Lost'] == 1]     
    if rowsLost.empty:
        cumWeekLost = -1
    else:
        cumWeekLost = rowsLost.iloc[0]['Cum. Weeknr.']
        
                       
    rowsClosed = grupo.loc[df['Closed'] == 1]     
    if rowsClosed.empty:
        cumWeekClosed = -1
    else:
        cumWeekClosed = rowsClosed.iloc[0]['Cum. Weeknr.']
        
        
    rowsUnlikely = grupo.loc[df['Unlikely'] == 1]     
    if rowsUnlikely.empty:
        cumWeekUnlikely = -1
    else:
        cumWeekUnlikely = rowsUnlikely.iloc[0]['Cum. Weeknr.']
        
        
    rowsPipeline = grupo.loc[df['Pipeline'] == 1]     
    if rowsPipeline.empty:
        cumWeekPipeline = -1
    else:
        cumWeekPipeline = rowsPipeline.iloc[0]['Cum. Weeknr.']
        
        
    rowsBestCase = grupo.loc[df['Best Case'] == 1]     
    if rowsBestCase.empty:
        cumWeekBestCase = -1
    else:
        cumWeekBestCase = rowsBestCase.iloc[0]['Cum. Weeknr.']
        
        
    rowsCommit = grupo.loc[df['Commit'] == 1]     
    if rowsCommit.empty:
        cumWeekCommit = -1
    else:
        cumWeekCommit = rowsCommit.iloc[0]['Cum. Weeknr.']
        
        
    # we calculate the certainty4conv as the last update value among unlikely, pipeline, cumWeekBC, or Commit
    if (cumWeekCommit >= cumWeekBestCase) and (cumWeekCommit >= cumWeekPipeline) and (cumWeekCommit >= cumWeekUnlikely):
        certainty4Conv = 0.8
    else:
        if (cumWeekBestCase > cumWeekCommit) and (cumWeekBestCase >= cumWeekPipeline) and (cumWeekBestCase >= cumWeekUnlikely):
            certainty4Conv = 0.4
        else:
            if (cumWeekPipeline > cumWeekBestCase) and (cumWeekPipeline > cumWeekCommit) and (cumWeekPipeline >= cumWeekUnlikely):
                certainty4Conv = 0.2
            else:
                certainty4Conv = 0
    
    # in case we have no event at all for conv update, we set it to -1 to double check or remove these leads later
    if (cumWeekCommit == cumWeekBestCase) and (cumWeekCommit == cumWeekPipeline) and (cumWeekCommit == cumWeekUnlikely):
        certainty4Conv = -1
        
    # we also check if there is no 'Created' week (== -1). In that case, we don't save the lead
    if (cumWeekCreated != -1):
        
        if (cumWeekClosed != -1):
            
            # finally, if for those leads with creation and closing, we have a conv cert of -1, we set it to 0
            if (certainty4Conv == -1):                
                certainty4Conv = 0
            
            converted = -1
            if (cumWeekWon > -1):
                converted = 1
                
                if  (cumWeekLost > -1):
                    # error: both are > -1
                    converted = 2
            else:
                if (cumWeekLost > -1):
                    converted = 0
                else:
                    # error: both values are -1
                    converted = -1

            # only saving if the lead is either won or lost or unknown (-1)
            #if (converted == 1 or converted == 0 or converted == -1):
                
            # normalize amount to [0,1]
            normalizedAmount = normData(float(grupo.iloc[0]['Amount']), minValueMagnitude, maxValueMagnitude)
            
            # newLocalID = {'LeadID':id_grupo, 'SalespersonID':grupo.iloc[0]['Salesperson ID'], 'Amount':grupo.iloc[0]['Amount'], 'BusinessModel':grupo.iloc[0]['Business Model'], 
            newLocalID = {'LeadID':id_grupo, 'Amount':normalizedAmount, 'BusinessModel':grupo.iloc[0]['Business Model'],     
                          'WeekCreated':cumWeekCreated, 'WeekClosed':cumWeekClosed, 'WeekLost':cumWeekLost, 'WeekWon':cumWeekWon, 
                          'WeekUnlikely-0':cumWeekUnlikely, 'WeekPipeline-20':cumWeekPipeline, 'WeekBestCase-40':cumWeekBestCase, 
                          'WeekCommit-80':cumWeekCommit, 'CertaintyForConv':certainty4Conv, 'ConvertedLead':converted}
        
            dfNuevo = dfNuevo._append(newLocalID, ignore_index = True)   

            numTotalSavedLeads += 1
                
        else:
            numNullClosedWeeks += 1
    else:
        numNullCreatedWeeks += 1
        
    


    # Realiza el conteo de 'Lost' y 'Won' para este grupo
    # total_created = rowsCreated.sum()
    # total_closed = rowsClosed.sum()
    # total_perdidos = rowsLost.sum()
    # total_ganados = rowsWon.sum()

    # print(f"ID {id_grupo}:")
    # print(f"Total Created: {total_created}")
    # print(f"Total Closed: {total_closed}")
    # print(f"Total Lost: {total_perdidos}")
    # print(f"Total Won: {total_ganados}\n")

    #nueva_fila = [id_grupo, grupo.loc[0]['Amount'], semana_won, semana_created]
    
    #dfNuevo = dfNuevo.append(nueva_fila, ignore_index=True)
    
   
# save to a CSV file with the parsing
dfNuevo.to_csv(file_output, sep=';', index=False, encoding='utf-8')
 
print("Finished, saved " + str(numTotalSavedLeads) + " leads. \nFound " + str(numNullCreatedWeeks) 
      + " leads with no Created week; "+ str(numNullClosedWeeks) 
            + " leads with no Closed week in the dataset")
