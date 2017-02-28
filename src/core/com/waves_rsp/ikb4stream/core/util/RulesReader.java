/*
 * Copyright (C) 2017 ikb4stream team
 * ikb4stream is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ikb4stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package com.waves_rsp.ikb4stream.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides only static method {@link RulesReader#parseJSONRules(String)}
 * @author ikb4stream
 * @version 1.0
 */
public class RulesReader {
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesReader.class);

    /**
     * Private constructor to block instantiation
     */
    private RulesReader() {

    }

    /**
     * Parse file
     * @param filename as JSON to parse
     * @return Map<String, Integer> contains Json elements within rules.json
     * @throws NullPointerException if filename is null
     */
    public static Map<String, Integer> parseJSONRules(String filename) {
        Objects.requireNonNull(filename);
        Map<String, Integer> map = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filename);
        JsonNode root;
        try {
            root = objectMapper.readTree(file);
            JsonNode keywordNode = root.path("keyword");
            if (keywordNode.isArray()) {
                for (JsonNode knode : keywordNode) {
                    JsonNode wordNode = knode.path("word");
                    int scoreNode = knode.path("score").asInt();
                    map.put(wordNode.asText(), scoreNode);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Rules file does not exist\n" + e.getMessage());
        }
        return map;
    }
}

