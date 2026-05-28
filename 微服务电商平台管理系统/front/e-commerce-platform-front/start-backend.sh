#!/bin/bash

echo "Starting E-Commerce Backend Service..."
echo

cd e-commerce-back

echo "Checking if Maven is installed..."
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed or not in PATH."
    echo "Please install Maven first: https://maven.apache.org/install.html"
    exit 1
fi

echo
echo "Starting Spring Boot application..."
echo "Backend will be available at: http://localhost:8080"
echo

mvn spring-boot:run