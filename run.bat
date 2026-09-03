@echo off
setlocal
if not exist out mkdir out
if not exist lib\mysql-connector-j-9.4.0.jar (
  echo MySQL JDBC driver not found in lib folder.
  pause
  exit /b 1
)
echo Compiling Cricket Management System...
javac -cp "lib\mysql-connector-j-9.4.0.jar" -d out exception\*.java model\*.java util\*.java dao\*.java service\*.java main\*.java
if errorlevel 1 (
  echo Compilation failed.
  pause
  exit /b 1
)
echo.
echo Starting Cricket Management System...
java -cp "out;lib\mysql-connector-j-9.4.0.jar" main.CricketerManagementApp
pause
