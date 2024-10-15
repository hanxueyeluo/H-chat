package com.easychat.entity.query;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * @Description 靓号表
 * @author null
 * @Date 2024/05/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoBeautyQuery extends BaseQuery {
	/**
	 * 自增id
	 */
	private Integer id;

	/**
	 * 邮箱
	 */
	private String email;

	private String emailFuzzy;
	/**
	 * 用户id
	 */
	private String userId;

	private String userIdFuzzy;
	/**
	 * 0：未使用 1：已使用
	 */
	private Integer status;

}