package com.easychat.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 会话信息表Mapper
 * @author null
 * @Date 2024/09/12
 */
@Mapper
public interface SessionMapper<T, P> extends BaseMapper {
	/**
	 * 根据SessionId查询
	 */
	T selectBySessionId(@Param("sessionId") String sessionId);

	/**
	 * 根据SessionId更新
	 */
	Integer updateBySessionId(@Param("bean") T t, @Param("sessionId") String sessionId);

	/**
	 * 根据SessionId删除
	 */
	Integer deleteBySessionId(@Param("sessionId") String sessionId);

}