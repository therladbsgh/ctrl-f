#!/usr/local/bin/python3
from PIL import Image
import pytesseract
import threading
import numpy as np
from pandas import *
import cv2

# defining a constant
p_inf = float("inf")


# function to read the input and assign values depending on rgb
def read_input_img(img):
    image = cv2.imread(img)
    rows = len(image)
    cols = len(image[0])

    inp = [[0 for y in range(cols)] for x in range(rows)]

    for i in range(rows):
        for j in range(cols):
            inp[i][j] = sum(image[i, j]) / 3

    return inp


# given an input matrix, calculate the cost of each cell.
def calc_cost(inp):
    rows = len(inp)
    cols = len(inp[0])

    ret = [[0 for y in range(cols)] for x in range(rows)]

    for i in range(rows):
        for j in range(cols):
            res = 0
            for x in range(-1, 2):
                for y in range(-1, 2):
                    if i + x < 0 or j + y < 0 or i + x >= rows or j + y >= cols:
                        continue
                    else:
                        res += abs(inp[i][j] - inp[i + x][j + y])
            ret[i][j] = res

    return ret


def penalty(curr_row, start_row):
    if abs(curr_row - start_row) > 2:
        return 9999999
    else:
        return 1

# the meat of the project: modified seam carve algorithmself.
# We modify it so that the carve it finds can't fluctuate for more than
# -+ 2 pixels.
#
# each entry in comp will store (sum, starting row for this comp)


def find_carve(cost):
    rows = len(cost)
    cols = len(cost[0])
    start_row = 0

    # stores the intermediate results
    comp = [[(0, 0) for y in range(cols)] for x in range(rows)]

    # setup work
    for i in range(rows):
        comp[i][0] = (cost[i][0], i)

    # going horizontally
    for j in range(1, cols):
        for i in range(0, rows):
            # should consider three rows before it, same as classical sc
            # note the penalty will likely preclude impossible choices
            # i.e. out of range
            pool = []

            pool.append(comp[i][j - 1][0] * penalty(i, comp[i][j - 1][1]))

            if i + 1 < rows:
                pool.append(comp[i + 1][j - 1][0] *
                            penalty(i, comp[i + 1][j - 1][1]))
            else:
                pool.append(p_inf)

            if i - 1 >= 0:
                pool.append(comp[i - 1][j - 1][0] *
                            penalty(i, comp[i - 1][j - 1][1]))
            else:
                pool.append(p_inf)

            sr = -1
            e1 = min(pool)

            assert(len(pool) == 3)

            if e1 == pool[0]:
                sr = comp[i][j - 1][1]
            elif e1 == pool[1]:
                sr = comp[i + 1][j - 1][1]
            else:
                sr = comp[i - 1][j - 1][1]

            assert(sr != -1)

            # the new tuple will be formed of the minimum value considered above
            # and the starting row for that cell
            comp[i][j] = (e1 + cost[i][j], sr)

    return comp


def find_min_start_row(comp):
    rows = len(comp)
    cols = len(comp[0])

    arrsort = [comp[i][cols - 1] for i in range(rows)]

    arrsort.sort(key=lambda tup: tup[1])

    return arrsort


def pop_and_clean(arr):
    sr = arr.pop(0)

    while len(arr) > 0 and abs(arr[0][1] - sr[1]) <= 5:
        arr.pop(0)

    return sr[1]


def proc_img(img):
    print(pytesseract.image_to_string(img))


# given a row, chop it off and create a new thread to deal with the image.
# we know that the img will be a numpy arr.
def chop_and_thread(row, img, cols):
    rows = len(img)
    print(rows)
    thr_args = img[0:row, 0:cols]
    img = img[row:rows, 0:cols]

    type(thr_args)

    time.sleep(0.1)

    cv2.imshow("Chopped", thr_args)
    k = cv2.waitKey(0)
    if k == ord('n'):
        cv2.destroyAllWindows()

    thread = threading.Thread(target=proc_img, args=(thr_args,))
    thread.start()

    return img


# helper function
def pprint(mat):
    print(DataFrame(mat))
    print("----------------------")


if __name__ == '__main__':
    inp = read_input_img("aaa.jpg")
    cost = calc_cost(inp)
    comp = find_carve(cost)
    min_sr = find_min_start_row(comp)

    chopping_image = cv2.imread("aaa.jpg")
    cols = len(chopping_image[0])

    while len(min_sr) > 0:
        sr = pop_and_clean(min_sr)

        chopping_image = chop_and_thread(sr, chopping_image, cols)
