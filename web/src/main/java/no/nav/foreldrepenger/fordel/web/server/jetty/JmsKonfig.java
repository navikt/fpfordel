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

    private static void settOppJndiConnectionfactory(String queueManagerRootProperty, String jmsCf)
            throws JMSException, URISyntaxException {

        URI hostUri = new URI(getProperty("mottak_queue.queueManager"));
        String channel = getProperty("mqGateway02.channel");
        boolean useSslOnJetty = Boolean.parseBoolean(getProperty("mqGateway02.useSslOnJetty"));
        JndiUtil.register(jmsCf, createConnectionfactory(hostUri, channel, useSslOnJetty)); // NOSONAR we need the side
                                                                                            // effect
    }

    private static MQConnectionFactory createConnectionfactory(URI hostUri, String channel, boolean useSsl)
            throws JMSException {

        final MQConnectionFactory cf = new MQConnectionFactory();

        cf.setHostName(hostUri.getHost());
        cf.setPort(hostUri.getPort());
        if (channel != null) {
            cf.setChannel(channel);
        }
        cf.setQueueManager(hostUri.getPath().replace("/", ""));
        cf.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);

        if (useSsl) {
            // Denne trengs for at IBM MQ libs skal bruke/gjenkjenne samme ciphersuite navn
            // som Oracle JRE:
            // (Uten denne vil ikke IBM MQ libs gjenkjenne "TLS_RSA_WITH_AES_128_CBC_SHA")
            System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
            cf.setSSLCipherSuite("TLS_RSA_WITH_AES_128_CBC_SHA");
        }

        return cf;
    }

    private static void settOppJndiMessageQueue(String queueManagerRootProperty, String queueName, String jndiName)
            throws JMSException, URISyntaxException {

        settOppJndiConnectionfactory(queueManagerRootProperty, "jms/ConnectionFactory");
        JndiUtil.register(jndiName, new MQQueue(queueName)); // NOSONAR we need the side effect
    }

    private static String getProperty(String key) {
        String val = System.getProperty(key);
        if (val == null) {
            val = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        return val;
    }
}
