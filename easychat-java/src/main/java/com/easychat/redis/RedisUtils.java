package com.easychat.redis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component("redisUtils")
public class RedisUtils <v>{
    @Resource
    private RedisTemplate<String,v> redisTemplate;
    private static final Logger logger=  LoggerFactory.getLogger(RedisUtils.class);
    public void delete(String... key){
        if(key !=null && key.length>0){
            redisTemplate.delete(key[0]);
        }else {
            redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
        }
    }
    public  v get(String key){
        return key==null?null:redisTemplate.opsForValue().get(key);
    }
    public boolean set(String key ,v value){
        try {
            redisTemplate.opsForValue().set(key,value);
            return true;
        }catch (Exception e){
            logger.error("设置redisKey：{},value：{}失败",key,value);
            return false;
        }
    }
    public boolean setex(String key ,v value, long time){
        try {
            if (time > 0){
                redisTemplate.opsForValue().set(key, value, time,TimeUnit.SECONDS);
            }else {
                set(key, value);
            }
            return true;
        }catch (Exception e){
            logger.error("设置redisKey：{},value：{}失败",key,value);
            return false;
        }
    }
    public boolean expire(String key,long time){
        try {
            if (time>0){
                redisTemplate.expire(key,time,TimeUnit.SECONDS);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public List<v> getQueueList(String key){
        return redisTemplate.opsForList().range(key,0,-1);
    }
    public boolean lpush(String key,v  value ,long time){
        try {
            redisTemplate.opsForList().leftPush(key, value);
            if (time>0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public long remove(String key,Object value){
        try{
            Long remove=redisTemplate.opsForList().remove(key,1,value);
            return remove;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
    public boolean lpushAll(String key ,List<v> values,long time){
        try {
            redisTemplate.opsForList().leftPushAll(key,values);
            if (time>0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
