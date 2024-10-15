package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.query.InfoQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.InfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController("adminUserInfoController")
@RequestMapping("/admin")
public class AdminUserInfoController extends ABaseController {

    @Resource
    private InfoService infoService;

    @RequestMapping("/loadUser")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO loadUser(InfoQuery userInfoQuery) {
        userInfoQuery.setOrderBy("create_time desc");
        PaginationResultVO resultVO = infoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO updateUserStatus(@NotNull Integer status,@NotEmpty String userId) throws BusinessException {
        infoService.updateUserStatus(status,userId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO forceOffLine(@NotEmpty String userId){
        infoService.forceOffLine(userId);
        return getSuccessResponseVO(null);
    }

}