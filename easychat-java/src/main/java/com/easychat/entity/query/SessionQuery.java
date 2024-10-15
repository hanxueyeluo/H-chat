package com.easychat.entity.query;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * @Description 会话信息表
 * @author null
 * @Date 2024/09/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionQuery extends BaseQuery {
	/**
	 * 会话id
	 */
	private String sessionId;

	private String sessionIdFuzzy;
	/**
	 * 最后接收的消息
	 */
	private String lastMessage;

	private String lastMessageFuzzy;
	/**
	 * 最后接收消息时间(毫秒)
	 */
	private Long lastReceiveTime;

}