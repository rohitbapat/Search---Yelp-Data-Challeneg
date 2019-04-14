# -*- coding: utf-8 -*-
"""
Created on Sat Apr 13 19:19:48 2019

@author: rohit
"""

import pickle as pk
import numpy as np
import json
import pandas as pd

### Code to get data from collection
import pymongo
from pymongo import MongoClient
client = MongoClient()
db = client.get_database('Yelp_Data')
collection = db.get_collection('Tip_Data')
tip_df = pd.DataFrame(list(collection.find()))
###


### Load data from pickle files
df = pk.load(open("D:\\Search\\YelpData\\business_data.pkl",'rb'))
###

### List of cuisines
cuisines = ['Restaurants','Food','Indian','Chinese','Pakistani','Mexican','American','Middle Eastern','Thai','Italian','Vietnamese','Sri Lankan','Canadian','Mediterranean','Korean','Japanese','Lebanese','Ethiopian','British','Singaporean']
###

new_cat = []

food_df = list()
for cui in cuisines:
    food_df.extend([sorted(str(x).split(', ')) for x in (df[df['categories'].str.contains('.*'+cui+'.*',regex = True) == True]['categories'])])

new_categs = list()
for i in range(len(df)):
    cuisine_list = str(df.loc[i,'categories']).split(', ')
    new_list = list()
    row_val = list()
    for item in cuisine_list:
        if item in cuisines:
            row_val.append(item)
    new_categs.append(row_val)
df['new categories'] = new_categs
df['new categories'] = df['new categories'].apply(lambda x: np.nan if len(x) == 0 else x)
df.dropna(inplace = True)  

df['new categories'] = [sorted(x) for x in df['new categories']]
unique_data = [list(x) for x in set(tuple(x) for x in df['new categories'])]

review_df = pk.load(open("D:\\Search\\YelpData\\review_data.pkl",'rb'))
review_df = review_df.merge(df[['new categories','business_id']], how = 'left', on = 'business_id')
review_df['new categories'].dropna(inplace = True)

tip_df = tip_df.merge(df[['new categories','business_id']], how = 'left', on = 'business_id')
tip_df.dropna(inplace = True)

review_df.to_csv("D:\\Search\\YelpData\\Project\\All_Data_Task1\\Review_Data.csv")
tip_df.to_csv("D:\\Search\\YelpData\\Project\\All_Data_Task1\\Tip_Data.csv")


