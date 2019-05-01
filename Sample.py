# import statements
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

# Load data required for the offline and online recommendation
def load_data():
    print('Load Started')
    filedata =  open('D:/Search/YelpData/yelp_dataset/business.json', 'rb')
    businessDF = pd.read_json(filedata, lines=True)
    # filter the dataframe for only Las Vegas data
    business_Lasvegas = businessDF[businessDF['city'] == "Las Vegas"]
    
    # Optional module for loading the matrix factorization data
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
    # Instead we load this precomputed DataFrame from pickle file
    predictionDF = pk.load(open("D:/Search/YelpData/yelp_dataset/predictionDF.pkl",'rb'))
    count_vectorizer = TfidfVectorizer(stop_words='english',lowercase=True)
    # generate a sparse matrix of the buiness names
    sparse_matrix = count_vectorizer.fit_transform(business_Lasvegas['name'])
    
    # convert matrix to a dense matrix
    doc_term_matrix = sparse_matrix.todense()
    df_dense = pd.DataFrame(doc_term_matrix, columns=count_vectorizer.get_feature_names())
    print('Load Completed')
    # return the 3 dataframes required. return vectorizer to vectorize test query
    return business_Lasvegas,predictionDF,df_dense,count_vectorizer
    
# function to get cosine similarity of the query term with dense matrix
def similarity(search,business_Lasvegas,df_dense,count_vectorizer):
    # use same vectorizer for a same dimension matrix
    Y = count_vectorizer.transform([search])
    Y = Y.todense()
    
    # use cosine similarity of sklearn to get a score of similarity
    check_similarity = cosine_similarity(Y, df_dense)
    # map similarity scores and business_ids
    df_sim = pd.DataFrame({'business_id':business_Lasvegas['business_id'],'scores':check_similarity[0].tolist()})
    df_sim = df_sim.sort_values(by=['scores'],ascending=False)
    businessSimilarity = df_sim['business_id'].head(10)
    li = list(businessSimilarity.value_counts().index)
    # return dataframe from similarity prediction
    similarityPrediction = business_Lasvegas.loc[business_Lasvegas['business_id'].isin(li)]
    return similarityPrediction


# sort the combined data
def find_closest(row,lat,long):
    user_location = [float(lat),float(long)]
    # Haversine Distance adopted from https://stackoverflow.com/questions/4913349/haversine-formula-in-python-bearing-and-distance-between-two-gps-points
    R = 6373.0
    lon2 = float(row['longitude'])
    lat2 = float(row['latitude'])
    lon1 = user_location[1]
    lat1 = user_location[0]
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = (math.sin(dlat/2))**2 + math.cos(lat1) * math.cos(lat2) * (math.sin(dlon/2))**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    distance = R * c
    # return the distance from the user
    return distance

def getPredictionsByMatrixFactorization(user,predictionDF,business_Lasvegas):
    a = predictionDF.loc[[user]]
    businessVal = {}
    for col in a.columns:
        businessVal[col] = a[col][user]
    # get predictions from matrix factorization method
    businesses = sorted(businessVal.items(), key=lambda x: x[1], reverse=True)[:15]
    li = [x for x,y in businesses]
    matrixPrediction = business_Lasvegas.loc[business_Lasvegas['business_id'].isin(li)]
    return matrixPrediction
