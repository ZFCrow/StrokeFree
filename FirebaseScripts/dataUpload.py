import firebase_admin
from firebase_admin import credentials, firestore
import pandas as pd

# Initialize Firebase Admin SDK
cred = credentials.Certificate('StrokeFree/FirebaseScripts/serviceAccountKey.json')
firebase_admin.initialize_app(cred)

# Initialize Firestore
db = firestore.client()
collection_ref = db.collection('exercises')

# Read CSV using Pandas
df = pd.read_csv('StrokeFree/FirebaseScripts/strokeData.csv')

# Upload each row as a new document in Firestore
for index, row in df.iterrows():
    # Convert row to dictionary
    data = {
        'strokeType': row['Stroke Type'],
        'exerciseName': row['Exercise Name'],
        'description': row['Description'],
        'focusArea': row['Focus Area'],
        'videoUrl': row['Video URL']
    }
    
    # Add to Firestore
    collection_ref.add(data)

print('CSV file successfully uploaded to Firestore')
