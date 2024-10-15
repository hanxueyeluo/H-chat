package com.easychat.service;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.Info;
import com.easychat.entity.query.InfoQuery;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description 用户信息Service
 * @author null
 * @Date 2024/05/22
 */
public interface InfoService{

	/**
	 * 根据条件查询列表
	 */
	List<Info> findListByParam(InfoQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(InfoQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Info> findListByPage(InfoQuery query);

	/**
	 * 新增
	 */
	Integer add(InfoQuery bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<InfoQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<InfoQuery> listBean);

	/**
	 * 根据UserId查询
	 */
	Info getInfoByUserId(String userId);

	/**
	 * 根据UserId更新
	 */
	Integer updateInfoByUserId(Info bean, String userId);

	/**
	 * 根据UserId删除
	 */
	Integer deleteInfoByUserId(String userId);

	/**
	 * 根据Email查询
	 */
	Info getInfoByEmail(String email);

	/**
	 * 根据Email更新
	 */
	Integer updateInfoByEmail(Info bean, String email);

	/**
	 * 根据Email删除
	 */
	Integer deleteInfoByEmail(String email);

	/*
	注册
	 */
	void register(String email, String nickName, String password) throws BusinessException;
	/*
	登录
	 */
	UserInfoVO login(String email, String password) throws BusinessException;


	void updateUserInfo(Info userInfo, MultipartFile avatarFile,MultipartFile avatarCover) throws IOException;

	void updateUserStatus(Integer status,String userId) throws BusinessException;

	void forceOffLine(String userId);

}