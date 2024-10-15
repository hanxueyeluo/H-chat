package com.easychat.entity.query;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Date;


/**
 * @Description app发布表
 * @author null
 * @Date 2024/09/06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateQuery extends BaseQuery {
	/**
	 * id
	 */
	private Integer id;

	/**
	 * 版本号
	 */
	private String version;

	private String versionFuzzy;
	/**
	 * 更新信息
	 */
	private String updateDesc;

	private String updateDescFuzzy;
	/**
	 * 创建时间
	 */
	private Date createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 0:未发布 1:灰度发布 2:全部发布
	 */
	private Integer status;

	/**
	 * 灰度uid
	 */
	private String grayscaleUid;

	private String grayscaleUidFuzzy;
	/**
	 * 文件类型 0:本地文件 1:外链
	 */
	private Integer fileType;

	/**
	 * 外链地址
	 */
	private String outerLink;

	private String outerLinkFuzzy;
}