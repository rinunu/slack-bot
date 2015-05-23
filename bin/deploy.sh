#! /bin/sh

# `git `

# s3=s3://deploy-sleepnel-bot/slack_bot-0.1-SNAPSHOT.zip
rev=`git rev-parse HEAD`
file=sleepnel-bot-${rev}.zip
app=SleepNelBot
region=us-east-1
bucket=deploy-sleepnel-bot
group=SleepNelBotGroup

aws --version

bin/sbt universal:packageBin

aws s3 cp target/universal/*.zip s3://${bucket}/${file}

aws \
  --region $region \
  deploy create-deployment \
  --application-name $app \
  --deployment-group-name $group \
  --deployment-config-name CodeDeployDefault.OneAtATime \
  --s3-location bucket=$bucket,key="$file",bundleType=zip
