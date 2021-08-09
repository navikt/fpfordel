package no.nav.foreldrepenger.mottak.hendelse;

import java.time.Duration;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.apptjeneste.AppServiceHandler;
import no.nav.vedtak.log.metrics.LivenessAware;
import no.nav.vedtak.log.metrics.ReadinessAware;

/*
 * Dokumentasjon https://confluence.adeo.no/pages/viewpage.action?pageId=315215917
 */
@ApplicationScoped
public class JournalføringHendelseStream implements LivenessAware, ReadinessAware, AppServiceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JournalføringHendelseStream.class);
    private static final String TEMA_FOR = Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode();

    private KafkaStreams stream;
    private Topic<String, JournalfoeringHendelseRecord> topic;

    JournalføringHendelseStream() {
    }

    @Inject
    public JournalføringHendelseStream(JournalføringHendelseHåndterer journalføringHendelseHåndterer,
            JournalføringHendelseProperties journalføringHendelseProperties) {
        this.topic = journalføringHendelseProperties.getTopic();
        this.stream = createKafkaStreams(topic, journalføringHendelseHåndterer, journalføringHendelseProperties);
    }

    @SuppressWarnings("resource")
    private static KafkaStreams createKafkaStreams(Topic<String, JournalfoeringHendelseRecord> topic,
            JournalføringHendelseHåndterer journalføringHendelseHåndterer,
            JournalføringHendelseProperties properties) {
        if ((properties.getSchemaRegistryUrl() != null) && !properties.getSchemaRegistryUrl().isEmpty()) {
            var schemaMap = Map.of("schema.registry.url", properties.getSchemaRegistryUrl(), "specific.avro.reader", true);
            topic.getSerdeKey().configure(schemaMap, true);
            topic.getSerdeValue().configure(schemaMap, false);
        }

        final Consumed<String, JournalfoeringHendelseRecord> consumed = Consumed
                .<String, JournalfoeringHendelseRecord>with(Topology.AutoOffsetReset.LATEST)
                .withKeySerde(topic.getSerdeKey())
                .withValueSerde(topic.getSerdeValue());

        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(topic.getTopic(), consumed)
                .filter((key, value) -> TEMA_FOR.equals(value.getTemaNytt().toString()))
                .foreach((key, value) -> journalføringHendelseHåndterer.handleMessage(value));

        return new KafkaStreams(builder.build(), properties.getProperties());
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
        stream.setUncaughtExceptionHandler((t, e) -> {
            LOG.error("{} :: Caught exception in stream, exiting", getTopicName(), e);
            stop();
        });
    }

    @Override
    public void start() {
        addShutdownHooks();

        stream.start();
        LOG.info("Starter konsumering av topic={}, tilstand={}", getTopicName(), stream.state());
    }

    public KafkaStreams.State getTilstand() {
        return stream.state();
    }

    public String getTopicName() {
        return topic.getTopic();
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
        LOG.info("Starter shutdown av topic={}, tilstand={} med 10 sekunder timeout", getTopicName(), stream.state());
        stream.close(Duration.ofSeconds(20));
        LOG.info("Shutdown av topic={}, tilstand={} med 10 sekunder timeout", getTopicName(), stream.state());
    }

}
