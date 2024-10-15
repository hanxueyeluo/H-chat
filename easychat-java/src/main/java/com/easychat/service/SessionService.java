package com.easychat.service;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.Session;
import com.easychat.entity.query.SessionQuery;

import java.util.List;
/**
 * @Description 会话信息表Service
 * @author null
 * @Date 2024/09/12
 */
public interface SessionService{

	/**
	 * 根据条件查询列表
	 */
	List<Session> findListByParam(SessionQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(SessionQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Session> findListByPage(SessionQuery query);

	/**
	 * 新增
	 */
	Integer add(SessionQuery bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<SessionQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<SessionQuery> listBean);

	/**
	 * 根据SessionId查询
	 */
	Session getSessionBySessionId(String sessionId);

	/**
	 * 根据SessionId更新
	 */
	Integer updateSessionBySessionId(Session bean, String sessionId);

	/**
	 * 根据SessionId删除
	 */
	Integer deleteSessionBySessionId(String sessionId);

}