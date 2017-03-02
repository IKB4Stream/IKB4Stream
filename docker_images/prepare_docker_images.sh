#!/bin/sh

ROOT_PATH="build/distributions/"
CURRENT_PATH = pwd

ZIP_NAME="ikb4stream-1.0-SNAPSHOT-all.zip"
FOLDER_NAME="ikb4stream-1.0-SNAPSHOT-all/"

PRODUCER_JAR_NAME="ikb4stream-1.0-SNAPSHOT-producer.jar"
PRODUCER_VERSION="latest"

CONSUMER_JAR_NAME="ikb4stream-1.0-SNAPSHOT-consumer.jar"
CONSUMER_VERSION="latest"

RESOURCES_NAME="resources/"

PRODUCER_IMAGE_FOLDER_PATH="docker_images/producer"
CONSUMER_IMAGE_FOLDER_PATH="docker_images/consumer"

echo "#####################################################################"
echo "#                Ikb4stream => Docker image creation                #"
echo "#                       Already end of support!                     #"
echo "#####################################################################"

#go into the root path
pwd
cd $ROOT_PATH

#unzip file
echo "File unzipping..."
unzip $ZIP_NAME
echo "File unzipped!"

#create folder to store images
#echo "Image's folders creation..."
#mkdir producer/
#mkdir consumer/
#echo "Folders created!"

#files copies
echo "Files copies..."
cp $PRODUCER_JAR_NAME $PRODUCER_IMAGE_FOLDER_PATH"/"
cp $CONSUMER_JAR_NAME $CONSUMER_IMAGE_FOLDER_PATH"/"
cp -r $RESOURCES_NAMES $PRODUCER_IMAGE_FOLDER_PATH
cp -r $RESOURCES_NAMES $CONSUMER_IMAGE_FOLDER_PATH
echo "Files copied!"

#producer image creation
#echo "Creation of the producer image version : $PRODUCER_VERSION"
#cd producer/
#docker build -t producer:$PRODUCER_VERSION .
#cd ..
#echo "Producer image created!"

#consumer image creation
#echo "Creation of the consumer image version : $CONSUMER_VERSION"
#cd consumer/
#docker build -t consumer:$CONSUMER_VERSION .
#cd ..
#echo "Consumer image created!"

#cleaning the folder
#echo "Cleaning the folder"
#rm *.jar
#echo "Folder cleaned!"

ls

echo "#########################"
echo "#       FINISHED!       #"
echo "#########################"
