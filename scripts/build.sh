#! /bin/sh

cd ..

./gradlew clean build -x test

docker build -t daeyo-app:latest .

docker tag daeyo-app:latest sso9594/daeyo-app:latest

docker push sso9594/daeyo-app:latest