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

/**
 * Interface to implement if you want to use your class as module of communication
 * @author ikb4stream
 * @version 1.0
 */
public interface ICommunication {
    /**
     * Called at startup
     * @param databaseReader Implementation of {@link com.waves_rsp.ikb4stream.consumer.database.DatabaseReader DatabaseReader}
     * @see IDatabaseReader
     */
    void start(IDatabaseReader databaseReader);

    /**
     * Called at closure
     */
    void close();

    /**
     * Called to know if module should be launch
     * @return true if Communication module must be started
     */
    boolean isActive();
}
