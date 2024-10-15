package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.config.AppConfig;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.po.Update;
import com.easychat.entity.query.UpdateQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.exception.BusinessException;
import com.easychat.redis.RedisComponent;
import com.easychat.service.UpdateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;


@RestController("adminAppUpdateController")
@RequestMapping("/admin")
public class AdminAppUpdateController extends ABaseController {

    @Resource
    private UpdateService updateService;


    @RequestMapping("/loadUpdateList")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO loadUpdateList(UpdateQuery query) {
        query.setOrderBy("id desc");
        PaginationResultVO resultVO=updateService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/saveUpdate")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO saveUpdate(Integer id,
                                  @NotEmpty String version,
                                  @NotEmpty String updateDesc,
                                  @NotNull Integer fileType,
                                  String outerLink,
                                  MultipartFile file) throws BusinessException, IOException {
        Update appUpdate=new Update();
        appUpdate.setId(id);
        appUpdate.setVersion(version);
        appUpdate.setFileType(fileType);
        appUpdate.setUpdateDesc(updateDesc);
        appUpdate.setOuterLink(outerLink);
        updateService.saveUpdate(appUpdate,file);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delUpdate")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO delUpdate(@NotNull Integer id) throws BusinessException {
       updateService.deleteUpdateById(id);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/postUpdate")
    @GlobalInterceptor(checkAdmin = true)
    private ResponseVO postUpdate(@NotNull Integer id,@NotNull Integer status,String grayscaleUid) throws BusinessException {
        updateService.postUpdate(id, status, grayscaleUid);
        return getSuccessResponseVO(null);
    }

}