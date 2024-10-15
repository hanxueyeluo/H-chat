package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.po.Info;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.service.InfoService;
import com.easychat.utils.CopyTools;
import com.easychat.utils.StringTools;
import com.easychat.websocket.ChannelContextUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.IOException;

@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController{

    @Resource
    private InfoService infoService;

    @Resource
    private ChannelContextUtils channelContextUtils;


    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    private ResponseVO getUserInfo(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        Info userInfo=infoService.getInfoByUserId(tokenUserInfoDto.getUserId());
        UserInfoVO userInfoVO= CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
        return getSuccessResponseVO(userInfoVO);
    }

    @RequestMapping("/saveUserInfo")
    @GlobalInterceptor
    private ResponseVO saveUserInfo(HttpServletRequest request, Info userInfo, MultipartFile avatarFile,MultipartFile avatarCover) throws IOException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);

        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setPassword(null);
        userInfo.setStatus(null);
        userInfo.setCreateTime(null);
        userInfo.setLastLoginTime(null);

        this.infoService.updateUserInfo(userInfo,avatarFile,avatarCover);

        return getUserInfo(request);
    }

    @RequestMapping("/updatePassword")
    @GlobalInterceptor
    private ResponseVO updatePassword(HttpServletRequest request,@NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        Info userInfo=new Info();
        userInfo.setPassword(StringTools.encodeMd5(password));
        this.infoService.updateInfoByUserId(userInfo,tokenUserInfoDto.getUserId());
        channelContextUtils.closeContext(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/logout")
    @GlobalInterceptor
    private ResponseVO logout(HttpServletRequest request,@NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        channelContextUtils.closeContext(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

}
