package com.easychat.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description app发布表Mapper
 * @author null
 * @Date 2024/09/06
 */
@Mapper
public interface UpdateMapper<T, P> extends BaseMapper {
	/**
	 * 根据Id查询
	 */
	T selectById(@Param("id") Integer id);

	/**
	 * 根据Id更新
	 */
	Integer updateById(@Param("bean") T t, @Param("id") Integer id);

	/**
	 * 根据Id删除
	 */
	Integer deleteById(@Param("id") Integer id);

	/**
	 * 根据Version查询
	 */
	T selectByVersion(@Param("version") String version);

	/**
	 * 根据Version更新
	 */
	Integer updateByVersion(@Param("bean") T t, @Param("version") String version);

	/**
	 * 根据Version删除
	 */
	Integer deleteByVersion(@Param("version") String version);

	T selectLatestUpdate(@Param("appVersion") String appVersion,@Param("uid") String uid);

}