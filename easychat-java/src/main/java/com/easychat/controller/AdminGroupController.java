package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.query.GroupInfoQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.GroupInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController("adminGroupController")
@RequestMapping("/admin")
public class AdminGroupController extends ABaseController {

    @Resource
    private GroupInfoService groupInfoService;

    @RequestMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO loadBeautyAccountList(GroupInfoQuery query) {
        query.setOrderBy("create_time desc");
        query.setQueryMemberCount(true);
        query.setQueryGroupOwnerName(true);
        PaginationResultVO resultVO = groupInfoService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO dissolutionGroup(@NotEmpty String groupId) throws BusinessException {
        GroupInfo groupInfo=this.groupInfoService.getGroupInfoByGroupId(groupId);
        if (groupInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        groupInfoService.dissolutionGroup(groupInfo.getGroupOwnerId(),groupId);
        return getSuccessResponseVO(null);
    }


}