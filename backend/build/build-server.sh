#!/bin/bash

rm ../static/js/* -R
rm ../static/css/* -R
cd ../../frontend
/usr/bin/node /usr/share/npm/bin/npm-cli.js run build --scripts-prepend-node-path=auto;
cd ./build
cp manifest.json ../../backend/static
cp asset-manifest.json ../../backend/static
cd ./static
cp media ../../../backend/static -r;
cp css ../../../backend/static -r;
cp js ../../../backend/static -r;
