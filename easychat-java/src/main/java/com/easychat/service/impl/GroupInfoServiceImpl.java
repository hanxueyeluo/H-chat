package com.easychat.service.impl;
import com.easychat.entity.config.AppConfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.*;
import com.easychat.entity.query.*;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.exception.BusinessException;
import com.easychat.mapper.*;
import com.easychat.redis.RedisComponent;
import com.easychat.service.ContactService;
import com.easychat.service.GroupInfoService;
import com.easychat.service.SessionUserService;
import com.easychat.utils.CopyTools;
import com.easychat.utils.StringTools;
import com.easychat.websocket.ChannelContextUtils;
import com.easychat.websocket.MessageHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
/**
 * @Description 群组Service
 * @author null
 * @Date 2024/07/22
 */
@Service("GroupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private ContactMapper<Contact, ContactQuery> contactMapper;

	@Resource
	private AppConfig appConfig;

	@Resource
	private SessionMapper<Session, SessionQuery> sessionMapper;

	@Resource
	private SessionUserMapper<SessionUser, SessionUserQuery> sessionUserMapper;

	@Resource
	private MessageMapper<Message,MessageQuery> messageMapper;

	@Resource
	private ChannelContextUtils channelContextUtils;

	@Resource
	private MessageHandler messageHandler;

	@Resource
	private SessionUserService sessionUserService;

	@Resource
	private ContactService contactService;

	@Resource
	private InfoMapper<Info,InfoQuery> infoMapper;

	@Resource
	@Lazy
	private GroupInfoService groupInfoService;


	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<GroupInfo> findListByParam(GroupInfoQuery query){
		return this.groupInfoMapper.selectList(query);
	}
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(GroupInfoQuery query){
		return this.groupInfoMapper.selectCount(query);
	}
	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery query){
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize()==null? PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<GroupInfo> list = this.findListByParam(query);
		PaginationResultVO<GroupInfo> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	/**
	 * 新增
	 */
	@Override
	public Integer add(GroupInfoQuery bean){
		return this.groupInfoMapper.insert(bean);
	}
	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<GroupInfoQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.groupInfoMapper.insertBatch(listBean);
	}
	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<GroupInfoQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.groupInfoMapper.insertOrUpdateBatch(listBean);
	}
	/**
	 * 根据GroupId查询
	 */
	@Override
	public GroupInfo getGroupInfoByGroupId(String groupId){
		return this.groupInfoMapper.selectByGroupId(groupId);
	}
	/**
	 * 根据GroupId更新
	 */
	@Override
	public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId){
		return this.groupInfoMapper.updateByGroupId(bean,groupId);
	}
	/**
	 * 根据GroupId删除
	 */
	@Override
	public Integer deleteGroupInfoByGroupId(String groupId){
		return this.groupInfoMapper.deleteByGroupId(groupId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws BusinessException, IOException {

		Date curDate=new Date();

		//新增
		if (StringTools.isEmpty(groupInfo.getGroupId())) {
			GroupInfoQuery groupInfoQuery=new GroupInfoQuery();
			groupInfoQuery.setGroupOwnerId(groupInfo.getGroupOwnerId());
			Integer count=this.groupInfoMapper.selectCount(groupInfoQuery);
			SysSettingDto sysSettingDto= redisComponent.getSysSetting();
			if (count > sysSettingDto.getMaxGroupCount()) {
				throw new BusinessException("最多只能创建"+sysSettingDto.getMaxGroupCount()+"个群聊");
			}
			if (avatarFile == null) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}

			groupInfo.setCreateTime(curDate);
			groupInfo.setGroupId(StringTools.getGroupId());
			this.groupInfoMapper.insert(groupInfo);

			//将群组添加联系人
			Contact contact=new Contact();
			contact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			contact.setContactType(UserContactTypeEnum.GROUP.getType());
			contact.setContactId(groupInfo.getGroupId());
			contact.setUserId(groupInfo.getGroupOwnerId());
			contact.setCreateTime(curDate);
			contact.setLastUpdateTime(curDate);
			this.contactMapper.insert(contact);

			//创建会话
			String sessionId=StringTools.getChatSessionId4Group(groupInfo.getGroupId());
			Session chatSession=new Session();
			chatSession.setSessionId(sessionId);
			chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
			chatSession.setLastReceiveTime(curDate.getTime());
			this.sessionMapper.insertOrUpdate(chatSession);

			SessionUser sessionUser=new SessionUser();
			sessionUser.setUserId(groupInfo.getGroupOwnerId());
			sessionUser.setContactId(groupInfo.getGroupId());
			sessionUser.setContactName(groupInfo.getGroupName());
			sessionUser.setSessionId(sessionId);
			this.sessionUserMapper.insert(sessionUser);

			//创建消息
			Message chatMessage=new Message();
			chatMessage.setSessionId(sessionId);
			chatMessage.setType(MessageTypeEnum.GROUP_CREATE.getType());
			chatMessage.setContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
			chatMessage.setSendTime(curDate.getTime());
			chatMessage.setContactId(groupInfo.getGroupId());
			chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
			chatMessage.setStatus(MessageStatusEnum.SEND.getStatus());
			messageMapper.insert(chatMessage);

			//将群组添加到联系人
			redisComponent.addUserContact(groupInfo.getGroupOwnerId(),groupInfo.getGroupId());

			//将联系人通道添加到群组通道
			channelContextUtils.addUser2Group(groupInfo.getGroupOwnerId(),groupInfo.getGroupId());
			//发送ws消息
			sessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
			sessionUser.setLastReceiveTime(curDate.getTime());
			sessionUser.setMemberCount(1);

			MessageSendDto messageSendDto= CopyTools.copy(chatMessage, MessageSendDto.class);
			messageSendDto.setExtendData(sessionUser);
			messageSendDto.setLastMessage(sessionUser.getLastMessage());
			messageHandler.sendMessage(messageSendDto);
		}else {
			GroupInfo dbInfo=this.groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
			if (!dbInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
			this.groupInfoMapper.updateByGroupId(groupInfo,groupInfo.getGroupId());

			String contactNameUpdate=null;
			if (!dbInfo.getGroupName().equals(groupInfo.getGroupName())) {
				contactNameUpdate=groupInfo.getGroupName();
			}
			if (contactNameUpdate == null) {
				return;
			}
			sessionUserService.updateRedundancyInfo(contactNameUpdate,groupInfo.getGroupId());
		}
		if (avatarFile == null) {
			return;
		}
		String baseFolder=appConfig.getProjectFolder()+ Constants.FILE_FOLDER_FILE;
		File targetFileFolder=new File(baseFolder+Constants.FILE_FOLDER_AVATAR_NAME);
		if (!targetFileFolder.exists()) {
			targetFileFolder.mkdirs();
		}
		String filePath=targetFileFolder.getPath()+"/"+groupInfo.getGroupId()+Constants.IMAGE_SUFFIX;
		avatarFile.transferTo(new File(filePath));
		avatarCover.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void dissolutionGroup(String groupOwnerId, String groupId) throws BusinessException {
		GroupInfo dbInfo=this.groupInfoMapper.selectByGroupId(groupId);
		if (dbInfo == null||!dbInfo.getGroupOwnerId().equals(groupOwnerId)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//删除群组
		GroupInfo updateInfo=new GroupInfo();
		updateInfo.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
		this.groupInfoMapper.updateByGroupId(updateInfo,groupId);

		//更新联系人信息
		ContactQuery contactQuery=new ContactQuery();
		contactQuery.setContactId(groupId);
		contactQuery.setContactType(UserContactTypeEnum.GROUP.getType());

		Contact updateUserContact=new Contact();
		updateUserContact.setStatus(UserContactStatusEnum.DEL.getStatus());
		this.contactMapper.updateByParam(updateUserContact,contactQuery);


		List<Contact> userContactList=this.contactMapper.selectList(contactQuery);
		for (Contact userContact:userContactList){
			redisComponent.removeUserContact(userContact.getUserId(), userContact.getContactId());
		}

		String sessionId=StringTools.getChatSessionId4Group(groupId);
		Date curDate=new Date();
		String messageContent=MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage();
		Session chatSession=new Session();
		chatSession.setLastMessage(messageContent);
		chatSession.setLastReceiveTime(curDate.getTime());
		sessionMapper.updateBySessionId(chatSession,sessionId);

		Message chatMessage=new Message();
		chatMessage.setSessionId(sessionId);
		chatMessage.setSendTime(curDate.getTime());
		chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
		chatMessage.setStatus(MessageStatusEnum.SEND.getStatus());
		chatMessage.setType(MessageTypeEnum.DISSOLUTION_GROUP.getType());
		chatMessage.setContactId(groupId);
		chatMessage.setContent(messageContent);
		messageMapper.insert(chatMessage);

		MessageSendDto messageSendDto=CopyTools.copy(chatMessage, MessageSendDto.class);
		messageHandler.sendMessage(messageSendDto);
	}

	@Override
	public void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String selectContacts, Integer opType) throws BusinessException {
		GroupInfo groupInfo=groupInfoMapper.selectByGroupId(groupId);
		if (groupInfo == null||!groupInfo.getGroupOwnerId().equals(tokenUserInfoDto.getUserId())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		String[] contactIdList=selectContacts.split(",");
		for (String contactId:contactIdList){
			if (Constants.ZERO.equals(opType)) {
				groupInfoService.leaveGroup(contactId,groupId,MessageTypeEnum.REMOVE_GROUP);
			}else {
				contactService.addContact(contactId,null,groupId,UserContactTypeEnum.GROUP.getType(), null);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum) throws BusinessException {
		GroupInfo groupInfo=groupInfoMapper.selectByGroupId(groupId);
		if (groupInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (userId.equals(groupInfo.getGroupOwnerId())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		Integer count=contactMapper.deleteByUserIdAndContactId(userId,groupId);
		if (count == 0) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		Info userInfo=infoMapper.selectByUserId(userId);
		String sessionId=StringTools.getChatSessionId4Group(groupId);
		Date curDate=new Date();
		String messageContent=String.format(messageTypeEnum.getInitMessage(),userInfo.getNickName());

		Session chatSession=new Session();
		chatSession.setLastMessage(messageContent);
		chatSession.setLastReceiveTime(curDate.getTime());
		sessionMapper.updateBySessionId(chatSession,sessionId);

		Message chatMessage=new Message();
		chatMessage.setSessionId(sessionId);
		chatMessage.setSendTime(curDate.getTime());
		chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
		chatMessage.setStatus(MessageStatusEnum.SEND.getStatus());
		chatMessage.setType(messageTypeEnum.getType());
		chatMessage.setContactId(groupId);
		chatMessage.setContent(messageContent);
		messageMapper.insert(chatMessage);

		ContactQuery contactQuery=new ContactQuery();
		contactQuery.setContactId(groupId);
		contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		Integer memberCount=this.contactMapper.selectCount(contactQuery);
		MessageSendDto messageSendDto=CopyTools.copy(chatMessage, MessageSendDto.class);
		messageSendDto.setExtendData(userId);
		messageSendDto.setMemberCount(memberCount);
		messageHandler.sendMessage(messageSendDto);

	}
}