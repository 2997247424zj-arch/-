@echo off
echo Starting E-Commerce Backend Service...
echo.

cd e-commerce-back

echo Checking if Maven is installed...
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Maven is not installed or not in PATH.
    echo Please install Maven first: https://maven.apache.org/install.html
    pause
    exit /b 1
)

echo.
echo Starting Spring Boot application...
echo Backend will be available at: http://localhost:8080
echo.

mvn spring-boot:run

pause