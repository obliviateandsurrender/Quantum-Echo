import os
from pprint import pp
from flask import Flask, request, send_file
import numpy as np
import cv2
import base64
import matplotlib.pyplot as pplot
import pennylane as qml
from pennylane import numpy as np
from pennylane.templates import RandomLayers
import tensorflow as tf
from tensorflow import keras
from tqdm import tqdm
import json

app = Flask(__name__)

dev = qml.device("default.qubit", wires=4)
#n_epochs = 30   # Number of optimization epochs
n_layers = 2    # Number of random layers

# Random circuit parameters
rand_params = np.random.uniform(low=0, high=2 * np.pi, size=(n_layers, 4))



@qml.qnode(dev)
def circuit(phi):
    # Encoding of 4 classical input values
    for j in range(4):
        qml.RY(np.pi * phi[j], wires=j)

    # Random quantum circuit
    RandomLayers(rand_params, wires=list(range(4)))

    # Measurement producing 4 classical output values
    return [qml.expval(qml.PauliZ(j)) for j in range(4)]
    
def quanv(image):
    """Convolves the input image with many applications of the same quantum circuit."""
    out = np.zeros((14, 14, 4))
    #print(image.shape)
    # Loop over the coordinates of the top-left pixel of 2X2 squares
    for j in range(0, 28, 2):
        for k in range(0, 28, 2):
            # Process a squared 2x2 region of the image with a quantum circuit
            q_results = circuit(
                [
                    image[j, k],
                    image[j, k + 1],
                    image[j + 1, k],
                    image[j + 1, k + 1]
                ]
            )
            # Assign expectation values to different channels of the output pixel (j/2, k/2)
            for c in range(4):
                out[j // 2, k // 2, c] = q_results[c]
    return out

# a simple page that says hello
@app.route('/', methods=['GET', 'POST'])
def hello():
    with open(r'/home/whatsis/Downloads/NYUAHD/Hackathon22/butterfly.png', "rb") as imageFile:
        img = base64.b64encode(imageFile.read())
    print(len(img))
    return img

@app.route('/test', methods=['POST'])
def test():
    request_data = request.get_json()
    image = request_data['image']
    f_64 = base64.b64decode(image)
    with open('/home/whatsis/Downloads/NYUAHD/Hackathon22/test.png', 'wb') as w:
        w.write(f_64)
    
    #work()
    return work()
def work():
    file = r'/home/whatsis/Downloads/NYUAHD/Hackathon22/test.png'
    img = cv2.imread(file, cv2.IMREAD_GRAYSCALE)
    dir = r'/home/whatsis/Downloads/NYUAHD/Hackathon22/masks'
    template = []
    fnames = []
    for filename in os.listdir(dir):
        f = os.path.join(dir,filename)
        fnames.append(filename)
        template.append(cv2.imread(f,0))
    #print(fnames)
    h = []
    w = []
    for i in template:
        hh,ww = np.array(i).shape
        h.append(hh)
        w.append(ww)
    #print(template[0], h[0], w[0])
    methods = [cv2.TM_CCOEFF]
    q_model = tf.keras.models.load_model('Hackathon22/q_model')
    pred_classes = []
    labls = {'ⴰ': 0, 'ⴱ': 1, 'ⵛ': 2, 'ⴷ': 3, 'ⴹ': 4, 'ⵄ': 5, 'ⴼ': 6, 'ⴳ': 7, 'ⵖ': 8, 'ⴳⵯ': 9, 'ⵀ': 10, 
             'ⵃ': 11, 'ⵊ': 12, 'ⴽ': 13, 'ⴽⵯ': 14, 'ⵍ': 15, 'ⵎ': 16, 'ⵏ': 17, 'ⵇ': 18, 'ⵔ': 19, 'ⵕ': 20, 
             'ⵙ': 21, 'ⵚ': 22, 'ⵜ': 23, 'ⵟ': 24, 'ⵡ': 25, 'ⵅ': 26, 'ⵢ': 27, 'ⵣ': 28, 'ⵥ': 29, 'ⴻ': 30, 'ⵉ': 31, 'ⵓ': 32}
    for method in methods:
        img2 = img.copy()
        for i in tqdm(range(len(template))):
            result = cv2.matchTemplate(img2, template[i], method)
            min_val, max_val, min_loc, max_loc = cv2.minMaxLoc(result)
            #print(result)
            if method in [cv2.TM_SQDIFF, cv2.TM_SQDIFF_NORMED]:
                location = min_loc
            else:
                location = max_loc
            bottom_right = (location[0] + w[i], location[1] + h[i])   
            
            a = img2[location[1]:bottom_right[1]+1, location[0]:bottom_right[0]+1]
            img_resize = cv2.resize(a, (28,28), interpolation=cv2.INTER_AREA)
            img_resize = cv2.bitwise_not(img_resize)
            #print(img_resize.shape)
            pred_cls = np.argmax(q_model.predict(np.array([quanv(img_resize)])), axis=-1)
            pred_classes.append(list(labls.keys())[list(labls.values()).index(pred_cls)])
            #cv2.imwrite(fnames[i], img_resize)
            

    
    final_out = ' '.join(labls)
    return json.dumps({"result":final_out})#'{result:' + final_out + '}'

app.run(debug=True, host="0.0.0.0")

