from google_images_download import google_images_download   #importing the library

response = google_images_download.googleimagesdownload()   #class instantiation

keyword_list = ["medieval castle Austria","medieval castle Spain","medieval castle portugal",
    "medieval castle france","medieval castle germany","medieval castle hungary",
    "medieval castle poland","medieval castle serbia","medieval castle romania",
    "medieval castle england","medieval castle scotland","medieval castle wales",]

for x in keyword_list:
    arguments = {"keywords":x,"limit":25,"print_urls":True}   #creating list of arguments

    paths = response.download(arguments)   #passing the arguments to the function
    print(paths)   #printing absolute paths of the downloaded images