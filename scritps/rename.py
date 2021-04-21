import os
import sys
import string

#funzione per rinominare tutti i file della cartella
def rename(folder_name):
    for count, filename in enumerate(os.listdir("C:\\Users\\Github\\sistemiDigitali\\data" + folder_name)):
        if filename.find(".jpg")>0:
            ext = ".jpg"
        elif filename.find(".png")>0:
            ext = ".png"
        elif filename.find(".jpeg")>0:
                ext = ".jpeg"
        new = folder_name + "." + str(count) + ext
        src = 'C:\\Users\\Github\\sistemiDigitali\\data' + folder_name + "\\" + filename
        dst = 'C:\\Users\\Github\\sistemiDigitali\\data' + folder_name + "\\" + new
        os.rename(src, dst)


if __name__ == '__main__':
    folder_name = input("Enter the folder name: ")
    rename(folder_name)
