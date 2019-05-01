import numpy as np
import pandas as pd
import pickle as pk
import json
import re
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from gensim import corpora, models
from sklearn.model_selection import train_test_split

from gensim import corpora, models
import collections


stop_words = set(stopwords.words('english'))

'''
Generate data set with the filter of cuisins and Las Vegas city
business_data_lasvegas - Las vegas business data
Combined_Data.csv - All cusins specific data

'''
#
combinedData = pd.read_csv("Combined_Data.csv", delimiter="|")
filedata2 = open('business_data_lasvegas.pkl', 'rb')
data = pk.load(filedata2)
data = data.dropna(subset = ['attributes','reviews','categories'])
data = data.drop(['address', 'attributes', 'hours', 'is_open', 'latitude', 'longitude', 'postal_code', 'state'], axis=1)  



combinedDF = pd.merge(data, combinedData , on="business_id", how="left")
combinedDF = combinedDF[combinedDF['city'] == 'Las Vegas']
combinedDF = combinedDF.drop(['_id', 'categories', 'name', 'review_count', 'stars','reviews_x', 'Unnamed: 0',], axis=1)
combinedDF = combinedDF.dropna()
combinedDF.to_csv("combinedDF.csv", sep="|", index = None, encoding = None)
combinedDF = pd.read_csv("combinedDF.csv", sep="|", encoding = None)
train = pd.read_csv("train.csv", delimiter="|")

'''
TO generate queries of maximum frequency words
'''
#test = pd.read_csv("test.csv", delimiter="|")

datatrain = combinedDF.head(100)

listofqueries = []
for index,row in datatrain.iterrows():
    rows=[]
    words = row['reviews_y'].split()
    for word in words:
        w = word.lower()
        if w not in stop_words and w!='':
            rows.append(w)
            
    a = collections.Counter(rows)
    li = a.most_common(10)
    
    string = " ".join(w1 for w1,w2 in li)
    listofqueries.append(string)
    

datatrain['query'] = listofqueries
datatrain['query'] = datatrain['query'].replace('',np.NaN)
datatrain = datatrain.drop(['city', 'text'], axis=1)
#newTest = test.drop(['Unnamed: 0', 'Unnamed: 0.1', 'reviews', 'text'], axis=1)
datatrain.to_csv("Top100.csv", index=False, sep="|")

#test2 = pd.read_csv("testQuery.csv", delimiter="|")
#train = pd.read_csv("trainTask1_updated.csv", delimiter="|")


'''
Generate queries with LDA Model
'''
dataLDA = datatrain
listofqueries = []
for index,row in dataLDA.iterrows():
    rows=[]
    words = row['reviews_y'].encode('utf8').split()
    for word in words:
        w = word.decode('utf8').lower()
        if w not in stop_words:
            rows.append(w)
    listOfWords = []
    binLength = len(rows)//5
    for i in range(5):
        listOfWords.append(rows[i*binLength:(i*binLength)+binLength])
    dictionary_LDA = corpora.Dictionary(listOfWords)
    dictionary_LDA.filter_extremes(no_below=2, no_above=20)
    corpus = [dictionary_LDA.doc2bow(list_of_tokens) for list_of_tokens in listOfWords]
#    print(corpus)
    lda_model = models.LdaMulticore(corpus, num_topics=1, id2word=dictionary_LDA, passes=2, workers=2)
    topics = lda_model.print_topics(num_words=10)
    phrases = []
    for i, ind in topics:
        new_line = re.findall('\w+', ind)
        new_line = [word for word in new_line if not word.isdigit()]
        phrases.append(" ".join(new_line))
        listofqueries.append(" ".join(new_line))
        print(phrases)

dataLDA['query'] = listofqueries
dataLDA['query'] = dataLDA['query'].replace('',np.NaN)
dataLDA = dataLDA.drop(['reviews_y'], axis=1)
dataLDA.to_csv("LDATop100.csv", index=False, sep="|")

