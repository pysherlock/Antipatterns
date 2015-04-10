/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.servlet.collector;

import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.pipeline.CachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.component.PipelineComponent;
import org.apache.cocoon.sitemap.Invocation;
import org.apache.cocoon.sitemap.node.InvocationResult;
import org.apache.cocoon.sitemap.node.SerializeNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ResponseHeaderCollector {

    private static CollectorDataStore collectorDataStore = new ThreadLocalCollectorDataStore();
    private static final String KEY_ETAG = ResponseHeaderCollector.class.getName() + "/etag";
    private static final String KEY_IF_MODIFIED_SINCE = ResponseHeaderCollector.class.getName() + "/if-modified-since";
    private static final String KEY_IF_NONE_MATCH = ResponseHeaderCollector.class.getName() + "/if-none-match";
    private static final String KEY_LAST_MODIFIED = ResponseHeaderCollector.class.getName() + "/last-modified";
    private static final String KEY_MIME_TYPE = ResponseHeaderCollector.class.getName() + "/mime-type";
    private static final String KEY_PIPELINE_EXECUTED = ResponseHeaderCollector.class.getName() + "/pipeline-executed";
    private static final String KEY_REQUEST_METHOD = ResponseHeaderCollector.class.getName() + "/method";
    private static final String KEY_STATUS_CODE = ResponseHeaderCollector.class.getName() + "/status-code";

    @SuppressWarnings("unchecked")
    @Around("execution(* org.apache.cocoon.pipeline.Pipeline.execute(..))")
    public Object interceptInvoke(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // prepare data
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        setModifiedResponse(false);
        Pipeline<PipelineComponent> pipeline = (Pipeline<PipelineComponent>) proceedingJoinPoint.getTarget();

        // collect last-modified data
        final long ifModifiedSince = getIfLastModifiedSince();
        final long lastModified = pipeline.getLastModified();

        // collect ETag
        final String noneMatch = getIfNoneMatch();
        if (pipeline instanceof CachingPipeline) {
            CachingPipeline<PipelineComponent> cachingPipeline = (CachingPipeline<PipelineComponent>) pipeline;
            CacheKey cacheKey = cachingPipeline.getCacheKey();
            if (cacheKey != null) {
                setETag(Integer.toHexString(cacheKey.hashCode()));
            }
        }

        // set last-modified
        if (lastModified > -1 || getLastModified() <= 0) {
            setLastModified(lastModified);
        }

        // pipeline execution
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Object result = null;
        boolean repeatPipeline = true;

        // check the eTag (if ETag and If-None-Match parameters match, there is no need to
        // re-execute the pipeline
        String eTag = getETag();
        if (eTag != null && noneMatch != null && noneMatch.equals(eTag)) {
            repeatPipeline = false;
        }

        // only check for last modification if eTag doesn't match
        if (repeatPipeline && ifModifiedSince > 0 && lastModified > 0) {
            if (ifModifiedSince / 1000 >= lastModified / 1000) {
                repeatPipeline = false;
            }
        }

        // repeat the pipeline
        if (repeatPipeline) {
            setModifiedResponse(true);
            result = proceedingJoinPoint.proceed();
        }

        // collect the mime-type
        String newValue = pipeline.getContentType();
        if (newValue != null) {
            setMimeType(newValue);
        }

        return result;
    }

    @Around("execution(* org.apache.cocoon.sitemap.node.SerializeNode.invoke(..)) && args(invocation)")
    public Object interceptInvoke(ProceedingJoinPoint proceedingJoinPoint, Invocation invocation) throws Throwable {
        SerializeNode target = (SerializeNode) proceedingJoinPoint.getTarget();
        String statusCode = invocation.resolveParameter(target.getParameters().get("status-code"));

        InvocationResult invocationResult = (InvocationResult) proceedingJoinPoint.proceed();
        if (invocationResult.isContinued() && statusCode != null) {
            try {
                setStatusCode(Integer.valueOf(statusCode));
            } catch (NumberFormatException nfe) {
                throw new InvalidStatusCodeException("The status-code '" + statusCode + " is not valid number.", nfe);
            }
        }

        return invocationResult;
    }

    public void setCollectorDataStore(CollectorDataStore collectorDataStore) {
        ResponseHeaderCollector.collectorDataStore = collectorDataStore;
    }

    public static String getETag() {
        return (String) collectorDataStore.get(KEY_ETAG);
    }

    public static long getIfLastModifiedSince() {
        Number attribute = (Number) collectorDataStore.get(KEY_IF_MODIFIED_SINCE);

        if (attribute == null) {
            return -1;
        }

        return attribute.longValue();
    }

    public static String getIfNoneMatch() {
        return (String) collectorDataStore.get(KEY_IF_NONE_MATCH);
    }

    public static long getLastModified() {
        Object lastModified = collectorDataStore.get(KEY_LAST_MODIFIED);

        if (lastModified == null) {
            return -1;
        }

        return (Long) lastModified;
    }

    public static String getMimeType() {
        return (String) collectorDataStore.get(KEY_MIME_TYPE);
    }

    public static String getRequestMethod() {
        return (String) collectorDataStore.get(KEY_REQUEST_METHOD);
    }

    public static int getStatusCode() {
        Integer statusCode = (Integer) collectorDataStore.get(KEY_STATUS_CODE);

        if (statusCode == null) {
            return HttpServletResponse.SC_OK;
        }

        return statusCode;
    }

    public static boolean isModifiedResponse() {
        return (Boolean) collectorDataStore.get(KEY_PIPELINE_EXECUTED);
    }

    public static void setETag(String etag) {
        collectorDataStore.set(KEY_ETAG, etag);
    }

    public static void setIfLastModifiedSince(long ifLastModifiedSince) {
        collectorDataStore.set(KEY_IF_MODIFIED_SINCE, ifLastModifiedSince);
    }

    public static void setIfNoneMatch(String ifNoneMatch) {
        collectorDataStore.set(KEY_IF_NONE_MATCH, ifNoneMatch);
    }

    public static void setLastModified(Long lastModified) {
        collectorDataStore.set(KEY_LAST_MODIFIED, lastModified);
    }

    public static void setMimeType(String mimeType) {
        collectorDataStore.set(KEY_MIME_TYPE, mimeType);
    }

    public static void setModifiedResponse(boolean executed) {
        collectorDataStore.set(KEY_PIPELINE_EXECUTED, executed);
    }

    public static void setRequestMethod(String method) {
        collectorDataStore.set(KEY_REQUEST_METHOD, method);
    }

    public static void setStatusCode(int statusCode) {
        collectorDataStore.set(KEY_STATUS_CODE, statusCode);
    }
}
