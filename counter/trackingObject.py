from counter import counterClass
from firebase import firebase 

firebase = firebase.FirebaseApplication('https://bustracker-c4a91.firebaseio.com')


class trackingObject:
    objectID = ""
    centroid = ""
    enterLine = ""
    exitLine = ""
    position = ""
    outside = False
    middle = False
    inside = False
    isCount = False

    def __init__(self, objectID, centroid, enterLine, exitLine):

        self.objectID = objectID
        self.centroid = centroid
        self.enterLine = enterLine
        self.exitLine = exitLine
        self.position = ""
        self.outside = False
        self.middle = False
        self.inside = False
        self.isCount = False

    def checkTrackPosition(self, centroid, enterLine, exiteLine):

        x, _ = centroid

        if x > enterLine:
            self.outside = True
            self.position = "outside"

        if exiteLine < x < enterLine:
            self.middle = True

        if x < exiteLine:
            self.inside = True
            self.position = "inside"

        if self.outside and self.middle and self.inside and (not self.isCount):
            self.isCount = True
            if self.position == "inside":
                counterClass.EntranceCounter += 1
                firebase.put('users/bus1',"passengers", (counterClass.EntranceCounter  - counterClass.ExitCounter))
            else:
                counterClass.ExitCounter += 1
                firebase.put('users/bus1',"passengers", (counterClass.EntranceCounter  - counterClass.ExitCounter))
