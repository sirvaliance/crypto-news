#!/bin/sh
datadir="$(dirname $0)/mongo-data/"
mkdir -p "$datadir"
mongod -dbpath "$datadir"
