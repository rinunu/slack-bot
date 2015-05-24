#! /bin/sh

dir=`dirname $0`
. $dir/env.sh
daemon --name sleepnel-bot --output=/var/log/sleepnel-bot-out -- /bot/bin/slack_bot
