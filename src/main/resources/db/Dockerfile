# Use the official PostgreSQL image from Docker Hub
FROM postgres:latest

# Set environment variables for PostgreSQL
ENV POSTGRES_USER=mctgdb
ENV POSTGRES_PASSWORD=mctgadmin
ENV POSTGRES_DB=mctg_db

# Copy the SQL script to the container
COPY mctg.sql /docker-entrypoint-initdb.d/

EXPOSE 5432

# Install the uuid-ossp extension
RUN apt-get update && \
    apt-get install -y postgresql-contrib && \
    apt-get clean
# The PostgreSQL image automatically runs scripts in /docker-entrypoint-initdb.d/
