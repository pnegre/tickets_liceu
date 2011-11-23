#!/bin/bash

ant clean && ant debug && adb -s emulator-5554 install -r bin/TicketsLiceu-debug.apk


