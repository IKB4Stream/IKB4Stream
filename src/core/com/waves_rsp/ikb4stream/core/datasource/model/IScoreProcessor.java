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

package com.waves_rsp.ikb4stream.core.datasource.model;

import com.waves_rsp.ikb4stream.core.model.Event;

import java.util.List;

/**
 * Interface to implement if you want to use your class as module of ScoreProcessor
 *
 * @author ikb4stream
 * @version 1.0
 */
public interface IScoreProcessor {
    /**
     * This method create a score to the event in param
     *
     * @param event {@link Event} without score
     * @return {@link Event} with score gave by processScore
     */
    Event processScore(Event event);

    /**
     * List all sources that ScoreProcessor can be use
     *
     * @return List of sources
     */
    List<String> getSources();
}
