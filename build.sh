#!/bin/bash

echo "[*] MineDoor - By Marcus (https://github.com/SuperMarcus)"

DIR="$(pwd)"
date > "$DIR/install.log" 2>&1
uname -a >> "$DIR/install.log" 2>&1

echo "[*] Checking dependecies..."

type mvn >> "$DIR/install.log" 2>&1 || { echo >&2 "[!] Please install \"mvn\""; read -p "Press [Enter] to continue..."; exit 1; }
type git >> "$DIR/install.log" 2>&1 || { echo >&2 "[!] Please install \"git\""; read -p "Press [Enter] to continue..."; exit 1; }

shopt -s expand_aliases
type wget >> "$DIR/install.log" 2>&1
if [ $? -eq 0 ]; then
	alias download_file="wget --no-check-certificate -q -O -"
else
	type curl >> "$DIR/install.log" 2>&1
	if [ $? -eq 0 ]; then
		alias download_file="curl --insecure --silent --location"
	else
		echo "error, curl or wget not found"
	fi
fi

echo "[*] Downloading latest git checkout..."

git clone --recursive "https://github.com/SuperMarcus/MineDoor.git" >> "$DIR/install.log" 2>&1

cd MineDoor

echo "[*] Compiling..."

mvn package >> "$DIR/install.log" 2>&1

echo "[*] Copying dependencies..."

mvn dependency:copy-dependencies >> "$DIR/install.log" 2>&1

echo "[*] Copying files..."

cp -ri build/* ../
cd ..

echo "[*] Downloading \"start.sh\"..."

download_file "https://raw.githubusercontent.com/SuperMarcus/MineDoor/master/start.sh" > start.sh
chmod 777 start.sh

echo "[*] Cleaning..."

rm -rf MineDoor

echo "[*] Done! Run \"./start.sh\" to start MineDoor."