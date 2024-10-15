package com.easychat.entity.po;

import java.io.Serializable;
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
public class Session implements Serializable {
	/**
	 * 会话id
	 */
	private String sessionId;

	/**
	 * 最后接收的消息
	 */
	private String lastMessage;

	/**
	 * 最后接收消息时间(毫秒)
	 */
	private Long lastReceiveTime;

	@Override
	public String toString (){
		return "会话id:" + ( sessionId== null ? "空" : sessionId) + ",最后接收的消息:" + ( lastMessage== null ? "空" : lastMessage) + ",最后接收消息时间(毫秒):" + ( lastReceiveTime== null ? "空" : lastReceiveTime);
	}
}