import Sample
from flask import Flask, request, render_template
from flask_jsonpify import jsonpify
import pandas as pd
import time
from datetime import datetime

app = Flask(__name__)

global business_Lasvegas
global predictionDF
global df_dense
global count_vectorizer

@app.route('/')
def index():
    global business_Lasvegas
    global predictionDF
    global df_dense
    global count_vectorizer
    business_Lasvegas,predictionDF,df_dense,count_vectorizer = Sample.load_data()
    return render_template('index.html')
    

@app.route('/', methods=['POST'])
def my_form_post():
    global business_Lasvegas
    global predictionDF
    global df_dense
    global count_vectorizer
    user = request.form['userid']
    latitude = request.form['latitude']
    longitude = request.form['longitude']
    query = request.form['query']
    #query = 'spa cafe'
    #user = '---1lKK3aKOuomHnwAkAow'
    #latitude = 36.099872
    #longitude = -115.074574
    similarityPrediction = Sample.similarity(query,business_Lasvegas,df_dense,count_vectorizer)
    matrixPrediction = Sample.getPredictionsByMatrixFactorization(user,predictionDF,business_Lasvegas)
    df = pd.concat([matrixPrediction, similarityPrediction], ignore_index=True)
    df['Distance'] = df.apply(Sample.find_closest,lat = latitude, long = longitude, axis = 1)
    df[['latitude','longitude','Distance']]
    result_df = df.sort_values(by=['Distance'])
    curr_time = time.ctime()
    day = curr_time.split(' ')[0]
    day_dict = {'Mon':'Monday','Tue':'Tuesday','Wed':'Wednesday','Thu':'Thursday','Fri':'Friday','Sat':'Saturday','Sun':'Sunday'}
    day = day_dict[day]
    curr_hours = curr_time.split(' ')[3]
    s1 = curr_hours[:5]
    open_close = list()
    status = list()
    for open_hrs in result_df['hours']:
        if open_hrs != None:
            hrs = open_hrs[day]
            operations = hrs.split('-')
            s2 = operations[1]
            s3 = operations[0]
            FMT = '%H:%M'
            tdelta = datetime.strptime(s2, FMT) - datetime.strptime(s1, FMT)
            if datetime.strptime(s3, FMT) > datetime.strptime(s1, FMT):
                tdelta = datetime.strptime(s3, FMT) - datetime.strptime(s1, FMT)
                tdelta = str(tdelta).split(':')
                tdelta = ':'.join(tdelta[x] for x in range(2))
                open_close.append('Close')
                status.append(('Closed : Opens in',str(tdelta),'hrs'))
            elif str(tdelta).startswith('-'):
                tdelta = datetime.strptime(s3, FMT) - datetime.strptime(s1, FMT)
                tdelta = str(tdelta).split(',')[1]
                tdelta = str(tdelta).split(':')
                tdelta = ':'.join(tdelta[x] for x in range(2))
                open_close.append('Close')
                status.append(('Closed : Opens in',str(tdelta),'hrs'))
            else:
                tdelta = str(tdelta).split(':')
                tdelta = ':'.join(tdelta[x] for x in range(2))
                open_close.append('Open')
                status.append(('Open : Closes in',str(tdelta),'hrs'))
        else:
            open_close.append('Close')
            status.append(('Closed Today'))
    result_df['open_close'] = open_close
    result_df['status'] = status
    result_df = result_df[['name','Distance','stars','open_close','status']]
    #print(result_df)
    #JSONP_data = jsonpify(result_df)
    #return JSONP_data
    #return render_template('result.html', result = result_df)
    return render_template('result.html',  tables=[result_df.to_html(classes='data')], titles=result_df.columns.values)

if __name__ == '__main__':
    app.run(debug = True) 
    #index()
    #my_form_post()
    #print(business_Lasvegas.head())
    
'''
from flask import Flask
app = Flask(__name__)

@app.route('/')
def index():
   return '<form method="POST">User ID:<br><input type="text" name="userid"><br><br>Current Location<br>Latitude:<br><input type="text" name="latitude"><br>Longitude:<br><input type="text" name="longitude"><br><br><input type="submit" value="Submit"></form>'
if __name__ == '__main__':
   app.run(debug = True)
'''