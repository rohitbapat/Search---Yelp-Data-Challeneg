# Search (Information Retrieval)
Yelp-Data-Challenge

# Task 1:


- We are using reviews,tips and business id to find the category of the search query.

- We have done indexing based on business id. To find the desired result we have used tfIdf to rank reviews and tips and the overall rank is formulated by multiplying both reviews and tips of the corresponding business Id.

- We have also added a provision to check for "not" in a query. If the query contains not, then all the results that doesnot contain the query or those who have very low rank in that query will be displayed.
