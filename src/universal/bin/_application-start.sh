#! /bin/sh

export SLACK_TOKEN=`cat /bot/slack-token`
daemon --name sleepnel-bot --output=/var/log/sleepnel-bot-out -- /bot/bin/slack_bot
