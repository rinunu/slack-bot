#! /bin/sh

dir=`dirname $0`
. /bot/env.sh
daemon --name sleepnel-bot --output=/var/log/sleepnel-bot-out -- /bot/bin/slack_bot
