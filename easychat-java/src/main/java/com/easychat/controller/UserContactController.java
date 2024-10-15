package com.easychat.controller;


import cn.hutool.core.util.ArrayUtil;
import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.dto.UserContactSearchResultDto;
import com.easychat.entity.enums.PageSize;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.enums.UserContactTypeEnum;
import com.easychat.entity.po.Contact;
import com.easychat.entity.po.Info;
import com.easychat.entity.query.ContactApplyQuery;
import com.easychat.entity.query.ContactQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.ContactApplyService;
import com.easychat.service.ContactService;
import com.easychat.service.InfoService;
import com.easychat.utils.CopyTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/contact")
public class UserContactController extends ABaseController{

    @Resource
    private ContactService contactService;

    @Resource
    private InfoService infoService;

    @Resource
    private ContactApplyService contactApplyService;

    @RequestMapping("/search")
    @GlobalInterceptor
    private ResponseVO search(HttpServletRequest request,@NotEmpty String contactId){
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        UserContactSearchResultDto resultDto=contactService.searchContact(tokenUserInfoDto.getUserId(),contactId);
        return getSuccessResponseVO(resultDto);
    }

    @RequestMapping("/applyAdd")
    @GlobalInterceptor
    private ResponseVO applyAdd(HttpServletRequest request,@NotEmpty String contactId,String applyInfo) throws BusinessException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        Integer joinType=contactApplyService.applyAdd(tokenUserInfoDto,contactId,applyInfo);
        return getSuccessResponseVO(joinType);
    }

    @RequestMapping("/loadApply")
    @GlobalInterceptor
    private ResponseVO loadApply(HttpServletRequest request,Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);

        ContactApplyQuery applyQuery=new ContactApplyQuery();
        applyQuery.setOrderBy("last_apply_time desc");
        applyQuery.setReceiveUserId(tokenUserInfoDto.getUserId());
        applyQuery.setPageNo(pageNo);
        applyQuery.setPageSize(PageSize.SIZE15.getSize());
        applyQuery.setQueryContactInfo(true);
        PaginationResultVO resultVO=contactApplyService.findListByPage(applyQuery);
        return getSuccessResponseVO(resultVO);
    }


    @RequestMapping("/dealWithApply")
    @GlobalInterceptor
    private ResponseVO dealWithApply(HttpServletRequest request,@NotNull Integer applyId,@NotNull Integer status) throws BusinessException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        this.contactApplyService.dealWithApply(tokenUserInfoDto.getUserId(),applyId,status);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadContact")
    @GlobalInterceptor
    private ResponseVO loadContact(HttpServletRequest request,@NotNull String contactType) throws BusinessException {
        UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByName(contactType);
        if (contactTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        ContactQuery contactQuery=new ContactQuery();
        contactQuery.setUserId(tokenUserInfoDto.getUserId());
        contactQuery.setContactType(contactTypeEnum.getType());
        if (contactTypeEnum.USER == contactTypeEnum) {
            contactQuery.setQueryContactUserInfo(true);
        } else if (contactTypeEnum.GROUP==contactTypeEnum) {
            contactQuery.setQueryGroupInfo(true);
            contactQuery.setExcludeMyGroup(true);
        }
        contactQuery.setOrderBy("last_update_time desc");
        contactQuery.setStatusArray(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus()});
        List<Contact> contactList=contactService.findListByParam(contactQuery);
        return getSuccessResponseVO(contactList);
    }


    /**
     * 获取联系人信息 不一定是好友
     * @param request
     * @param contactId
     * @return
     * @throws BusinessException
     */

    @RequestMapping("/getContactInfo")
    @GlobalInterceptor
    private ResponseVO getContactInfo(HttpServletRequest request,@NotNull String contactId)  {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        Info userinfo=infoService.getInfoByUserId(contactId);
        UserInfoVO userInfoVO= CopyTools.copy(userinfo, UserInfoVO.class);
        userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());
        Contact contact=contactService.getContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),contactId);
        if (contact != null) {
            userInfoVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        }
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 获取兰溪人信息 必须是好友
     * @param request
     * @param contactId
     * @return
     * @throws BusinessException
     */

    @RequestMapping("/getContactUserInfo")
    @GlobalInterceptor
    private ResponseVO getContactUserInfo(HttpServletRequest request,@NotNull String contactId) throws BusinessException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        Contact contact=contactService.getContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),contactId);
        if (contact == null||!ArrayUtil.contains(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST.getStatus(),
        },contact.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        Info userInfo=infoService.getInfoByUserId(contactId);
        UserInfoVO userInfoVO=CopyTools.copy(userInfo, UserInfoVO.class);
        return getSuccessResponseVO(userInfoVO);
    }

    @RequestMapping("/delContact")
    @GlobalInterceptor
    private ResponseVO delContact(HttpServletRequest request,@NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        contactService.removeUserContact(tokenUserInfoDto.getUserId(),contactId,UserContactStatusEnum.DEL);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/addContact2BlackList")
    @GlobalInterceptor
    private ResponseVO addContact2BlackList(HttpServletRequest request,@NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        contactService.removeUserContact(tokenUserInfoDto.getUserId(),contactId,UserContactStatusEnum.BLACKLIST);
        return getSuccessResponseVO(null);
    }

}
