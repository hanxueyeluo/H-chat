package com.easychat.service;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.SessionUser;
import com.easychat.entity.query.SessionUserQuery;
import org.springframework.context.annotation.Lazy;

import java.util.List;
/**
 * @Description 会话用户表Service
 * @author null
 * @Date 2024/09/12
 */

public interface SessionUserService{

	/**
	 * 根据条件查询列表
	 */
	List<SessionUser> findListByParam(SessionUserQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(SessionUserQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<SessionUser> findListByPage(SessionUserQuery query);

	/**
	 * 新增
	 */
	Integer add(SessionUserQuery bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<SessionUserQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<SessionUserQuery> listBean);

	/**
	 * 根据UserIdAndContactId查询
	 */
	SessionUser getSessionUserByUserIdAndContactId(String userId, String contactId);

	/**
	 * 根据UserIdAndContactId更新
	 */
	Integer updateSessionUserByUserIdAndContactId(SessionUser bean, String userId, String contactId);

	/**
	 * 根据UserIdAndContactId删除
	 */
	Integer deleteSessionUserByUserIdAndContactId(String userId, String contactId);

    void updateRedundancyInfo(String contactName, String contactId);
}