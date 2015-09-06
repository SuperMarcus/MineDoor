#!/bin/bash

echo "[*] MineDoor - By Marcus (https://github.com/SuperMarcus)"

DIR="$(pwd)"
date > "$DIR/install.log" 2>&1
uname -a >> "$DIR/install.log" 2>&1

while getopts "nt" OPTION 2> /dev/null; do
	case ${OPTION} in
		n)
			NO_CHECKOUT="yes"
			;;
		t)
		    TRAVIS_BUILD="yes"
		    ;;
		\?)
			break
			;;
	esac
done

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

if [ "$NO_CHECKOUT" == "yes" ]; then
    echo "[*] Skip checkout from git..."
else
	echo "[*] Downloading latest git checkout..."
    git clone --recursive "https://github.com/SuperMarcus/MineDoor.git" >> "$DIR/install.log" 2>&1
fi

if [ "$TRAVIS_BUILD" == "yes" ]; then
    if [ -f ./pom.xml ]; then
        echo "[*] Building in travis..."
    else
        echo "[!] No valid travis checkout found. Please check the \"install.log\" file."
        exit 1
    fi
elif [ -d ./MineDoor ]; then
    cd MineDoor
else
    echo "[!] No valid checkout found. Please check the \"install.log\" file."
    exit 1
fi

echo "[*] Compiling..."

mvn package >> "$DIR/install.log" 2>&1

ERRORS="$(cat "$DIR/install.log" | grep ERROR)"

echo "[*] Copying dependencies..."

mvn dependency:copy-dependencies >> "$DIR/install.log" 2>&1

if [ "$TRAVIS_BUILD" == "yes" ]; then
    if [ "$ERRORS" == "" ]; then
        echo "[*] Finished travis build."
        exit 0
    else
        echo "[!] Some errors triggered when compiling. Build failing"
        echo "$ERRORS"
        exit 1
    fi
fi

echo "[*] Copying files..."

cp -ri build/* ../
cd ..

echo "[*] Downloading \"start.sh\"..."

download_file "https://raw.githubusercontent.com/SuperMarcus/MineDoor/master/start.sh" > start.sh
chmod 777 start.sh

echo "[*] Cleaning..."

rm -rf MineDoor

if [ "$ERRORS" == "" ]; then
    echo "[*] Done! Run \"./start.sh\" to start MineDoor."
    exit 0
else
    echo "[!] Some errors triggered when compiling. Please check the \"install.log\" file."
    echo "$ERRORS"
    exit 1
fi