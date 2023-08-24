package no.nav.foreldrepenger.mottak.hendelse;

import static org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.felles.integrasjon.kafka.KafkaProperties;
import no.nav.vedtak.log.metrics.Controllable;
import no.nav.vedtak.log.metrics.LiveAndReadinessAware;

/*
 * Dokumentasjon https://confluence.adeo.no/pages/viewpage.action?pageId=432217859
 */
@ApplicationScoped
public class JournalHendelseStream implements LiveAndReadinessAware, Controllable {

    private static final Logger LOG = LoggerFactory.getLogger(JournalHendelseStream.class);

    private static final String APPLICATION_ID = "fpfordel"; // Hold konstant pga offset commit !!

    private static final String HENDELSE_MIDL = "JournalpostMottatt";
    private static final String HENDELSE_MIDL_LEGACY = "MidlertidigJournalført";
    private static final String HENDELSE_ENDRET = "TemaEndret";
    private static final String TEMA_FOR = Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode();

    private KafkaStreams stream;
    private Topic<String, JournalfoeringHendelseRecord> topic;

    JournalHendelseStream() {
    }

    @Inject
    public JournalHendelseStream(@KonfigVerdi("kafka.topic.journal.hendelse") String topicName,
                                 JournalføringHendelseHåndterer journalføringHendelseHåndterer) {
        this.topic = Topic.createConfiguredTopic(topicName);
        this.stream = createKafkaStreams(topic, journalføringHendelseHåndterer);
    }

    @SuppressWarnings("resource")
    private static KafkaStreams createKafkaStreams(Topic<String, JournalfoeringHendelseRecord> topic,
                                                   JournalføringHendelseHåndterer journalføringHendelseHåndterer) {
        final Consumed<String, JournalfoeringHendelseRecord> consumed = Consumed.<String, JournalfoeringHendelseRecord>with(
            Topology.AutoOffsetReset.LATEST).withKeySerde(topic.serdeKey()).withValueSerde(topic.serdeValue());

        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(topic.topic(), consumed)
            .filter((key, value) -> TEMA_FOR.equals(value.getTemaNytt()))
            .filter((key, value) -> hendelseSkalHåndteres(value))
            .foreach((key, value) -> journalføringHendelseHåndterer.handleMessage(value));

        return new KafkaStreams(builder.build(), KafkaProperties.forStreamsGenericValue(APPLICATION_ID, topic.serdeValue()));
    }

    private static boolean hendelseSkalHåndteres(JournalfoeringHendelseRecord payload) {
        var hendelse = payload.getHendelsesType();
        return HENDELSE_MIDL.equalsIgnoreCase(hendelse) || HENDELSE_ENDRET.equalsIgnoreCase(hendelse) || HENDELSE_MIDL_LEGACY.equalsIgnoreCase(
            hendelse);
    }

    private void addShutdownHooks() {
        stream.setStateListener((newState, oldState) -> {
            LOG.info("{} :: From state={} to state={}", getTopicName(), oldState, newState);

            if (newState == KafkaStreams.State.ERROR) {
                // if the stream has died there is no reason to keep spinning
                LOG.warn("{} :: No reason to keep living, closing stream", getTopicName());
                stop();
            }
        });
        stream.setUncaughtExceptionHandler(ex -> {
            LOG.error("{} :: Caught exception in stream, exiting", getTopicName(), ex);
            return SHUTDOWN_CLIENT;
        });
    }

    @Override
    public void start() {
        addShutdownHooks();

        stream.start();
        LOG.info("Starter konsumering av topic={}, tilstand={}", getTopicName(), stream.state());
    }

    public String getTopicName() {
        return topic.topic();
    }

    @Override
    public boolean isAlive() {
        return (stream != null) && stream.state().isRunningOrRebalancing();
    }

    @Override
    public boolean isReady() {
        return isAlive();
    }

    @Override
    public void stop() {
        LOG.info("Starter shutdown av topic={}, tilstand={} med 15 sekunder timeout", getTopicName(), stream.state());
        stream.close(Duration.ofSeconds(15));
        LOG.info("Shutdown av topic={}, tilstand={} med 15 sekunder timeout", getTopicName(), stream.state());
    }
}
