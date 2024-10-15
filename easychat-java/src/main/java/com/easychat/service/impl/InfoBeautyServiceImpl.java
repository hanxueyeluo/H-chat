package com.easychat.service.impl;
import com.easychat.entity.enums.BeautyAccountStatusEnum;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.entity.po.Info;
import com.easychat.entity.query.InfoQuery;
import com.easychat.entity.query.SimplePage;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.InfoBeauty;
import com.easychat.entity.query.InfoBeautyQuery;
import com.easychat.entity.enums.PageSize;
import com.easychat.exception.BusinessException;
import com.easychat.mapper.InfoBeautyMapper;
import com.easychat.mapper.InfoMapper;
import com.easychat.service.InfoBeautyService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
/**
 * @Description 靓号表Service
 * @author null
 * @Date 2024/05/22
 */
@Service("InfoBeautyService")
public class InfoBeautyServiceImpl implements InfoBeautyService{

	@Resource
	private InfoBeautyMapper<InfoBeauty,InfoBeautyQuery> infoBeautyMapper;

	@Resource
	private InfoMapper<Info, InfoQuery> infoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<InfoBeauty> findListByParam(InfoBeautyQuery query){
		return this.infoBeautyMapper.selectList(query);
	}
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(InfoBeautyQuery query){
		return this.infoBeautyMapper.selectCount(query);
	}
	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<InfoBeauty> findListByPage(InfoBeautyQuery query){
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize()==null?PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<InfoBeauty> list = this.findListByParam(query);
		PaginationResultVO<InfoBeauty> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	/**
	 * 新增
	 */
	@Override
	public Integer add(InfoBeautyQuery bean){
		return this.infoBeautyMapper.insert(bean);
	}
	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<InfoBeautyQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.infoBeautyMapper.insertBatch(listBean);
	}
	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<InfoBeautyQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.infoBeautyMapper.insertOrUpdateBatch(listBean);
	}
	/**
	 * 根据Id查询
	 */
	@Override
	public InfoBeauty getInfoBeautyById(Integer id){
		return this.infoBeautyMapper.selectById(id);
	}
	/**
	 * 根据Id更新
	 */
	@Override
	public Integer updateInfoBeautyById(InfoBeauty bean, Integer id){
		return this.infoBeautyMapper.updateById(bean,id);
	}
	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteInfoBeautyById(Integer id){
		return this.infoBeautyMapper.deleteById(id);
	}
	/**
	 * 根据Email查询
	 */
	@Override
	public InfoBeauty getInfoBeautyByEmail(String email){
		return this.infoBeautyMapper.selectByEmail(email);
	}
	/**
	 * 根据Email更新
	 */
	@Override
	public Integer updateInfoBeautyByEmail(InfoBeauty bean, String email){
		return this.infoBeautyMapper.updateByEmail(bean,email);
	}
	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteInfoBeautyByEmail(String email){
		return this.infoBeautyMapper.deleteByEmail(email);
	}
	/**
	 * 根据UserId查询
	 */
	@Override
	public InfoBeauty getInfoBeautyByUserId(String userId){
		return this.infoBeautyMapper.selectByUserId(userId);
	}
	/**
	 * 根据UserId更新
	 */
	@Override
	public Integer updateInfoBeautyByUserId(InfoBeauty bean, String userId){
		return this.infoBeautyMapper.updateByUserId(bean,userId);
	}
	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteInfoBeautyByUserId(String userId){
		return this.infoBeautyMapper.deleteByUserId(userId);
	}

	@Override
	public void saveAccount(InfoBeauty beauty) throws BusinessException {
		if (beauty.getId() != null) {
			InfoBeauty dbInfo=this.infoBeautyMapper.selectById(beauty.getId());
			if (BeautyAccountStatusEnum.USEED.getStatus().equals(dbInfo.getStatus())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}
		InfoBeauty dbInfo=this.infoBeautyMapper.selectByEmail(beauty.getEmail());
		//新增时判断邮箱是否存在
		if (beauty.getId()==null&&dbInfo!=null) {
			throw new BusinessException("邮箱靓号已存在");
		}
		//修改时判断邮箱是否存在
		if(beauty.getId()!=null&&dbInfo!=null&&dbInfo.getId()!=null&& !beauty.getId().equals(dbInfo.getId())){
			throw new BusinessException("靓号邮箱已存在");
		}

		//判断靓号是否已经存在
		dbInfo=this.infoBeautyMapper.selectByUserId(beauty.getUserId());
		if (beauty.getId()==null&&dbInfo!=null) {
			throw new BusinessException("靓号已存在");
		}
		if(beauty.getId()!=null&&dbInfo!=null&&dbInfo.getId()!=null&& !beauty.getId().equals(dbInfo.getId())){
			throw new BusinessException("靓号已存在");
		}

		//判断邮箱是否已经注册
		Info userInfo=this.infoMapper.selectByEmail(beauty.getEmail());
		if (userInfo != null) {
			throw new BusinessException("靓号邮箱已经注册");
		}
		userInfo=this.infoMapper.selectByUserId(beauty.getUserId());
		if (userInfo != null) {
			throw new BusinessException("靓号已被注册");
		}
		if (beauty.getId()!=null) {
			this.infoBeautyMapper.updateById(beauty,beauty.getId());
		}else {
			beauty.setStatus(BeautyAccountStatusEnum.NO_USE.getStatus());
			this.infoBeautyMapper.insert(beauty);
		}
	}
}