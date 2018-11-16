/*
 * This file is provided to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.basho.riak.client.convert;

import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.cap.VClock;
import com.basho.riak.client.operations.RiakOperation;

/**
 * Implement this and pass to a {@link RiakOperation} for serializing/deserializing your domain objects to IRiakObject
 * @author russell
 * 
 */
public interface Converter<T> {
    /**
     * Convert from domain specific type to RiakObject
     *
     * @param bucket bucket
     * @param domainObject domain object
     * @return a RiakObject populated from domainObject
     */
    IRiakObject fromDomain(String bucket, T domainObject, VClock vclock) throws ConversionException;

    /**
     * Convert from a riakObject to a domain specific instance
     * 
     * @param riakObject
     *            the RiakObject to convert
     * @return an instance of type T
     */
    T toDomain(IRiakObject riakObject) throws ConversionException;
}
