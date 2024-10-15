package com.easychat.entity.po;

import java.io.Serializable;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @Description 靓号表
 * @author null
 * @Date 2024/05/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoBeauty implements Serializable {
	/**
	 * 自增id
	 */
	private Integer id;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 0：未使用 1：已使用
	 */

	private Integer status;

	@Override
	public String toString (){
		return "自增id:" + ( id== null ? "空" : id) + ",邮箱:" + ( email== null ? "空" : email) + ",用户id:" + ( userId== null ? "空" : userId) + ",0：未使用 1：已使用:" + ( status== null ? "空" : status);
	}
}