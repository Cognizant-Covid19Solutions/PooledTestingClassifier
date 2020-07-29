'''
Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
'''

import sys
script_dir = sys.path[0]
script_dir = r'C:\Users\HP\Desktop\COVID'
import pandas as pd
import numpy as np

raw_ip = pd.read_csv(script_dir+'\\input\\pool_data.csv')

raw_ip['Gender_Male'] = 0
raw_ip.loc[raw_ip['GENDER'] == 'Male' , 'Gender_Male'] = 1
raw_ip['Gender_Female'] = 0
raw_ip.loc[raw_ip['GENDER'] == 'Female' , 'Gender_Female'] = 1

raw_ip['Age_0_10'] = 0
raw_ip.loc[raw_ip['AGE'] <= 10 , 'Age_0_10'] = 1
raw_ip['Age_20_40'] = 0
raw_ip.loc[(raw_ip['AGE'] > 20) & (raw_ip['AGE'] <= 40), 'Age_20_40'] = 1
raw_ip['Age_40_60'] = 0
raw_ip.loc[(raw_ip['AGE'] > 40) & (raw_ip['AGE'] <= 60) , 'Age_40_60'] = 1
raw_ip['Age_60'] = 0
raw_ip.loc[raw_ip['AGE'] > 60 , 'Age_60'] = 1

raw_ip['Type_of_residence_Individual_home'] = 0
raw_ip.loc[raw_ip['HOUSEHOLD'] == 'Individual Home' , 'Type_of_residence_Individual_home'] = 1
raw_ip['Type_of_residence_Housing_in_structures_with_less_than_10_units'] = 0
raw_ip.loc[raw_ip['HOUSEHOLD'] == 'Housing in structures with less than 10 units' , 'Type_of_residence_Housing_in_structures_with_less_than_10_units'] = 1
raw_ip['Type_of_residence_Housing_in_structures_with_more_than_10_units'] = 0
raw_ip.loc[raw_ip['HOUSEHOLD'] == 'Housing in structures with more than 10 units' , 'Type_of_residence_Housing_in_structures_with_more_than_10_units'] = 1
raw_ip['Type_of_residence_Community_living'] = 0
raw_ip.loc[raw_ip['HOUSEHOLD'] == 'Community living' , 'Type_of_residence_Community_living'] = 1
raw_ip['Type_of_residence_Mobile_home'] = 0
raw_ip.loc[raw_ip['HOUSEHOLD'] == 'Mobile home' , 'Type_of_residence_Mobile_home'] = 1

raw_ip['Travel history - Yes'] = 0
raw_ip.loc[raw_ip['TRAVEL_HISTORY'] == 'Yes' , 'Travel history - Yes'] = 1

raw_ip['Existing_Health_condition_Hypertension'] = 0
raw_ip['Existing_Health_condition_COPD'] = 0
raw_ip['Existing_Health_condition_CKD'] = 0
raw_ip['Existing_Health_condition_Diabetics'] = 0
raw_ip['Existing_Health_condition_Asthmatic'] = 0
raw_ip['Existing_Health_condition_Cardiovascular_Disease'] = 0

raw_ip['MEDICAL_CONDITIONS'] = raw_ip['MEDICAL_CONDITIONS'].astype(str)
for index, row in raw_ip.iterrows(): 
    conditions_list = row["MEDICAL_CONDITIONS"].split(',')
    if('Hypertension' in conditions_list):
        raw_ip.at[index, 'Existing_Health_condition_Hypertension'] = 1
    if('COPD' in conditions_list):
        raw_ip.at[index, 'Existing_Health_condition_COPD'] = 1
    if('CKD' in conditions_list):
        raw_ip.at[index, 'Existing_Health_condition_CKD'] = 1
    if('Diabetics' in conditions_list):
        raw_ip.at[index, 'Existing_Health_condition_Diabetics'] = 1
    if('Asthmatic' in conditions_list):
        raw_ip.at[index, 'Existing_Health_condition_Asthmatic'] = 1
    if('CVD' in conditions_list):
        raw_ip.at[index, 'Existing_Health_condition_Cardiovascular_Disease'] = 1        

#risk_input = pd.read_excel(script_dir+'\\risk_calc_input.xlsx')
risk_input = raw_ip
zip_county = pd.read_csv(script_dir+'\\ZIP_COUNTY_032020.csv')
svi_county = pd.read_csv(script_dir+'\\SVI2018_US_COUNTY.csv')

fips = []
zip_code = []

risk_input['ZIPCODE'] = risk_input['ZIPCODE'].astype(str)
for x in risk_input['ZIPCODE']:
    if('-' in x):
        fips.append(x.split('-')[1].strip())
        zip_code.append(x.split('-')[0].strip())
    else:
        fips.append(np.nan)
        zip_code.append(x.strip())
risk_input['FIPS'] = fips
risk_input['ZIP'] = zip_code

risk_input['FIPS'] = risk_input['FIPS'].astype(float)
risk_input['ZIP'] = risk_input['ZIP'].astype(float)

SVI_lookup = pd.merge(svi_county[['FIPS','F_THEME1','F_THEME2','F_THEME3','F_THEME4']], 
		zip_county[['ZIP', 'COUNTY']], 
		left_on='FIPS',
		right_on='COUNTY',
		how='left')

SVI_lookup = SVI_lookup.drop(['FIPS','COUNTY'], axis = 1) 

SVI_lookup['F_THEME_SUM'] = SVI_lookup['F_THEME1']+SVI_lookup['F_THEME2']+SVI_lookup['F_THEME3']+SVI_lookup['F_THEME4']

SVI_lookup.sort_values(["ZIP","F_THEME_SUM"], axis = 0, ascending = False, 
             inplace = True, na_position ='last')

SVI_lookup = SVI_lookup.drop_duplicates('ZIP')

SVI_lookup = SVI_lookup.rename(columns={'F_THEME1':'F_THEME1_ZIP','F_THEME2':'F_THEME2_ZIP','F_THEME3':'F_THEME3_ZIP','F_THEME4':'F_THEME4_ZIP'})

final_DF = pd.merge(risk_input, svi_county[['FIPS','F_THEME1','F_THEME2','F_THEME3','F_THEME4']], on='FIPS', how='left')        
final_DF = pd.merge(final_DF, SVI_lookup, on='ZIP', how='left')

final_DF.loc[final_DF['F_THEME1'].isnull(),'F_THEME1'] = final_DF['F_THEME1_ZIP']
final_DF.loc[final_DF['F_THEME2'].isnull(),'F_THEME2'] = final_DF['F_THEME2_ZIP']
final_DF.loc[final_DF['F_THEME3'].isnull(),'F_THEME3'] = final_DF['F_THEME3_ZIP']
final_DF.loc[final_DF['F_THEME4'].isnull(),'F_THEME4'] = final_DF['F_THEME4_ZIP']

'''
final_DF['Distance from confirmed case 0-2KM'] = 1
final_DF['Distance from confirmed case 2-5KM'] = 0
final_DF['Distance from confirmed case > 5KM'] = 0

final_DF['Average Grade in the last 1 week - A'] = 0
final_DF['Average Grade in the last 1 week - B'] = 0
final_DF['Average Grade in the last 1 week - C'] = 0
final_DF['Average Grade in the last 1 week - D'] = 0
final_DF['Average Grade in the last 1 week - F'] = 1
'''

final_DF['Height (Meters)'] = final_DF['HEIGHT'] * 100
final_DF['BMI'] = (final_DF['WEIGHT']/2.205) / (final_DF['Height (Meters)'] * final_DF['Height (Meters)'])

final_DF['BMI_more_than_40'] = 0
final_DF['BMI_26_to_39'] = 0
final_DF['BMI_less_than_25'] = 0

final_DF.loc[final_DF['BMI'] <= 25 , 'BMI_less_than_25'] = 1
final_DF.loc[(final_DF['BMI'] > 26) & (final_DF['BMI'] <= 39), 'BMI_26_to_39'] = 1
final_DF.loc[(final_DF['BMI'] > 40) , 'BMI_more_than_40'] = 1

final_DF['Score'] = (
final_DF['Age_0_10']*1 + final_DF['Age_20_40']*2 + final_DF['Age_40_60']*3 + final_DF['Age_60']*4 + 
final_DF['Type_of_residence_Individual_home']*1 + final_DF['Type_of_residence_Housing_in_structures_with_less_than_10_units']*2	+ final_DF['Type_of_residence_Housing_in_structures_with_more_than_10_units']*3 + final_DF['Type_of_residence_Community_living']*4	+ final_DF['Type_of_residence_Mobile_home']*5 +
#final_DF['Distance from confirmed case 0-2KM']*3 + final_DF['Distance from confirmed case 2-5KM']*2 + final_DF['Distance from confirmed case > 5KM']*1 +
final_DF['Existing_Health_condition_Hypertension'] + final_DF['Existing_Health_condition_COPD'] + final_DF['Existing_Health_condition_Diabetics'] + final_DF['Existing_Health_condition_Asthmatic'] + final_DF['Existing_Health_condition_Cardiovascular_Disease'] +
final_DF['Travel history - Yes'] +
final_DF['F_THEME1'] + final_DF['F_THEME2'] + final_DF['F_THEME3'] + final_DF['F_THEME4'] +
#final_DF['Average Grade in the last 1 week - A'] * 0 + final_DF['Average Grade in the last 1 week - B'] * 1 + final_DF['Average Grade in the last 1 week - C'] * 2 + final_DF['Average Grade in the last 1 week - D'] * 3 + final_DF['Average Grade in the last 1 week - F'] * 4 +
final_DF['BMI_more_than_40'] * 3 + final_DF['BMI_26_to_39'] * 2 + final_DF['BMI_less_than_25'] * 1
)   

final_DF['Remarks'] = ''
for index, row in final_DF.iterrows():
    remarks = ''
    if(row['Age_60'] == 1):
        final_DF.at[index, 'Score'] = 40
        remarks = remarks + ' Age_greater_than_60'
    if(row['Travel history - Yes'] == 1):
        final_DF.at[index, 'Score'] = 40
        remarks = remarks + ' Travel_history'
    if(row['Existing_Health_condition_COPD'] == 1):
        final_DF.at[index, 'Score'] = 40
        remarks = remarks + ' COPD_Symptoms'
    if(row['Existing_Health_condition_Asthmatic'] == 1):
        final_DF.at[index, 'Score'] = 40
        remarks = remarks + ' Asthmatic_Symptoms'
    final_DF.at[index, 'Remarks'] = remarks
                        
final_DF.loc[final_DF['Score'] <= 19   , 'Risk'] = 'Low'
final_DF.loc[(final_DF['Score'] > 19) & (final_DF['Score'] < 28) , 'Risk'] = 'Medium'
final_DF.loc[final_DF['Score'] >= 28   , 'Risk'] = 'High'

final_DF.sort_values("Score", axis = 0, ascending = False, 
             inplace = True, na_position ='last')
final_DF['Pool Number'] = ''

last_val = 0

count = 0
total_count = len(final_DF[final_DF['Risk'] == 'High'].index)
for index, row in final_DF[final_DF['Risk'] == 'High'].iterrows(): 
	if(count+4 > total_count):
		final_DF.at[index, 'Pool Number'] = 'H'+last_val
	else:
		final_DF.at[index, 'Pool Number'] = 'H'+str(int(count/4)+1)
		last_val = str(int(count/4)+1)
	count=count+1

count = 0
total_count = len(final_DF[final_DF['Risk'] == 'Medium'].index)
for index, row in final_DF[final_DF['Risk'] == 'Medium'].iterrows():
	if(count+11 > total_count):
		final_DF.at[index, 'Pool Number'] = 'M'+ last_val
	else: 		
		final_DF.at[index, 'Pool Number'] = 'M'+str(int(count/11)+1)
		last_val = str(int(count/11)+1)
	count=count+1

count = 0
total_count = len(final_DF[final_DF['Risk'] == 'Low'].index)
for index, row in final_DF[final_DF['Risk'] == 'Low'].iterrows(): 		
	if(count+32 > total_count):
		final_DF.at[index, 'Pool Number'] = 'L'+ last_val
	else: 		
		final_DF.at[index, 'Pool Number'] = 'L'+str(int(count/32)+1)
		last_val = str(int(count/32)+1)
	count=count+1

op_cols = ['IND_ID', 'GROUP_ID', 'AGE', 'GENDER', 'ZIPCODE',
       'HEIGHT', 'WEIGHT', 'HOUSEHOLD', 'MEDICAL_CONDITIONS', 'TRAVEL_HISTORY',
       'Score', 'Risk', 'Pool Number', 'Remarks']

final_DF.sort_values("IND_ID", axis = 0, ascending = True, 
             inplace = True, na_position ='last')
        
final_DF[op_cols].to_csv(script_dir+'\\output\\risk_calc_output.csv',index=False)

'''
Authors: Sachin C S (Cognizant), Aman Chawla (Cognizant)

Copyright {2020} Cognizant Technology Solutions
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
'''