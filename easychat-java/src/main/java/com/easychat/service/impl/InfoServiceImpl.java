package com.easychat.service.impl;
import com.easychat.entity.config.AppConfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.Contact;
import com.easychat.entity.po.InfoBeauty;
import com.easychat.entity.query.ContactQuery;
import com.easychat.entity.query.SimplePage;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.po.Info;
import com.easychat.entity.query.InfoQuery;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import com.easychat.mapper.ContactMapper;
import com.easychat.mapper.InfoBeautyMapper;
import com.easychat.mapper.InfoMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.service.ContactService;
import com.easychat.service.InfoService;
import com.easychat.service.SessionUserService;
import com.easychat.utils.CopyTools;
import com.easychat.utils.StringTools;
import com.easychat.websocket.MessageHandler;
import org.apache.catalina.User;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 用户信息Service
 * @author null
 * @Date 2024/05/22
 */
@Service("InfoService")
public class InfoServiceImpl implements InfoService{

	@Resource
	private InfoMapper<Info,InfoQuery> infoMapper;
	/**
	 * 根据条件查询列表
	 */

	@Resource
	private InfoBeautyMapper<InfoBeauty,InfoQuery> infoBeautyMapper;

	@Resource
	private AppConfig appConfig;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private ContactMapper<Contact,ContactQuery> contactMapper;

	@Resource
	private ContactService contactService;

	@Resource
	private SessionUserService sessionUserService;

	@Resource
	private MessageHandler messageHandler;


	@Override
	public List<Info> findListByParam(InfoQuery query){
		return this.infoMapper.selectList(query);
	}
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(InfoQuery query){
		return this.infoMapper.selectCount(query);
	}
	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<Info> findListByPage(InfoQuery query){
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize()==null?PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<Info> list = this.findListByParam(query);
		PaginationResultVO<Info> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	/**
	 * 新增
	 */
	@Override
	public Integer add(InfoQuery bean){
		return this.infoMapper.insert(bean);
	}
	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<InfoQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.infoMapper.insertBatch(listBean);
	}
	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<InfoQuery> listBean){
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.infoMapper.insertOrUpdateBatch(listBean);
	}
	/**
	 * 根据UserId查询
	 */
	@Override
	public Info getInfoByUserId(String userId){
		return this.infoMapper.selectByUserId(userId);
	}
	/**
	 * 根据UserId更新
	 */
	@Override
	public Integer updateInfoByUserId(Info bean, String userId){
		return this.infoMapper.updateByUserId(bean,userId);
	}
	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteInfoByUserId(String userId){
		return this.infoMapper.deleteByUserId(userId);
	}
	/**
	 * 根据Email查询
	 */
	@Override
	public Info getInfoByEmail(String email){
		return this.infoMapper.selectByEmail(email);
	}
	/**
	 * 根据Email更新
	 */
	@Override
	public Integer updateInfoByEmail(Info bean, String email){
		return this.infoMapper.updateByEmail(bean,email);
	}
	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteInfoByEmail(String email){
		return this.infoMapper.deleteByEmail(email);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void register(String email, String nickName, String password) throws BusinessException {
		Map<String, Object> result = new HashMap<>();
		Info info = this.infoMapper.selectByEmail(email);
		if (null != info) {
			throw new BusinessException("邮箱帐号已存在");
		}
		String userId = StringTools.getUserId();
		InfoBeauty beautyAccount = this.infoBeautyMapper.selectByEmail(email);
		Boolean useBeautyAccount = null != beautyAccount && BeautyAccountStatusEnum.NO_USE.getStatus().equals(beautyAccount.getStatus());
		if (useBeautyAccount) {
			userId = UserContactTypeEnum.USER.getPrefix() + beautyAccount.getUserId();
		}

		Date curData = new Date();
		info = new Info();
		info.setUserId(userId);
		info.setNickName(nickName);
		info.setEmail(email);
		info.setPassword(StringTools.encodeMd5(password));
		info.setCreateTime(curData);
		info.setStatus(UserStatusEnum.ENABLE.getStatus());
		info.setLastOffTime(curData.getTime());
		info.setJoinType(JoinTypeEnum.APPLY.getType());
		this.infoMapper.insert(info);
		if (useBeautyAccount) {
			InfoBeauty updateBeauty = new InfoBeauty();
			updateBeauty.setStatus(BeautyAccountStatusEnum.USEED.getStatus());
			this.infoBeautyMapper.updateById(updateBeauty, beautyAccount.getId());
		}
		contactService.addContact4Robot(userId);
	}

	@Override
	public UserInfoVO login(String email, String password) throws BusinessException {
		Info info=this.infoMapper.selectByEmail(email);
		if (null==info || info.getPassword().equals(password)){
			throw new BusinessException("账号或者密码不存在");
		}
		if (UserStatusEnum.DISABLE.equals(info.getStatus())){
			throw new BusinessException("账号已禁用");
		}
		//查询联系人
		ContactQuery contactQuery=new ContactQuery();
		contactQuery.setUserId(info.getUserId());
		contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		List<Contact> contactList=contactMapper.selectList(contactQuery);
		List<String> contactIdList=contactList.stream().map(item->item.getContactId()).collect(Collectors.toList());
		redisComponent.cleanUserContact(info.getUserId());
		if (!contactIdList.isEmpty()) {
			redisComponent.addUserContactBatch(info.getUserId(),contactIdList);
		}

		TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(info);

		Long lastHeartBeat = redisComponent.getUserHeartBeat(info.getUserId());
		if (lastHeartBeat != null) {
			throw new BusinessException("此账号已在别处登录，请退出后再登录");
		}
		//保存登录信息到redis中
		String token=StringTools.encodeMd5(tokenUserInfoDto.getUserId()+StringTools.getRandomString(Constants.LENGTH_20));
		tokenUserInfoDto.setToken(token);
		redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

		UserInfoVO userInfoVO= CopyTools.copy(info, UserInfoVO.class);
		userInfoVO.setToken(tokenUserInfoDto.getToken());
		userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
		return userInfoVO;
	}
	private TokenUserInfoDto getTokenUserInfoDto(Info info){
		TokenUserInfoDto tokenUserInfoDto=new TokenUserInfoDto();
		tokenUserInfoDto.setUserId(info.getUserId());
		tokenUserInfoDto.setNickName(info.getNickName());
		String adminEmails= appConfig.getAdminEmails();
		if (!StringTools.isEmpty(adminEmails) && ArrayUtils.contains(adminEmails.split(","),info.getEmail())){
			tokenUserInfoDto.setAdmin(true);
		}else {
			tokenUserInfoDto.setAdmin(false);
		}
		return tokenUserInfoDto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUserInfo(Info userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
		if (avatarFile == null) {
			String baseFolder=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE;
			File targetFileFolder=new File(baseFolder+Constants.FILE_FOLDER_AVATAR_NAME);
			if (!targetFileFolder.exists()) {
				targetFileFolder.mkdirs();
			}
			String filePath= targetFileFolder.getPath()+"/"+userInfo.getUserId()+Constants.IMAGE_SUFFIX;
			avatarFile.transferTo(new File(filePath));
			avatarCover.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));
		}
		Info dbInfo=this.infoMapper.selectByUserId(userInfo.getUserId());
		/**
		 * 先查询后更新所用时间消耗少，因为在查询时事务还未开启。先更新，事务已开启到查询完成后才提交
		 */
		this.infoMapper.updateByUserId(userInfo, userInfo.getUserId());
		String contactNameUpdate=null;
		if (!dbInfo.getNickName().equals(userInfo.getNickName())) {
			contactNameUpdate=userInfo.getNickName();
		}
		if (contactNameUpdate == null) {
			return;
		}

		TokenUserInfoDto tokenUserInfoDto=redisComponent.getTokenUserInfoDtoByUser(userInfo.getUserId());
		tokenUserInfoDto.setNickName(contactNameUpdate);
		redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
		sessionUserService.updateRedundancyInfo(contactNameUpdate, userInfo.getUserId());
	}

	@Override
	public void updateUserStatus(Integer status, String userId) throws BusinessException {
		UserStatusEnum userStatusEnum=UserStatusEnum.getByStatus(status);
		if (userStatusEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		Info userInfo=new Info();
		userInfo.setStatus(userStatusEnum.getStatus());
		this.infoMapper.updateByUserId(userInfo,userId);
	}

	@Override
	public void forceOffLine(String userId) {
		MessageSendDto messageSendDto=new MessageSendDto();
		messageSendDto.setContactType(UserContactTypeEnum.USER.getType());
		messageSendDto.setMessageType(MessageTypeEnum.FORCE_OFF_LINE.getType());
		messageSendDto.setContactId(userId);
		messageHandler.sendMessage(messageSendDto);
	}
}