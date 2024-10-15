package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.po.InfoBeauty;
import com.easychat.entity.query.InfoBeautyQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.InfoBeautyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@RestController("adminUserInfoBeautyController")
@RequestMapping("/admin")
public class AdminUserInfoBeautyController extends ABaseController {

    @Resource
    private InfoBeautyService infoBeautyService;

    @RequestMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO loadBeautyAccountList(InfoBeautyQuery query) {
        query.setOrderBy("id desc");
        PaginationResultVO resultVO = infoBeautyService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/saveBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO saveBeautyAccount(InfoBeauty beauty) throws BusinessException {
        infoBeautyService.saveAccount(beauty);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO delBeautyAccount(@NotNull Integer id) {
        infoBeautyService.deleteInfoBeautyById(id);
        return getSuccessResponseVO(null);
    }

}