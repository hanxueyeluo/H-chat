package com.easychat.websocket;



import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.WsInitData;
import com.easychat.entity.enums.MessageTypeEnum;
import com.easychat.entity.enums.UserContactApplyStatusEnum;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.enums.UserContactTypeEnum;
import com.easychat.entity.po.*;
import com.easychat.entity.query.ContactApplyQuery;
import com.easychat.entity.query.InfoQuery;
import com.easychat.entity.query.MessageQuery;
import com.easychat.entity.query.SessionUserQuery;
import com.easychat.mapper.ContactApplyMapper;
import com.easychat.mapper.InfoMapper;
import com.easychat.mapper.MessageMapper;
import com.easychat.mapper.SessionUserMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.service.SessionUserService;
import com.easychat.utils.JsonUtils;
import com.easychat.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Component
public class ChannelContextUtils {

    private static final Logger logger= LoggerFactory.getLogger(ChannelContextUtils.class);

    private static final ConcurrentHashMap<String,Channel> USER_CONTEXT_MAP=new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP=new ConcurrentHashMap<>();

    @Resource
    private InfoMapper<Info, InfoQuery> infoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private SessionUserMapper<SessionUser,SessionUserQuery> sessionUserMapper;

    @Resource
    private MessageMapper<Message,MessageQuery> messageMapper;

    @Resource
    private ContactApplyMapper<ContactApply,ContactApplyQuery> contactApplyMapper;

        public void addContext(String userId, Channel channel){
            String channelId=channel.id().toString();
            logger.info("channelId:{}",channelId);
            AttributeKey attributeKey=null;
            if (!attributeKey.exists(channelId)) {
                attributeKey=AttributeKey.newInstance(channelId);
            }else {
                attributeKey=AttributeKey.valueOf(channelId);
            }

            channel.attr(attributeKey).set(userId);

            List<String> contactIdList=redisComponent.getUserContactList(userId);
            for (String groupId:contactIdList){
                if (groupId.startsWith(UserContactTypeEnum.GROUP.GROUP.getPrefix())) {
                    add2Group(groupId,channel);
                }
            }

            USER_CONTEXT_MAP.put(userId, channel);
            redisComponent.saveHeartBeat(userId);

            //更新用户最后连接时间
            Info updateInfo=new Info();
            updateInfo.setLastLoginTime(new Date());
            infoMapper.updateByUserId(updateInfo,userId);

            //给用户发消息
            Info userInfo=new Info();
            Long sourceLastOffTime=userInfo.getLastOffTime();
            Long lastOffTime=sourceLastOffTime;
            if (sourceLastOffTime != null&&System.currentTimeMillis()- Constants.MILLISSECONDS_3DAYS_AGO>sourceLastOffTime) {
                lastOffTime=Constants.MILLISSECONDS_3DAYS_AGO;
            }

            /**
             * 查询会话信息 查询用户的所有会话信息，保证换了设备会话同步
             */
            SessionUserQuery sessionUserQuery=new SessionUserQuery();
            sessionUserQuery.setUserId(userId);
            sessionUserQuery.setOrderBy("last_receive_time desc");
            List<SessionUser> sessionUserList=sessionUserMapper.selectList(sessionUserQuery);

            WsInitData wsInitData=new WsInitData();
            wsInitData.setChatSessionList(sessionUserList);

            /**
             * 查询聊天消息
             */
            //查询所有联系人
            List<String> groupIdList=contactIdList.stream().filter(item->item.startsWith(UserContactTypeEnum.GROUP.GROUP.getPrefix())).collect(Collectors.toList());
            groupIdList.add(userId);
            MessageQuery messageQuery=new MessageQuery();
            messageQuery.setContactIdList(groupIdList);
            messageQuery.setLastReceiveTime(lastOffTime);
            List<Message> chatMessageList=messageMapper.selectList(messageQuery);
            wsInitData.setChatMessageList(chatMessageList);
            /**
             * 查询好友申请
             */
            ContactApplyQuery applyQuery=new ContactApplyQuery();
            applyQuery.setReceiveUserId(userId);
            applyQuery.setLastApplyTimestamp(lastOffTime);
            applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            Integer applyCount=contactApplyMapper.selectCount(applyQuery);
            wsInitData.setApplyCount(applyCount);
            //发送消息
            MessageSendDto messageSendDto=new MessageSendDto();
            messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
            messageSendDto.setContactId(userId);
            messageSendDto.setExtendData(wsInitData);
            sendMsg(messageSendDto,userId);
        }
        public void addUser2Group(String userId,String groupId){
            Channel channel=USER_CONTEXT_MAP.get(userId);
        }
        public void add2Group(String groupId,Channel channel){
            ChannelGroup group=GROUP_CONTEXT_MAP.get(groupId);
            if (group == null) {
                group=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                GROUP_CONTEXT_MAP.put(groupId,group);
            }
            if (channel == null) {
                return;
            }
            group.add(channel);
        }
        public void removeContext(Channel channel){
            Attribute<String> attribute=channel.attr(AttributeKey.valueOf(channel.id().toString()));
            String userId= attribute.get();
            if (!StringTools.isEmpty(userId)){
                USER_CONTEXT_MAP.remove(userId);
            }
            redisComponent.removeUserHeartBeat(userId);
            //更新用户最后离线时间
            Info userInfo=new Info();
            userInfo.setLastOffTime(System.currentTimeMillis());
            infoMapper.updateByUserId(userInfo,userId);
        }

        public void sendMessage(MessageSendDto messageSendDto){
            UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByPrefix(messageSendDto.getContactId());
            switch (contactTypeEnum){
                case USER:
                    send2User(messageSendDto);
                    break;
                case GROUP:
                    send2Group(messageSendDto);
                    break;
            }
        }

        //发送给用户
        private void send2User(MessageSendDto messageSendDto){
            String contactId=messageSendDto.getContactId();
            if (StringTools.isEmpty(contactId)) {
                return;
            }
            sendMsg(messageSendDto,contactId);
            //强制下线
            if (MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDto.getMessageType())) {
                closeContext(contactId);
            }
        }

        public void closeContext(String userId){
            if (StringTools.isEmpty(userId)) {
                return;
            }
            redisComponent.cleanUserTokenByUserId(userId);
            Channel channel=USER_CONTEXT_MAP.get(userId);
            if (channel == null) {
                return;
            }
            channel.close();
        }

        //发送给群组
        private void send2Group(MessageSendDto messageSendDto){
            if (StringTools.isEmpty(messageSendDto.getContactId())) {
                return;
            }
            ChannelGroup channelGroup=GROUP_CONTEXT_MAP.get(messageSendDto.getContactId());
            if (channelGroup == null) {
                return;
            }
            channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));

            //移出群聊
            MessageTypeEnum messageTypeEnum=MessageTypeEnum.getByType(messageSendDto.getMessageType());
            if (MessageTypeEnum.LEAVE_GROUP == messageTypeEnum||MessageTypeEnum.REMOVE_GROUP==messageTypeEnum) {
                String userId=(String) messageSendDto.getExtendData();
                redisComponent.removeUserContact(userId,messageSendDto.getContactId());
                Channel channel=USER_CONTEXT_MAP.get(userId);
                if (channel == null) {
                    return;
                }
                channelGroup.remove(channel);
            }
            if (MessageTypeEnum.DISSOLUTION_GROUP== messageTypeEnum) {
                GROUP_CONTEXT_MAP.remove(messageSendDto.getContactId());
                channelGroup.close();
            }
        }

    //发送消息
    public void sendMsg(MessageSendDto messageSendDto,String receiveId){
        Channel userChannel=USER_CONTEXT_MAP.get(receiveId);
        if (userChannel == null) {
            return;
        }
        //相对于客户端而言，联系人就是发送人，所以这里要转一下再发送
        if (MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDto.getMessageType())) {
            Info userInfo=(Info) messageSendDto.getExtendData();
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            messageSendDto.setContactId(userInfo.getUserId());
            messageSendDto.setContactName(userInfo.getNickName());
            messageSendDto.setExtendData(null);
        }else{
            messageSendDto.setContactId(messageSendDto.getSendUserId());
            messageSendDto.setContactName(messageSendDto.getSendNickName());
        }
        userChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));
    }

}
