/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.monitoring.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.cocoon.pipeline.caching.Cache;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.CacheValue;
import org.apache.cocoon.pipeline.caching.ExpiresCacheKey;
import org.apache.commons.lang.StringUtils;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This class can perform burst operation (like cleaning specified cache elements) on all cache data.
 *
 */
@ManagedResource(objectName = "org.apache.cocoon:group=Cache,name=CacheBurstActions", description = "This module can perform burst operation (like cleaning specified cache elements) on all cache data.")
public class CacheBurstActions {

    private final List<Cache> caches;

    public CacheBurstActions(Map<String, Cache> caches) {
        this.caches = new ArrayList<Cache>(caches.values());
    }

    /**
     * Clears all cache entry's that size is greater that minSize parameter.<br>
     * <br>
     * This operation will stops clearing elements after first failure, this means that
     * it will clear all cache entry's until one of that entry's fails to clear.
     *
     * @param minSize Minimal size (in bytes) of cache entry that should be cleaned.
     * @return true if operation ends with success, false otherwise
     */
    @ManagedOperation(description = "Clears all cache entry's that size is greater that minSize parameter.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "minSize", description = "Minimal size (in bytes) of cache entry that should be cleaned.") })
    public final boolean clearAllGreaterThen(long minSize) {
        return this.clear(minSize, -1, null, null, null, null);
    }

    /**
     * Clears all cache entries whose size is smaller than maxSize parameter.<br>
     * <br>
     * This operation will stops clearing elements after first failure, this means that it will
     * clear all cache entry's until one of that entry's fails to clear.
     *
     * @param maxSize Maximum size (in bytes) of cache entry that should be cleaned.
     * @return true if operation ends with success, false otherwise
     */
    @ManagedOperation(description = "Clears all cache entry's that size is smaller than maxSize parameter.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "maxSize", description = "Maximum size (in bytes) of cache entry that should be cleaned.") })
    public final boolean clearAllSmallerThen(long maxSize) {
        return this.clear(-1, maxSize, null, null, null, null);
    }

    /**
     * Clears all cache entry's that are older than baseDate parameter.<br>
     * <br>
     * This operation will stops clearing elements after first failure, this means that
     * it will clear all cache entry's until one of that entry's fails to clear.
     *
     * @param baseDate
     * @return true if operation ends with success, false otherwise
     */
    @ManagedOperation(description = "Clears all cache entry's that are older than baseDate parameter.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "baseDate", description = "") })
    public final boolean clearAllOlderThen(String baseDate) {
        return this.clear(-1, -1, baseDate, null, null, null);
    }

    /**
     * Clears all cache entry's that are younger than baseDate parameter.<br>
     * <br>
     * This operation will stops clearing elements after first failure, this means that
     * it will clear all cache entry's until one of that entry's fails to clear.
     *
     * @param baseDate
     * @return true if operation ends with success, false otherwise
     */
    @ManagedOperation(description = "Clears all cache entry's that are younger than baseDate parameter.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "baseDate", description = "") })
    public final boolean clearAllYoungerThen(String baseDate) {
        return this.clear(-1, -1, null, baseDate, null, null);
    }

    /**
     * Clears all cache entry's that matches all conditions in parameters. All parameters are connected logical AND, so to perform action cache entry should match all conditions at once.<br>
     * <br>
     * This operation will stops clearing elements after first failure, this means that
     * it will clear all cache entry's until one of that entry's fails to clear.
     *
     * @param minSize Minimal size (in bytes) of cache entry that should be cleaned. If value of this parameter < 0 then it isn't consider.
     * @param maxSize Maximum size (in bytes) of cache entry that should be cleaned. If value of this parameter < 0 then it isn't consider.
     * @param minLasModifyDate If this parameter is null or is empty it isn't consider.
     * @param maxLasModifyDate If this parameter is null or is empty it isn't consider.
     * @param minExpiresDate This parameter apply's only to ExpiresCacheKey entry's. If this parameter is null or is empty it isn't consider.
     * @param maxExpiresDate This parameter apply's only to ExpiresCacheKey entry's. If this parameter is null or is empty it isn't consider.
     * @return true if operation ends with success, false otherwise
     */
    @ManagedOperation(description = "Clears all cache entry's that matches all conditions in parameters. All parameters are connected logical AND, so to perform action cache entry should match all conditions at once.")
    @ManagedOperationParameters( {
            @ManagedOperationParameter(name = "minSize", description = "Minimal size (in bytes) of cache entry that should be cleaned. If value of this parameter < 0 then it isn't consider."),
            @ManagedOperationParameter(name = "maxSize", description = "Maximum size (in bytes) of cache entry that should be cleaned. If value of this parameter < 0 then it isn't consider."),
            @ManagedOperationParameter(name = "minLastModifyDate", description = "If this parameter is null or is empty it isn't consider."),
            @ManagedOperationParameter(name = "maxLastModifyDate", description = "If this parameter is null or is empty it isn't consider."),
            @ManagedOperationParameter(name = "minExpiresDate", description = "This parameter apply's only to ExpiresCacheKey entry's. If this parameter is null or is empty it isn't consider."),
            @ManagedOperationParameter(name = "maxExpiresDate", description = "This parameter apply's only to ExpiresCacheKey entry's. If this parameter is null or is empty it isn't consider.") })
    public final boolean clear(long minSize, long maxSize, String minLastModifyDate, String maxLastModifiDate,
            String minExpiresDate, String maxExpiresDate) {

        final AtomicBoolean state = new AtomicBoolean(true);
        this.performActionOnCaches(minSize, maxSize, minLastModifyDate, maxLastModifiDate, minExpiresDate, maxExpiresDate,
                new CacheAction() {

                    public void performAction(Cache cache, CacheKey cacheKey) {
                        if (state.get()) {
                            boolean tmp = cache.remove(cacheKey);
                            state.set(tmp);
                        }
                    }
                });

        return state.get();
    }

    /**
     * List all cache entry's that size is greater then minSize parameter. You should use this operation just before performing clearAllGreaterThen() to check what entry's would be cleaned. Returned list is limited to first 100 entry's.
     *
     * @param Minimal size (in bytes) of cache entry.
     * @return list of cache entry's that matches query arguments
     */
    @ManagedOperation(description = "List all cache entry's that size is greater then minSize parameter. You should use this operation just before performing clearAllGreaterThen() to check what entry's would be cleaned. Returned list is limited to first 100 entry's.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "minSize", description = "Minimal size (in bytes) of cache entry.") })
    public final String[] listAllGreaterThen(long minSize) {
        return this.list(minSize, -1, null, null, null, null);
    }

    /**
     * List all cache entry's that size is smaller then minSize parameter. You should use this operation just before performing clearAllSmallerThen() to check what entry's would be cleaned. Returned list is limited to first 100 entry's.
     *
     * @param maxSize Maximum size (in bytes) of cache entry.
     * @return list of cache entry's that matches query arguments
     */
    @ManagedOperation(description = "List all cache entry's that size is smaller then minSize parameter. You should use this operation just before performing clearAllSmallerThen() to check what entry's would be cleaned. Returned list is limited to first 100 entry's.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "maxSize", description = "Maximum size (in bytes) of cache entry.") })
    public final String[] listAllSmallerThen(long maxSize) {
        return this.list(-1, maxSize, null, null, null, null);
    }

    /**
     * List all cache entry's that older than baseDate parameter. You should use this operation just before performing clearAllOlderThen() to check what entry's would be cleaned. Returned list is limited to first 100 entry's.
     *
     * @param baseDate
     * @return list of cache entry's that matches query arguments
     */
    @ManagedOperation(description = "List all cache entry's that older than baseDate parameter. You should use this operation just before performing clearAllOlderThen() to check what entry's would be cleaned. Returned list is limited to first 100 entry's.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "baseDate", description = "") })
    public final String[] listAllOlderThen(String baseDate) {
        return this.list(-1, -1, baseDate, null, null, null);
    }

    /**
     * List all cache entry's that younger than baseDate parameter. You should use this operation just before performing clearAllYoungerThen() to check what entry's would be cleaned. Returned list is limited to first 100 entry's.
     *
     * @param baseDate
     * @return list of cache entry's that matches query arguments
     */
    @ManagedOperation(description = "List all cache entry's that younger than baseDate parameter. You should use this operation just before performing clearAllYoungerThen() to check what entry's would be cleaned. Returned list is limited to first 100 entry's.")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "baseDate", description = "") })
    public final String[] listAllYoungerThen(String baseDate) {
        return this.list(-1, -1, baseDate, null, null, null);
    }

    /**
     * List all cache entry's that that matches all conditions in parameters. All parameters are connected logical AND, so to perform action cache entry should match all conditions at once. You should use this operation just before performing clear() to check what entry's would be cleaned. Returned list is limited to first 100 entry's.
     *
     * @param minSize Minimal size (in bytes) of cache entry. If value of this parameter < 0 then it isn't consider.
     * @param maxSize Maximum size (in bytes) of cache entry. If value of this parameter < 0 then it isn't consider.
     * @param minLastModifyDate If this parameter is null or is empty it isn't consider.
     * @param maxLastModifiDate If this parameter is null or is empty it isn't consider.
     * @param minExpiresDate This parameter apply's only to ExpiresCacheKey entry's. If this parameter is null or is empty it isn't consider.
     * @param maxExpiresDate This parameter apply's only to ExpiresCacheKey entry's. If this parameter is null or is empty it isn't consider.
     * @return
     */
    @ManagedOperation(description = "List all cache entry's that that matches all conditions in parameters. All parameters are connected logical AND, so to perform action cache entry should match all conditions at once. You should use this operation just before performing clear() to check what entry's would be cleaned. Returned list is limited to first 100 entry's.")
    @ManagedOperationParameters( {
            @ManagedOperationParameter(name = "minSize", description = "Minimal size (in bytes) of cache entry. If value of this parameter < 0 then it isn't consider."),
            @ManagedOperationParameter(name = "maxSize", description = "Maximum size (in bytes) of cache entry. If value of this parameter < 0 then it isn't consider."),
            @ManagedOperationParameter(name = "minLastModifyDate", description = "If this parameter is null or is empty it isn't consider."),
            @ManagedOperationParameter(name = "maxLastModifyDate", description = "If this parameter is null or is empty it isn't consider."),
            @ManagedOperationParameter(name = "minExpiresDate", description = "This parameter apply's only to ExpiresCacheKey entry's. If this parameter is null or is empty it isn't consider."),
            @ManagedOperationParameter(name = "maxExpiresDate", description = "This parameter apply's only to ExpiresCacheKey entry's. If this parameter is null or is empty it isn't consider.") })
    public final String[] list(long minSize, long maxSize, String minLastModifyDate, String maxLastModifiDate,
            String minExpiresDate, String maxExpiresDate) {

        final List<String> results = new ArrayList<String>();
        this.performActionOnCaches(minSize, maxSize, minLastModifyDate, maxLastModifiDate, minExpiresDate, maxExpiresDate,
                new CacheAction() {

                    public void performAction(Cache cache, CacheKey cacheKey) {
                        results.add(cache.toString() + cacheKey.toString());
                    }
                });

        List<String> returnList;
        if (results.size() > 100) {
            returnList = results.subList(0, 99);
            returnList.add("There is " + (results.size() - 100) + " more elements that matches this filter.");
        } else {
            returnList = results;
        }

        return returnList.toArray(new String[] {});
    }

    /**
     * This method actually performs action on cache's.
     *
     * @param minSize If value of this parameter < 0 value of point wouldn't increased.
     * @param maxSize If value of this parameter < 0 value of point wouldn't increased.
     * @param minLastModifyDate If this parameter is null or is empty value of point wouldn't increased.
     * @param maxLastModifiDate If this parameter is null or is empty value of point wouldn't increased.
     * @param minExpiresDate If this parameter is null or is empty value of point wouldn't increased.
     * @param maxExpiresDate If this parameter is null or is empty value of point wouldn't increased.
     * @param action call back to action that should be performed if cache entry matches given criteria
     */
    private void performActionOnCaches(long minSize, long maxSize, String minLastModifyDate, String maxLastModifyDate,
            String minExpiresDate, String maxExpiresDate, CacheAction action) {

        Date minExpires = this.parseDate(minExpiresDate);
        Date maxExpires = this.parseDate(maxExpiresDate);
        Date minLastModify = this.parseDate(minLastModifyDate);
        Date maxLastModify = this.parseDate(maxLastModifyDate);
        int points = this.countPoints(minSize, maxSize, minLastModify, maxLastModify, minExpires, maxExpires);

        for (Cache cache : this.caches) {
            for (CacheKey cacheKey : cache.keySet()) {
                int score = 0;
                CacheValue cacheValue = cache.get(cacheKey);
                if (cacheValue == null) { // prevent from NullPointerException
                    continue;
                }

                // check minSize condition
                if (minSize >= 0 && cacheValue.size() >= minSize) {
                    // if minSize condition is set (it value is < 0) and it is not fulfilled we skip this element
                    score++;
                }

                // check maxSize condition
                if (maxSize >= 0 && cacheValue.size() <= maxSize) {
                    score++;
                }

                Date lastModifyDate = new Date(cacheKey.getLastModified());
                if (minLastModify != null && lastModifyDate.compareTo(minLastModify) >= 0) {
                    score++;
                }

                if (maxLastModify != null && lastModifyDate.compareTo(maxLastModify) <= 0) {
                    score++;
                }

                // expires condition can be only applied to ExpiresCacheKey instances
                if ((minExpires != null || maxExpires != null) && cacheKey instanceof ExpiresCacheKey) {
                    ExpiresCacheKey expiresCacheKey = (ExpiresCacheKey) cacheKey;
                    Date expiresDate = new Date(expiresCacheKey.getExpirationTimestamp());

                    if (minExpires != null && expiresDate.compareTo(minExpires) >= 0) {
                        score++;
                    }

                    if (maxExpires != null && expiresDate.compareTo(maxExpires) <= 0) {
                        score++;
                    }
                }

                if (score == points) {
                    action.performAction(cache, cacheKey);
                }
            }
        }
    }

    /**
     * Returns date i past based on value of string parameter.
     *
     * @param date string in format that matches regular expression ^\d+[smhd]$
     * @return
     */
    private Date parseDate(String date) {
        if (StringUtils.isEmpty(date)) {
            return null;
        }

        if (!date.matches("^\\d+[smhd]$")) {
            throw new UnsupportedOperationException("Unsupported date format: " + date);
        }

        char unit = date.charAt(date.length() - 1);
        long multipler = Long.parseLong(date.substring(0, date.length() - 2));

        long factor;
        switch (unit) {
        case 's': // second
            factor = 1000;
            break;
        case 'm': // minute
            factor = 60 * 1000;
            break;
        case 'h': // hour
            factor = 60 * 60 * 1000;
            break;
        case 'd': // day
            factor = 24 * 60 * 60 * 1000;
            break;
        default:
            throw new UnsupportedOperationException("Unsupported unit: " + unit);
        }

        return new Date(System.currentTimeMillis() - multipler * factor);
    }

    /**
     * Counts points for single cache query. If
     *
     * @param minSize If value of this parameter < 0 value of point wouldn't increased.
     * @param maxSize If value of this parameter < 0 value of point wouldn't increased.
     * @param minLastModifyDate If this parameter is null value of point wouldn't increased.
     * @param maxLastModifiDate If this parameter is null value of point wouldn't increased.
     * @param minExpiresDate If this parameter is null or of point wouldn't increased.
     * @param maxExpiresDate If this parameter is null or of point wouldn't increased.
     * @return value of point's
     */
    private int countPoints(long minSize, long maxSize, Date minLastModifyDate, Date maxLastModifiDate,
            Date minExpiresDate, Date maxExpiresDate) {

        int points = 0;

        if (minSize >= 0) {
            points++;
        }

        if (maxSize >= 0) {
            points++;
        }

        if (minLastModifyDate != null) {
            points++;
        }

        if (maxLastModifiDate != null) {
            points++;
        }

        if (minExpiresDate != null) {
            points++;
        }

        if (maxExpiresDate != null) {
            points++;
        }
        return points;
    }

    private interface CacheAction {
        void performAction(Cache cache, CacheKey cacheKey);
    }
}
