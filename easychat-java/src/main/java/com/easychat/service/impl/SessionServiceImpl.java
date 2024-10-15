package com.easychat.service.impl;
import com.easychat.entity.enums.PageSize;
import com.easychat.entity.query.SimplePage;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.Session;
import com.easychat.entity.query.SessionQuery;
import com.easychat.mapper.SessionMapper;
import com.easychat.service.SessionService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
/**
 * @Description 会话信息表Service
 * @author null
 * @Date 2024/09/12
 */
@Service("SessionService")
public class SessionServiceImpl implements SessionService{

	@Resource
	private SessionMapper<Session,SessionQuery> sessionMapper;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Session> findListByParam(SessionQuery query){
		return this.sessionMapper.selectList(query);
	}
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(SessionQuery query){
		return this.sessionMapper.selectCount(query);
	}
	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<Session> findListByPage(SessionQuery query){
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize()==null? PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<Session> list = this.findListByParam(query);
		PaginationResultVO<Session> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	/**
	 * 新增
	 */
	@Override
	public Integer add(SessionQuery bean){
		return this.sessionMapper.insert(bean);
	}
	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SessionQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.sessionMapper.insertBatch(listBean);
	}
	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SessionQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.sessionMapper.insertOrUpdateBatch(listBean);
	}
	/**
	 * 根据SessionId查询
	 */
	@Override
	public Session getSessionBySessionId(String sessionId){
		return this.sessionMapper.selectBySessionId(sessionId);
	}
	/**
	 * 根据SessionId更新
	 */
	@Override
	public Integer updateSessionBySessionId(Session bean, String sessionId){
		return this.sessionMapper.updateBySessionId(bean,sessionId);
	}
	/**
	 * 根据SessionId删除
	 */
	@Override
	public Integer deleteSessionBySessionId(String sessionId){
		return this.sessionMapper.deleteBySessionId(sessionId);
	}
}