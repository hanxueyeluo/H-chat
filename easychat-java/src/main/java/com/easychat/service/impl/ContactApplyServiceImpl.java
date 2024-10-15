package com.easychat.service.impl;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.util.ArrayUtil;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.Contact;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.po.Info;
import com.easychat.entity.query.*;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.ContactApply;
import com.easychat.exception.BusinessException;
import com.easychat.mapper.ContactApplyMapper;
import com.easychat.mapper.ContactMapper;
import com.easychat.mapper.GroupInfoMapper;
import com.easychat.mapper.InfoMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.service.ContactApplyService;
import com.easychat.service.ContactService;
import com.easychat.utils.StringTools;
import com.easychat.websocket.MessageHandler;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * @Description 联系人申请Service
 * @author null
 * @Date 2024/07/22
 */
@Service("ContactApplyService")
public class ContactApplyServiceImpl implements ContactApplyService{

	@Resource
	private ContactApplyMapper<ContactApply,ContactApplyQuery> contactApplyMapper;

	@Resource
	private ContactMapper<Contact, ContactQuery> contactMapper;

	@Resource
	private ContactService contactService;

	@Resource
	private MessageHandler messageHandler;

	@Resource
	private InfoMapper<Info, InfoQuery> infoMapper;

	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;


	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ContactApply> findListByParam(ContactApplyQuery query){
		return this.contactApplyMapper.selectList(query);
	}
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(ContactApplyQuery query){
		return this.contactApplyMapper.selectCount(query);
	}
	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<ContactApply> findListByPage(ContactApplyQuery query){
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize()==null?PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<ContactApply> list = this.findListByParam(query);
		PaginationResultVO<ContactApply> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	/**
	 * 新增
	 */
	@Override
	public Integer add(ContactApplyQuery bean){
		return this.contactApplyMapper.insert(bean);
	}
	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ContactApplyQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.contactApplyMapper.insertBatch(listBean);
	}
	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ContactApplyQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.contactApplyMapper.insertOrUpdateBatch(listBean);
	}
	/**
	 * 根据ApplyId查询
	 */
	@Override
	public ContactApply getContactApplyByApplyId(Integer applyId){
		return this.contactApplyMapper.selectByApplyId(applyId);
	}
	/**
	 * 根据ApplyId更新
	 */
	@Override
	public Integer updateContactApplyByApplyId(ContactApply bean, Integer applyId){
		return this.contactApplyMapper.updateByApplyId(bean,applyId);
	}
	/**
	 * 根据ApplyId删除
	 */
	@Override
	public Integer deleteContactApplyByApplyId(Integer applyId){
		return this.contactApplyMapper.deleteByApplyId(applyId);
	}
	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId查询
	 */
	@Override
	public ContactApply getContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId){
		return this.contactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
	}
	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId更新
	 */
	@Override
	public Integer updateContactApplyByApplyUserIdAndReceiveUserIdAndContactId(ContactApply bean, String applyUserId, String receiveUserId, String contactId){
		return this.contactApplyMapper.updateByApplyUserIdAndReceiveUserIdAndContactId(bean,applyUserId, receiveUserId, contactId);
	}
	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
	 */
	@Override
	public Integer deleteContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId){
		return this.contactApplyMapper.deleteByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void dealWithApply(String userId, Integer applyId, Integer status) throws BusinessException {
		UserContactApplyStatusEnum statusEnum=UserContactApplyStatusEnum.getByStatus(status);
		if (statusEnum == null|| UserContactApplyStatusEnum.INIT==statusEnum) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		ContactApply applyInfo=this.contactApplyMapper.selectByApplyId(applyId);
		if (applyInfo == null||!userId.equals(applyInfo.getReceiveUserId())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		ContactApply updateInfo=new ContactApply();
		updateInfo.setStatus(statusEnum.getStatus());
		updateInfo.setLastApplyTime(System.currentTimeMillis());

		ContactApplyQuery applyQuery=new ContactApplyQuery();
		applyQuery.setApplyId(applyId);
		applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());

		Integer count=contactApplyMapper.updateByParam(updateInfo,applyQuery);
		if (count == 0) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		if (UserContactApplyStatusEnum.PASS.getStatus().equals(status)) {
			this.contactService.addContact(applyInfo.getApplyUserId(), applyInfo.getReceiveUserId(), applyQuery.getContactId(), applyInfo.getContactType(),applyInfo.getApplyInfo());
			return;
		}
		if (UserContactApplyStatusEnum.BLACKLIST==statusEnum) {
			Date curData=new Date();
			Contact contact=new Contact();
			contact.setUserId(applyInfo.getApplyUserId());
			contact.setContactId(applyInfo.getContactId());
			contact.setContactType(applyInfo.getContactType());
			contact.setCreateTime(curData);
			contact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
			contact.setLastUpdateTime(curData);
			contactApplyMapper.insertOrUpdate(contact);

		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) throws BusinessException {
		UserContactTypeEnum typeEnum=UserContactTypeEnum.getByPrefix(contactId);
		if (typeEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//申请人
		String applyUserId=tokenUserInfoDto.getUserId();

		//默认申请信息
		applyInfo= StringTools.isEmpty(applyInfo)? String.format(Constants.APPLY_INFO_TEMPLATE,tokenUserInfoDto.getNickName()):applyInfo;

		Long curTime=System.currentTimeMillis();

		Integer joinType=null;
		String receiveUserId=contactId;

		//查询对方好友是否已添加，如果已经拉黑无法添加
		Contact contact=contactMapper.selectByUserIdAndContactId(applyUserId,contactId);
		if (contact != null&& ArrayUtil.contains(new Integer[]{
						UserContactStatusEnum.BLACKLIST_BE.getStatus(),
						UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus()},
				contact.getStatus())) {
			throw new BusinessException("对方已将你拉黑，无法添加");
		}
		if (UserContactTypeEnum.GROUP == typeEnum) {
			GroupInfo groupInfo=groupInfoMapper.selectByGroupId(contactId);
			if (groupInfo == null|| GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())) {
				throw new BusinessException("群聊不存在或已解散");
			}
			receiveUserId=groupInfo.getGroupOwnerId();
			joinType=groupInfo.getJoinType();
		}else {
			Info info=infoMapper.selectByUserId(contactId);
			if (info == null) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
			joinType=info.getJoinType();
		}
		//直接加入不用记录申请记录
		if (JoinTypeEnum.JOIN.getType().equals(joinType)) {
			contactService.addContact(applyUserId,receiveUserId,contactId,typeEnum.getType(),applyInfo);
			return joinType;
		}

		ContactApply dbApply=this.contactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId,receiveUserId,contactId);
		if (dbApply == null) {
			ContactApply contactApply=new ContactApply();
			contactApply.setApplyUserId(applyUserId);
			contactApply.setContactType(typeEnum.getType());
			contactApply.setReceiveUserId(receiveUserId);
			contactApply.setLastApplyTime(curTime);
			contactApply.setContactId(contactId);
			contactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
			contactApply.setApplyInfo(applyInfo);
			this.contactApplyMapper.insert(contactApply);
		}else {
			//更新状态
			ContactApply contactApply=new ContactApply();
			contactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
			contactApply.setLastApplyTime(curTime);
			contactApply.setApplyInfo(applyInfo);
			this.contactApplyMapper.updateByApplyId(contactApply,dbApply.getApplyId());
		}

		if (dbApply == null||!UserContactApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())) {
			MessageSendDto messageSendDto=new MessageSendDto();
			messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
			messageSendDto.setMessageContent(applyInfo);
			messageSendDto.setContactId(receiveUserId);
			messageHandler.sendMessage(messageSendDto);

		}
		return joinType;
	}

}