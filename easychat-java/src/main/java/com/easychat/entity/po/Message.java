package com.easychat.entity.po;

import java.io.Serializable;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @Description 聊天消息表
 * @author null
 * @Date 2024/09/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
	/**
	 * id
	 */
	private Long id;

	/**
	 * 会话id
	 */
	private String sessionId;

	/**
	 * 消息类型
	 */
	private Integer type;

	/**
	 * 消息内容
	 */
	private String content;

	/**
	 * 发送人id
	 */
	private String sendUserId;

	/**
	 * 发送人昵称
	 */
	private String sendUserNickName;

	/**
	 * 发送时间
	 */
	private Long sendTime;

	/**
	 * 联系人id
	 */
	private String contactId;

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

	/**
	 * 文件类型
	 */
	private Integer fileType;

	/**
	 * 状态 0:正在发送 1:已发送
	 */
	private Integer status;

	@Override
	public String toString (){
		return "id:" + ( id== null ? "空" : id) + ",会话id:" + ( sessionId== null ? "空" : sessionId) + ",消息类型:" + ( type== null ? "空" : type) + ",消息内容:" + ( content== null ? "空" : content) + ",发送人id:" + ( sendUserId== null ? "空" : sendUserId) + ",发送人昵称:" + ( sendUserNickName== null ? "空" : sendUserNickName) + ",发送时间:" + ( sendTime== null ? "空" : sendTime) + ",联系人id:" + ( contactId== null ? "空" : contactId) + ",联系人类型 0:单聊 1:群聊:" + ( contactType== null ? "空" : contactType) + ",文件大小:" + ( fileSize== null ? "空" : fileSize) + ",文件名:" + ( fileName== null ? "空" : fileName) + ",文件类型:" + ( fileType== null ? "空" : fileType) + ",状态 0:正在发送 1:已发送:" + ( status== null ? "空" : status);
	}
}