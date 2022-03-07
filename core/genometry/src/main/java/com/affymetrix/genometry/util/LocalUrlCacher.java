package com.affymetrix.genometry.util;

import com.affymetrix.common.PreferenceUtils;
import static com.affymetrix.genometry.symloader.ProtocolConstants.FILE_PROTOCOL_SCHEME;
import static com.affymetrix.genometry.symloader.ProtocolConstants.FTP_PROTOCOL_SCHEME;
import static com.affymetrix.genometry.symloader.ProtocolConstants.HTTP_PROTOCOL_SCHEME;
import static com.affymetrix.genometry.symloader.ProtocolConstants.IGB_PROTOCOL_SCHEME;
import static com.affymetrix.genometry.symloader.ProtocolConstants.SUPPORTED_PROTOCOL_SCHEMES;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableHTTPStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO this is a terribly written class which should be trashed and replaced
public final class LocalUrlCacher {

    private static final Logger logger = LoggerFactory.getLogger(LocalUrlCacher.class);
    public static final String CACHE_CONTENT_ROOT = PreferenceUtils.getAppDataDirectory() + "cache/";
    private static final String CACHE_HEADER_ROOT = CACHE_CONTENT_ROOT + "headers/";
    private static final String HTTP_STATUS_HEADER = "HTTP_STATUS";
    private static final String HTTP_LOCATION_HEADER = "Location";
    private static final int HTTP_TEMP_REDIRECT = 307;
    private static final boolean CACHE_FILE_URLS = false;
    public static final int IGNORE_CACHE = 100;
    public static final int ONLY_CACHE = 101;
    public static final int NORMAL_CACHE = 102;

    public static enum CacheUsage {

        Normal(NORMAL_CACHE),
        Disabled(IGNORE_CACHE),
        Offline(ONLY_CACHE);

        public final int usage;

        CacheUsage(int usage) {
            this.usage = usage;
        }
    }

    public static enum CacheOption {

        IGNORE,
        ONLY,
        NORMAL
    }

    // the "quickload" part of the constant value is there for historical reasons
    public static final String PREF_CACHE_USAGE = "quickload_cache_usage";
    public static final int CACHE_USAGE_DEFAULT = LocalUrlCacher.NORMAL_CACHE;
    public static final String URL_NOT_REACHABLE = "URL_NOT_REACHABLE";

    public static final int CONNECT_TIMEOUT = 30000;
    public static final int READ_TIMEOUT = 60000;

    private static boolean offline = false;

    /**
     * Determines whether the given URL string represents a file URL.
     */
    private static boolean isFile(String url) {
        if (url == null || url.length() < 4) {
            return false;
        }
        return (url.substring(0, 4).compareToIgnoreCase(FILE_PROTOCOL_SCHEME) == 0);
    }

    public static boolean isLocalFile(URI uri) {
        String scheme = uri.getScheme();
        return StringUtils.equalsIgnoreCase(scheme, FILE_PROTOCOL_SCHEME);
    }

    public static SeekableStream getSeekableStream(URI uri) throws IOException {
        if (LocalUrlCacher.isLocalFile(uri)) {
            File f = new File(uri.getPath());
            return new SeekableFileStream(f);
        }
        return new SeekableHTTPStream(uri.toURL());
    }

    public static InputStream getInputStream(URL url) throws IOException {
        return getInputStream(url.toString(), getPreferredCacheUsage(), true, null, null, false);
    }

    public static InputStream getInputStream(URL url, boolean write_to_cache, Map<String, String> rqstHeaders, Map<String, List<String>> respHeaders) throws IOException {
        return getInputStream(url.toString(), getPreferredCacheUsage(), write_to_cache, rqstHeaders, respHeaders, false);
    }

    public static InputStream getInputStream(String url) throws IOException {
        return getInputStream(url, getPreferredCacheUsage(), true, null, null, false);
    }

    public static InputStream getInputStream(String url, boolean write_to_cache, Map<String, String> rqstHeaders)
            throws IOException {
        return getInputStream(url, getPreferredCacheUsage(), write_to_cache, rqstHeaders, null, false);
    }

    public static InputStream getInputStream(String url, boolean write_to_cache, Map<String, String> rqstHeaders, boolean fileMayNotExist, boolean allowHtml)
            throws IOException {
        return getInputStream(url, getPreferredCacheUsage(), write_to_cache, rqstHeaders, null, fileMayNotExist, allowHtml);
    }

    public static InputStream getInputStream(String url, boolean write_to_cache, Map<String, String> rqstHeaders, boolean fileMayNotExist)
            throws IOException {
        return getInputStream(url, getPreferredCacheUsage(), write_to_cache, rqstHeaders, null, fileMayNotExist);
    }

    public static InputStream getInputStream(String url, boolean write_to_cache)
            throws IOException {
        return getInputStream(url, getPreferredCacheUsage(), write_to_cache, null, null, false);
    }

    public static InputStream getInputStream(String url, int cache_option, boolean write_to_cache, Map<String, String> rqstHeaders)
            throws IOException {
        return getInputStream(url, cache_option, write_to_cache, rqstHeaders, null, false);
    }

    private static InputStream getInputStream(
            String url, int cache_option, boolean write_to_cache, Map<String, String> rqstHeaders, Map<String, List<String>> respHeaders, boolean fileMayNotExist)
            throws IOException {
        return getInputStream(url, cache_option, write_to_cache, rqstHeaders, respHeaders, fileMayNotExist, true);
    }

    /**
     * @param url URL to load.
     * @param cache_option caching option (should be enum)
     * @param write_to_cache Write to cache.
     * @param rqstHeaders a Map which when getInputStream() returns will be
     * populated with any headers returned from the url Each entry will be
     * either: { header name ==> header value } OR if multiple headers have same
     * name, then value of entry will be a List of the header values: { header
     * name ==> [header value 1, header value 2, ...] } headers will get cleared
     * of any entries it had before getting passed as arg
     * @param fileMayNotExist Don't warn if file doesn't exist.
     * @param allowHtml If returning an html file is allowed. e.g. Request
     * 'http://www.transvar.org?=/contents.txt' will return a html page
     * @return input stream from the loaded url
     * @throws java.io.IOException
     */
    private static InputStream getInputStream(
            String url, int cache_option, boolean write_to_cache, Map<String, String> rqstHeaders, Map<String, List<String>> respHeaders, boolean fileMayNotExist, boolean allowHtml)
            throws IOException {

        // if url is a file url, and not caching files, then just directly return stream
        if (isFile(url)) {
            //IGB.getInstance().logInfo("URL is file url, so not caching: " + furl);
            return getUncachedFileStream(url, fileMayNotExist);
        }

        // if NORMAL_CACHE:
        //   if not in cache, then return input stream from http connection
        //   if in cache, then check URL content has changed via GET with if-modified-since header based
        //        on modification date of cached file
        //      if content is returned, check last-modified header just to be sure (some servers might ignore
        //        if-modified-since header?)
        InputStream resultStream = null;
        File cacheFile = getCacheFile(CACHE_CONTENT_ROOT, url);
        File header_cache_file = getCacheFile(CACHE_HEADER_ROOT, url);
        long local_timestamp = -1;

        // special-case when one cache file exists, but the other doesn't or is zero-length. Shouldn't happen, really.
        if (cacheFile.exists() && (!header_cache_file.exists() || header_cache_file.length() == 0)) {
            cacheFile.delete();
        } else if ((!cacheFile.exists() || cacheFile.length() == 0) && header_cache_file.exists()) {
            header_cache_file.delete();
        }

        if (cache_option != IGNORE_CACHE && cacheFile.exists() && header_cache_file.exists()) {
            local_timestamp = cacheFile.lastModified();
        }
        URLConnection conn = null;
        long remote_timestamp = 0;
        boolean url_reachable = false;
        int http_status = -1;

        if (cache_option != ONLY_CACHE) {
            try {
                conn = connectToUrl(url, local_timestamp);

                if (respHeaders != null) {
                    respHeaders.putAll(conn.getHeaderFields());
                }

                remote_timestamp = conn.getLastModified();

                if (conn instanceof HttpURLConnection) {
                    HttpURLConnection hcon = (HttpURLConnection) conn;
                    http_status = hcon.getResponseCode();
                    String responseContentType = hcon.getContentType();

                    // If html is not allowed
                    if (!allowHtml && responseContentType != null && responseContentType.toLowerCase().contains("text/html")) {
                        return null;
                    }

                    //Handle one redirect
                    if (http_status == HTTP_TEMP_REDIRECT) {
                        conn = handleTemporaryRedirect(conn, url, local_timestamp);
                        hcon = (HttpURLConnection) conn;
                        http_status = hcon.getResponseCode();
                    }

                    // Status codes:
                    //     1xx Informational
                    //     2xx Success
                    //     3xx Redirection
                    //     4xx Client Error
                    //     5xx Server Error
                    //  So only consider URL reachable if 2xx or 3xx (not quite sure what to do yet with redirection)
                    url_reachable = ((http_status >= 200) && (http_status < 400));
                } else {
                    // Assuming it to be FtpURLConnection.
                    url_reachable = true;
                    remote_timestamp = conn.getIfModifiedSince();
                }

            } catch (IOException ioe) {
                url_reachable = false;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                url_reachable = false;
            }
            if (!url_reachable) {
                if (!fileMayNotExist) {
                    logger.warn("URL not reachable, status code = {}: {}", new Object[]{http_status, url});
//                    throw new FileNotFoundException(); 
                }
                if (rqstHeaders != null) {
                    rqstHeaders.put("LocalUrlCacher", URL_NOT_REACHABLE);
                }
                if (!cacheFile.exists()) {
                    if (!fileMayNotExist) {
                        logger.warn("URL {} is not reachable, and is not cached!", url);
//                        throw new FileNotFoundException();
                    }
                    return null; //TODO throw appropriate exception instead of reaching this situation
                }
            }
        }

        // found cached data
        if (local_timestamp != -1) {
            resultStream = TryToRetrieveFromCache(url_reachable, http_status, cacheFile, remote_timestamp, local_timestamp, url, cache_option);
            if (rqstHeaders != null) {
                retrieveHeadersFromCache(rqstHeaders, header_cache_file);
            }
        }

        // Need to get data from URL, because no cache hit, or stale, or cache_option set to IGNORE_CACHE...
        if (resultStream == null && url_reachable && (cache_option != ONLY_CACHE)) {
            resultStream = RetrieveFromURL(conn, rqstHeaders, write_to_cache, cacheFile, header_cache_file);
        }

        if (rqstHeaders != null && logger.isTraceEnabled()) {
            reportHeaders(url, rqstHeaders);
        }
        if (resultStream == null) {
            logger.warn(
                    "couldn''t get content for: {}", url);
        }
        return resultStream;
    }

    private static URLConnection handleTemporaryRedirect(URLConnection conn, String url, long local_timestamp) throws IOException {

        String redirect_url = conn.getHeaderField(HTTP_LOCATION_HEADER);
        if (redirect_url == null) {
            logger.warn(
                    "Url {} moved temporarily. But no redirect url provided", url);
            return conn;
        }
        logger.warn(
                "Url {} moved temporarily. \n Using redirect \nurl {}", new Object[]{url, redirect_url});

        conn = connectToUrl(redirect_url, local_timestamp);

        return conn;
    }

    public static URLConnection connectToUrl(String url, long local_timestamp) throws IOException {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                try {
                    for (Certificate cert : session.getPeerCertificates()) {
                        String[] certDomainComponents = ((X509Certificate) cert).getSubjectDN().toString().split("\\.");
                        certDomainComponents[certDomainComponents.length - 1] = certDomainComponents[certDomainComponents.length - 1].split(",")[0];
                        String[] urlDomainComponents = hostname.split("\\.");
                        // Trust certificates whose common name's core domain matches that of the request URL
                        if ((urlDomainComponents[urlDomainComponents.length - 1] + urlDomainComponents[urlDomainComponents.length - 2])
                                .equals(certDomainComponents[certDomainComponents.length - 1] + certDomainComponents[certDomainComponents.length - 2])) {
                            return true;
                        }
                    }
                } catch (SSLPeerUnverifiedException ex) {
                    logger.info("Could not verify SSL peer: ", ex);
                }
                return false;
            }
        });        
        URL theurl = new URL(url);
        URLConnection conn = theurl.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("Accept-Encoding", "gzip");
        if (local_timestamp != -1) {
            conn.setIfModifiedSince(local_timestamp);
        }
        //    because some method calls on URLConnection like those below don't always throw errors
        //    when connection can't be opened -- which would end up allowing url_reachable to be set to true
        ///   even when there's no connection
        conn.connect();
        return conn;
    }

    private static InputStream getUncachedFileStream(String url, boolean fileMayNotExist) throws IOException {
        URL furl = new URL(url);
        URLConnection huc = furl.openConnection();
        huc.setConnectTimeout(CONNECT_TIMEOUT);
        huc.setReadTimeout(READ_TIMEOUT);
        InputStream fstr = null;
        try {
            fstr = huc.getInputStream();
        } catch (FileNotFoundException ex) {
            if (fileMayNotExist) {
                logger.info(
                        "Couldn''t find file {}, but it''s optional.", url);
                return null; // We don't care if the file doesn't exist.
            }
        }
        return fstr;
    }

    public static File getCacheFile(String root, String url) {
        File fil = new File(root);
        if (!fil.exists()) {
            logger.info("Creating new cache directory: {}", fil.getAbsolutePath());
            if (!fil.mkdirs()) {
                logger.error("Could not create directory: {}", fil.toString());
            }
        }
        String cache_file_name = getCacheFileName(url);
        File cache_file = new File(root, cache_file_name);
        return cache_file;
    }

    private static String getCacheFileName(String url) {
        HashFunction hf = Hashing.md5();
        HashCode hc = hf.newHasher().putString(url, Charsets.UTF_8).hash();
        return hc.toString();
    }

    /**
     * Invalidate cache file so it will be rebuilt if needed.
     *
     * @param url
     */
    public static void invalidateCacheFile(String url) {
        File cache_file = getCacheFile(CACHE_CONTENT_ROOT, url);
        if (cache_file.exists()) {
            if (!cache_file.delete()) {
                cache_file.deleteOnExit();	// something went wrong.  Try to delete it later
            }
        }

        File header_cache_file = getCacheFile(CACHE_HEADER_ROOT, url);
        if (header_cache_file.exists()) {
            if (!header_cache_file.delete()) {
                header_cache_file.deleteOnExit();	// something went wrong.  Try to delete it later
            }
        }
    }

    private static InputStream TryToRetrieveFromCache(
            boolean url_reachable, int http_status, File cache_file, long remote_timestamp, long local_timestamp,
            String url, int cache_option)
            throws IOException {
        if (url_reachable) {
            //  has a timestamp and response contents not modified since local cached copy last modified, so use local
            if (http_status == HttpURLConnection.HTTP_NOT_MODIFIED) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Received HTTP_NOT_MODIFIED status for URL, using cache: {}", cache_file);
                }
            } else if (remote_timestamp > 0 && remote_timestamp <= local_timestamp) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Cache exists and is more recent, using cache: {}", cache_file);
                }
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("cached file exists, but URL is more recent, so reloading cache: {}", url);
                }
                return null;
            }
        } else {
            // url is not reachable
            if (cache_option != ONLY_CACHE) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Remote URL not reachable: {}", url);
                }

                if (cache_file.exists()) { // If cache file exists and url is not reacheable then probably site is down.
                    logger.warn("Remote URL {} not reachable to compare to local cache. So ignoring the cache.", url);
                    return null;
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Loading cached file for URL: {}", url);
            }

        }
        return new BufferedInputStream(new FileInputStream(cache_file));
    }

    private static void retrieveHeadersFromCache(Map<String, String> rqstHeaders, File header_cache_file) throws IOException {
        // using cached content, so should also use cached headers
        //   eventually want to improve so headers get updated if server is accessed and url is reachable
        BufferedInputStream hbis = null;
        try {
            hbis = new BufferedInputStream(new FileInputStream(header_cache_file));
            Properties headerprops = new Properties();
            headerprops.load(hbis);
            for (String propKey : headerprops.stringPropertyNames()) {
                rqstHeaders.put(propKey, headerprops.getProperty(propKey));
            }
        } finally {
            GeneralUtils.safeClose(hbis);
        }
    }

    /**
     * Retrieve a page from a URL, optionally storing it in the cache.
     *
     * @throws IOException
     */
    private static InputStream RetrieveFromURL(
            URLConnection conn, Map<String, String> headers, boolean write_to_cache, File cache_file, File header_cache_file) throws IOException {
        final InputStream connstr;
        String contentEncoding = conn.getHeaderField("Content-Encoding");
        boolean isGZipped = contentEncoding != null && "gzip".equalsIgnoreCase(contentEncoding);
        if (isGZipped) {
            connstr = GeneralUtils.getGZipInputStream(conn.getURL().toString(), conn.getInputStream());
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "gzipped stream, so ignoring reported content length of {}", conn.getContentLength());
            }
        } else {
            connstr = conn.getInputStream();
        }

        if (write_to_cache) {
            writeHeadersToCache(header_cache_file, populateHeaderProperties(conn, headers));
            return new CachingInputStream(connstr, cache_file, conn.getURL().toExternalForm());
        } else {
            return connstr;
        }
    }

    // populating header Properties (for persisting) and header input Map
    private static Properties populateHeaderProperties(URLConnection conn, Map<String, String> headers) {
        Map<String, List<String>> headermap = conn.getHeaderFields();
        Properties headerprops = new Properties();
        for (Map.Entry<String, List<String>> ent : headermap.entrySet()) {
            String key = ent.getKey();
            // making all header names lower-case
            List<String> vals = ent.getValue();
            if (vals.isEmpty()) {
                continue;
            }
            String val = vals.get(0);
            if (key == null) {
                key = HTTP_STATUS_HEADER;
            } // HTTP status code line has a null key, change so can be stored
            key = key.toLowerCase();
            headerprops.setProperty(key, val);
            if (headers != null) {
                headers.put(key, val);
            }
        }
        return headerprops;
    }

    private static void reportHeaders(String url, Map<String, String> headers) {
        logger.info("HEADERS for URL: {}", url);
        for (Map.Entry<String, String> ent : headers.entrySet()) {
            logger.info("key: {}, val: {}", new Object[]{ent.getKey(), ent.getValue()});
        }
    }

    /**
     * Forces flushing of entire cache. Simply removes all cached files.
     */
    public static void clearCache() {
        logger.info("Clearing cache");
        DeleteFilesInDirectory(CACHE_HEADER_ROOT);
        DeleteFilesInDirectory(CACHE_CONTENT_ROOT);
    }

    private static void DeleteFilesInDirectory(String filename) {
        File dir = new File(filename);
        if (dir.exists()) {
            for (File fil : dir.listFiles()) {
                fil.delete();
            }
        }
    }

    /**
     * @return the location of the root directory of the cache.
     */
    public static String getCacheRoot() {
        return CACHE_CONTENT_ROOT;
    }

    /**
     * @return the current value of the persistent user preference
     * PREF_CACHE_USAGE.
     */
    public static int getPreferredCacheUsage() {
        return PreferenceUtils.getIntParam(PREF_CACHE_USAGE, CACHE_USAGE_DEFAULT);
    }

    public static void setPreferredCacheUsage(int usage) {
        logger.info("Setting Caching mode to {}", getCacheUsage(usage));
        PreferenceUtils.saveIntParam(LocalUrlCacher.PREF_CACHE_USAGE, usage);
    }

    private static void writeHeadersToCache(File header_cache_file, Properties headerprops) throws IOException {
        // cache headers also -- in [cache_dir]/headers ?
        if (logger.isTraceEnabled()) {
            logger.info(
                    "writing headers to cache: {}", header_cache_file.getPath());
        }
        BufferedOutputStream hbos = null;
        try {
            hbos = new BufferedOutputStream(new FileOutputStream(header_cache_file));
            headerprops.store(hbos, null);
        } finally {
            GeneralUtils.safeClose(hbos);
        }
    }

    public static CacheUsage getCacheUsage(int usage) {
        for (CacheUsage u : CacheUsage.values()) {
            if (u.usage == usage) {
                return u;
            }
        }

        return null;
    }

    public static File convertURIToFile(URI uri) {
        return convertURIToFile(uri, false);
    }

    public static File convertURIToFile(URI uri, boolean fileMayNotExist) {
        if (uri.getScheme() == null) {
            // attempt to find a local file
        }
        if (isLocalFile(uri)) {
            File f = new File(uri);
            if (!GeneralUtils.getUnzippedName(f.getName()).equalsIgnoreCase(f.getName())) {
                try {
                    File f2 = File.createTempFile(f.getName(), null);
                    f2.deleteOnExit();	// This is only a temporary file!  Delete on exit.
                    GeneralUtils.unzipFile(f, f2);
                    return f2;
                } catch (IOException ex) {
                    logger.error(null, ex);
                    return null;
                }
            }
            return f;
        }
        String scheme = uri.getScheme().toLowerCase();
        if (scheme.startsWith(HTTP_PROTOCOL_SCHEME) || scheme.startsWith(FTP_PROTOCOL_SCHEME)) {
            InputStream istr = null;
            try {
                String uriStr = uri.toString();
                istr = LocalUrlCacher.getInputStream(uriStr, false, null, fileMayNotExist);

                if (istr == null) {
                    return null;
                }

                StringBuffer stripped_name = new StringBuffer();
                InputStream str = GeneralUtils.unzipStream(istr, uriStr, stripped_name);
                String stream_name = stripped_name.toString();
                if (str instanceof BufferedInputStream) {
                } else {
                    str = new BufferedInputStream(str);
                }
                return GeneralUtils.convertStreamToFile(str, stream_name.substring(stream_name.lastIndexOf('/')));
            } catch (IOException ex) {
                logger.error(null, ex);
            } finally {
                GeneralUtils.safeClose(istr);
            }
        }
        logger.error(
                "URL scheme: {} not recognized", scheme);
        return null;
    }

    /**
     * Get stream associated with this uri. Don't unzip here.
     *
     * @param uri
     */
    public static BufferedInputStream convertURIToBufferedUnzippedStream(URI uri) throws Exception {
        String scheme = uri.getScheme().toLowerCase();
        InputStream is = null;
        if (scheme.length() == 0 || scheme.equals(FILE_PROTOCOL_SCHEME)) {
            is = new FileInputStream(new File(uri));
        } else if (scheme.startsWith(HTTP_PROTOCOL_SCHEME) || scheme.startsWith(FTP_PROTOCOL_SCHEME)) {
            is = LocalUrlCacher.getInputStream(uri.toString());
            return HttpRequest.get(uri.toURL())
                    .acceptGzipEncoding()
                    .uncompress(true)
                    .trustAllCerts()
                    .trustAllHosts()
                    .followRedirects(true).buffer();
        } else {
            logger.error("URL scheme: {} not recognized", scheme);
            return null;
        }
        if (is == null) {
            throw new FileNotFoundException();
        }
        StringBuffer stripped_name = new StringBuffer();
        InputStream str = GeneralUtils.unzipStream(is, uri.toString(), stripped_name);
        if (str instanceof BufferedInputStream) {
            return (BufferedInputStream) str;
        }
        return new BufferedInputStream(str);
    }

    /**
     * Get stream associated with this URI. Don't unzip here.
     */
    public static BufferedInputStream convertURIToBufferedStream(URI uri) {
        String scheme = uri.getScheme().toLowerCase();
        InputStream is = null;
        try {
            if (StringUtils.equals(scheme, FILE_PROTOCOL_SCHEME)) {
                return new BufferedInputStream(new FileInputStream(new File(uri)));
            } else if (scheme.startsWith(HTTP_PROTOCOL_SCHEME) || scheme.startsWith(FTP_PROTOCOL_SCHEME)) {
                is = LocalUrlCacher.getInputStream(uri.toString());
                return HttpRequest.get(uri.toURL())
                        .acceptGzipEncoding()
                        .trustAllCerts()
                        .trustAllHosts()
                        .followRedirects(true).buffer();
            } else {
                logger.error(
                        "URL scheme: {} not recognized", scheme);
                return null;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    public static boolean isURL(String url) {
        if (StringUtils.isNotBlank(url)) {
            String scheme = StringUtils.substringBefore(url, ":");
            return SUPPORTED_PROTOCOL_SCHEMES.contains(scheme);
        }
        return false;
    }

    public static boolean isValidURL(String url) {
        try {
            URI uri = new URI(url);
            return isValidURI(uri);
        } catch (URISyntaxException ex) {
            logger.warn("Invalid url {}:", url, ex);
        }

        return false;
    }

    public static boolean isValidURI(URI uri) {
        String scheme;
        try {
            scheme = uri.getScheme().toLowerCase();
        } catch (NullPointerException ex) {
            logger.warn("Check if Url {} is proper. No schema found", uri.toString());
            return false;
        }
        if (StringUtils.equals(scheme, FILE_PROTOCOL_SCHEME)) {
            File f = new File(uri);
            if (f.exists()) {
                return true;
            }
        }

        if (scheme.startsWith(HTTP_PROTOCOL_SCHEME) || scheme.startsWith(FTP_PROTOCOL_SCHEME)) {
            InputStream istr = null;
            try {

                URLConnection conn = uri.toURL().openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                istr = conn.getInputStream();
                if (istr == null) {
                    return false;
                }
                final int bytesRead = istr.read(new byte[1]);
                return (bytesRead != -1);

            } catch (MalformedURLException ex) {
                logger.warn("Malformed Invalid uri: {}", uri.toString());
            } catch (IOException ex) {
                //do nothing
            } finally {
                GeneralUtils.safeClose(istr);
            }
        }

        return scheme.startsWith(IGB_PROTOCOL_SCHEME);
    }

    public static boolean isValidRequest(URI uri) throws IOException {
        if (uri.getScheme().equalsIgnoreCase(FILE_PROTOCOL_SCHEME)) {
            File f = new File(uri);
            return f.exists();
        } else {
            int code = -1;
            try {
                code = HttpRequest.get(uri.toURL())
                        .trustAllCerts()
                        .trustAllHosts()
                        .followRedirects(true)
                        .connectTimeout(1000)
                        .code();
            } catch (HttpRequest.HttpRequestException ex) {
            }
            return code == HttpURLConnection.HTTP_OK;
        }
    }

    public static String getReachableUrl(String urlString) {
        if (urlString.startsWith(FILE_PROTOCOL_SCHEME)) {
            File f = new File(urlString);
            if (f.exists()) {
                return urlString;
            }
        }

        if (urlString.startsWith(HTTP_PROTOCOL_SCHEME) || urlString.startsWith(FTP_PROTOCOL_SCHEME)) {
            InputStream istr = null;
            try {

                URLConnection conn = connectToUrl(urlString, -1);
                if (conn instanceof HttpURLConnection) {
                    String reachable_url = urlString;
                    HttpURLConnection hcon = (HttpURLConnection) conn;
                    int http_status = hcon.getResponseCode();

                    //Handle one redirect
                    if (http_status == HTTP_TEMP_REDIRECT) {
                        reachable_url = conn.getHeaderField(HTTP_LOCATION_HEADER);
                        conn = handleTemporaryRedirect(conn, urlString, -1);
                        hcon = (HttpURLConnection) conn;
                        http_status = hcon.getResponseCode();
                    }

                    //  So only consider URL reachable if 2xx or 3xx
                    boolean url_reachable = ((http_status >= 200) && (http_status < 400));

                    if (url_reachable) {
                        return reachable_url;
                    }else{
                        logger.error("URL ({}) cannot be reached HTTP Status {}",urlString,http_status);
                    }
                } else {
                    return urlString;
                }

            } catch (Exception ex) {
                logger.error("URL "+urlString+ "cannot be reached -->"+ex.getMessage(), ex);
            } finally {
                GeneralUtils.safeClose(istr);
            }
        }

        return null;
    }

    public static String httpPost(String urlStr, Map<String, String> params) throws IOException {

        logger.debug("URL :" + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection conn
                = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        String content = sb.toString();

        try ( // Create the form content
                DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.writeBytes(content);
            out.flush();
        }

        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line).append("\n");
        }
        rd.close();

        conn.disconnect();

        return sb.toString();
    }

    public static boolean isURIReachable(URI uri) {
        try {
            if (LocalUrlCacher.getInputStream(uri.toURL()) == null) {
                return false;
            }
        } catch (IOException ex) {
            logger.error(null, ex);
            return false;
        }

        return true;
    }

    public static Optional<String> retrieveFileAsStringFromCache(URL url) {
        String fileName = getCacheFileName(url.toString());
        File file = new File(fileName);
        if (file.exists()) {
            try {
                return Optional.of(CharStreams.toString(new FileReader(file)));
            } catch (FileNotFoundException ex) {
                logger.debug("File {} not found in cache", url);
            } catch (IOException ex) {
                logger.debug("File {} not found in cache", url);
            }
        }
        return Optional.absent();
    }

    public static void writeToCache(URL url, String content) {
        try {
            String fileName = getCacheFileName(url.toString());
            File cacheDir = new File(CACHE_CONTENT_ROOT);
            cacheDir.mkdir();
            Files.write(content, new File(CACHE_CONTENT_ROOT, fileName), Charsets.UTF_8);
        } catch (IOException ex) {
            logger.error("Error writing {} to cache", url, ex);
        }
    }

}
