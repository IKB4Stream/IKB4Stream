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

/**
 * Interface to implement if you want to use your class as module of producer
 *
 * @author ikb4stream
 * @version 1.0
 */
public interface IProducerConnector {
    /**
     * This method registers a DataProducer which allows to push in DataQueue
     *
     * @param dataProducer {@link IDataProducer} contains the data queue
     */
    void load(IDataProducer dataProducer);

    /**
     * This method indicates whether the ProducerConnector is enable
     *
     * @return true if we should launch this module
     */
    boolean isActive();
}
