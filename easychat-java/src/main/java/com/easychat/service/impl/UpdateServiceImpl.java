package com.easychat.service.impl;
import com.easychat.entity.config.AppConfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.enums.AppUpdateFileTypeEnum;
import com.easychat.entity.enums.AppUpdateStatusEnum;
import com.easychat.entity.enums.PageSize;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.entity.query.SimplePage;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.Update;
import com.easychat.entity.query.UpdateQuery;
import com.easychat.exception.BusinessException;
import com.easychat.mapper.UpdateMapper;
import com.easychat.service.UpdateService;
import com.easychat.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
/**
 * @Description app发布表Service
 * @author null
 * @Date 2024/09/06
 */
@Service("UpdateService")
public class UpdateServiceImpl implements UpdateService{

	@Resource
	private AppConfig appConfig;

	@Resource
	private UpdateMapper<Update,UpdateQuery> updateMapper;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Update> findListByParam(UpdateQuery query){
		return this.updateMapper.selectList(query);
	}
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UpdateQuery query){
		return this.updateMapper.selectCount(query);
	}
	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<Update> findListByPage(UpdateQuery query){
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize()==null? PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<Update> list = this.findListByParam(query);
		PaginationResultVO<Update> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	/**
	 * 新增
	 */
	@Override
	public Integer add(UpdateQuery bean){
		return this.updateMapper.insert(bean);
	}
	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UpdateQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.updateMapper.insertBatch(listBean);
	}
	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UpdateQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.updateMapper.insertOrUpdateBatch(listBean);
	}
	/**
	 * 根据Id查询
	 */
	@Override
	public Update getUpdateById(Integer id){
		return this.updateMapper.selectById(id);
	}
	/**
	 * 根据Id更新
	 */
	@Override
	public Integer updateUpdateById(Update bean, Integer id){
		return this.updateMapper.updateById(bean,id);
	}
	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteUpdateById(Integer id) throws BusinessException {
		Update dbInfo=this.getUpdateById(id);
		if (!AppUpdateStatusEnum.INIT.getStatus().equals(dbInfo.getStatus())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		return this.updateMapper.deleteById(id);
	}
	/**
	 * 根据Version查询
	 */
	@Override
	public Update getUpdateByVersion(String version){
		return this.updateMapper.selectByVersion(version);
	}
	/**
	 * 根据Version更新
	 */
	@Override
	public Integer updateUpdateByVersion(Update bean, String version){
		return this.updateMapper.updateByVersion(bean,version);
	}
	/**
	 * 根据Version删除
	 */
	@Override
	public Integer deleteUpdateByVersion(String version){
		return this.updateMapper.deleteByVersion(version);
	}

	@Override
	public void saveUpdate(Update appUpdate, MultipartFile file) throws BusinessException, IOException {
		AppUpdateFileTypeEnum fileTypeEnum=AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
		if (fileTypeEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		if (appUpdate.getId() != null) {
			Update dbInfo=this.getUpdateById(appUpdate.getId());
			if (!AppUpdateStatusEnum.INIT.getStatus().equals(dbInfo.getStatus())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}
		
		UpdateQuery updateQuery=new UpdateQuery();
		updateQuery.setOrderBy("id desc");
		updateQuery.setSimplePage(new SimplePage(0,1));
		List<Update> appUpdateList=updateMapper.selectList(updateQuery);
		if (!appUpdateList.isEmpty()) {
			Update lastest=appUpdateList.get(0);
			Long dbVersion=Long.parseLong(appUpdate.getVersion().replace(".",""));
			Long currentVersion=Long.parseLong(appUpdate.getVersion().replace(".",""));
			if (appUpdate.getId() == null&&currentVersion<=dbVersion) {
				throw new BusinessException("当前版本必须大于历史版本");
			}
			if (appUpdate.getId() != null&& currentVersion>=dbVersion&&!appUpdate.getId().equals(lastest.getId() )) {
				throw new BusinessException("当前版本必须大于历史版本");
			}
			Update versionDb=updateMapper.selectByVersion(appUpdate.getVersion());
			if (appUpdate.getId() != null&& versionDb !=null && !versionDb.getId().equals(appUpdate.getId())) {
				throw new BusinessException("版本号已存在");
			}
		}
		if (appUpdate.getId() == null) {
			appUpdate.setCreateTime(new Date());
			appUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
			updateMapper.insert(appUpdate);
		}else{
			updateMapper.updateById(appUpdate,appUpdate.getId());
		}
		if (file != null) {
			File folder=new File(appConfig.getProjectFolder()+ Constants.APP_UPDATE_FOLDER);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			file.transferTo(new File(folder.getAbsolutePath()+"/"+appUpdate.getId()+Constants.APP_EXE_SUFFIX));
		}
	}

	@Override
	public void postUpdate(Integer id, Integer status, String grayscaleUid) throws BusinessException {
		AppUpdateStatusEnum statusEnum=AppUpdateStatusEnum.getByStatus(status);
		if (statusEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (AppUpdateStatusEnum.GRAYSCALE == statusEnum&& StringTools.isEmpty(grayscaleUid)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (statusEnum != AppUpdateStatusEnum.GRAYSCALE) {
			grayscaleUid="";
		}
		Update appUpdate=new Update();
		appUpdate.setStatus(status);
		appUpdate.setGrayscaleUid(grayscaleUid);
		updateMapper.updateById(appUpdate,id);
	}

	@Override
	public Update getLatestUpdate(String appVersion, String uid) {
		return updateMapper.selectLatestUpdate(appVersion,uid);
	}
}