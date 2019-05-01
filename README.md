# Task 2
## Problem Statement
The purpose of Yelp Dataset Challenge was to solve the real time problems where the root motive is to recommend places to user for their visit. This whole process is a feedback to the Yelp system to recommend better and better businesses.Our aim of this task was to design a system to give recommendations to the user based on the user_id, location, similar users and search queries. As the original dataset was huge we limited this task to just Las Vegas city.


## Proposed System (-)
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
| User_1      | some wordy text | $1600 |
| User_2      | centered        |   $12 |
| User_n | are neat        |    $1 |
