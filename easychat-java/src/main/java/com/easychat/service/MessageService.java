package com.easychat.service;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.Message;
import com.easychat.entity.query.MessageQuery;
import com.easychat.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
/**
 * @Description 聊天消息表Service
 * @author null
 * @Date 2024/09/12
 */
public interface MessageService{

	/**
	 * 根据条件查询列表
	 */
	List<Message> findListByParam(MessageQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(MessageQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Message> findListByPage(MessageQuery query);

	/**
	 * 新增
	 */
	Integer add(MessageQuery bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<MessageQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<MessageQuery> listBean);

	/**
	 * 根据Id查询
	 */
	Message getMessageById(Long id);

	/**
	 * 根据Id更新
	 */
	Integer updateMessageById(Message bean, Long id);

	/**
	 * 根据Id删除
	 */
	Integer deleteMessageById(Long id);


	MessageSendDto saveMessage(Message chatMessage, TokenUserInfoDto tokenUserInfoDto) throws BusinessException;

	void saveMessageFile(String userId, Long messageId, MultipartFile file,MultipartFile cover) throws BusinessException;

	File downloadFile(TokenUserInfoDto tokenUserInfoDto,Long fileId,Boolean showCover) throws BusinessException;


}