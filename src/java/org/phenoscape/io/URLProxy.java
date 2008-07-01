package org.phenoscape.io;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class URLProxy {
  
  private final File cacheFolder;
  public enum CacheOption {
    FORCE_CACHE,
    USE_CACHE,
    NO_CACHE;
  }

  public URLProxy(File cacheLocation) {
    if (!cacheLocation.isDirectory()) {
      throw new IllegalArgumentException("Cache location must be a directory.");
    }
    this.cacheFolder = cacheLocation;
  }
  
//  public boolean isOutOfDate(URL url) {
//    
//  }
  
//  public String get(URL url) {
//    return this.get(url, CacheOption.USE_CACHE);
//  }
  
  public boolean isCached(URL url) {
    return this.getCacheFile(url).exists();
  }
  
//  public String get(URL url, CacheOption option) {
//    
//  }
  
  private String getCacheFileName(URL url) {
    // using HTML form encoding as convenient way to make URL safe for use as filename
    try {
      return URLEncoder.encode(url.toString(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log().error("UTF-8 encoding not supported", e);
      return null;
    }
  }
  
  private File getCacheFile(URL url) {
    return new File(this.cacheFolder, this.getCacheFileName(url));
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
