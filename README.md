# rabbitmqsender
A command line Java class to send messages to rabbitmq cluster

To compile, open the shell and execute:
chmod +x build.sh
./build.sh

The .jar file will be compiled in ./build/libs folder

To run the application:

java -jar rabbitmqsender-<version>-all.jar -u=<username> -p=<password> -v=<virtual host> -h=<host name or IP> -n=<port> -e=<exchange> -q=<queue name> -r=<routing key> -t=<message type> -m=<message>

or, if you want details:

java -jar rabbitmqsender-<version>-all.jar

The software is distributed "as-is" without warranties or support.
