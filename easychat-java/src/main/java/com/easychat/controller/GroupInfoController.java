package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.GroupStatusEnum;
import com.easychat.entity.enums.MessageTypeEnum;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.po.Contact;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.query.ContactQuery;
import com.easychat.entity.query.GroupInfoQuery;
import com.easychat.entity.vo.GroupInfoVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.ContactService;
import com.easychat.service.GroupInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController("groupInfoController")
@RequestMapping("/group")
@Validated
public class GroupInfoController extends ABaseController{
    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private ContactService contactService;

    @RequestMapping("/saveGroup")
    @GlobalInterceptor
    public ResponseVO saveGroup(HttpServletRequest request,
                                String groupId,
                                @NotEmpty String groupName,
                                String groupNotice,
                                @NotNull Integer joinType,
                                MultipartFile avatarFile,
                                MultipartFile avatarCover) throws BusinessException, IOException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);

        GroupInfo groupInfo=new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setJoinType(joinType);
        this.groupInfoService.saveGroup(groupInfo,avatarFile,avatarCover);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadMyGroup")
    @GlobalInterceptor
    public ResponseVO loadMyGroup(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        GroupInfoQuery groupInfoQuery=new GroupInfoQuery();
        groupInfoQuery.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfoQuery.setOrderBy("create_time desc");
        List<GroupInfo> groupInfoList=this.groupInfoService.findListByParam(groupInfoQuery);
        return getSuccessResponseVO(groupInfoList);
    }

    @RequestMapping("/getGroupInfo")
    @GlobalInterceptor
    public ResponseVO getGroupInfo(HttpServletRequest request,@NotEmpty String groupId) throws BusinessException {
        GroupInfo groupInfo=getGroupDetailCommon(request,groupId);
        ContactQuery contactQuery=new ContactQuery();
        contactQuery.setContactId(groupId);
        Integer memberCount=this.contactService.findCountByParam(contactQuery);
        groupInfo.setMemberCount(memberCount);
        return getSuccessResponseVO(groupInfo);
    }

    private GroupInfo getGroupDetailCommon(HttpServletRequest request,String groupId) throws BusinessException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);

        Contact contact=this.contactService.getContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),groupId);
        if (contact == null||!UserContactStatusEnum.FRIEND.getStatus().equals(contact.getStatus())) {
            throw new BusinessException("你不在群聊或者群聊不存在或已解散");
        }
        GroupInfo groupInfo=this.groupInfoService.getGroupInfoByGroupId(groupId);
        if (groupInfo == null||!GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())) {
            throw new BusinessException("群聊不存在或已解散");
        }
        return groupInfo;
    }

    @RequestMapping("/getGroupInfo4Chat")
    @GlobalInterceptor
    public ResponseVO getGroupInfo4Chat(HttpServletRequest request,@NotEmpty String groupId) throws BusinessException {
        GroupInfo groupInfo=getGroupDetailCommon(request,groupId);
        ContactQuery contactQuery=new ContactQuery();
        contactQuery.setContactId(groupId);
        contactQuery.setQueryUserInfo(true);
        contactQuery.setOrderBy("create_time asc");
        contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<Contact> userContactList=this.contactService.findListByParam(contactQuery);
        GroupInfoVO groupInfoVO=new GroupInfoVO();
        groupInfoVO.setGroupInfo(groupInfo);
        groupInfoVO.setContactList(userContactList);
        return getSuccessResponseVO(groupInfoVO);
    }

    @RequestMapping("/addOrRemoveGroupUser")
    @GlobalInterceptor
    public ResponseVO addOrRemoveGroupUser(HttpServletRequest request,
                                           @NotEmpty String groupId,
                                           @NotEmpty String selectContacts,
                                           @NotNull Integer opType) throws BusinessException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        groupInfoService.addOrRemoveGroupUser(tokenUserInfoDto,groupId,selectContacts,opType);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/leaveGroup")
    @GlobalInterceptor
    public ResponseVO leaveGroup(HttpServletRequest request,
                                           @NotEmpty String groupId) throws BusinessException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        groupInfoService.leaveGroup(tokenUserInfoDto.getUserId(),groupId, MessageTypeEnum.LEAVE_GROUP);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor
    public ResponseVO dissolutionGroup(HttpServletRequest request,
                                 @NotEmpty String groupId) throws BusinessException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        groupInfoService.dissolutionGroup(tokenUserInfoDto.getUserId(),groupId);
        return getSuccessResponseVO(null);
    }

}
