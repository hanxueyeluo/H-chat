package com.easychat.entity.query;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * @Description 联系人申请
 * @author null
 * @Date 2024/07/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactApplyQuery extends BaseQuery {
	/**
	 * 自增ID
	 */
	private Integer applyId;

	/**
	 * 申请人id
	 */
	private String applyUserId;

	private String applyUserIdFuzzy;
	/**
	 * 接收人id
	 */
	private String receiveUserId;

	private String receiveUserIdFuzzy;
	/**
	 * 联系人类型 0:好友 1::群组
	 */
	private Integer contactType;

	/**
	 * 联系人群组ID
	 */
	private String contactId;

	private String contactIdFuzzy;
	/**
	 * 最后申请时间
	 */
	private Long lastApplyTime;

	/**
	 * 状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑
	 */
	private Integer status;

	/**
	 * 申请信息
	 */
	private String applyInfo;

	private String applyInfoFuzzy;

	private Boolean queryContactInfo;

	private Long lastApplyTimestamp;


	public String getApplyUserIdFuzzy() {
		return applyUserIdFuzzy;
	}

	public void setApplyUserIdFuzzy(String applyUserIdFuzzy) {
		this.applyUserIdFuzzy = applyUserIdFuzzy;
	}

	public String getReceiveUserId() {
		return receiveUserId;
	}

	public void setReceiveUserId(String receiveUserId) {
		this.receiveUserId = receiveUserId;
	}

	public String getContactIdFuzzy() {
		return contactIdFuzzy;
	}

	public void setContactIdFuzzy(String contactIdFuzzy) {
		this.contactIdFuzzy = contactIdFuzzy;
	}

	public Integer getApplyId() {
		return applyId;
	}

	public void setApplyId(Integer applyId) {
		this.applyId = applyId;
	}

	public String getApplyUserId() {
		return applyUserId;
	}

	public void setApplyUserId(String applyUserId) {
		this.applyUserId = applyUserId;
	}

	public String getReceiveUserIdFuzzy() {
		return receiveUserIdFuzzy;
	}

	public void setReceiveUserIdFuzzy(String receiveUserIdFuzzy) {
		this.receiveUserIdFuzzy = receiveUserIdFuzzy;
	}

	public Integer getContactType() {
		return contactType;
	}

	public void setContactType(Integer contactType) {
		this.contactType = contactType;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public Long getLastApplyTime() {
		return lastApplyTime;
	}

	public void setLastApplyTime(Long lastApplyTime) {
		this.lastApplyTime = lastApplyTime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getApplyInfo() {
		return applyInfo;
	}

	public void setApplyInfo(String applyInfo) {
		this.applyInfo = applyInfo;
	}

	public String getApplyInfoFuzzy() {
		return applyInfoFuzzy;
	}

	public void setApplyInfoFuzzy(String applyInfoFuzzy) {
		this.applyInfoFuzzy = applyInfoFuzzy;
	}

	public Boolean getQueryContactInfo() {
		return queryContactInfo;
	}

	public void setQueryContactInfo(Boolean queryContactInfo) {
		this.queryContactInfo = queryContactInfo;
	}
}