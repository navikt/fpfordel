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
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.apptjeneste.AppServiceHandler;
import no.nav.vedtak.log.metrics.LivenessAware;
import no.nav.vedtak.log.metrics.ReadinessAware;

/*
 * Dokumentasjon https://confluence.adeo.no/pages/viewpage.action?pageId=315215917
 */
@ApplicationScoped
public class JournalføringHendelseStream implements LivenessAware, ReadinessAware, AppServiceHandler {

    private static final Environment ENV = Environment.current();

    private static final Logger LOG = LoggerFactory.getLogger(JournalføringHendelseStream.class);
    private static final String HENDELSE_MIDL = "MidlertidigJournalført";
    private static final String HENDELSE_ENDRET = "TemaEndret";
    private static final String TEMA_FOR = Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode();

    private static final boolean isDeployment = ENV.isProd() || ENV.isDev();

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
        if (isDeployment) return null;
        if ((properties.getSchemaRegistryUrl() != null) && !properties.getSchemaRegistryUrl().isEmpty()) {
            var schemaMap = Map.of("schema.registry.url", properties.getSchemaRegistryUrl(), "specific.avro.reader", true);
            topic.serdeKey().configure(schemaMap, true);
            topic.serdeValue().configure(schemaMap, false);
        }

        final Consumed<String, JournalfoeringHendelseRecord> consumed = Consumed
                .<String, JournalfoeringHendelseRecord>with(Topology.AutoOffsetReset.LATEST)
                .withKeySerde(topic.serdeKey())
                .withValueSerde(topic.serdeValue());

        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(topic.topic(), consumed)
                .filter((key, value) -> TEMA_FOR.equals(value.getTemaNytt().toString()))
                .filter((key, value) -> hendelseSkalHåndteres(value))
                .foreach((key, value) -> journalføringHendelseHåndterer.loggMessage(value));

        return new KafkaStreams(builder.build(), properties.getProperties());
    }

    private static boolean hendelseSkalHåndteres(JournalfoeringHendelseRecord payload) {
        var hendelse = payload.getHendelsesType().toString();
        return HENDELSE_MIDL.equalsIgnoreCase(hendelse) || HENDELSE_ENDRET.equalsIgnoreCase(hendelse);
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
        if (isDeployment) return;
        addShutdownHooks();

        stream.start();
        LOG.info("Starter konsumering av topic={}, tilstand={}", getTopicName(), stream.state());
    }

    public KafkaStreams.State getTilstand() {
        return stream.state();
    }

    public String getTopicName() {
        return topic.topic();
    }

    @Override
    public boolean isAlive() {
        if (isDeployment) return true;
        return (stream != null) && stream.state().isRunningOrRebalancing();
    }

    @Override
    public boolean isReady() {
        if (isDeployment) return true;
        return isAlive();
    }

    @Override
    public void stop() {
        if (isDeployment) return;
        LOG.info("Starter shutdown av topic={}, tilstand={} med 10 sekunder timeout", getTopicName(), stream.state());
        stream.close(Duration.ofSeconds(20));
        LOG.info("Shutdown av topic={}, tilstand={} med 10 sekunder timeout", getTopicName(), stream.state());
    }

}
