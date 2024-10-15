package com.easychat.entity.po;

import java.io.Serializable;
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
 * @Description 群组
 * @author null
 * @Date 2024/07/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfo implements Serializable {
	/**
	 * 群ID
	 */
	private String groupId;

	/**
	 * 群组名
	 */
	private String groupName;

	/**
	 * 群主id
	 */
	private String groupOwnerId;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 群公告
	 */
	private String groupNotice;

	/**
	 * 0:直接加入 1:管理员同意后加入
	 */
	private Integer joinType;

	/**
	 * 状态 1:正常 0:解散
	 */
	private Integer status;

	/**
	 * 成员数
	 * @return
	 */
	private Integer memberCount;

	/**
	 * 群主昵称
	 */

	private String groupOwnerNickName;

	public String getGroupOwnerNickName() {
		return groupOwnerNickName;
	}

	public void setGroupOwnerNickName(String groupOwnerNickName) {
		this.groupOwnerNickName = groupOwnerNickName;
	}

	public Integer getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}

	@Override
	public String toString (){
		return "群ID:" + ( groupId== null ? "空" : groupId) + ",群组名:" + ( groupName== null ? "空" : groupName) + ",群主id:" + ( groupOwnerId== null ? "空" : groupOwnerId) + ",创建时间:" + ( createTime== null ? "空" : DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ",群公告:" + ( groupNotice== null ? "空" : groupNotice) + ",0:直接加入 1:管理员同意后加入:" + ( joinType== null ? "空" : joinType) + ",状态 1:正常 0:解散:" + ( status== null ? "空" : status);
	}
}