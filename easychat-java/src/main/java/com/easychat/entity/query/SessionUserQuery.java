package com.easychat.entity.query;

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
public class SessionUserQuery extends BaseQuery {
	/**
	 * 用户id
	 */
	private String userId;

	private String userIdFuzzy;
	/**
	 * 联系人id
	 */
	private String contactId;

	private String contactIdFuzzy;
	/**
	 * 会话id
	 */
	private String sessionId;

	private String sessionIdFuzzy;
	/**
	 * 联系人名称
	 */
	private String contactName;

	private String contactNameFuzzy;
}