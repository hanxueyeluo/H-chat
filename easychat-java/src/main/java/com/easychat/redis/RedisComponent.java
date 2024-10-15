package com.easychat.redis;

import cn.hutool.extra.tokenizer.TokenizerEngine;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.utils.StringTools;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component("RedisComponent")
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取心跳
     * @param userId
     * @return
     */
    public Long getUserHeartBeat(String userId){
        return (Long) redisUtils.get(Constants.REDIS_KEY_WS_USER_HEART_BEAT+userId);
    }

    public void saveHeartBeat(String userId){
        redisUtils.setex(Constants.REDIS_KEY_WS_USER_HEART_BEAT+userId,System.currentTimeMillis(),Constants.REDIS_KEY_EXPIRES_HEART_BEAT);
    }

    public void removeUserHeartBeat(String userId){
        redisUtils.delete(Constants.REDIS_KEY_WS_USER_HEART_BEAT+userId);
    }

    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto){
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN+tokenUserInfoDto.getToken(),tokenUserInfoDto,Constants.REDIS_KEY_EXPIRES_DAY*2);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID+tokenUserInfoDto.getUserId(),tokenUserInfoDto.getToken(),Constants.REDIS_KEY_EXPIRES_DAY*2);
    }

    public TokenUserInfoDto getTokenUserInfoDto(String token){
        TokenUserInfoDto tokenUserInfoDto=(TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+token);
        return tokenUserInfoDto;
    }

    public TokenUserInfoDto getTokenUserInfoDtoByUser(String userId){
        String token=(String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID+userId);
        return getTokenUserInfoDto(token);
    }

    public SysSettingDto getSysSetting(){
        SysSettingDto sysSettingDto= (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto=sysSettingDto==null?new SysSettingDto():sysSettingDto;
        return sysSettingDto;
    }

    public void saveSysSetting(SysSettingDto sysSettingDto){
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingDto);
    }
    //清空联系人
    public void cleanUserContact(String userId){
        redisUtils.delete(Constants.REDIS_KEY_USER_CONTACT+userId);
    }

    //批量添加联系人
    public void addUserContactBatch(String userId, List<String> contactList){
        redisUtils.lpushAll(Constants.REDIS_KEY_USER_CONTACT+userId,contactList,Constants.REDIS_KEY_TOKEN_EXPIRES);
    }

    public void addUserContact(String userId, String contactId){
        List<String> contactIdList=getUserContactList(userId);
        if (contactIdList.contains(contactId)) {
            return;
        }
        redisUtils.lpush(Constants.REDIS_KEY_USER_CONTACT+userId,contactId,Constants.REDIS_KEY_TOKEN_EXPIRES);
    }

    public List<String> getUserContactList(String userId){
        return (List<String>) redisUtils.getQueueList(Constants.REDIS_KEY_USER_CONTACT+userId);
    }

    public void cleanUserTokenByUserId(String userId){
        String token=(String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID+userId);
        if (StringTools.isEmpty(token)) {
            return;
        }
        redisUtils.delete(Constants.REDIS_KEY_WS_TOKEN+ token);
    }

    public void removeUserContact(String userId,String contactId){
        redisUtils.remove(Constants.REDIS_KEY_USER_CONTACT+userId,contactId);
    }

}
