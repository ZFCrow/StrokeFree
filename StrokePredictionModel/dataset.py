import kagglehub
import pandas as pd
import numpy as np
import joblib
import os
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report, f1_score
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
from imblearn.over_sampling import SMOTE  # Install with: pip install imbalanced-learn
from sklearn.preprocessing import MinMaxScaler  # Feature scaling for better learning

class StrokingModel:
    def __init__(self, modelPath=None, dataPath=None, featuresPath=None):
        self.model = None
        self.data = None
        self.xTrain = None
        self.yTrain = None
        self.xTest = None
        self.yTest = None
        self.featureColumns = None  # Required columns for prediction
        self.scaler = MinMaxScaler()  # Initialize a scaler for feature normalization
        self.categorialColumns = ['gender', 'work_type', 'Residence_type', 'smoking_status']

        if modelPath and os.path.exists(modelPath):
            self.model = joblib.load(modelPath)
            print(f"✅ Model loaded successfully from {modelPath}")
        else:
            print("⚠️ No pre-trained model found. Creating a new model with `trainModel()`.")

        if dataPath and os.path.exists(dataPath):
            self.data = pd.read_csv(dataPath)
            print(f"📂 Data loaded successfully from {dataPath}")

        if featuresPath and os.path.exists(featuresPath):
            self.featureColumns = joblib.load(featuresPath)
            print(f"📝 Features loaded successfully from {featuresPath}")

    def downloadData(self):
        """Download the dataset from KaggleHub."""
        path = kagglehub.dataset_download("fedesoriano/stroke-prediction-dataset")
        print("Path to dataset files:", path)

    def trainModel(self):
        """Train the model with balanced data and optimized hyperparameters."""
        # Load dataset
        self.data = pd.read_csv("data.csv")
        y = self.data["stroke"]
        X = self.data.drop(["stroke", "id"], axis=1)

        # One-hot encode categorical variables
        X = pd.get_dummies(X, columns=self.categorialColumns, drop_first=True)

        # Save feature columns
        self.featureColumns = X.columns
        joblib.dump(self.featureColumns, "features.pkl")

        # Feature Scaling (Normalize all numerical features)
        X_scaled = self.scaler.fit_transform(X)

        # Split into training & testing sets with stratification
        self.xTrain, self.xTest, self.yTrain, self.yTest = train_test_split(
            X_scaled, y, test_size=0.2, random_state=42, stratify=y
        )

        # **Reduce SMOTE Sampling to 0.1**
        smote = SMOTE(sampling_strategy=0.1, random_state=42)  # Avoid excessive oversampling
        self.xTrain, self.yTrain = smote.fit_resample(self.xTrain, self.yTrain)

        print(f"✅ After SMOTE, class distribution:\n{pd.Series(self.yTrain).value_counts()}")

        # **Balanced Random Forest Model**
        self.model = RandomForestClassifier(
            n_estimators=150,
            max_depth=10,  # Allow more learning
            min_samples_split=8,  # Avoid overfitting
            min_samples_leaf=4,  # Prevent small noisy nodes
            class_weight={0: 1, 1: 3},  # Reduce the emphasis on stroke
            random_state=42
        )

        self.model.fit(self.xTrain, self.yTrain)
        print("✅ Model training completed.")

    def evalModel(self):
        """Evaluate the model's accuracy and classification performance."""
        if self.model and self.xTest is not None:
            y_pred = self.model.predict(self.xTest)
            accuracy = accuracy_score(self.yTest, y_pred)
            print(f"✅ Model Accuracy: {accuracy:.4f}")
            print("📊 Classification Report:\n", classification_report(self.yTest, y_pred))
        else:
            print("⚠️ Model or test data not available.")

    def findBestThreshold(self):
        """Automatically find the best decision threshold for stroke classification."""
        if self.model and self.xTest is not None:
            y_probs = self.model.predict_proba(self.xTest)[:, 1]  # Get stroke probabilities

            # Try different thresholds
            thresholds = np.arange(0.3, 0.7, 0.05)  # Test from 0.3 to 0.7
            best_threshold = 0.5
            best_f1 = 0

            for t in thresholds:
                y_pred = (y_probs >= t).astype(int)
                f1 = f1_score(self.yTest, y_pred)
                if f1 > best_f1:
                    best_f1 = f1
                    best_threshold = t

            print(f"📌 Best Threshold for Balance: {best_threshold:.3f}")
            return best_threshold
        else:
            print("⚠️ Model or test data not available.")
            return 0.5  # Default threshold

    def predict(self, data, threshold=0.5):
        """Predict stroke probability with the best threshold."""
        if self.model:
            data = self.scaler.transform(data)  # Apply the same scaling
            prob = self.model.predict_proba(data)[:, 1]  # Get stroke probability
            print(f"🔍 Stroke Probability: {prob[0]:.4f}")  # Show actual probability
            return (prob >= threshold).astype(int)  # Apply threshold
        else:
            print("⚠️ No model found.")
            return None

    def downloadModel(self):
        """Save the trained model."""
        joblib.dump(self.model, "model.pkl")
        print("✅ Model saved as model.pkl.")

    def convertmodelToONNX(self):
        """Convert the trained model to ONNX format."""
        initialType = [('float_input', FloatTensorType([None, len(self.featureColumns)]))]
        onnxModel = convert_sklearn(self.model, initial_types=initialType)

        # Save ONNX model
        with open("model.onnx", "wb") as f:
            f.write(onnxModel.SerializeToString())

        print("✅ Model converted and saved as model.onnx.")

if __name__ == "__main__":
    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    # Initialize and rebuild the model
    model = StrokingModel(dataPath="data.csv", modelPath="model.pkl", featuresPath="features.pkl")

    # **Step 1: Train the model again**
    model.trainModel()

    # **Step 2: Evaluate the model**
    model.evalModel()

    # **Step 3: Find the best threshold automatically**
    best_threshold = model.findBestThreshold()

    # **Step 4: Test prediction using the best threshold**
    test_data = pd.DataFrame([[50, 0, 0, 1, 77.67, 25.6, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0]],
                            columns=model.featureColumns)

    prediction = model.predict(test_data, threshold=best_threshold)
    print(f"Prediction: {prediction}")  # Expecting a balanced result

    # **Step 5: Convert to ONNX**
    model.convertmodelToONNX()
