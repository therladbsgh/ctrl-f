import pytesseract
import cv2


def read_image(text, image_file):
    img = cv2.imread(image_file)
    result = pytesseract.image_to_data(img, nice=999,
                                       output_type=pytesseract.Output.DICT)
    print(pytesseract.image_to_data(img, nice=999))

    boxes = []
    for i in range(len(result['text'])):
        if text in result['text'][i]:
            boxes.append({
                'left': result['left'][i],
                'top': result['top'][i],
                'width': result['width'][i],
                'height': result['height'][i]
            })
    return boxes


print(read_image('dog', 'math.png'))
