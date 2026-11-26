@echo off
chcp 65001 >nul
title Bloom Beauty - Subir a GitHub
cd /d "%~dp0"

echo ==========================================================
echo   Subiendo Bloom Beauty a https://github.com/Ashly958/bloom_beauty
echo ==========================================================
echo.

where git >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Git no esta instalado en este PC.
  echo Instalalo desde https://git-scm.com/download/win y vuelve a ejecutar este archivo.
  echo.
  pause
  exit /b 1
)

if not exist ".git" git init
git config user.name "Ashly Nicole Perez Lopez"
git config user.email "Ashly958@users.noreply.github.com"
git config core.autocrlf true

git add -A
git commit -q -m "Parcial 2: backend Spring Boot, diagramas C4/UML, ADRs y diccionario de datos" -m "Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>" -m "Claude-Session: https://claude.ai/code/session_01PU7uqY9HzxghyHQFeLPEXx"
git branch -M main

git remote remove origin >nul 2>nul
git remote add origin https://github.com/Ashly958/bloom_beauty.git

echo.
echo Si se abre una ventana de GitHub, inicia sesion y pulsa "Authorize".
echo.

rem Si el repositorio ya tenia un README u otro archivo, se combina conservando los archivos locales
git fetch origin >nul 2>nul
git rev-parse --verify origin/main >nul 2>nul
if not errorlevel 1 git merge origin/main --allow-unrelated-histories -X ours --no-edit -m "Combina el contenido inicial del repositorio"

git push -u origin main
if errorlevel 1 (
  echo.
  echo [ERROR] No se pudo subir. Revisa el mensaje de arriba.
  echo.
  pause
  exit /b 1
)

echo.
echo ==========================================================
echo   LISTO. Proyecto publicado en:
echo   https://github.com/Ashly958/bloom_beauty
echo ==========================================================
echo.
pause
