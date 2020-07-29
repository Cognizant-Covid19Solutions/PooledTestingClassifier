# PooledTestingClassifier

Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.

*Applicable datasets are made available by their respective providers pursuant to the terms set forth on the applicable sites on which they are made available.*

## DEVELOPMENT APPROACH
 
In the current global COVID-19 pandemic scenario, widespread testing for COVID-19 will continue to play a key role in preventing the spread of infection. For countries and states where aggressive testing is needed for more than just symptomatic cases, test kits are unavailable or in limited supply and therefore, alternative testing protocols are called for. At this time, many countries are facing a shortage of test kits forcing them to conduct fewer tests, which may result in unknown and unknowable risks such as asymptomatic carriers or pre-symptomatic individuals who move away from self-isolation and into more day to day contact with their communities.  One method advocated to help the screening process and increase the current COVID-19 testing capacity is to adopt a “Pooled Testing” strategy.  Pooled Testing is a method that can enable simultaneous testing of dozens of samples combined together, based on the representative risks of the members of the pool, and may help accelerate the rate of testing and detect the presence of   COVID-19 in the pooled populations. 
 
**Support for a Pooled Testing Model for COVID:**

Pooled testing is a procedure where individual blood specimens are pooled into groups based on pre-selected criteria to test for a binary response- for e.g. COVID positive or COVID negative.  Pooled testing can be useful in guiding testing strategies for large populations with limited testing resources.  
The premise of this Pooled Testing strategy is that, if a specimen sample of the pool test is negative, then all individuals within the pool are most likely negative for the presence of COVID19. 
If the pooled sample is positive for the presence of COVID, then testing of each member of the Pool should be done to determine the positive individuals from the pool, which in turn helps focus testing strategies for contacts and exposure pathways of the positive individuals.  
At this time, while being discussed by public health officials and experts, Pooled testing is not currently an established means of detecting COVID positive populations within a largely untested population where testing kits are scarce.    
However, the methodology proposed in this COVID Pooled Testing Model is a simple model which takes into account risk factors like age, comorbidities, lifestyle etc. The foundation of this COVID Pooled Testing Model is the use of pooled testing in a wide variety of infectious disease screening settings and results from those studies [1-5]. Many aspects of the COVID Pooled Testing Model are based on very recent research at the German Red Cross Blood Donor Service in Frankfurt headed by Professor Erhard Seifried and the Institute for Medical Virology at the University Hospital Frankfurt at Goethe University headed by Professor Sandra Ciesek, which reported developing a procedure for increasing worldwide testing capacities for detecting SARS-CoV-2 [6].  
In addition, while there are various Pooled Testing Methods, they can generally be classified into two types-(1) Non- Informative Methods (i.e.) Methods which assume the population to be homogeneous (2) Informative Methods (i.e.) Methods which assume the population to be heterogeneous [7]. 
 
**Technical Specifications for Pooling Model Development:**

In this COVID Pooled Testing Model, we have first created an algorithm to divide the population into three risk groups (High, Medium, Low) for determining the pools for testing.  The goal of this algorithm is to attempt to achieve optimal use of testing resources while attaining high prevalence detection rates. The algorithm developed falls under the classification of informative methods. 
We begin with the collection of individual level data considering the following attributes: **Age, Gender, Height, Weight, Existing Medical Conditions, Travel History, Zip code, Residence Type**.   Next, the *social vulnerability index (SVI)* for an area is obtained by using the Zip Code. Then, a risk score is computed by considering the Age range, Type of residence, Travel history, Medical conditions, SVI index values and BMI value.  For example, scores for age range are calculated by giving a weightage between 1 to 4. 

Once the score is created, based on the Score, individuals are then divided into three risk groups:  **Score less than 20 – Low Risk, score between 20 to 27 – Medium Risk, Score greater than 28 – High Risk**. 
 
 
 
Ranking is based on certain assumptions such as if an individual’s age is greater than 60 or Travel history is affirmative, or has Symptoms of COPD or Symptoms of Asthma, then the individual  is grouped into the high risk category automatically. 
 
Once initial group in done based on risk profile for each of the individuals providing a sample, the individuals are grouped into different pools with maximum pool size for different risk categories. Low risk individuals are grouped into pool sizes of 32, Medium risk into pool sizes of 11 and High risk individuals are grouped into pool sizes of 4. The optimal pool sizes for the various risk groups have been arrived at based on Dr. Hanel’s proposed methodology for boosting testing efficiency and capacity for COV-SARS2 [8].  
   
 
### Procedure & Process Flow for Developing Pools for COVID Testing
 
**Creating Risk Pools:**

To process individual level data, segregate into three risk zones (High, Medium, and Low) and group into different pools for testing. 
 
1. Individual level data is obtained using JavaScript based UI as Form entry or bulk file upload. Below are the input details obtained: 
Age, Sex, Height, Weight, Existing Medical Conditions, Travel History, Zip code, Residence Type. 

2. Get Social Vulnerability Index (SVI) for a particular area using Zip Code. 

3. Calculate a score by considering Age range, Type of residence, Travel history, Medical conditions, SVI index values and BMI value. 

4. Based on the Score, Divide the individuals into different Risk zones as per below conditions. 
      Less than 20   - Low 
      From 20 to 27 - Medium 
      Greater than 28 - High 
      
5. Group the individuals into High category bypassing other conditions if any of the below is valid. 
      Age greater than 60 
      Travel history - Yes 
      Symptoms of COPD 
      Symptoms of Asthma 
      
6. Group the individuals into different pools with maximum pool size for different zones given below. Leftover IDs are added to last pool. 
      High - 4 
      Medium - 11 
      Low - 32   

## CODE DETAILS

The following softwares needs to be installed in order to run PooledTestingClassiifer application.

1. Java JDK 1.8 or above
2. H2 Database
3. Apache Tomcat 8 or above
4. Apache Maven
5. Python 3.6 or above

Upon installation of the above pre-requisties perform the maven build on the pom.xml file present in the root of repository.

To Deploy the web application place the war file generated from the maven build into the Tomcat Webapps folder.

### CREATION OF RISK POOLS FOR TESTING: 

The Pooled Testing algorithm processes individual level data and segregates the same into three risk profiles (High, Medium, and Low) based on which, testing pools are created. 

The Python code for Pooled Testing (risk_scoring.py) can be run on Python (>3.6) and leverages the following packages: 

Pandas (>=1.0.3)  

Numpy (>=1.18.4) 

This code also requires the input data of individuals to be processed in a specific format, shown as follows: 

 

A Risk score for each individual is generated based on a combination of different points of information pertaining to the individual. 

For example, height and weight are used to calculate BMI Index which in turn converted into a range and assigned weightage based on the respective range. 

Among other factors, the zip code of residence is also used, in order to map the individual to a Social Vulnerability Index value pertaining to their location (Dataset link is provided at last) 

Based on the Risk score calculated, each individual is then grouped to a particular risk profile (High, Medium or Low) based on the expected vulnerability to COVID-19. These grouped individuals are then further segmented into different test pools of varying sizes, depending on the risk profile and multiple other common factors. 

The output file generated from the python code is given to the JavaScript based UI which will display the data along with the pool division. 

 

Required datasets:  

 

SVI Dataset - https://svi.cdc.gov/data-and-tools-download.html 

 

(No direct download link. Go to Data Download section and select Year – 2018, Geography – United States, Geography Type – Counties, File Type – CSV. Documentation for the same can be found here: https://svi.cdc.gov/Documents/Data/2018_SVI_Data/SVI2018Documentation.pdf) 

 
*Applicable datasets are made available by their respective providers pursuant to the terms set forth on the applicable sites on which they are made available.*

### NOTICE:  

*The suggested methodology and procedures in this COVID Pooled Testing Model are based on the cited research and the expertise of individuals at Cognizant Technology Solutions in developing testing models.*   

*The COVID Pooled Testing Model is being offered as an alternative to individual testing in areas  or communities where testing of every person is not feasible due to the scarcity of testing kits.*  

*The COVID Pooled Testing Model is not intended to substitute for advice or direction from local or national healthcare authorities regarding those populations who require individual testing, and has not been endorse,  assessed or cleared for use by any public health or regulatory authorities for its proposed use.*

>**References:**
>1. Blood testing. URL http://www.redcrossblood.org/learn-about-blood/what-happens-donated-blood/blood-testing, retrieved January 7, 2012 
>
>2. Dodd R, Notari E, Stramer S. Current prevalence and incidence of infectious disease markers and estimated window-period risk in the American Red Cross donor population. Transfusion. 2002; 42:975–979. [PubMed: 12385406]
>
>3. Gaydos C. Nucleic acid amplification tests for gonorrhea and chlamydia: practice and applications.Infectious Disease Clinics of North America. 2005; 19:367–386. [PubMed: 15963877] 
>
>4. Hourfar M, Themann A, Eickmann M, Puthavathana P, Laue T, Seifried E, Schmidt M. Blood screening for influenza. Emerging Infectious Diseases. 2007; 13:1081–1083. [PubMed: 18214186] 
>
>5. White D, Kramer L, Backenson P, Lukacik G, Johnson G, Oliver J, Howard J, Means R, Eidson M, Gotham I, et al. Mosquito surveillance and polymerase chain reaction detection of West Nile Virus, New York state. Emerging Infectious Diseases. 2001; 7:643–649. [PubMed: 11585526] 
>
>6. Pool testing of SARS-CoV-02 samples increases worldwide test capacities many times over | EurekAlert! Science News, https://eurekalerthttps://eurekalert.org/pub_releases/2020-03/guf-pto033020.php, 2020 
>
>7. An Overview of Pooled Testing Procedures with Application to Covid-19 Pandemic, Murali P, 2020, https://medium.com/@dr.padmamurali/an-overview-of-pooled-testing-procedures-with-applicationto-covid-19-pandemic-c01cc8fec617  
>
>8. Boosting test-efficiency by pooled testing strategies for SARS-CoV-2, Hanel  R,  Thurner S, 2020, arXiv:2003.09944  
 
