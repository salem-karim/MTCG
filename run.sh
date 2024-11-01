#!/usr/bin/env bash

docker build -t mctg_postgres .
docker run -d -p 5432:5432 --name mtcgdb mctg_postgres
mvn exec:java -Dexec.mainClass=org.mtcg.Main
docker stop mtcgdb
docker rm mtcgdb
