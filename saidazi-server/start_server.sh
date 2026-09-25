export JAVA_HOME="D:/java"
cd "D:/geek/外/saidazi-server"
nohup mvn spring-boot:run -Dspring.profiles.active=test > server.log 2>&1 &
echo "PID=$!"
# Wait for up to 60 seconds for server to start
for i in $(seq 1 30); do
  sleep 2
  if curl -s http://localhost:8080/api/match/teams?userId=1 > /dev/null 2>&1; then
    echo "SERVER READY after ${i} attempts"
    break
  fi
  echo "attempt $i..."
done
# Show the last 30 lines of the log
tail -30 server.log
