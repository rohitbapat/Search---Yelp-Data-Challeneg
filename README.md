# Task 2
## Problem Statement
The purpose of Yelp Dataset Challenge was to solve the real time problems where the root motive is to recommend places to user for their visit. This whole process is a feedback to the Yelp system to recommend better and better businesses.Our aim of this task was to design a system to give recommendations to the user based on the user_id, location, similar users and search queries. As the original dataset was huge we limited this task to just Las Vegas city.


## Proposed System:
We implemented this task in 5 phases namely,
- Collaborative Filtering
- Cosine Similarity
- Haversine Distance
- Business Hours Calculations
- Python Flask Integration

## Collaborative Filtering
This approach was mainly used for recommendations based on the similarity between the users.  We used the matrix factorization method to have businesses and users as the startinf matrix with star ratings as node value. The example of the matrix is:

| -  | Business_1  | Business_2 |
| :------------ |:---------------:| -----:|
| User_1      | 3.5 | NaN |
| User_2      | 4.0        |   3.0 |
| User_n | NaN        |    3.5 |

We used the Principal Component Analysis with Singular Value Decomposition to generate ratings for test users. We included the results of recommendation for similar users.

We used the RMSE metric for evaluation of the Collaborative Filtering Approach.

## Cosine Similarity
To deal with limitations like Cold start problem in collaborative filtering we also included a search query term for the user in Flask implementation. We used the cosine similarity from sklearn to find the best options of the user.
We added these recommendations to the previously suggested businesses through collaborative filtering.

## Haversine Distance
We used the latitude, longitude attributes of the `business.json` file and also considered user location to sort the recommendations based on distance.

## Business Hours based Results 
After sorting the businesses based on the Haversine distance formula, before displaying the results to the user, we also wanted to include the attribute of business hours. We used the business hours attribute in the `business_data.json` to get the hours of operation for the business. Based on the user request we, checked the Time of the day. Based on this input we tagged the business as open or close. We also calculated the respective ETA of closing and opening hours based on the shortlisted businesses.

# Steps to execute the Task 2
We have integrated the Task 2 approach in Python Flask to implement a primitive search engine simulation with both the approaches.

We used Anaconda Distribution with Python 3 Python version.
## Python Packages Requirements
- scikit-learn `conda install -c anaconda scikit-learn`
- scipy `conda install -c anaconda scipy`
- re `conda install -c conda-forge regex`
- flask `conda install -c anaconda flask`
- pandas `conda install -c anaconda pandas`
- html files in the templates folder for rendering the input/output pages `index.html` and `result.html`
- Download the predictionDF.pkl file from box in the same directory as the Python codes.(This file is used to load the user business based matrix for collaborative filtering)

## Steps to Execute Task 2
- Download predictionDF.pkl
- Make sure the `Search.py` and `Sample.py` files are in same location.
- Make sure the `index.html` and `result.html` exists in `templates` directory. 
- run the following command on the console `python Search.py`.

`(base) D:\Search\YelpData\Project\MapRepo\Flask-GoogleMaps>python Search.py
C:\Users\rohit\Anaconda3\lib\site-packages\sklearn\utils\fixes.py:313: FutureWarning: numpy not_equal will not check object identity in the future. The comparison did not return the same result as suggested by the identity (`is`)) and will change.
  _nan_object_mask = _nan_object_array != _nan_object_array
 * Serving Flask app "Search" (lazy loading)
 * Environment: production
   WARNING: Do not use the development server in a production environment.
   Use a production WSGI server instead.
 * Debug mode: on
 * Restarting with stat
C:\Users\rohit\Anaconda3\lib\site-packages\sklearn\utils\fixes.py:313: FutureWarning: numpy not_equal will not check object identity in the future. The comparison did not return the same result as suggested by the identity (`is`)) and will change.
  _nan_object_mask = _nan_object_array != _nan_object_array
 * Debugger is active!
 * Debugger PIN: 250-945-521
 * Running on http://127.0.0.1:5000/ (Press CTRL+C to quit)`
 
- On getting this message on console, go to the browser type in `localhost:5000`
- The console will now show `Load Started` and after some time changes to `Load Completed` state.
- On the UI, enter a User ID like For eg: `---1lKK3aKOuomHnwAkAow` , Latitude : `36.099872` , Longitude: `-115.074574`, Query: `burger`
- Click Submit
- The result page renders a table with the recommendation based on the user_id and query. It also tells you the operation status and hours of operation with respect to current time.