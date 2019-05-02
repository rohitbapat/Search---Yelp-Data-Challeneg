# -*- coding: utf-8 -*-
"""
Created on Sat Apr 13 19:19:48 2019

@author: rohit, soumya, swarnima
"""

import pickle as pk
import numpy as np
import json
import pandas as pd
import re
from sklearn.feature_extraction.text import CountVectorizer
import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import TfidfVectorizer
import matplotlib.pyplot as plt
import pymongo
from pymongo import MongoClient

### Code to get data from collection
### Add part to load json files to MongoDB
'''
After installing mongodb we used the json files of Yelp Data to load the database and then used mongo import
of python to load it as a dataframe
DB Name name : Yelp_Data
We used the following command for loading json file

mongoimport --db Yelp_Data --collection Review_Data --file review.json
mongoimport --db Yelp_Data --collection Tip_Data --file tip.json
mongoimport --db Yelp_Data --collection Business_Data --file business.json 
'''



client = MongoClient()
db = client.get_database('Yelp_Data')

### Load data from pickle files only from business.json
collection = db.get_collection('Business_Data')
df = pd.DataFrame(list(collection.find()))
df = df[df['city'] == 'Las Vegas']
df = df.reset_index()

### List of cuisines
cuisines = ['Restaurants','Food','Indian','Chinese','Pakistani','Mexican','American','Middle Eastern','Thai','Italian','Vietnamese','Sri Lankan','Canadian','Mediterranean','Korean','Japanese','Lebanese','Ethiopian','British','Singaporean']
###

# new categories column with the required cuisine list only
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

# sort the new categories alphabetically
df['new categories'] = [sorted(x) for x in df['new categories']]

# Load review data
collection = db.get_collection('Review_Data')
review_df = pd.DataFrame(list(collection.find()))


review_df['text'] = review_df.groupby(['business_id']).agg({'text':lambda x:' '.join(x)})

# merge the review data with the businesses of Las Vegas
review_df = review_df.merge(df[['new categories','business_id']], how = 'left', on = 'business_id')
review_df['new categories'].dropna(inplace = True)
review_df[['reviews']] = review_df[['reviews']].applymap(lambda x: x.replace("\n", "").replace("\r", ""))
review_df[['reviews']] = review_df[['reviews']].applymap(lambda x: re.sub(r'[^\d\w\s]', '', x))
review_df.dropna(inplace = True)

# Load Tip Data
collection = db.get_collection('Tip_Data')
tip_df = pd.DataFrame(list(collection.find()))

# Merge tip and review data
review_df = review_df.merge(tip_df[['text','business_id']], how = 'left', on = 'business_id')
review_df.dropna(inplace = True)
review_df[['text']] = review_df[['text']].applymap(lambda x: re.sub(r'[^\d\w\s]', '', x))

# dump to csv file for Task 1
review_df.to_csv("Combined_Data.csv")

