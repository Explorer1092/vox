#!/bin/bash

echo "changing ulimit ..."
ulimit -u 100000

echo "prepare to run $@"
$@
