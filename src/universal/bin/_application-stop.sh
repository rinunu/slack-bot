#! /bin/sh

if daemon --name sleepnel-bot --running
then
  daemon --name sleepnel-bot --stop
fi
