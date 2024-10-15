package com.easychat.service;


import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.Update;
import com.easychat.entity.query.UpdateQuery;
import com.easychat.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
/**
 * @Description app发布表Service
 * @author null
 * @Date 2024/09/06
 */
public interface UpdateService{

	/**
	 * 根据条件查询列表
	 */
	List<Update> findListByParam(UpdateQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UpdateQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Update> findListByPage(UpdateQuery query);

	/**
	 * 新增
	 */
	Integer add(UpdateQuery bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UpdateQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UpdateQuery> listBean);

	/**
	 * 根据Id查询
	 */
	Update getUpdateById(Integer id);

	/**
	 * 根据Id更新
	 */
	Integer updateUpdateById(Update bean, Integer id);

	/**
	 * 根据Id删除
	 */
	Integer deleteUpdateById(Integer id) throws BusinessException;

	/**
	 * 根据Version查询
	 */
	Update getUpdateByVersion(String version);

	/**
	 * 根据Version更新
	 */
	Integer updateUpdateByVersion(Update bean, String version);

	/**
	 * 根据Version删除
	 */
	Integer deleteUpdateByVersion(String version);

	void saveUpdate(Update appUpdate, MultipartFile file) throws BusinessException, IOException;

	void postUpdate(Integer id,Integer status,String grayscaleUid) throws BusinessException;

	Update getLatestUpdate(String appVersion,String uid);

}