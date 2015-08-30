/**
 * Copyright 2011 Terracotta, Inc.
 * Copyright 2011 Oracle America Incorporated
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.spy.memcached.jcache;

import net.spy.memcached.ConnectionFactoryBuilder.Locator;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.FailureMode;
import net.spy.memcached.HashAlgorithm;
import net.spy.memcached.transcoders.Transcoder;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.OptionalFeature;
import javax.cache.Status;
import javax.cache.implementation.AbstractCacheManager;
import javax.cache.implementation.DelegatingCacheBuilder;
import javax.transaction.UserTransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class SpyCacheManager
        extends AbstractCacheManager
        implements
        CacheManager
{

    private static final Logger LOGGER = Logger.getLogger("javax.cache");
    private final HashMap<String, Cache<?, ?>> caches = new HashMap<String, Cache<?, ?>>();
    private volatile Status status;
    private String servers;
    private Locator locator;
    private HashAlgorithm hashAlgorithm;
    private FailureMode failureMode;
    private long opTimeout;
    private Protocol protocol;
    private Transcoder<?> transcoder;

    /**
     * Constructs a new RICacheManager with the specified name.
     *
     * @param classLoader
     *            the ClassLoader that should be used in converting values into
     *            Java Objects.
     * @param name
     *            the name of this cache manager
     * @throws NullPointerException
     *             if classLoader or name is null.
     */
    public SpyCacheManager(String name, ClassLoader classLoader)
    {
        super(name, classLoader);
        status = Status.UNINITIALISED;
        if (classLoader == null) {
            throw new NullPointerException("No classLoader specified");
        }
        if (name == null) {
            throw new NullPointerException("No name specified");
        }
    }

    public void start()
    {
        if (servers == null) {
            throw new NullPointerException("No servers specified");
        }
        status = Status.STARTED;
    }

    /**
     * Returns the status of this CacheManager.
     * <p/>
     *
     * @return one of {@link javax.cache.Status}
     */
    @Override
    public Status getStatus()
    {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> CacheBuilder<K, V> createCacheBuilder(String cacheName)
    {

        if (caches.get(cacheName) != null) {
            throw new CacheException("Cache " + cacheName + " already exists");
        }

        // TODO: where did these naming constraints come from?
        if (cacheName == null) {
            throw new NullPointerException(
                    "A cache name must must not be null.");
        }
        Pattern searchPattern = Pattern.compile("\\S+");
        Matcher matcher = searchPattern.matcher(cacheName);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "A cache name must contain one or more non-whitespace characters");
        }

        return new SpyCacheBuilder<K, V>(cacheName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Cache<K, V> getCache(String cacheName)
    {
        if (status != Status.STARTED) {
            throw new IllegalStateException();
        }
        synchronized (caches) {
            /*
			 * Can't really verify that the K/V cast is safe but it is required
			 * by the API, using a local variable for the cast to allow for a
			 * minimal scoping of @SuppressWarnings
			 */
            @SuppressWarnings("unchecked")
            final Cache<K, V> cache = (Cache<K, V>) caches.get(cacheName);
            return cache;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Cache<?, ?>> getCaches()
    {
        synchronized (caches) {
            HashSet<Cache<?, ?>> set = new HashSet<Cache<?, ?>>();
            for (Cache<?, ?> cache : caches.values()) {
                set.add(cache);
            }
            return Collections.unmodifiableSet(set);
        }
    }

    private void addCacheInternal(Cache<?, ?> cache)
    {
        synchronized (caches) {
            if (caches.get(cache.getName()) != null) {
                throw new CacheException("Cache " + cache.getName()
                        + " already exists");
            }
            caches.put(cache.getName(), cache);
        }
        cache.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeCache(String cacheName)
    {
        if (status != Status.STARTED) {
            throw new IllegalStateException();
        }
        if (cacheName == null) {
            throw new NullPointerException();
        }
        Cache<?, ?> oldCache;
        synchronized (caches) {
            oldCache = caches.remove(cacheName);
        }
        if (oldCache != null) {
            oldCache.stop();
        }

        return oldCache != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserTransaction getUserTransaction()
    {
        throw new UnsupportedOperationException(
                "Transactions are not supported.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(OptionalFeature optionalFeature)
    {
        return Caching.isSupported(optionalFeature);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown()
    {
        if (status != Status.STARTED) {
            throw new IllegalStateException();
        }
        super.shutdown();
        ArrayList<Cache<?, ?>> cacheList;
        synchronized (caches) {
            cacheList = new ArrayList<Cache<?, ?>>(caches.values());
            caches.clear();
        }
        for (Cache<?, ?> cache : cacheList) {
            try {
                cache.stop();
            }
            catch (Exception e) {
                getLogger()
                        .log(Level.WARNING, "Error stopping cache: " + cache);
            }
        }
        status = Status.STOPPED;
    }

    @Override
    public <T> T unwrap(java.lang.Class<T> cls)
    {
        if (cls.isAssignableFrom(this.getClass())) {
            return cls.cast(this);
        }

        throw new IllegalArgumentException("Unwapping to " + cls
                + " is not a supported by this implementation");
    }

    public Locator getLocator()
    {
        return locator;
    }

    public void setLocator(Locator locator)
    {
        this.locator = locator;
    }

    public HashAlgorithm getHashAlgorithm()
    {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(HashAlgorithm hashAlgorithm)
    {
        this.hashAlgorithm = hashAlgorithm;
    }

    public FailureMode getFailureMode()
    {
        return failureMode;
    }

    public void setFailureMode(FailureMode failureMode)
    {
        this.failureMode = failureMode;
    }

    public long getOpTimeout()
    {
        return opTimeout;
    }

    public void setOpTimeout(long opTimeout)
    {
        this.opTimeout = opTimeout;
    }

    public Protocol getProtocol()
    {
        return protocol;
    }

    public void setProtocol(Protocol protocol)
    {
        this.protocol = protocol;
    }

    public Transcoder<?> getTranscoder()
    {
        return transcoder;
    }

    public void setTranscoder(Transcoder<?> transcoder)
    {
        this.transcoder = transcoder;
    }

    public String getServers()
    {
        return servers;
    }

    public void setServers(String servers)
    {
        if (servers == null) {
            throw new NullPointerException("No servers specified");
        }

        this.servers = servers;
    }

    /**
     * Obtain the logger.
     *
     * @return the logger.
     */
    Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * RI implementation of {@link CacheBuilder}
     *
     * @param <K>
     *            the key type
     * @param <V>
     *            the value type
     */
    private class SpyCacheBuilder<K, V>
            extends DelegatingCacheBuilder<K, V>
    {
        public SpyCacheBuilder(String cacheName)
        {
            super(new SpyCache.Builder<K, V>(cacheName, getName(),
                    getClassLoader(), servers));
        }

        @Override
        public Cache<K, V> build()
        {
            Cache<K, V> cache = super.build();
            addCacheInternal(cache);
            return cache;
        }
    }
}
