package com.waves_rsp.ikb4stream.communication.kafka;

import com.waves_rsp.ikb4stream.core.communication.ICommunication;
import com.waves_rsp.ikb4stream.core.communication.IDatabaseReader;
import com.waves_rsp.ikb4stream.core.communication.model.BoundingBox;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.communication.model.Request;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class KafkaCommunication implements ICommunication {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(KafkaCommunication.class, "resources/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaCommunication.class);
    private final String kafkaTopic;
    private KafkaStreams streams;

    public KafkaCommunication() {
        this.kafkaTopic = PROPERTIES_MANAGER.getProperty("communications.kafka.topic");
    }

    private void getRequests(IPollCallback callback) {
        Map<String, CharSequence> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, PROPERTIES_MANAGER.getProperty("communications.kafka.application_id"));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, PROPERTIES_MANAGER.getProperty("communications.kafka.server"));
        StreamsConfig config = new StreamsConfig(props);

        KStreamBuilder builder = new KStreamBuilder();
        builder.stream(kafkaTopic)
                .map((key, value) -> {
                    String val = new String((byte[]) value);
                    LOGGER.trace("Request received");
                    AnomalyRequest anomalyRequest = RDFParser.parse(val);
                    return new KeyValue<>(key, new Request(
                        anomalyRequest.getStart(),
                        anomalyRequest.getEnd(),
                        new BoundingBox(new LatLong[]{
                            new LatLong(anomalyRequest.getMinLatitude(), anomalyRequest.getMinLongitude()),
                            new LatLong(anomalyRequest.getMaxLatitude(), anomalyRequest.getMinLongitude()),
                            new LatLong(anomalyRequest.getMaxLatitude(), anomalyRequest.getMaxLongitude()),
                            new LatLong(anomalyRequest.getMinLatitude(), anomalyRequest.getMaxLongitude()),
                            new LatLong(anomalyRequest.getMinLatitude(), anomalyRequest.getMinLongitude()),
                        }),
                        Date.from(Instant.now()))
                    );
                })
                .filter((key, value) -> value != null) // Filter all non valid RDF.
                .map((key, value) -> new KeyValue<>(key, callback.onNewRequest(value)));

        this.streams = new KafkaStreams(builder, config);
        this.streams.start();
    }

    @Override
    public void start(IDatabaseReader databaseReader) {
        this.getRequests(request -> {
            LOGGER.info("Request = " + request);
            final String[] r = {"[]"};
            databaseReader.getEvent(request, (t, result) -> {
                if(t != null) { LOGGER.error("DatabaseReader error: " + t.getMessage()); return; }
                r[0] = result;
            });
            LOGGER.info("Result = " + r[0]);
            return r[0];
        });
    }

    @Override
    public void close() {
        this.streams.close();
    }
}
