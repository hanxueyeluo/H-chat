package com.easychat.service;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.ContactApply;
import com.easychat.entity.query.ContactApplyQuery;
import com.easychat.exception.BusinessException;

import java.util.List;
/**
 * @Description 联系人申请Service
 * @author null
 * @Date 2024/07/22
 */
public interface ContactApplyService{

	/**
	 * 根据条件查询列表
	 */
	List<ContactApply> findListByParam(ContactApplyQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(ContactApplyQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ContactApply> findListByPage(ContactApplyQuery query);

	/**
	 * 新增
	 */
	Integer add(ContactApplyQuery bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ContactApplyQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ContactApplyQuery> listBean);

	/**
	 * 根据ApplyId查询
	 */
	ContactApply getContactApplyByApplyId(Integer applyId);

	/**
	 * 根据ApplyId更新
	 */
	Integer updateContactApplyByApplyId(ContactApply bean, Integer applyId);

	/**
	 * 根据ApplyId删除
	 */
	Integer deleteContactApplyByApplyId(Integer applyId);

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId查询
	 */
	ContactApply getContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId);

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId更新
	 */
	Integer updateContactApplyByApplyUserIdAndReceiveUserIdAndContactId(ContactApply bean, String applyUserId, String receiveUserId, String contactId);

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
	 */
	Integer deleteContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId);

	Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) throws BusinessException;

	void dealWithApply(String userId,Integer applyId,Integer status) throws BusinessException;


}