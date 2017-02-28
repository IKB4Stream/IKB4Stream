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

package com.waves_rsp.ikb4stream.core.communication;

import com.waves_rsp.ikb4stream.core.communication.model.Request;

/**
 * Interface of {@link com.waves_rsp.ikb4stream.consumer.database.DatabaseReader DatabaseReader} given to {@link ICommunication}
 * @author ikb4stream
 * @version 1.0
 */
@FunctionalInterface
public interface IDatabaseReader {
    /**
     * Get Event based on {@link Request}
     * @param request {@link Request} Request to execute on database
     * @param callback {@link DatabaseReaderCallback} Callback use after response of request
     */
    void getEvent(Request request, DatabaseReaderCallback callback);
}
