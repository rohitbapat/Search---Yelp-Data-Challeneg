import numpy as np
import pandas as pd
import pickle as pk
import json
import re
from sklearn.feature_extraction.text import CountVectorizer
import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import TfidfVectorizer
from scipy.sparse.linalg import svds
import math
from sklearn.model_selection import train_test_split

# Load Business data

def load_data():
    print('Load Started')
    
    filedata =  open('D:/Search/YelpData/yelp_dataset/business.json', 'rb')
    businessDF = pd.read_json(filedata, lines=True)
    business_Lasvegas = businessDF[businessDF['city'] == "Las Vegas"]
    
    '''
    # Review Data
    # reviewDF = pd.read_json(open('/content/gdrive/My Drive/Colab Notebooks/test.json'), lines=True)
    filedata =  open('D:/Search/YelpData/yelp_dataset/review.json', 'rb')
    data = []
    count = 1
    for line in filedata:
        count+=1
        if(count>1000000):
            break
        data.append(json.loads(line.decode('utf-8')))
    reviewDF = pd.DataFrame(data)
    
    filedata = ""
    business_review = business_Lasvegas.merge(reviewDF, on = 'business_id')
    business_review = business_review.dropna(subset = ['attributes','hours','categories'])
    business_review_stars = business_review.drop(['address', 'attributes', 'categories', 'city', 'hours', 'is_open', 'latitude', 'longitude', 'name', 'postal_code', 'review_count', 'stars_x', 'state', 'cool', 'date', 'funny', 'review_id', 'text', 'useful'], axis = 1)
    
    # Remove businesses with less than 10 reviews? 
    cnt = business_review_stars['business_id'].value_counts()
    cnt = cnt.index[cnt >= 10]
    business_review_stars = business_review_stars[business_review_stars['business_id'].isin(cnt)]
    
    
    # Aggregate
    business_review_stars=business_review_stars.groupby(['user_id','business_id']).agg({'stars_y':'mean'})
    business_review_stars = business_review_stars.reset_index()
    
    train, test = train_test_split(business_review_stars, test_size=0.2)
    del business_review_stars
    
    def fill(row):
        a = row[~np.isnan(row)].mean()
        return row.fillna(a)
    
    mat = train.pivot(index = 'user_id', columns ='business_id', values = 'stars_y')
    cols = mat.columns
    ind = mat.index
    
    mat = mat.apply(fill, axis=1)
    mat.head(5)
    
    
    # Convert to matrx and subtract mean
    mat = mat.as_matrix()
    user_ratings_mean = np.mean(mat, axis = 1).reshape(-1, 1)
    R_demeaned = mat - user_ratings_mean

    del mat
    
    U, sigma, Vt = svds(R_demeaned, k = 70)
    sigma = np.diag(sigma)
    all_user_predicted_ratings = np.dot(np.dot(U, sigma), Vt) + user_ratings_mean.reshape(-1,1)
    
    
    predictionDF = pd.DataFrame(all_user_predicted_ratings, columns = cols)
    predictionDF = predictionDF.set_index(ind)
    
    del all_user_predicted_ratings
    '''
    predictionDF = pk.load(open("D:/Search/YelpData/yelp_dataset/predictionDF.pkl",'rb'))
    count_vectorizer = TfidfVectorizer(stop_words='english',lowercase=True)
    #count_vectorizer = CountVectorizer()
    sparse_matrix = count_vectorizer.fit_transform(business_Lasvegas['name'])
    
    doc_term_matrix = sparse_matrix.todense()
    df_dense = pd.DataFrame(doc_term_matrix, columns=count_vectorizer.get_feature_names())
    print('Load Completed')
    return business_Lasvegas,predictionDF,df_dense,count_vectorizer
    
def similarity(search,business_Lasvegas,df_dense,count_vectorizer):
    Y = count_vectorizer.transform([search])
    Y = Y.todense()
    
    check_similarity = cosine_similarity(Y, df_dense) 
    df_sim = pd.DataFrame({'business_id':business_Lasvegas['business_id'],'scores':check_similarity[0].tolist()})
    df_sim = df_sim.sort_values(by=['scores'],ascending=False)
    businessSimilarity = df_sim['business_id'].head(10)
    li = list(businessSimilarity.value_counts().index)
    similarityPrediction = business_Lasvegas.loc[business_Lasvegas['business_id'].isin(li)]
    return similarityPrediction

###
###
def find_closest(row,lat,long):
    user_location = [float(lat),float(long)]
    #user_location = [float(36.099872),float(-115.074574)]
    R = 6373.0
    lon2 = float(row['longitude'])
    lat2 = float(row['latitude'])
    #lon2 = float(-115.074574)
    #lat2 = float(36.099872)
    lon1 = user_location[1]
    lat1 = user_location[0]
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = (math.sin(dlat/2))**2 + math.cos(lat1) * math.cos(lat2) * (math.sin(dlon/2))**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    distance = R * c
    #dist.append(distance)
    return distance

def getPredictionsByMatrixFactorization(user,predictionDF,business_Lasvegas):
    a = predictionDF.loc[[user]]
    businessVal = {}
    for col in a.columns:
        businessVal[col] = a[col][user]

    businesses = sorted(businessVal.items(), key=lambda x: x[1], reverse=True)[:15]
    li = [x for x,y in businesses]
    matrixPrediction = business_Lasvegas.loc[business_Lasvegas['business_id'].isin(li)]
    return matrixPrediction
'''
###
user = '--1mPJZdSY9KluaBYAGboQ'
matrixPrediction = getPredictionsByMatrixFactorization(user)
###

# business_Lasvegas[business_Lasvegas['business_id'] in li]
#matrixPrediction = business_Lasvegas.loc[business_Lasvegas['business_id'].isin(li)]
###

#df = pd.concat([matrixPrediction, similarityPrediction], ignore_index=True)

#del matrixPrediction
#del similarityPrediction

#df['Distance'] = df.apply(find_closest,axis = 1)
#df[['latitude','longitude','Distance']]
#result_df = df.sort_values(by=['Distance'])
'''
