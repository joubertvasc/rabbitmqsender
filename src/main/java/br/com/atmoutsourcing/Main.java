/**
 *
 * Author: Joubert Vasconcelos
 * Date: 2020-01-06
 *
 * How to build and deploy:
 * ./build.sh
 * scp build/libs/rabbitmqsender-<version>-all.jar <user>root@<server>:<folder>
 *
 * ex.:
 * ./build.sh
 * scp build/libs/rabbitmqsender-1.0-all.jar root@10.45.0.40:/usr/share/rabbitclient
 */
package br.com.atmoutsourcing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Main {

    private static final String VERSION = "1.0";

    private static final String FANOUT = "fanout";
    private static final String DIRECT = "direct";
    private static final String TOPIC = "topic";

    public static void main(String[] args) {
        String url = "";
        String username = "";
        String password = "";
        String vhost = "/";
        String exchange = "";
        String routingKey = "";
        String queue = "";
        String message = "";
        String type = DIRECT;
        int attempts = 5;
        int port = 5672;

        for (String arg : args) {
            String[] param = arg.split("=");

            if (param.length == 2) {
                switch (param[0].toLowerCase()) {
                    case "-h":
                        url = param[1].trim();
                        break;
                    case "-u":
                        username = param[1].trim();
                        break;
                    case "-p":
                        password = param[1].trim();
                        break;
                    case "-v":
                        vhost = param[1].trim();
                        break;
                    case "-e":
                        exchange = param[1].trim();
                        break;
                    case "-r":
                        routingKey = param[1].trim();
                        break;
                    case "-m":
                        message = param[1].trim();
                        break;
                    case "-t":
                        type = param[1].trim();
                        break;
                    case "-q":
                        queue = param[1].trim();
                        break;
                    case "-a":
                        try {
                            attempts = Integer.valueOf(param[1].trim());
                        } catch (Exception e) {
                            attempts = -1;
                        }
                        break;
                    case "-n":
                        try {
                            port = Integer.valueOf(param[1].trim());
                        } catch (Exception e) {
                            port = -1;
                        }
                        break;
                }
            }
        }

        if (url.equals("") || username.equals("") || password.equals("") || vhost.equals("") ||
                exchange.equals("") || routingKey.equals("") || message.equals("") || type.equals("")) {
            showUsage();
        } else if (!type.equals(DIRECT) && !type.equals(FANOUT) && !type.equals(TOPIC)) {
            showUsage();
        } else if (queue.equals("") && routingKey.equals("")) {
            showUsage();
        } else if (attempts < 1 || attempts > 30 || port < 1024) {
            showUsage();
        } else {
            sendMessage(url, port, username, password, vhost, exchange, routingKey, queue, message, type, attempts);
        }
    }

    private static void showUsage() {
        System.out.println("ATM RABBITMQ Sender " + VERSION + " (Java Version)");
        System.out.println("");
        System.out.println("Usage: rabbitmqsender -h=<host> -n=<port> -u=<username> -p=<password> -v=<vhost> -e=<exchange> -r=<routingkey> -q=<queue> -t=<type> -a=<attempts> -m=<message>");
        System.out.println("");
        System.out.println("Params:");
        System.out.println("");
        System.out.println("-h host name or IP address to connect.");
        System.out.println("-u username used in connection.");
        System.out.println("-p password used in connection.");
        System.out.println("-e exchange to send the message.");
        System.out.println("-m text message to be delivered.");
        System.out.println("-q (optional if -r is defined) queue name. If blank the same Routing Key will be used.");
        System.out.println("-v (optional) virtual host to send the message. Default is /");
        System.out.println("-r (optional if -q is defined) routing key to send the message. If blank a direct message will be delivered.");
        System.out.println("-t (optional) message type. Default is " + DIRECT + ". Options are: " + DIRECT + ", " + FANOUT + " or " + TOPIC + ". Headers is not supported.");
        System.out.println("-n (optional) Port Number to connect. Default is 5672 and must be greater than 1024.");
        System.out.println("-a (optional) Number of connection attempts. Default is 5 and must be an integer value between 1 and 30.");
        System.out.println("");
    }

    private static void sendMessage(String hostName, int portNumber, String userName, String password, String virtualHost,
                                    String exchangeName, String routingKey, String queueName, String message,
                                    String exchangeType, int attempts) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        factory.setHost(hostName);
        factory.setPort(portNumber);

        Connection conn = null;
        String errorMessage = "";
        for (int i = 0; i < attempts; i++) {
            try {
                conn = factory.newConnection();
                break;
            } catch (Exception e) {
                errorMessage = e.getMessage();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (conn == null) {
            System.out.println("Could not connect to RabbitMQ in " + hostName + ":" + String.valueOf(portNumber) + " after " + String.valueOf(attempts) + " attempts: " + errorMessage);
        } else {
            try {
                Channel channel = conn.createChannel();
                try {
                    channel.exchangeDeclare(exchangeName, exchangeType, false);
                    channel.queueDeclare(queueName.equals("") ? routingKey : queueName, true, false, false, null);
                    channel.queueBind(queueName, exchangeName, routingKey.equals("") ? queueName : routingKey);

                    byte[] messageBodyBytes = message.getBytes();
                    channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);
                    System.out.println("Message successfully published.");
                } finally {
                    channel.close();
                    conn.close();
                }
            } catch (Exception e) {
                System.out.println("The message could not be published: " + e.getMessage() + ".");
            }
        }
    }
}
