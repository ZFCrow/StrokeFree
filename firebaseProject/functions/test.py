from firebase_admin import credentials, firestore, initialize_app
from datetime import datetime

# Initialize Firebase Admin SDK
cred = credentials.Certificate('serviceAccountKey.json')
initialize_app(cred)

# Initialize Firestore client
db = firestore.client()

# Function to fetch user goals and update logs for the day
def update_user_logs():
    # Get current date (this can be adjusted to fetch logs for any given date)
    current_date = datetime.today().strftime('%Y-%m-%d')

    # Retrieve the users collection
    users_ref = db.collection('users')
    users_snapshot = users_ref.stream()

    # Loop through each user and process their goals
    for user_doc in users_snapshot:
        user_data = user_doc.to_dict()
        user_goals = user_data.get('userGoals', [])

        # Prepare the log structure for the current date
        user_log = {}
        for goal in user_goals:
            goal_name = goal.get('name')
            goal_sets = goal.get('sets')

            user_log[goal_name] = {
                'completed': 0,  # Start with 0 completed for each goal
                'goal': goal_sets,  # The sets for each goal
                'achieved': False  # Initially not achieved
            }

        # Check if the log already exists for this user and date
        user_logs_ref = db.collection('userLogs').document(user_doc.id)  # Using user id as document name
        user_logs_doc = user_logs_ref.get()

        # If there's no log for the current date, create a new one
        if not user_logs_doc.exists:
            user_logs_ref.set({current_date: user_log})
            print(f"Log created for user {user_doc.id} on {current_date}")
        else:
            # If logs exist, update the log with the new goals
            existing_logs = user_logs_doc.to_dict()
            existing_logs[current_date] = user_log
            user_logs_ref.set(existing_logs)
            print(f"Log updated for user {user_doc.id} on {current_date}")

# Call the function to update the logs
update_user_logs()
