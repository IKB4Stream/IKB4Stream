#! /bin/sh

#Docker images preparation
DOCKER_IMAGES_PREPARE_PATH="docker_images/"

./$DOCKER_IMAGES_PREPARE_PATH/prepare_docker_images.sh

#Docker imafges preparation
cd $DOCKER_IMAGES_PREPARE_PATH
./build_docker_images.sh

#Project launch
docker-compose up
