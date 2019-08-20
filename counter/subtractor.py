import cv2
import numpy as np
from centerTracking import CentroidTracker
from trackingObject import trackingObject
from counter import counterClass


#cap = cv2.VideoCapture('test4.mp4')


cap = cv2.VideoCapture('http://192.168.137.40:4747/video')
fgbg = cv2.createBackgroundSubtractorKNN(history=300, dist2Threshold=500, detectShadows=False)


trackableObjects = {}

ret, frame1 = cap.read()
ret, frame2 = cap.read()

width = int(cap.get(3))
height = int(cap.get(4))

OffsetRefLines = 80
middleLine = (width / 2)

EntranceCounter = 0
ExitCounter = 0


ct = CentroidTracker()

while cap.isOpened():

    ret, frame = cap.read()

    fgmask = fgbg.apply(frame)

    blur = cv2.GaussianBlur(fgmask, (5, 5), 0)
    _, thresh = cv2.threshold(blur, 70, 255, cv2.THRESH_BINARY)

    kernel = np.ones((5, 5), np.uint8)
    erosion = cv2.erode(thresh, kernel, iterations=10)

    cv2.imshow("eroison", erosion)
    contours, _ = cv2.findContours(erosion, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

    enterLine = int(middleLine + OffsetRefLines)
    exitLine = int(middleLine - OffsetRefLines)
    cv2.line(frame, (enterLine, 0), (enterLine, height), (0, 255, 0), 2)
    cv2.line(frame, (exitLine, 0), (exitLine, height), (0, 255, 0), 2)

    rects = []

    for contour in contours:

        if cv2.contourArea(contour) < 20000:
            continue

        (x, y, w, h) = cv2.boundingRect(contour)

        rectangles = [x, y, (x + w), (y + h)]
        rects.append(rectangles)

    objects = ct.update(rects)

    for (objectID, centroid) in objects.items():

        to = trackableObjects.get(objectID, None)

        if to is None:
            to = trackingObject(objectID, centroid, enterLine, exitLine)
            trackableObjects[objectID] = to
        else:
            to.checkTrackPosition(centroid, enterLine, exitLine)

        tracking = "tracking {}".format(objectID)

        cv2.putText(frame, tracking, (centroid[0] - 20, centroid[1] - 20),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.75, (0, 255, 0), 2)
        cv2.circle(frame, (centroid[0], centroid[1]), 5, (0, 255, 0), -1)

        print("exit - " + str(counterClass.ExitCounter))
        print("enter - " + str(counterClass.EntranceCounter))

    enter = "Enter " + str(counterClass.EntranceCounter)
    exit = "Exit " + str(counterClass.ExitCounter)
    cv2.putText(frame, enter, (100, 100), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
    cv2.putText(frame, exit, (100, 140), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)


    cv2.imshow("output", frame)
# frame1 = frame2
# ret, frame2 = cap.read()

    key = cv2.waitKey(5)
    if key == 27:
        break

cap.release()
cv2.destroyAllWindows()
