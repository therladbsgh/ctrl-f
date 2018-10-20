import flask
from flask import Flask
import flask_cors

import pytesseract
import numpy as np
import cv2

app = Flask(__name__)
flask_cors.CORS(app, resources={r"/*": {"origins": "*"}})


@app.route('/hello')
def hello():
    return flask.json.dumps({'text': ['a', 'b', 'c']})


@app.route('/get_boxes', methods=['POST'])
def read_image():
    data = flask.request.form
    files = flask.request.files

    if 'word' not in data:
        return flask.json.jsonify({'error': 'text not specified'})
    if 'file' not in files:
        return flask.json.jsonify({'error': 'No file'})

    text = data['word']
    image_file = files['file']
    image_file = np.fromstring(image_file.read(), np.uint8)
    image_file = cv2.imdecode(image_file, cv2.IMREAD_COLOR)
    # img = cv2.imread(image_file)

    result = pytesseract.image_to_data(image_file, nice=999,
                                       output_type=pytesseract.Output.DICT)

    boxes = []
    for i in range(len(result['text'])):
        if text in result['text'][i]:
            boxes.append({
                'left': result['left'][i],
                'top': result['top'][i],
                'width': result['width'][i],
                'height': result['height'][i]
            })
    return flask.json.dumps(boxes)


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=8080, debug=True)
