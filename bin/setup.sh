# ec2 をセットアップする
# 自動で実行するようにする

# http://docs.aws.amazon.com/codedeploy/latest/userguide/how-to-run-agent.html
sudo apt-get update
sudo apt-get install awscli
sudo apt-get install ruby2.0
cd /home/ubuntu
sudo aws s3 cp s3://bucket-name/latest/install . --region region-name
sudo chmod +x ./install
sudo ./install auto




# /bot/env.sh を作る


sudo apt-get install daemon
sudo apt-get install openjdk-7-jre
sudo dpkg-reconfigure tzdata
