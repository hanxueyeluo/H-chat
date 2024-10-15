package com.easychat.service;

import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.dto.UserContactSearchResultDto;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.Contact;
import com.easychat.entity.query.ContactQuery;
import com.easychat.exception.BusinessException;

import java.util.List;
/**
 * @Description 联系人Service
 * @author null
 * @Date 2024/07/22
 */
public interface ContactService{

	/**
	 * 根据条件查询列表
	 */
	List<Contact> findListByParam(ContactQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(ContactQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Contact> findListByPage(ContactQuery query);

	/**
	 * 新增
	 */
	Integer add(ContactQuery bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ContactQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ContactQuery> listBean);

	/**
	 * 根据UserIdAndContactId查询
	 */
	Contact getContactByUserIdAndContactId(String userId, String contactId);

	/**
	 * 根据UserIdAndContactId更新
	 */
	Integer updateContactByUserIdAndContactId(Contact bean, String userId, String contactId);

	/**
	 * 根据UserIdAndContactId删除
	 */
	Integer deleteContactByUserIdAndContactId(String userId, String contactId);

	UserContactSearchResultDto searchContact(String userId, String contact);


	void addContact(String applyUserId,String receiveUserId,String contactId,Integer contactType,String applyInfo) throws BusinessException;

	void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum);

	void addContact4Robot(String userId);

}