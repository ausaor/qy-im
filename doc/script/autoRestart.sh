#!/bin/bash
ID=`ps -ef | grep im-platform.jar | grep -v 'grep' | awk '{print $2}'` #注意查询出bash本身的进程号
echo $ID
echo "--------------"
for id in $ID
do
kill -9 $id
echo "killed $id"
done
sleep 2
echo "restart begin"
source /etc/profile
nohup java -jar -Xms128m -Xmx512m -XX:PermSize=128M -XX:MaxPermSize=512M /app/qy-im/qy-platform/im-platform.jar --spring.profiles.active=prod  > /app/qy-im/qy-platform/nohup.out 2>&1 &
processID=`ps -ef | grep im-platform.jar | grep -v 'grep' | awk '{print $2}'`
echo "restart success $processID"
