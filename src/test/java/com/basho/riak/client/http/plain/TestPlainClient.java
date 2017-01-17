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
package com.basho.riak.client.http.plain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.basho.riak.client.http.RiakBucketInfo;
import com.basho.riak.client.http.RiakClient;
import com.basho.riak.client.http.RiakObject;
import com.basho.riak.client.http.plain.ConvertToCheckedExceptions;
import com.basho.riak.client.http.plain.PlainClient;
import com.basho.riak.client.http.plain.RiakIOException;
import com.basho.riak.client.http.plain.RiakResponseException;
import com.basho.riak.client.http.request.RequestMeta;
import com.basho.riak.client.http.response.BucketResponse;
import com.basho.riak.client.http.response.FetchResponse;
import com.basho.riak.client.http.response.HttpResponse;
import com.basho.riak.client.http.response.StoreResponse;
import com.basho.riak.client.http.response.StreamHandler;
import com.basho.riak.client.http.response.WalkResponse;

public class TestPlainClient {
    
    // The basic status codes in HTTP 1.1
    private static final int HTTP_STATUSES[] = {100, 101, 199, 200, 201, 202, 203, 204, 205, 206, 299, 300, 301, 302, 303, 304, 305, 306, 307, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 499, 500, 501, 502, 503, 504, 505, 599};
    
    final String bucket = "bucket";
    final String key = "key";
    final String walkSpec = "walkSpec";

    @Mock RiakBucketInfo bucketInfo;
    @Mock RiakClient mockRiakClient;
    @Mock RiakObject object;
    @Mock RequestMeta meta;
    @Mock StreamHandler handler;
    PlainClient impl;
    
    @Before public void setup() {
        MockitoAnnotations.initMocks(this);
        impl = new PlainClient(mockRiakClient);
    }
    
    @Test public void installs_convert_to_checked_exception_handler() {
        verify(mockRiakClient).setExceptionHandler(isA(ConvertToCheckedExceptions.class));
    }

    @Test public void methods_defer_to_impl() throws RiakIOException, RiakResponseException, IOException {
        final HttpResponse mockHttpResponse = mock(HttpResponse.class);
        when(mockRiakClient.setBucketSchema(bucket, bucketInfo, meta)).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusCode()).thenReturn(204);
        impl.setBucketSchema(bucket, bucketInfo, meta);
        verify(mockRiakClient).setBucketSchema(bucket, bucketInfo, meta);

        final BucketResponse mockBucketResponse = mock(BucketResponse.class);
        when(mockRiakClient.listBucket(bucket, meta)).thenReturn(mockBucketResponse);
        when(mockBucketResponse.getStatusCode()).thenReturn(200);
        impl.listBucket(bucket, meta);
        verify(mockRiakClient).listBucket(bucket, meta);

        final StoreResponse mockStoreResponse = mock(StoreResponse.class);
        when(mockRiakClient.store(object, meta)).thenReturn(mockStoreResponse);
        when(mockStoreResponse.getStatusCode()).thenReturn(200);
        impl.store(object, meta);
        verify(mockRiakClient).store(object, meta);

        final FetchResponse mockFetchResponse = mock(FetchResponse.class);
        when(mockRiakClient.fetchMeta(bucket, key, meta)).thenReturn(mockFetchResponse);
        when(mockFetchResponse.getStatusCode()).thenReturn(200);
        when(mockFetchResponse.hasObject()).thenReturn(true);
        impl.fetchMeta(bucket, key, meta);
        verify(mockRiakClient).fetchMeta(bucket, key, meta);

        when(mockRiakClient.fetch(bucket, key, meta)).thenReturn(mockFetchResponse);
        when(mockFetchResponse.getStatusCode()).thenReturn(200);
        when(mockFetchResponse.hasObject()).thenReturn(true);
        impl.fetch(bucket, key, meta);
        verify(mockRiakClient).fetch(bucket, key, meta);

        reset(mockRiakClient);
        when(mockRiakClient.fetch(bucket, key, meta)).thenReturn(mockFetchResponse);
        when(mockFetchResponse.getStatusCode()).thenReturn(200);
        when(mockFetchResponse.hasObject()).thenReturn(true);
        impl.fetchAll(bucket, key, meta);
        verify(mockRiakClient).fetch(bucket, key, meta);

        impl.stream(bucket, key, handler, meta);
        verify(mockRiakClient).stream(bucket, key, handler, meta);

        when(mockRiakClient.delete(bucket, key, meta)).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusCode()).thenReturn(204);
        impl.delete(bucket, key, meta);
        verify(mockRiakClient).delete(bucket, key, meta);

        final WalkResponse mockWalkResponse = mock(WalkResponse.class);
        when(mockRiakClient.walk(bucket, key, walkSpec, meta)).thenReturn(mockWalkResponse);
        when(mockWalkResponse.getStatusCode()).thenReturn(200);
        when(mockWalkResponse.hasSteps()).thenReturn(true);
        impl.walk(bucket, key, walkSpec, meta);
        verify(mockRiakClient).walk(bucket, key, walkSpec, meta);
    }
    
    @Test public void setBucketSchema_throws_except_for_204(){
        HttpResponse mockResponse = mock(HttpResponse.class);

        for (int i : HTTP_STATUSES) {
            when(mockResponse.getStatusCode()).thenReturn(i);
            when(mockRiakClient.setBucketSchema(bucket, bucketInfo, meta)).thenReturn(mockResponse);
            boolean threw = true;
            try {
                impl.setBucketSchema(bucket, bucketInfo, meta);
                threw = false;
            } catch (RiakIOException | RiakResponseException e) {
            }
          assertTrue("Wrong behavior for status " + i, throwsForAllStatusesExcept(new int[] { 204 }, i, threw));
        }
    }

    @Test public void listBucket_throws_except_for_200() {
        BucketResponse mockResponse = mock(BucketResponse.class);
        for (int i : HTTP_STATUSES) {
            when(mockResponse.getStatusCode()).thenReturn(i);
            when(mockRiakClient.listBucket(bucket, meta)).thenReturn(mockResponse);
            boolean threw = true;
            try {
                impl.listBucket(bucket, meta);
                threw = false;
            } catch (RiakIOException | RiakResponseException e) {
            }
          assertTrue("Wrong behavior for status " + i, throwsForAllStatusesExcept(new int[] { 200 }, i, threw));
        }
    }

    @Test public void store_throws_except_for_200_and_204() {
        StoreResponse mockResponse = mock(StoreResponse.class);
        for (int i : HTTP_STATUSES) {
            when(mockResponse.getStatusCode()).thenReturn(i);
            when(mockRiakClient.store(object, meta)).thenReturn(mockResponse);
            boolean threw = true;
            try {
                impl.store(object, meta);
                threw = false;
            } catch (RiakIOException | RiakResponseException e) {
            }
          assertTrue("Wrong behavior for status " + i, throwsForAllStatusesExcept(new int[] { 200, 204 }, i, threw));
        }
    }

    @Test public void fetchMeta_throws_except_for_200_304_or_404() {
        FetchResponse mockResponse = mock(FetchResponse.class);
        for (int i : HTTP_STATUSES) {
            when(mockResponse.getStatusCode()).thenReturn(i);
            when(mockResponse.hasObject()).thenReturn(true);
            when(mockRiakClient.fetchMeta(bucket, key, meta)).thenReturn(mockResponse);
            boolean threw = true;
            try {
                impl.fetchMeta(bucket, key, meta);
                threw = false;
            } catch (RiakIOException | RiakResponseException e) {
            }
          assertTrue("Wrong behavior for status " + i, throwsForAllStatusesExcept(new int[] { 200, 304, 404 }, i, threw));
        }
    }

    @Test public void fetchMeta_returns_null_on_404() throws RiakIOException, RiakResponseException {
        final FetchResponse mockResponse = mock(FetchResponse.class);
        
        when(mockResponse.getStatusCode()).thenReturn(404);
        when(mockRiakClient.fetchMeta(bucket, key, meta)).thenReturn(mockResponse);

        assertNull(impl.fetchMeta(bucket, key, meta));
    }
    
    @Test(expected=RiakResponseException.class) public void fetchMeta_throws_if_metadata_not_returned() throws RiakIOException, RiakResponseException {
        final FetchResponse mockResponse = mock(FetchResponse.class);
        
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.hasObject()).thenReturn(false);
        when(mockRiakClient.fetchMeta(bucket, key, meta)).thenReturn(mockResponse);

        impl.fetchMeta(bucket, key, meta);
    }

    @Test public void fetch_throws_except_for_200_304_or_404() {
        FetchResponse mockResponse = mock(FetchResponse.class);
        for (int i : HTTP_STATUSES) {
            when(mockResponse.getStatusCode()).thenReturn(i);
            when(mockResponse.hasObject()).thenReturn(true);
            when(mockRiakClient.fetch(bucket, key, meta)).thenReturn(mockResponse);
            boolean threw = true;
            try {
                impl.fetch(bucket, key, meta);
                threw = false;
            } catch (RiakIOException | RiakResponseException e) {
            }
          assertTrue("Wrong behavior for status " + i, throwsForAllStatusesExcept(new int[] { 200, 304, 404 }, i, threw));
        }
    }
    
    @Test public void fetch_returns_null_on_404() throws RiakIOException, RiakResponseException {
        final FetchResponse mockResponse = mock(FetchResponse.class);
        
        when(mockResponse.getStatusCode()).thenReturn(404);
        when(mockRiakClient.fetch(bucket, key, meta)).thenReturn(mockResponse);

        assertNull(impl.fetch(bucket, key, meta));
    }

    @Test(expected=RiakResponseException.class) public void fetch_throws_if_object_not_returned() throws RiakIOException, RiakResponseException {
        final FetchResponse mockResponse = mock(FetchResponse.class);
        
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.hasObject()).thenReturn(false);
        when(mockRiakClient.fetch(bucket, key, meta)).thenReturn(mockResponse);

        impl.fetch(bucket, key, meta);
    }

    @Test public void fetchAll_throws_except_for_200_304_or_404() {
        FetchResponse mockResponse = mock(FetchResponse.class);
        for (int i : HTTP_STATUSES) {
            when(mockResponse.getStatusCode()).thenReturn(i);
            when(mockResponse.hasObject()).thenReturn(true);
            when(mockRiakClient.fetch(bucket, key, meta)).thenReturn(mockResponse);
            boolean threw = true;
            try {
                impl.fetchAll(bucket, key, meta);
                threw = false;
            } catch (RiakIOException | RiakResponseException e) {
            }
          assertTrue("Wrong behavior for status " + i, throwsForAllStatusesExcept(new int[] { 200, 304, 404 }, i, threw));
        }
    }
    
    @Test public void fetchAll_returns_null_on_404() throws RiakIOException, RiakResponseException {
        final FetchResponse mockResponse = mock(FetchResponse.class);
        
        when(mockResponse.getStatusCode()).thenReturn(404);
        when(mockRiakClient.fetch(bucket, key, meta)).thenReturn(mockResponse);

        assertNull(impl.fetchAll(bucket, key, meta));
    }

    @Test public void fetchAll_returns_siblings_if_exists() throws RiakIOException, RiakResponseException {
        final FetchResponse mockResponse = mock(FetchResponse.class);
        final List<RiakObject> siblings = new ArrayList<>();

        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.hasSiblings()).thenReturn(true);
        when(mockResponse.getSiblings()).thenAnswer(invocation -> siblings);
        when(mockRiakClient.fetch(bucket, key, meta)).thenReturn(mockResponse);

        assertSame(siblings, impl.fetchAll(bucket, key, meta));
    }

    @Test public void fetchAll_returns_object_if_no_siblings() throws RiakIOException, RiakResponseException {
        final FetchResponse mockResponse = mock(FetchResponse.class);
        
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.hasObject()).thenReturn(true);
        when(mockResponse.hasSiblings()).thenReturn(false);
        when(mockResponse.getObject()).thenReturn(object);
        when(mockRiakClient.fetch(bucket, key, meta)).thenReturn(mockResponse);

        Collection<? extends RiakObject> siblings = impl.fetchAll(bucket, key, meta);
        
        assertEquals(1, siblings.size());
        assertSame(object, siblings.iterator().next());
    }

    @Test(expected=RiakResponseException.class) public void fetchAll_throws_if_object_not_returned() throws RiakIOException, RiakResponseException {
        final FetchResponse mockResponse = mock(FetchResponse.class);
        
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.hasObject()).thenReturn(false);
        when(mockRiakClient.fetch(bucket, key, meta)).thenReturn(mockResponse);

        impl.fetchAll(bucket, key, meta);
    }

    @Test public void delete_throws_except_for_204_and_404() {
        HttpResponse mockResponse = mock(HttpResponse.class);
        for (int i : HTTP_STATUSES) {
            when(mockResponse.getStatusCode()).thenReturn(i);
            when(mockRiakClient.delete(bucket, key, meta)).thenReturn(mockResponse);
            boolean threw = true;
            try {
                impl.delete(bucket, key, meta);
                threw = false;
            } catch (RiakIOException | RiakResponseException e) {
            }
          assertTrue("Wrong behavior for status " + i, throwsForAllStatusesExcept(new int[] { 204, 404 }, i, threw));
        }
    }

    @Test public void walk_throws_except_for_200_and_404() {
        WalkResponse mockResponse = mock(WalkResponse.class);
        for (int i : HTTP_STATUSES) {
            when(mockResponse.getStatusCode()).thenReturn(i);
            when(mockResponse.hasSteps()).thenReturn(true);
            when(mockRiakClient.walk(bucket, key, walkSpec, meta)).thenReturn(mockResponse);
            boolean threw = true;
            try {
                impl.walk(bucket, key, walkSpec, meta);
                threw = false;
            } catch (RiakIOException | RiakResponseException e) {
            }
          assertTrue("Wrong behavior for status " + i, throwsForAllStatusesExcept(new int[] { 200, 404 }, i, threw));
        }
    }

    @Test public void walk_returns_null_on_404() throws RiakIOException, RiakResponseException {
        final WalkResponse mockResponse = mock(WalkResponse.class);
        
        when(mockResponse.getStatusCode()).thenReturn(404);
        when(mockRiakClient.walk(bucket, key, walkSpec, meta)).thenReturn(mockResponse);

        assertNull(impl.walk(bucket, key, walkSpec, meta));
    }

    @Test(expected=RiakResponseException.class) public void walk_throws_if_steps_not_returned() throws RiakIOException, RiakResponseException {
        final WalkResponse mockResponse = mock(WalkResponse.class);
        
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.hasSteps()).thenReturn(false);
        when(mockRiakClient.walk(bucket, key, walkSpec, meta)).thenReturn(mockResponse);
        
        impl.walk(bucket, key, walkSpec, meta);
    }

    private boolean throwsForAllStatusesExcept(int[] okStatus, int status, boolean threw) {
        boolean ok = false;
        for (int s : okStatus) {
            if (status == s)
                ok = true;
        }
        return (ok && !threw) || (!ok && threw);
    }
}