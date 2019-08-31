# devteam-neuro-lab
Android application for testing tflite models. One can export Keras models and convert the .h5 file to .tflite file to test it on a mobile device.

Use this code to convert Keras model to TensorflowLite model (this code can be run on Kaggle or Google CoLab):

# Load architecture from json and weights from .h5 file 
from tensorflow import lite
from keras.models import model_from_json

with open('model.json', 'r') as f:
    model = model_from_json(f.read())

# Load weights into the new model
model.load_weights('model_weights.h5')
model.save('model.h5')

# Or load model direct from .h5 file 
# model = load_model('model001.h5')

converter = lite.TFLiteConverter.from_keras_model_file("model.h5")
tflite_model = converter.convert()
open("model.tflite", "wb").write(tflite_model)
