package com.wm.guava;


import com.google.common.cache.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * Created by wushunxin on 16/3/2.
 */
public class GuavaCacheTest {

    @Ignore
    public void testLoadingCache() throws Exception{

        LoadingCache<String,String> cacheBuilder = CacheBuilder.newBuilder().build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                System.out.println("+++++++BBB");
                return "hell0,"+key;
            }
        });


        System.out.println("jerry value:"+ cacheBuilder.get("jerry"));
        System.out.println("jerry value:" + cacheBuilder.get("jerry"));
        System.out.println("peida value:" + cacheBuilder.get("peida"));
        System.out.println("lisa value:" + cacheBuilder.get("lisa"));
        System.out.println("peida value:" + cacheBuilder.get("peida"));
        cacheBuilder.put("harry", "ssdded");
        System.out.println("harry value:" + cacheBuilder.get("harry"));
    }

    @Ignore
    public void testCallableCache() throws ExecutionException {
        Cache<String,String> cache = CacheBuilder.newBuilder().maximumSize(1000l).build();
        String resultVal = cache.get("jerry", () -> {
            System.out.println("+++++++ccc");
            return "Hello,Jerry";
        });

        System.out.println("jerry value : " + resultVal);

        resultVal = cache.get("peida", () -> {
            System.out.println("+++++++ddd");
            return "Hello,peida";
        });

        System.out.println("peida value : "+resultVal);
    }

    /**
     * 不需要延迟处理
     * @param cacheLoader
     * @param <K>
     * @param <V>
     * @return
     * cache的参数说明：

    　　回收的参数：
    　　1. 大小的设置：CacheBuilder.maximumSize(long)  CacheBuilder.weigher(Weigher)  CacheBuilder.maxumumWeigher(long)
    　　2. 时间：expireAfterAccess(long, TimeUnit) expireAfterWrite(long, TimeUnit)
    　　3. 引用：CacheBuilder.weakKeys() CacheBuilder.weakValues()  CacheBuilder.softValues()
    　　4. 明确的删除：invalidate(key)  invalidateAll(keys)  invalidateAll()
    　　5. 删除监听器：CacheBuilder.removalListener(RemovalListener)


       refresh机制：
    　　1. LoadingCache.refresh(K)  在生成新的value的时候，旧的value依然会被使用。
    　　2. CacheLoader.reload(K, V) 生成新的value过程中允许使用旧的value
    　　3. CacheBuilder.refreshAfterWrite(long, TimeUnit) 自动刷新cache
     */

    public <K,V> LoadingCache<K,V> cached(CacheLoader<K,V> cacheLoader){
        LoadingCache<K,V> cache =  CacheBuilder
                                    .newBuilder()
                                    .maximumSize(3)
                                    .weakKeys()
                                    .softValues()
                                    .refreshAfterWrite(129, TimeUnit.SECONDS)
                                    .expireAfterWrite(3, TimeUnit.SECONDS)
                                    .removalListener(rn -> System.out.println(rn.getKey() + "被移除"))
                                    .build(cacheLoader);
        return cache;
    }

    public LoadingCache<String,String> commonCache(final String key){
        LoadingCache<String,String> commonCache = cached(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return "hello,"+key+"!";
            }
        });

        return commonCache;
    }

    @Ignore
    public void testCache() throws ExecutionException {
        LoadingCache<String,String>  commonCache = commonCache("peida");
        System.out.println("peida:"+commonCache.get("peida"));
//        LoadingCache<String,String>  commonCache1 = commonCache("Lisa");
//        System.out.println("Lisa:"+commonCache1.get("Lisa"));
//        LoadingCache<String,String>  commonCache2 = commonCache("Allen");
//        System.out.println("Allen:"+commonCache2.get("Allen"));
        commonCache.apply("Lisa");
        System.out.println("Lisa:"+commonCache.get("Lisa"));
//        LoadingCache<String,String>  commonCache2 = commonCache("Allen");
        System.out.println("Allen:"+commonCache.get("Allen"));
        System.out.println("peida1:"+commonCache.get("peida"));
    }


    private static Cache<String,String> cacheFormCallable = null;

    /**
     * 对需要延迟处理的可以采用这个机制
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> Cache<K,V> callableCached(){
        Cache<K,V> cache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(10,TimeUnit.MINUTES).build();
        return cache;
    }

    public String getCallableCache(final  String userName) throws ExecutionException {
        return cacheFormCallable.get(userName, new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println(userName + " from db");
                return "hello,"+userName + "!";
            }
        });
    }
    @Test
    public void testCallableCache2() throws ExecutionException {
        cacheFormCallable = callableCached();
        System.out.println("peide:"+getCallableCache("peide"));
        System.out.println("Harry:"+getCallableCache("Harry"));
        System.out.println("Lisa:"+getCallableCache("Lisa"));
        System.out.println("peide:"+getCallableCache("peide"));

    }

}
