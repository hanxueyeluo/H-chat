package com.easychat.entity.query;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Date;


/**
 * @Description 用户信息
 * @author null
 * @Date 2024/05/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoQuery extends BaseQuery {
	/**
	 * 用户id
	 */
	private String userId;

	private String userIdFuzzy;
	/**
	 * 邮箱
	 */
	private String email;

	private String emailFuzzy;
	/**
	 * 昵称
	 */
	private String nickName;

	private String nickNameFuzzy;
	/**
	 * 0:直接加入 1:同意后加好友
	 */
	private Integer joinType;

	/**
	 * 性别 0:女 1:男
	 */
	private Integer sex;

	/**
	 * 密码
	 */
	private String password;

	private String passwordFuzzy;
	/**
	 * 个性签名
	 */
	private String personalSignature;

	private String personalSignatureFuzzy;
	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private Date createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 最后登录时间
	 */
	private Date lastLoginTime;

	private String lastLoginTimeStart;

	private String lastLoginTimeEnd;

	/**
	 * 地区
	 */
	private String areaName;

	private String areaNameFuzzy;
	/**
	 * 地区编号
	 */
	private String areaCode;

	private String areaCodeFuzzy;
	/**
	 * 最后离开时间
	 */
	private Long lastOffTime;

}