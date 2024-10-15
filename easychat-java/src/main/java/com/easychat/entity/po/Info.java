package com.easychat.entity.po;

import java.io.Serializable;

import com.easychat.entity.constants.Constants;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.easychat.entity.enums.DateTimePatternEnum;
import com.easychat.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @Description 用户信息
 * @author null
 * @Date 2024/05/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Info implements Serializable {
	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 昵称
	 */
	private String nickName;

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

	/**
	 * 个性签名
	 */
	private String personalSignature;

	/**
	 * 状态
	 */

	private Integer status;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 最后登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;

	/**
	 * 地区
	 */
	private String areaName;

	/**
	 * 地区编号
	 */
	private String areaCode;

	/**
	 * 最后离开时间
	 */
	private Long lastOffTime;

	private Integer onlineType;


	public Integer getOnLineType() {
		if (lastLoginTime!=null &&lastLoginTime.getTime()>lastOffTime) {
			return Constants.ONE;
		}else {
			return Constants.ZERO;
		}
	}

	public void setOnLineType(Integer onLineType) {
		this.onlineType = onLineType;
	}

	@Override
	public String toString (){
		return "用户id:" + ( userId== null ? "空" : userId) + ",邮箱:" + ( email== null ? "空" : email) + ",昵称:" + ( nickName== null ? "空" : nickName) + ",0:直接加入 1:同意后加好友:" + ( joinType== null ? "空" : joinType) + ",性别 0:女 1:男:" + ( sex== null ? "空" : sex) + ",密码:" + ( password== null ? "空" : password) + ",个性签名:" + ( personalSignature== null ? "空" : personalSignature) + ",状态:" + ( status== null ? "空" : status) + ",创建时间:" + ( createTime== null ? "空" : DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ",最后登录时间:" + ( lastLoginTime== null ? "空" : DateUtils.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ",地区:" + ( areaName== null ? "空" : areaName) + ",地区编号:" + ( areaCode== null ? "空" : areaCode) + ",最后离开时间:" + ( lastOffTime== null ? "空" : lastOffTime);
	}
}