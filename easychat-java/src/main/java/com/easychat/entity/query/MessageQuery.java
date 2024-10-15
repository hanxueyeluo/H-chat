package com.easychat.entity.query;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * @Description 聊天消息表
 * @author null
 * @Date 2024/09/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageQuery extends BaseQuery {
	/**
	 * id
	 */
	private Long id;

	/**
	 * 会话id
	 */
	private String sessionId;

	private String sessionIdFuzzy;
	/**
	 * 消息类型
	 */
	private Integer type;

	/**
	 * 消息内容
	 */
	private String content;

	private String contentFuzzy;
	/**
	 * 发送人id
	 */
	private String sendUserId;

	private String sendUserIdFuzzy;
	/**
	 * 发送人昵称
	 */
	private String sendUserNickName;

	private String sendUserNickNameFuzzy;
	/**
	 * 发送时间
	 */
	private Long sendTime;

	/**
	 * 联系人id
	 */
	private String contactId;

	private String contactIdFuzzy;
	/**
	 * 联系人类型 0:单聊 1:群聊
	 */
	private Integer contactType;

	/**
	 * 文件大小
	 */
	private Long fileSize;

	/**
	 * 文件名
	 */
	private String fileName;

	private String fileNameFuzzy;
	/**
	 * 文件类型
	 */
	private Integer fileType;

	/**
	 * 状态 0:正在发送 1:已发送
	 */
	private Integer status;

	private List<String> contactIdList;

	private Long lastReceiveTime;

}