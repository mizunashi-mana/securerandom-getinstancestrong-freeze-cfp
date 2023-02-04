#!/usr/bin/env bash

set -euxo pipefail

if [ "$REMOVE_DEV_RANDOM" = 'true' ]; then
    rm /dev/random
fi

mkfifo /dev/random

java Main &
echo "PID: $!"

exec tee /dev/random
