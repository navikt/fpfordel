package no.nav.foreldrepenger.mottak.hendelse;

import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;

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

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.apptjeneste.AppServiceHandler;
import no.nav.vedtak.log.metrics.LivenessAware;
import no.nav.vedtak.log.metrics.ReadinessAware;


@ApplicationScoped
public class JournalHendelseStream implements LivenessAware, ReadinessAware, AppServiceHandler {

    private static final Environment ENV = Environment.current();

    private static final Logger LOG = LoggerFactory.getLogger(JournalHendelseStream.class);
    private static final String HENDELSE_MIDL = "JournalpostMottatt";
    private static final String HENDELSE_MIDL_LEGACY = "MidlertidigJournalført";
    private static final String HENDELSE_ENDRET = "TemaEndret";
    private static final String TEMA_FOR = Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode();

    private final boolean isDeployment = ENV.isProd() || ENV.isDev();

    private KafkaStreams stream;
    private Topic<String, JournalfoeringHendelseRecord> topic;

    JournalHendelseStream() {
    }

    @Inject
    public JournalHendelseStream(JournalføringHendelseHåndterer journalføringHendelseHåndterer,
                                 JournalHendelseProperties streamKafkaProperties) {
        this.topic = streamKafkaProperties.getTopic();
        this.stream = isDeployment ? createKafkaStreams(topic, journalføringHendelseHåndterer, streamKafkaProperties) : null;
    }

    @SuppressWarnings("resource")
    private static KafkaStreams createKafkaStreams(Topic<String, JournalfoeringHendelseRecord> topic,
                                                   JournalføringHendelseHåndterer journalføringHendelseHåndterer,
                                                   JournalHendelseProperties properties) {
        if ((properties.getSchemaRegistryUrl() != null) && !properties.getSchemaRegistryUrl().isEmpty()) {
            var schemaMap =
                Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, properties.getSchemaRegistryUrl(),
                    AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO",
                    AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG, properties.getBasicAuth(),
                    SPECIFIC_AVRO_READER_CONFIG, true);
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
        return HENDELSE_MIDL.equalsIgnoreCase(hendelse) || HENDELSE_ENDRET.equalsIgnoreCase(hendelse) || HENDELSE_MIDL_LEGACY.equalsIgnoreCase(hendelse);
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
        if (!isDeployment) return;
        addShutdownHooks();

        stream.start();
        LOG.info("Starter konsumering av topic={}, tilstand={}", getTopicName(), stream.state());
    }

    public String getTopicName() {
        return topic.topic();
    }

    @Override
    public boolean isAlive() {
        if (!isDeployment) return true;
        return (stream != null) && stream.state().isRunningOrRebalancing();
    }

    @Override
    public boolean isReady() {
        if (!isDeployment) return true;
        return isAlive();
    }

    @Override
    public void stop() {
        if (!isDeployment) return;
        LOG.info("Starter shutdown av topic={}, tilstand={} med 15 sekunder timeout", getTopicName(), stream.state());
        stream.close(Duration.ofSeconds(15));
        LOG.info("Shutdown av topic={}, tilstand={} med 15 sekunder timeout", getTopicName(), stream.state());
    }
}
