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
package com.basho.riak.client.http.response;

import java.io.InputStream;
import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * HTTP response information resulting from some HTTP operation
 */
public interface HttpResponse {

    /**
     * The target object's bucket
     */
    String getBucket();

    /**
     * The target object's key or null if bucket is target
     */
    String getKey();

    /**
     * Resulting status code from the HTTP request.
     */
    int getStatusCode();

    /**
     * The HTTP response headers.
     */
    Map<String, String> getHttpHeaders();

    /**
     * The HTTP response body or null if isStreamed()
     */
    byte[] getBody();
    
    String getBodyAsString();

    /**
     * The HTTP response body as an input stream if isStreamed(); null otherwise
     */
    InputStream getStream();

    /**
     * Whether the response body is available as an input stream
     */
    boolean isStreamed();

    /**
     * The actual {@link HttpRequestBase} used to make the HTTP request. Most of
     * the data here can be retrieved more simply using methods in this class.
     * The method will already be in a finished state.
     */
    HttpRequestBase getHttpMethod();

    /**
     * Whether the HTTP response is considered a success. Generally this
     * translates to a 2xx for any request, a 304 for GET and HEAD requests, or
     * 404 for DELETE requests.
     */
    boolean isSuccess();

    /**
     * Whether the HTTP request returned a 4xx or 5xx response
     */
    boolean isError();

    /**
     * Releases the underlying the HTTP connection when the response is streamed
     */
    void close();

    /**
     * @return the original {@link org.apache.http.HttpResponse} returned by http client
     */
    org.apache.http.HttpResponse getHttpResponse();
}
