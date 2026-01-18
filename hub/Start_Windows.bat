@echo off
cls
title AetherShell Hub

echo ------------------------------------------------
echo         AETHERSHELL HUB IS INITIALIZING
echo ------------------------------------------------

:: 2>nul hides the "Restricted Method" and "Unsafe" JVM warnings
java -jar hub.jar 2>nul

:: If the app crashes, this keeps the window open so they can tell you why
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Hub stopped unexpectedly.
    pause
)