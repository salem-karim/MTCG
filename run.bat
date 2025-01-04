@echo off
docker build -t mctg_postgres .
docker run -d -p 5434:5432 --name mtcgdb mctg_postgres
call mvnw.cmd clean test
call mvnw.cmd exec:java -Dexec.mainClass=org.mtcg.Main
docker stop mtcgdb
docker rm mtcgdb
