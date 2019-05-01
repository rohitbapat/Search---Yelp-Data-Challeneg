# Task 1:

# Problem Statement:
Yelp is a great platform to get recommendations, reviews, ratings for various businesses around the world. The recommendations it provides are more precise if the query submitted by the user is identified correctly. Therefore, as a part of Task 1, we wanted to predict the Business Categories corresponding to the search query using Reviews and Tips. This predicted category can then be used as per the requirement, say for Restaurant Recommendation, food recommendation etc. 

# Data Extraction and Preprocessing:
We used Yelp Dataset on the Official Yelp Dataset Challenge website. The dataset includes multiple JSON files giving different areas of information. For this task we used business.json, reviews.json, tip.json, categories.json. We used MongoDB as a supporting database to transfer these JSON files. We filtered the huge dataset to include only Las Vegas city and a limited cuisine data. The categories shortlisted for prediction were :
- Restaurants Food Indian 
- Mexican Pakistani Chinese  
- American Middle Eastern Thai
- Italian Vietnamese Sri Lankan
- Canadian Mediterranean Korean
- Japanese Lebanese Ethiopian
- British Singaporean

# Proposed System:
This project involved the below processes:
- Data Cleaning
- Query Optimization
- TF-IDF
- Evaluation

# Data Cleaning:
- As a part of Data Cleaning, we combined our data such that, each business is mapped to all the corresponding user reviews and tips.
- We then divided our data in two parts, training data and test data.

# Query Optimization:
Since, we were using reviews as query and they were really large texts, it was taking very long time to process the query. Therefore, we needed to optimize the query. For query optimization we removed stop words for review text. Then we used two approaches :

- Top 10 word filtering:
  In this method, we sorted the data with respect to frequency of the words occurring in the text in descending order and selected the top 10 words with maximum frequency. We then used these words as a query and calculated TFIDF to find the corresponding category.
  
- LDA (Latent Dirichlet Allocation):
  For further improvement, We removed the stop words and used LDA (Latent Dirichlet Allocation) to optimize our query. For this, we split each of our reviews in five separate statements to use as different documents for our LDA model. Our LDA model then generated topics which we passed to our TFIDF model as query to get the categories

# TF-IDF
To find TFIDF, we first performed indexing on the Business Ids using Standard Analyser and then we calculate TFIDF to predict the category.
TF = number of terms t in document D /total number of terms in the document
we consider reviews as our documents and, 
IDF = log(1+(total number of documents in the corpus/frequency of document for term t))
using these values, we rank the categories to a TF\*IDF score and the top categories are the desired output.
