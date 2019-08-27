package no.nav.foreldrepenger.fordel.web.server.jetty;

import java.net.URI;
import java.net.URISyntaxException;

import javax.jms.JMSException;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.wmq.compat.jms.internal.JMSC;

import no.nav.foreldrepenger.fordel.web.server.jetty.util.JndiUtil;


class JmsKonfig {

    void konfigurer() throws JMSException, URISyntaxException {
        String queueName = getProperty("mottak_queue.queueName");
        settOppJndiMessageQueue("mqGateway02", queueName, "jms/QueueMottak");
    }

    private static void settOppJndiConnectionfactory(@SuppressWarnings("unused") String queueManagerRootProperty, // NOSONAR
                                                     String jmsConnectionFactory) throws JMSException, URISyntaxException {

        URI hostUri = null;
        hostUri = new URI(getProperty("mottak_queue.queueManager"));

        String hostName = hostUri.getHost();
        Integer port = hostUri.getPort();
        String channel = getProperty("mqGateway02.channel");
        String queueManagerName = hostUri.getPath().replace("/", "");
        boolean useSslOnJetty = Boolean.parseBoolean(getProperty("mqGateway02.useSslOnJetty"));

        final MQConnectionFactory connectionFactory = createConnectionfactory(hostName, port, channel, queueManagerName,
                useSslOnJetty);

        JndiUtil.register(jmsConnectionFactory, connectionFactory); // NOSONAR we need the side effect
    }

    private static MQConnectionFactory createConnectionfactory(String hostName, Integer port, String channel,
                                                               String queueManagerName, boolean useSsl) throws JMSException {

        final MQConnectionFactory connectionFactory = new MQConnectionFactory();

        connectionFactory.setHostName(hostName);
        connectionFactory.setPort(port);
        if (channel != null) {
            connectionFactory.setChannel(channel);
        }
        connectionFactory.setQueueManager(queueManagerName);
        connectionFactory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);

        if (useSsl) {
            // Denne trengs for at IBM MQ libs skal bruke/gjenkjenne samme ciphersuite navn
            // som Oracle JRE:
            // (Uten denne vil ikke IBM MQ libs gjenkjenne "TLS_RSA_WITH_AES_128_CBC_SHA")
            System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");

            connectionFactory.setSSLCipherSuite("TLS_RSA_WITH_AES_128_CBC_SHA");
        }

        return connectionFactory;
    }

    private static void settOppJndiMessageQueue(String queueManagerRootProperty, String queueName, String jndiName)
            throws JMSException, URISyntaxException {

        String jmsConnectionFactory = "jms/ConnectionFactory";
        settOppJndiConnectionfactory(queueManagerRootProperty, jmsConnectionFactory);
        MQQueue queue = new MQQueue(queueName);
        JndiUtil.register(jndiName, queue); // NOSONAR we need the side effect
    }

    private static String getProperty(String key) {
        String val = System.getProperty(key);
        if (val == null) {
            val = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        return val;
    }
}
