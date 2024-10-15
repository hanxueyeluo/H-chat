package com.easychat.service.impl;
import cn.hutool.core.date.DateUtil;
import com.easychat.entity.config.AppConfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.Contact;
import com.easychat.entity.po.Session;
import com.easychat.entity.query.ContactQuery;
import com.easychat.entity.query.SessionQuery;
import com.easychat.entity.query.SimplePage;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.Message;
import com.easychat.entity.query.MessageQuery;
import com.easychat.exception.BusinessException;
import com.easychat.mapper.ContactMapper;
import com.easychat.mapper.MessageMapper;
import com.easychat.mapper.SessionMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.service.MessageService;
import com.easychat.utils.CopyTools;
import com.easychat.utils.StringTools;
import com.easychat.websocket.MessageHandler;
import com.mysql.cj.conf.DatabaseUrlContainer;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
/**
 * @Description 聊天消息表Service
 * @author null
 * @Date 2024/09/12
 */
@Service("MessageService")
public class MessageServiceImpl implements MessageService{

	public static final Logger logger= LoggerFactory.getLogger(MessageSendDto.class);

	@Resource
	private MessageMapper<Message,MessageQuery> messageMapper;

	@Resource
	private AppConfig appConfig;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private SessionMapper<Session, SessionQuery> sessionMapper;

	@Resource
	private MessageHandler messageHandler;

	@Resource
	private ContactMapper<Contact,ContactQuery> contactMapper;


	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Message> findListByParam(MessageQuery query){
		return this.messageMapper.selectList(query);
	}
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(MessageQuery query){
		return this.messageMapper.selectCount(query);
	}
	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<Message> findListByPage(MessageQuery query){
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize()==null? PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<Message> list = this.findListByParam(query);
		PaginationResultVO<Message> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	/**
	 * 新增
	 */
	@Override
	public Integer add(MessageQuery bean){
		return this.messageMapper.insert(bean);
	}
	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<MessageQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.messageMapper.insertBatch(listBean);
	}
	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<MessageQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.messageMapper.insertOrUpdateBatch(listBean);
	}
	/**
	 * 根据Id查询
	 */
	@Override
	public Message getMessageById(Long id){
		return this.messageMapper.selectById(id);
	}
	/**
	 * 根据Id更新
	 */
	@Override
	public Integer updateMessageById(Message bean, Long id){
		return this.messageMapper.updateById(bean,id);
	}
	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteMessageById(Long id){
		return this.messageMapper.deleteById(id);
	}

	@Override
	public MessageSendDto saveMessage(Message chatMessage, TokenUserInfoDto tokenUserInfoDto) throws BusinessException {
		//不是机器人，判断好友状态
		if (!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())){
			List<String> contactList=redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
			if (!contactList.contains(chatMessage.getContactId())) {
				UserContactTypeEnum userContactTypeEnum=UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
				if (userContactTypeEnum.USER == userContactTypeEnum) {
					throw new BusinessException(ResponseCodeEnum.CODE_902);
				}else {
					throw new BusinessException(ResponseCodeEnum.CODE_903);
				}
			}
		}

		String sessionId=null;
		String sendUserId=tokenUserInfoDto.getUserId();
		String contactId= chatMessage.getContactId();
		UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByPrefix(contactId);
		if (UserContactTypeEnum.USER == contactTypeEnum) {
			sessionId= StringTools.getChatSessionId4User(new String[]{sendUserId,contactId});
		}else {
			sessionId=StringTools.getChatSessionId4Group(contactId);
		}

		Long curTime=System.currentTimeMillis();
		chatMessage.setSendTime(curTime);


		MessageTypeEnum messageTypeEnum=MessageTypeEnum.getByType(chatMessage.getType());
		if (messageTypeEnum == null|| !ArrayUtils.contains(new Integer[] {MessageTypeEnum.CHAT.getType(),MessageTypeEnum.MEDIA_CHAT.getType()},chatMessage.getType())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		Integer status=MessageTypeEnum.MEDIA_CHAT==messageTypeEnum? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SEND.getStatus();
		chatMessage.setStatus(status);

		String messageContent=StringTools.cleanHtmlTag(chatMessage.getContent());
		chatMessage.setContent(messageContent);

		//更新会话
		Session chatSession=new Session();
		chatSession.setLastMessage(messageContent);

		if (UserContactTypeEnum.GROUP == contactTypeEnum) {
			chatSession.setLastMessage(tokenUserInfoDto.getNickName()+":"+messageContent);
		}
		chatSession.setLastReceiveTime(curTime);
		sessionMapper.updateBySessionId(chatSession,sessionId);

		//记录消息表
		chatMessage.setSendUserId(sendUserId);
		chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
		chatMessage.setContactType(contactTypeEnum.getType());
		messageMapper.insert(chatMessage);
		MessageSendDto messageSendDto= CopyTools.copy(chatMessage, MessageSendDto.class);

		if (Constants.ROBOT_UID.equals(contactId)) {
			SysSettingDto sysSettingDto= redisComponent.getSysSetting();
			TokenUserInfoDto robot=new TokenUserInfoDto();
			robot.setUserId(sysSettingDto.getRobotUid());
			robot.setNickName(sysSettingDto.getRobotNickName());
			Message robotMessage=new Message();
			robotMessage.setContactId(sendUserId);
			//这里可以对接ai，实现聊天
			robotMessage.setContent("我是一个机器人");
			robotMessage.setType(MessageTypeEnum.CHAT.getType());
			saveMessage(robotMessage,robot);
		}else {
			messageHandler.sendMessage(messageSendDto);
		}
		return messageSendDto;
	}

	@Override
	public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) throws BusinessException {
		Message chatMessage=messageMapper.selectById(messageId);
		if (chatMessage == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (!chatMessage.getSendUserId().equals(userId)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		SysSettingDto sysSettingDto= redisComponent.getSysSetting();
		String fileSuffix=StringTools.getFileSuffix(file.getOriginalFilename());
		if (!StringTools.isEmpty(fileSuffix)&&
		ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST,fileSuffix.toLowerCase())&&
		file.getSize()>sysSettingDto.getMaxImageSize()*Constants.FILE_SIZE_MB) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		} else if (!StringTools.isEmpty(fileSuffix)&&
				ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST,fileSuffix.toLowerCase())&&
				file.getSize()>sysSettingDto.getMaxVideoSize()*Constants.FILE_SIZE_MB) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}else if (!StringTools.isEmpty(fileSuffix)
				&&!ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST,fileSuffix.toLowerCase())
				&&!ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST,fileSuffix.toLowerCase())
				&&file.getSize()>sysSettingDto.getMaxFileSize()*Constants.FILE_SIZE_MB) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		String fileName=file.getOriginalFilename();
		String fileExtName=StringTools.getFileSuffix(fileName);
		String fileRealName=messageId+fileExtName;
		String month= DateUtil.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
		File folder =new File(appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+month);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File uploadFile=new File(folder.getPath()+"/"+fileRealName);
		try {
			file.transferTo(uploadFile);
			cover.transferTo(new File(uploadFile.getPath()+Constants.COVER_IMAGE_SUFFIX));
		}catch (IOException e){
			logger.error("上传文件失败",e);
			throw new BusinessException("文件上传失败");
		}
		Message uploadInfo=new Message();
		uploadInfo.setStatus(MessageStatusEnum.SEND.getStatus());
		MessageQuery messageQuery=new MessageQuery();
		messageQuery.setId(messageId);
		messageQuery.setStatus(MessageStatusEnum.SENDING.getStatus());
		messageMapper.updateByParam(uploadInfo,messageQuery);

		MessageSendDto messageSendDto=new MessageSendDto();
		messageSendDto.setStatus(MessageStatusEnum.SEND.getStatus());
		messageSendDto.setMessageId(messageId);
		messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
		messageSendDto.setContactId(chatMessage.getContactId());
		messageHandler.sendMessage(messageSendDto);
	}

	@Override
	public File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long messageId, Boolean showCover) throws BusinessException {

		Message message=messageMapper.selectById(messageId);
		String contactId= message.getContactId();
		UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByPrefix(contactId);
		if(UserContactTypeEnum.USER==contactTypeEnum&&tokenUserInfoDto.getUserId().equals(message.getContactId())){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (UserContactTypeEnum.GROUP == contactTypeEnum) {
			ContactQuery contactQuery=new ContactQuery();
			contactQuery.setUserId(tokenUserInfoDto.getUserId());
			contactQuery.setContactType(UserContactTypeEnum.GROUP.getType());
			contactQuery.setContactId(contactId);
			contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			Integer contactCount=contactMapper.selectCount(contactQuery);
			if (contactCount == 0) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}
		String month= DateUtil.format(new Date(message.getSendTime()),DateTimePatternEnum.YYYYMM.getPattern());
		File folder=new File(appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+month);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		String fileName=message.getFileName();
		String fileExtName=StringTools.getFileSuffix(fileName);
		String fileRealName=messageId+fileExtName;
		if (showCover != null&&showCover) {
			fileRealName=fileRealName+Constants.COVER_IMAGE_SUFFIX;
		}
		File file=new File(folder.getPath()+"/"+fileRealName);
		if (!file.exists()) {
			logger.info("文件不存在{}",messageId);
			throw new BusinessException(ResponseCodeEnum.CODE_602);
		}
		return file;
	}
}