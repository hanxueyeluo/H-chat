package com.easychat.service.impl;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.dto.UserContactSearchResultDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.*;
import com.easychat.entity.query.*;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.exception.BusinessException;
import com.easychat.mapper.*;
import com.easychat.redis.RedisComponent;
import com.easychat.service.ContactService;
import com.easychat.utils.CopyTools;
import com.easychat.utils.StringTools;
import com.easychat.websocket.ChannelContextUtils;
import com.easychat.websocket.MessageHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * @Description 联系人Service
 * @author null
 * @Date 2024/07/22
 */
@Service("ContactService")
public class ContactServiceImpl implements ContactService{

	@Resource
	private ContactMapper<Contact,ContactQuery> contactMapper;

	@Resource
	private InfoMapper<Info, InfoQuery> infoMapper;

	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private SessionMapper<Session,SessionQuery> sessionMapper;

	@Resource
	private SessionUserMapper<SessionUser,SessionUserQuery> sessionUserMapper;

	@Resource
	private MessageMapper<Message,MessageQuery> messageMapper;

	@Resource
	private MessageHandler messageHandler;

	@Resource
	private ChannelContextUtils channelContextUtils;




	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Contact> findListByParam(ContactQuery query){
		return this.contactMapper.selectList(query);
	}
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(ContactQuery query){
		return this.contactMapper.selectCount(query);
	}
	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<Contact> findListByPage(ContactQuery query){
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize()==null? PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<Contact> list = this.findListByParam(query);
		PaginationResultVO<Contact> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	/**
	 * 新增
	 */
	@Override
	public Integer add(ContactQuery bean){
		return this.contactMapper.insert(bean);
	}
	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ContactQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.contactMapper.insertBatch(listBean);
	}
	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ContactQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.contactMapper.insertOrUpdateBatch(listBean);
	}
	/**
	 * 根据UserIdAndContactId查询
	 */
	@Override
	public Contact getContactByUserIdAndContactId(String userId, String contactId){
		return this.contactMapper.selectByUserIdAndContactId(userId, contactId);
	}
	/**
	 * 根据UserIdAndContactId更新
	 */
	@Override
	public Integer updateContactByUserIdAndContactId(Contact bean, String userId, String contactId){
		return this.contactMapper.updateByUserIdAndContactId(bean,userId, contactId);
	}
	/**
	 * 根据UserIdAndContactId删除
	 */
	@Override
	public Integer deleteContactByUserIdAndContactId(String userId, String contactId){
		return this.contactMapper.deleteByUserIdAndContactId(userId, contactId);
	}

	@Override
	public UserContactSearchResultDto searchContact(String userId, String contactId) {
		UserContactTypeEnum typeEnum=UserContactTypeEnum.getByPrefix(contactId);
		if (typeEnum == null) {
			return null;
		}
		UserContactSearchResultDto resultDto=new UserContactSearchResultDto();
		switch (typeEnum){
			case USER:
				Info info=infoMapper.selectByUserId(contactId);
				if (info == null) {
					return null;
				}
				resultDto= CopyTools.copy(info, UserContactSearchResultDto.class);
				break;
			case GROUP:
				GroupInfo groupInfo=groupInfoMapper.selectByGroupId(contactId);
				if (groupInfo == null) {
					return null;
				}
				resultDto.setNickName(groupInfo.getGroupName());
				break;
		}

		resultDto.setContactType(typeEnum.toString());
		resultDto.setContactId(contactId);

		if (userId.equals(contactId)){
			resultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			return resultDto;
		}
		//是否查询好友
		Contact contact=this.contactMapper.selectByUserIdAndContactId(userId,contactId);
		resultDto.setStatus(contact==null?null:contact.getStatus());
		return resultDto;
	}



	@Override
	public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) throws BusinessException {
		//群聊人数
		if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
			ContactQuery contactQuery=new ContactQuery();
			contactQuery.setContactId(contactId);
			contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			Integer count=contactMapper.selectCount(contactQuery);
			SysSettingDto sysSettingDto=redisComponent.getSysSetting();
			if (count>=sysSettingDto.getMaxGroupMemberCount()) {
				throw new BusinessException("成员已满，无法加入");
			}
		}
		Date curDate=new Date();
		//同意，双方添加好友
		List<Contact> contactLise=new ArrayList<>();
		//申请人添加对方
		Contact contact=new Contact();
		contact.setUserId(applyUserId);
		contact.setContactId(contactId);
		contact.setContactType(contactType);
		contact.setCreateTime(curDate);
		contact.setLastUpdateTime(curDate);
		contact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		contactLise.add(contact);
		//如果是申请好友，接收人添加申请人，群组不用添加对方为好友
		if (UserContactTypeEnum.USER.getType().equals(contactType)) {
			contact=new Contact();
			contact.setUserId(receiveUserId);
			contact.setContactId(applyUserId);
			contact.setContactType(contactType);
			contact.setCreateTime(curDate);
			contact.setLastUpdateTime(curDate);
			contact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			contactLise.add(contact);
		}
		//批量插入
		contactMapper.insertOrUpdateBatch(contactLise);

		if (UserContactTypeEnum.USER.getType().equals(contactType)) {
			redisComponent.addUserContact(receiveUserId,applyUserId);
		}
		redisComponent.addUserContact(applyUserId,contactId);

		//创建会话
		String sessionId=null;
		if (UserContactTypeEnum.USER.getType().equals(contactType)) {
			sessionId=StringTools.getChatSessionId4User(new String[]{applyInfo,contactId});
		}else {
			sessionId=StringTools.getChatSessionId4Group(contactId);
		}
		List<SessionUser> chatSessionUserList=new ArrayList<>();
		if (UserContactTypeEnum.USER.getType().equals(contactType)) {
			Session chatSession=new Session();
			chatSession.setSessionId(sessionId);
			chatSession.setLastMessage(applyInfo);
			chatSession.setLastReceiveTime(curDate.getTime());
			this.sessionMapper.insertOrUpdate(chatSession);

			//申请人session
			SessionUser applySessionUser=new SessionUser();
			applySessionUser.setUserId(applyUserId);
			applySessionUser.setContactId(contactId);
			applySessionUser.setSessionId(sessionId);
			Info contactUser=this.infoMapper.selectByUserId(contactId);
			applySessionUser.setContactName(contactUser.getNickName());
			chatSessionUserList.add(applySessionUser);

			//接收人 session
			SessionUser sessionUser=new SessionUser();
			sessionUser.setUserId(contactId);
			sessionUser.setContactId(applyUserId);
			sessionUser.setSessionId(sessionId);
			Info applyUserInfo=this.infoMapper.selectByUserId(applyUserId);
			sessionUser.setContactName(applyUserInfo.getNickName());
			chatSessionUserList.add(sessionUser);

			this.sessionUserMapper.insertOrUpdateBatch(chatSessionUserList);

			//记录消息表
			Message chatMessage=new Message();
			chatMessage.setSessionId(sessionId);
			chatMessage.setType(MessageTypeEnum.ADD_FRIEND.getType());
			chatMessage.setContent(applyInfo);
			chatMessage.setSendUserId(applyUserId);
			chatMessage.setSendUserNickName(applyUserInfo.getNickName());
			chatMessage.setSendTime(curDate.getTime());
			chatMessage.setContactId(contactId);
			chatMessage.setContactType(UserContactTypeEnum.USER.getType());
			messageMapper.insert(chatMessage);

			MessageSendDto messageSendDto=CopyTools.copy(chatMessage, MessageSendDto.class);
			//发送给接收还有申请人
			messageHandler.sendMessage(messageSendDto);

			//发送给申请人，发送人就是接收人，联系人就是申请人
			messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
			messageSendDto.setContactId(applyUserId);
			messageSendDto.setExtendData(contactUser);
			messageHandler.sendMessage(messageSendDto);
		}else {
			//加入群组
			SessionUser chatSessionUser=new SessionUser();
			chatSessionUser.setUserId(applyUserId);
			chatSessionUser.setContactId(contactId);
			GroupInfo groupInfo=this.groupInfoMapper.selectByGroupId(contactId);
			chatSessionUser.setContactName(groupInfo.getGroupName());
			chatSessionUser.setSessionId(sessionId);
			this.sessionUserMapper.insertOrUpdate(chatSessionUser);

			Info applyUserInfo=this.infoMapper.selectByUserId(applyUserId);
			String sendMessage=String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(),applyUserInfo.getNickName());
			//增加session信息
			Session chatSession=new Session();
			chatSession.setSessionId(sessionId);
			chatSession.setLastReceiveTime(curDate.getTime());
			chatSession.setLastMessage(sendMessage);
			this.sessionMapper.insertOrUpdate(chatSession);
			//增加聊天消息
			Message chatMessage=new Message();
			chatMessage.setSessionId(sessionId);
			chatMessage.setType(MessageTypeEnum.ADD_GROUP.getType());
			chatMessage.setContent(sendMessage);
			chatMessage.setSendTime(curDate.getTime());
			chatMessage.setContactId(contactId);
			chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
			chatMessage.setStatus(MessageStatusEnum.SEND.getStatus());
			this.messageMapper.insert(chatMessage);
			//将群组添加到联系人
			redisComponent.addUserContact(applyUserId,groupInfo.getGroupId());
			//将联系人通道添加群组通道
			channelContextUtils.addUser2Group(applyUserId,groupInfo.getGroupId());
			//发送群消息
			MessageSendDto messageSendDto=CopyTools.copy(chatMessage,MessageSendDto.class);
			messageSendDto.setContactId(contactId);
			//获取群成员数量
			ContactQuery userContactQuery=new ContactQuery();
			userContactQuery.setContactId(contactId);
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			Integer memberCount=this.contactMapper.selectCount(userContactQuery);
			messageSendDto.setMemberCount(memberCount);
			messageSendDto.setContactName(groupInfo.getGroupName());
			//发消息
			messageHandler.sendMessage(messageSendDto);

		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum) {
		//移除好友
		Contact contact=new Contact();
		contact.setStatus(statusEnum.getStatus());
		contactMapper.updateByUserIdAndContactId(contact,userId,contactId);
		//将好友中也移除自己
		Contact friendContact=new Contact();
		if (UserContactStatusEnum.DEL==statusEnum) {
			friendContact.setStatus(UserContactStatusEnum.DEL_BE.getStatus());
		} else if (UserContactStatusEnum.BLACKLIST==statusEnum) {
			friendContact.setStatus(UserContactStatusEnum.BLACKLIST_BE.getStatus());
		}
		contactMapper.updateByUserIdAndContactId(friendContact,contactId,userId);

		redisComponent.removeUserContact(contactId,userId);
		redisComponent.removeUserContact(userId, contactId);


	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addContact4Robot(String userId) {
		Date curDate=new Date();
		SysSettingDto sysSettingDto= redisComponent.getSysSetting();
		String contactId=sysSettingDto.getRobotUid();
		String contactName=sysSettingDto.getRobotNickName();
		String sendMessage=sysSettingDto.getRobotWelCome();
		sendMessage=StringTools.cleanHtmlTag(sendMessage);
		//增加机器人好友
		Contact userContact=new Contact();
		userContact.setUserId(userId);
		userContact.setContactId(contactId);
		userContact.setContactType(UserContactTypeEnum.USER.getType());
		userContact.setCreateTime(curDate);
		userContact.setLastUpdateTime(curDate);
		userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		contactMapper.insert(userContact);
		//增加会话信息
		String sessionId=StringTools.getChatSessionId4User(new String[] {userId,contactId});
		Session chatSession=new Session();
		chatSession.setLastMessage(sendMessage);
		chatSession.setSessionId(sessionId);
		chatSession.setLastReceiveTime(curDate.getTime());
		this.sessionMapper.insert(chatSession);

		//增加会话人信息
		SessionUser sessionUser=new SessionUser();
		sessionUser.setUserId(userId);
		sessionUser.setContactId(contactId);
		sessionUser.setContactName(contactName);
		sessionUser.setSessionId(sessionId);
		this.sessionUserMapper.insert(sessionUser);

		//增加聊天消息
		Message chatMessage=new Message();
		chatMessage.setSessionId(sessionId);
		chatMessage.setType(MessageTypeEnum.CHAT.getType());
		chatMessage.setContent(sendMessage);
		chatMessage.setSendUserId(contactId);
		chatMessage.setSendUserNickName(contactName);
		chatMessage.setSendTime(curDate.getTime());
		chatMessage.setContactId(userId);
		chatMessage.setContactType(UserContactTypeEnum.USER.getType());
		chatMessage.setStatus(MessageStatusEnum.SEND.getStatus());
		this.messageMapper.insert(chatMessage);

	}
}