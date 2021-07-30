# Required Libraries
import cv2
import numpy as np
from os import listdir
from os.path import isfile, join
from pathlib import Path
import argparse
import numpy
  
# Argument parsing variable declared
ap = argparse.ArgumentParser()
  
ap.add_argument("-i", "--image",
                required=True,
                help="Path to folder")
  
args = vars(ap.parse_args())

#path where to save images
savePath = 'D:/GitHub/sistemiDigitali/data/test'
counter = 0

# Find all the images in the provided images folder
mypath = args["image"]
onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]
images = numpy.empty(len(onlyfiles), dtype=object)

# Iterate through every image
# and resize all the images.
for n in range(0, len(onlyfiles)):
  
    imgPath = join(mypath, onlyfiles[n])
    images[n] = cv2.imread(join(mypath, onlyfiles[n]),
                           cv2.IMREAD_UNCHANGED)
  
    # Load the image in img variable
    img = cv2.imread(imgPath, 1)
  
    # Define a resizing Scale
    # To declare how much to resize
    resized_dimentions = (224, 224)
  
    # Create resized image using the calculated dimentions
    resized_image = cv2.resize(img, resized_dimentions,
                               interpolation=cv2.INTER_AREA)
  
    counter+=1
    # Save the image in Output Folder
    cv2.imwrite(join(savePath , "resized"+str(counter)+".jpg"), resized_image)
  
print("Images resized Successfully")
