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
package com.basho.riak.client.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author russell
 * 
 */
public class UnmodifiableIteratorTest {

    @Mock private Iterator<String> mockerator;
    private UnmodifiableIterator<String> iterator;

    /**
     * @throws java.lang.Exception
     */
    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        iterator = new UnmodifiableIterator<>(mockerator);
    }

    /**
     * Test method for
     * {@link com.basho.riak.client.util.UnmodifiableIterator#hasNext()}.
     */
    @Test public void testHasNext() {
        when(mockerator.hasNext()).thenReturn(true);
        assertTrue(iterator.hasNext());
        verify(mockerator, times(1)).hasNext();
    }

    /**
     * Test method for
     * {@link com.basho.riak.client.util.UnmodifiableIterator#next()}.
     */
    @Test public void testNext() {
        when(mockerator.next()).thenReturn("eggs");
        assertEquals("eggs", iterator.next());
        verify(mockerator, times(1)).next();
    }

    /**
     * Test method for
     * {@link com.basho.riak.client.util.UnmodifiableIterator#remove()}.
     */
    @Test(expected = UnsupportedOperationException.class) public void testRemove() {
        iterator.remove();
    }
}
