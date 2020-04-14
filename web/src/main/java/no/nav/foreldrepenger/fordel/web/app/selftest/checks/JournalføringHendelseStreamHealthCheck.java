package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;

import no.nav.foreldrepenger.mottak.hendelse.JournalføringHendelseStream;

@ApplicationScoped
public class JournalføringHendelseStreamHealthCheck extends ExtHealthCheck {

    private JournalføringHendelseStream consumer;

    JournalføringHendelseStreamHealthCheck() {
    }

    @Inject
    public JournalføringHendelseStreamHealthCheck(JournalføringHendelseStream consumer) {
        this.consumer = consumer;
    }

    @Override
    protected String getDescription() {
        return "Test av consumering av journalføringshendelser fra kafka";
    }

    @Override
    protected String getEndpoint() {
        return consumer.getTopicName();
    }

    @Override
    protected InternalResult performCheck() {
        InternalResult intTestRes = new InternalResult();

        KafkaStreams.State tilstand = consumer.getTilstand();
        intTestRes.setMessage("Consumer is in state [" + tilstand.name() + "].");
        intTestRes.setOk(tilstand.isRunning() || KafkaStreams.State.CREATED.equals(tilstand));
        intTestRes.noteResponseTime();
        return intTestRes;
    }
}
