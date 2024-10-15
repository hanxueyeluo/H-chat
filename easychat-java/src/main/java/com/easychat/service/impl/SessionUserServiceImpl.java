package com.easychat.service.impl;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.enums.MessageTypeEnum;
import com.easychat.entity.enums.PageSize;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.enums.UserContactTypeEnum;
import com.easychat.entity.po.Contact;
import com.easychat.entity.query.ContactQuery;
import com.easychat.entity.query.SimplePage;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.SessionUser;
import com.easychat.entity.query.SessionUserQuery;
import com.easychat.mapper.ContactMapper;
import com.easychat.mapper.SessionUserMapper;
import com.easychat.service.SessionUserService;
import com.easychat.websocket.MessageHandler;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
/**
 * @Description 会话用户表Service
 * @author null
 * @Date 2024/09/12
 */
@Service("SessionUserService")
public class SessionUserServiceImpl implements SessionUserService{

	@Resource
	private SessionUserMapper<SessionUser,SessionUserQuery> sessionUserMapper;

	@Resource
	private MessageHandler messageHandler;

	@Resource
	private ContactMapper<Contact,ContactQuery> contactMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<SessionUser> findListByParam(SessionUserQuery query){
		return this.sessionUserMapper.selectList(query);
	}
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(SessionUserQuery query){
		return this.sessionUserMapper.selectCount(query);
	}
	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<SessionUser> findListByPage(SessionUserQuery query){
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize()==null? PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<SessionUser> list = this.findListByParam(query);
		PaginationResultVO<SessionUser> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	/**
	 * 新增
	 */
	@Override
	public Integer add(SessionUserQuery bean){
		return this.sessionUserMapper.insert(bean);
	}
	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SessionUserQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.sessionUserMapper.insertBatch(listBean);
	}
	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SessionUserQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.sessionUserMapper.insertOrUpdateBatch(listBean);
	}
	/**
	 * 根据UserIdAndContactId查询
	 */
	@Override
	public SessionUser getSessionUserByUserIdAndContactId(String userId, String contactId){
		return this.sessionUserMapper.selectByUserIdAndContactId(userId, contactId);
	}
	/**
	 * 根据UserIdAndContactId更新
	 */
	@Override
	public Integer updateSessionUserByUserIdAndContactId(SessionUser bean, String userId, String contactId){
		return this.sessionUserMapper.updateByUserIdAndContactId(bean,userId, contactId);
	}
	/**
	 * 根据UserIdAndContactId删除
	 */
	@Override
	public Integer deleteSessionUserByUserIdAndContactId(String userId, String contactId){
		return this.sessionUserMapper.deleteByUserIdAndContactId(userId, contactId);
	}
	@Override
	public void updateRedundancyInfo(String contactName, String contactId){
		SessionUser updateInfo=new SessionUser();
		updateInfo.setContactName(contactName);

		SessionUserQuery chatSessionUserQuery=new SessionUserQuery();
		chatSessionUserQuery.setContactId(contactId);
		this.sessionUserMapper.updateByParam(updateInfo,chatSessionUserQuery);

		UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByPrefix(contactId);
		if (contactTypeEnum == UserContactTypeEnum.GROUP) {
			MessageSendDto messageSendDto=new MessageSendDto();
			messageSendDto.setContactType(UserContactTypeEnum.getByPrefix(contactId).getType());
			messageSendDto.setContactId(contactId);
			messageSendDto.setExtendData(contactName);
			messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
			messageHandler.sendMessage(messageSendDto);
		}else {
			ContactQuery userContactQuery=new ContactQuery();
			userContactQuery.setContactType(UserContactTypeEnum.USER.getType());
			userContactQuery.setContactId(contactId);
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			List<Contact> userContactList=contactMapper.selectList(userContactQuery);
			for (Contact userContact : userContactList){
				MessageSendDto messageSendDto=new MessageSendDto();
				messageSendDto.setContactType(contactTypeEnum.getType());
				messageSendDto.setContactId(userContact.getUserId());
				messageSendDto.setExtendData(contactName);
				messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
				messageSendDto.setSendUserId(contactId);
				messageSendDto.setSendNickName(contactName);
				messageHandler.sendMessage(messageSendDto);
			}
		}
	}

}