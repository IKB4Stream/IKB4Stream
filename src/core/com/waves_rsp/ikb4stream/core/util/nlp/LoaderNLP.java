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

package com.waves_rsp.ikb4stream.core.util.nlp;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class LoaderNLP {
    private static final String PATH_BINARIES = "resources/opennlp-models/binaries/";
    private static final Logger LOGGER = LoggerFactory.getLogger(LoaderNLP.class);
    private static final TokenNameFinderModel TOKEN_NAME_FINDER_MODEL_PERS;
    private static final TokenNameFinderModel TOKEN_NAME_FINDER_MODEL_ORG;
    private static final TokenNameFinderModel TOKEN_NAME_FINDER_MODEL_LOC;
    private static final TokenizerModel TOKENIZER_MODEL;
    private static final SentenceModel SENTENCE_MODEL;
    private static final POSModel POS_MODEL;

    private LoaderNLP() {

    }

    static {
        try {
            InputStream fileFrSentBin = new FileInputStream(PATH_BINARIES + "fr-sent.bin");
            SENTENCE_MODEL = new SentenceModel(fileFrSentBin);
            fileFrSentBin.close();
            InputStream fileFrTokenBin = new FileInputStream(PATH_BINARIES + "fr-token.bin");
            TOKENIZER_MODEL = new TokenizerModel(fileFrTokenBin);
            fileFrTokenBin.close();
            InputStream fileFrPosMaxent2Bin = new FileInputStream(PATH_BINARIES + "fr-pos-maxent-2.bin");
            POS_MODEL = new POSModel(fileFrPosMaxent2Bin);
            fileFrPosMaxent2Bin.close();
            InputStream frNerOrganizationBin = new FileInputStream(PATH_BINARIES + "fr-ner-organization.bin");
            TOKEN_NAME_FINDER_MODEL_ORG = new TokenNameFinderModel(frNerOrganizationBin);
            frNerOrganizationBin.close();
            InputStream fileFrNerLocationBin = new FileInputStream(PATH_BINARIES + "fr-ner-location.bin");
            TOKEN_NAME_FINDER_MODEL_LOC = new TokenNameFinderModel(fileFrNerLocationBin);
            fileFrNerLocationBin.close();
            InputStream fileNerPersonBin = new FileInputStream(PATH_BINARIES + "fr-ner-person.bin");
            TOKEN_NAME_FINDER_MODEL_PERS = new TokenNameFinderModel(fileNerPersonBin);
            fileNerPersonBin.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    static TokenNameFinderModel getTokenNameFinderModelPers() {
        return TOKEN_NAME_FINDER_MODEL_PERS;
    }

    static TokenNameFinderModel getTokenNameFinderModelOrg() {
        return TOKEN_NAME_FINDER_MODEL_ORG;
    }

    static TokenNameFinderModel getTokenNameFinderModelLoc() {
        return TOKEN_NAME_FINDER_MODEL_LOC;
    }

    static TokenizerModel getTokenizerModel() {
        return TOKENIZER_MODEL;
    }

    static SentenceModel getSentenceModel() {
        return SENTENCE_MODEL;
    }

    static POSModel getPosModel() {
        return POS_MODEL;
    }
}
