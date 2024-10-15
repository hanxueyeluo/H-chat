package com.easychat.entity.po;

import java.io.Serializable;

import com.easychat.entity.enums.UserContactTypeEnum;
import com.easychat.utils.StringTools;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * @Description 会话用户表
 * @author null
 * @Date 2024/09/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionUser implements Serializable {
	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 联系人id
	 */
	private String contactId;

	/**
	 * 会话id
	 */
	private String sessionId;

	/**
	 * 联系人名称
	 */
	private String contactName;

	private String lastMessage;

	private Long lastReceiveTime;

	private Integer memberCount;

	private Integer contactType;

	public Integer getContactType() {
		if (StringTools.isEmpty(contactId)) {
			return null;
		}
		return UserContactTypeEnum.getByPrefix(contactId).getType();
	}

	public void setContactType(Integer contactType) {
		this.contactType = contactType;
	}

	public Integer getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public Long getLastReceiveTime() {
		return lastReceiveTime;
	}

	public void setLastReceiveTime(Long lastReceiveTime) {
		this.lastReceiveTime = lastReceiveTime;
	}

	@Override
	public String toString (){
		return "用户id:" + ( userId== null ? "空" : userId) + ",联系人id:" + ( contactId== null ? "空" : contactId) + ",会话id:" + ( sessionId== null ? "空" : sessionId) + ",联系人名称:" + ( contactName== null ? "空" : contactName);
	}
}