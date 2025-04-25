import pandas as pd 
from sklearn.linear_model import LinearRegression 


def turningEverMarriedToNumerical():
    print (df['ever_married'].value_counts())

    #convert ever married yes to 1 and no to 0 
    df["ever_married"] = df["ever_married"].apply(lambda x: 1 if x == "Yes" else 0) 

    df.to_csv("data.csv", index=False) 

# use linear regression to predict the bmi 
def handleMissingValues(): 
    df = pd.read_csv("data.csv") 
    datawithBMI = df.dropna(subset=["bmi"]) 
    datawithoutBMI = df[df["bmi"].isnull()] 
    print (datawithBMI.shape, datawithoutBMI.shape) 
    # just use the numerical columns to predict bmi 
    features = ['age', 'avg_glucose_level', 'hypertension', 'heart_disease']

    # train a linear regression model to predict bmi 
    reg = LinearRegression() 
    reg.fit(datawithBMI[features], datawithBMI["bmi"])

    bmipredictions = reg.predict(datawithoutBMI[features]) 
    print (bmipredictions) 
    print (len(bmipredictions))
    datawithoutBMI["bmi"] = bmipredictions 

    # combine the data with bmi and the data without bmi 
    df = pd.concat([datawithBMI, datawithoutBMI]) 
    df.to_csv("data.csv", index=False) 


def convertpkltoJson(): 
    import joblib 
    import json 
    features = joblib.load("features.pkl") 
    with open("features.json", "w") as f: 
        json.dump(features.tolist(), f)

#handleMissingValues() 
convertpkltoJson()