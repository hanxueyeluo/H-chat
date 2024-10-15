package com.easychat.service;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.MessageTypeEnum;
import com.easychat.entity.po.Message;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.query.GroupInfoQuery;
import com.easychat.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
/**
 * @Description 群组Service
 * @author null
 * @Date 2024/07/22
 */
public interface GroupInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<GroupInfo> findListByParam(GroupInfoQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(GroupInfoQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery query);

	/**
	 * 新增
	 */
	Integer add(GroupInfoQuery bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<GroupInfoQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<GroupInfoQuery> listBean);

	/**
	 * 根据GroupId查询
	 */
	GroupInfo getGroupInfoByGroupId(String groupId);

	/**
	 * 根据GroupId更新
	 */
	Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId);

	/**
	 * 根据GroupId删除
	 */
	Integer deleteGroupInfoByGroupId(String groupId);

	/**
	 * 保存群组
	 * @param groupInfo
	 * @param avatarFile
	 * @param avatarCover
	 */
	void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile,MultipartFile avatarCover) throws BusinessException, IOException;

	void dissolutionGroup(String groupOwnerId,String groupId) throws BusinessException;

	void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto,String groupId,String selectContacts,Integer opType) throws BusinessException;

	void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum) throws BusinessException;


}