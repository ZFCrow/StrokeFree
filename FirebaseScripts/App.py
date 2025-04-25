from flask import Flask, jsonify
from firebase_admin import credentials, firestore, initialize_app
from datetime import datetime
import os
import pytz 

# Initialize Flask app
app = Flask(__name__)



# Initialize Firebase Admin SDK
cred = credentials.Certificate('serviceAccountKey.json')
initialize_app(cred)

# Initialize Firestore client
db = firestore.client()

# Function to fetch user goals and update userLogs for the day
def update_user_logs():
    singapore_tz = pytz.timezone("Asia/Singapore") 

    # Get current date (this can be adjusted to fetch logs for any given date)
    current_date = datetime.now(singapore_tz).strftime('%Y-%m-%d')

    # Retrieve the users collection
    users_ref = db.collection('users')
    users_snapshot = users_ref.stream()

    # Loop through each user and process their goals
    for user_doc in users_snapshot:
        user_data = user_doc.to_dict()
        user_goals = user_data.get('userGoals', [])
        user_logs = user_data.get('userLogs', {})

        # Check if the log already exists for the current date
        if current_date in user_logs:
            print(f"Log already exists for user {user_doc.id} on {current_date}")
            continue

        # Prepare the log structure for the current date
        user_log = {}
        for goal in user_goals:
            goal_name = goal.get('name')
            goal_sets = goal.get('sets')

            # Adding the goal to the user log with default values for completed and achieved
            user_log[goal_name] = {
                'completed': 0,  # Start with 0 completed for each goal
                'goal': goal_sets,  # The sets for each goal
                'achieved': False  # Initially not achieved
            }

        # Add the new log for the day
        user_logs[current_date] = user_log

        # Update the user document with the new userLogs
        user_ref = db.collection('users').document(user_doc.id)
        user_ref.update({'userLogs': user_logs})

        print(f"Log updated for user {user_doc.id} on {current_date}")

    return "Logs updated successfully!"

# Flask route to trigger the update function
@app.route('/update_logs', methods=['GET'])
def update_logs():
    try:
        result = update_user_logs()  # Call the update function
        return jsonify({'message': result}), 200  # Respond with success message
    except Exception as e:
        return jsonify({'error': str(e)}), 500  # Respond with error message

@app.route('/')
def home():
    return "Welcome to the User Logs Update API!"
# Do not use app.run() for production, PythonAnywhere will run it using WSGI
if __name__ == '__main__':
    app.run(debug=True)  # This line is only used if you're running the app locally (not on PythonAnywhere)
    #Change the working directory to the current file location
    os.chdir(os.path.dirname(os.path.abspath(__file__)))