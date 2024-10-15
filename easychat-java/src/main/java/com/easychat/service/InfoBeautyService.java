package com.easychat.service;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.InfoBeauty;
import com.easychat.entity.query.InfoBeautyQuery;
import com.easychat.exception.BusinessException;

import java.util.List;
/**
 * @Description 靓号表Service
 * @author null
 * @Date 2024/05/22
 */
public interface InfoBeautyService{

	/**
	 * 根据条件查询列表
	 */
	List<InfoBeauty> findListByParam(InfoBeautyQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(InfoBeautyQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<InfoBeauty> findListByPage(InfoBeautyQuery query);

	/**
	 * 新增
	 */
	Integer add(InfoBeautyQuery bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<InfoBeautyQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<InfoBeautyQuery> listBean);

	/**
	 * 根据Id查询
	 */
	InfoBeauty getInfoBeautyById(Integer id);

	/**
	 * 根据Id更新
	 */
	Integer updateInfoBeautyById(InfoBeauty bean, Integer id);

	/**
	 * 根据Id删除
	 */
	Integer deleteInfoBeautyById(Integer id);

	/**
	 * 根据Email查询
	 */
	InfoBeauty getInfoBeautyByEmail(String email);

	/**
	 * 根据Email更新
	 */
	Integer updateInfoBeautyByEmail(InfoBeauty bean, String email);

	/**
	 * 根据Email删除
	 */
	Integer deleteInfoBeautyByEmail(String email);

	/**
	 * 根据UserId查询
	 */
	InfoBeauty getInfoBeautyByUserId(String userId);

	/**
	 * 根据UserId更新
	 */
	Integer updateInfoBeautyByUserId(InfoBeauty bean, String userId);

	/**
	 * 根据UserId删除
	 */
	Integer deleteInfoBeautyByUserId(String userId);

	void saveAccount(InfoBeauty beauty) throws BusinessException;

}