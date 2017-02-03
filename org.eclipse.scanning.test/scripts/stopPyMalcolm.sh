#!/usr/bin/env bash

echo "Stopping PyMalcolm..."
ps aux | grep -i imalcolm.py | awk {'print $2'} | xargs kill
echo "PyMalcolm Stopped"

exit 0