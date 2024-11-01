# Use the official PostgreSQL image from Docker Hub
FROM postgres:latest

# Set environment variables for PostgreSQL
ENV POSTGRES_USER=mtcgdb
ENV POSTGRES_PASSWORD=mtcgadmin
ENV POSTGRES_DB=mtcg_db

# Copy the SQL script to the container
COPY ./src/main/resources/db/mtcg.sql /docker-entrypoint-initdb.d/

EXPOSE 5432

# Install the uuid-ossp extension
RUN apt-get update && \
    apt-get install -y postgresql-contrib && \
    apt-get clean
# The PostgreSQL image automatically runs scripts in /docker-entrypoint-initdb.d/
