#!/bin/bash

rm ../static/* -Rf
cd ../../frontend
npm run build --scripts-prepend-node-path=auto;
cd ./dist
cp * ../backend/static/ -Rf
