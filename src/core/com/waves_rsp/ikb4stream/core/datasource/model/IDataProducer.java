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

/**
 * This class establishes the connexion between an external source and the DataQueue
 *
 * @author ikb4stream
 * @version 1.0
 */
@FunctionalInterface
public interface IDataProducer {
    /**
     * Push an {@link Event} to be analyse by a {@link IScoreProcessor}
     *
     * @param event {@link Event} to push in process
     */
    void push(Event event);
}
