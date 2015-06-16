slack bot

[![Circle CI](https://circleci.com/gh/rinunu/slack-bot/tree/master.svg?style=svg&circle-token=c19f01026d8a2de0cc0758cd2175391264bc7573)](https://circleci.com/gh/rinunu/slack-bot/tree/master)


# 実行
```sh
export SLACK_TOKEN0=xxxxx
export SIMSIMI_API_KEY=xxxxx # option
export THE_CAT_API_KEY=xxxxx # option
export CSE_SEARCH_ENGINE_ID=xxxxx # option
export CSE_API_KEY=xxxxx # option

bin/sbt "runMain nu.rinu.slackbot.SleepNelBot"
```

# いじる時は
[nu.rinu.slackbot.SimpleBot](src/main/scala/nu/rinu/slackbot/SimpleBot.scala) をいじってね


# 本番デプロイ

push してしばらく(2~3分)待ってね



# 各種 API について

## Google Custom Search

### ドキュメント
https://developers.google.com/custom-search/json-api/v1/introduction
この辺り

### API Key の発行/Custom Search Engine の作成
https://developers.google.com/custom-search/json-api/v1/introduction
この辺りを参考に。


