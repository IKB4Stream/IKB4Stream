package com.waves_rsp.ikb4stream.scoring.openagenda;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.RulesReader;
import com.waves_rsp.ikb4stream.core.util.nlp.OpenNLP;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OpenAgendaScoreProcessor implements IScoreProcessor {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OpenAgendaScoreProcessor.class, "resources/scoreprocessor/openagenda/config.properties");
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OpenAgendaScoreProcessor.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private static final byte MAX_SCORE = 100;
    private final OpenNLP openNLP = OpenNLP.getOpenNLP(Thread.currentThread());
    private final Map<String, Integer> rulesMap;

    public OpenAgendaScoreProcessor() {
        try {
            String filename = PROPERTIES_MANAGER.getProperty("openagenda.rules.file");
            rulesMap = RulesReader.parseJSONRules(filename);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid property {} ", e);
            throw new IllegalStateException("Invalid property\n" + e.getMessage());
        }
    }

    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        long start = System.currentTimeMillis();

        ObjectMapper mapper = new ObjectMapper();
        String eventDesc = "";
        byte score = 0;
        try {
            JsonNode jsonDescription = mapper.readTree(event.getDescription());
            String title = jsonDescription.get("title").asText();
            String description = jsonDescription.get("description").asText();
            eventDesc = title + " " + description;
            List<String> fbList = openNLP.applyNLPlemma(eventDesc);
            for (String word : fbList) {
                if (rulesMap.containsKey(word)) {
                    score += rulesMap.get(word);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Bad json format or tree cannot be read {} ", e);
            throw new IllegalArgumentException("Bad json format or tree cannot be read");
        }

        long end = System.currentTimeMillis();
        METRICS_LOGGER.log("time_scoring_" + event.getSource(), String.valueOf(end - start));
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), eventDesc, verifyMaxScore(score), event.getSource());
    }

    @Override
    public List<String> getSources() {
        return null;
    }

    /**
     * Check that the score can't overtake @MAX_SCORE
     *
     * @param score calculated by OpenNLP processing
     * @return score (max = @MAX_SCORE)
     */
    private byte verifyMaxScore(byte score) {
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }
        return score;
    }
}
