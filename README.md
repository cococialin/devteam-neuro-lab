# Android App CNN Tester
Android application for testing tflite models. One can export Keras models and convert the .h5 file to .tflite file to test it on a mobile device.

## Use this code to convert Keras model to TensorflowLite model: 

```
//Run on Kaggle or Google CoLab

from tensorflow import lite
from keras.models import model_from_json

//Load architecture from json and weights from .h5 file 
with open('model.json', 'r') as f:
    model = model_from_json(f.read())
model.load_weights('model_weights.h5')
model.save('model.h5')

// Or load model direct from .h5 file 
// model = load_model('model001.h5')

converter = lite.TFLiteConverter.from_keras_model_file("model.h5")
tflite_model = converter.convert()
open("model.tflite", "wb").write(tflite_model)
```

Made by [DevTeam](https://www.devteam.ro)
