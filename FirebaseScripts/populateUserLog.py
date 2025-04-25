from firebase_admin import credentials, firestore, initialize_app
from datetime import datetime
import pytz  

# Initialize Firebase Admin SDK
cred = credentials.Certificate('serviceAccountKey.json')
initialize_app(cred)

# Initialize Firestore client
db = firestore.client()

# Function to fetch user goals and update userLogs for the day
def update_user_logs():
    # Get current date in Singapore Time (UTC+8)
    singapore_tz = pytz.timezone("Asia/Singapore")
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

# Call the function to update the logs
update_user_logs()
