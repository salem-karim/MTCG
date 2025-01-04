#!/usr/bin/env bash

docker build -t mctg_postgres .
docker run -d -p 5434:5432 --name mtcgdb mctg_postgres
./mvnw clean test
./mvnw exec:java -Dexec.mainClass=org.mtcg.Main
docker stop mtcgdb
docker rm mtcgdb
