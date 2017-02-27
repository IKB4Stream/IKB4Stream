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


public class RulesReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesReader.class);
    private static final Map<String, Integer> map = new HashMap<>();

    private RulesReader() {

    }

    /**
     * Parse file rules.json
     * @param filename
     * @return Map<String, Integer> contains Json elements within rules.json
     */
    public static Map<String, Integer> parseJSONRules(String filename) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filename);


        JsonNode root = null;
        try {
            root = objectMapper.readTree(file);
            //root keyword
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

