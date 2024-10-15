package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.config.AppConfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.enums.AppUpdateFileTypeEnum;
import com.easychat.entity.po.Update;
import com.easychat.entity.vo.AppUpdateVo;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.UpdateService;
import com.easychat.utils.CopyTools;
import com.easychat.utils.StringTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

@RestController("updateController")
@RequestMapping("/update")
public class UpdateController extends ABaseController{

    @Resource
    private AppConfig appConfig;

    @Resource
    private UpdateService updateService;

    @RequestMapping("/checkVersion")
    @GlobalInterceptor
    private ResponseVO checkVersion(String appVersion,String uid){
        if (StringTools.isEmpty(appVersion)) {
            return getSuccessResponseVO(null);
        }
        Update appUpdate=updateService.getLatestUpdate(appVersion,uid);
        if (appUpdate == null) {
            return getSuccessResponseVO(null);
        }
        AppUpdateVo appUpdateVo= CopyTools.copy(appUpdate,AppUpdateVo.class);
        if (AppUpdateFileTypeEnum.LOCAL.getType().equals(appUpdate.getFileType())) {
            File file=new File(appConfig.getProjectFolder()+ Constants.APP_UPDATE_FOLDER+appUpdate.getId()+Constants.APP_EXE_SUFFIX);
            appUpdateVo.setSize(file.length());
        }else {
            appUpdateVo.setSize(0L);
        }
        appUpdateVo.setUpdateList(Arrays.asList(appUpdate.getUpdateDescArray()));
        String fileName=Constants.APP_NAME+appUpdate.getVersion()+Constants.APP_EXE_SUFFIX;
        appUpdateVo.setFileName(fileName);
        return getSuccessResponseVO(appUpdateVo);
    }

}
