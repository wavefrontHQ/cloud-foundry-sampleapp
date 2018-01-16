package springboot;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration {

  @Bean
  public CacheManager guavaCacheManager() {
    GuavaCacheManager gCacheManager = new GuavaCacheManager("guaveCacheSearches");
    gCacheManager.setCacheBuilder(
        CacheBuilder.newBuilder()
                    .softValues()
                    .expireAfterWrite(10, TimeUnit.MINUTES));
    return gCacheManager;
  }
}
