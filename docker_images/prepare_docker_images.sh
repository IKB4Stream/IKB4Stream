#!/bin/sh

DISTRI_PATH="build/distributions/"
ROOT_PATH=$(pwd)

ZIP_NAME="ikb4stream-1.0-SNAPSHOT-all.zip"
FOLDER_NAME="ikb4stream-1.0-SNAPSHOT-all/"

PRODUCER_JAR_NAME="ikb4stream-1.0-SNAPSHOT-producer.jar"
PRODUCER_VERSION="latest"

CONSUMER_JAR_NAME="ikb4stream-1.0-SNAPSHOT-consumer.jar"
CONSUMER_VERSION="latest"

RESOURCES_NAME="resources/"

PRODUCER_IMAGE_FOLDER_PATH="docker_images/producer"
CONSUMER_IMAGE_FOLDER_PATH="docker_images/consumer"
DOCKER_IMAGES_FOLDER="docker_images/"

echo "#####################################################################"
echo "#                Ikb4stream => Docker image creation                #"
echo "#                       Already end of support!                     #"
echo "#####################################################################"

#go into the root path
pwd
cd $ROOT_PATH"/"$DISTRI_PATH

#unzip file
echo "File unzipping..."
unzip $ZIP_NAME
echo "File unzipped!"

#files copies
echo "Files copies..."
cp $PRODUCER_JAR_NAME $ROOT_PATH"/"$PRODUCER_IMAGE_FOLDER_PATH"/"
cp $CONSUMER_JAR_NAME $ROOT_PATH"/"$CONSUMER_IMAGE_FOLDER_PATH"/"
cp -rf $RESOURCES_NAME $ROOT_PATH"/"$PRODUCER_IMAGE_FOLDER_PATH"/"$RESOURCES_NAME
cp -rf $RESOURCES_NAME $ROOT_PATH"/"$CONSUMER_IMAGE_FOLDER_PATH"/"$RESOURCES_NAME
cp -rf $RESOURCES_NAME $ROOT_PATH"/"$DOCKER_IMAGES_FOLDER
echo "Files copied!"

pwd
ls

echo "#########################"
echo "#       FINISHED!       #"
echo "#########################"
