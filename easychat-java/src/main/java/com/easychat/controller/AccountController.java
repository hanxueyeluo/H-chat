package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import com.easychat.redis.RedisComponent;
import com.easychat.redis.RedisUtils;
import com.easychat.service.InfoService;
import com.easychat.websocket.MessageHandler;
import com.wf.captcha.ArithmeticCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController{

    private static final Logger logger= LoggerFactory.getLogger(AccountController.class);

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private InfoService infoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;


    @RequestMapping("/checkCode")
    public ResponseVO checkCode(){
        ArithmeticCaptcha captcha=new ArithmeticCaptcha(100,42);
        String code=captcha.text();
        String checkCodeKey= UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey,code,Constants.REDIS_TIME_MIN1*10);
        String checkCodeBase64=captcha.toBase64();
        Map<String,String> result=new HashMap<>();
        result.put("checkCode",checkCodeBase64);
        result.put("checkCodeKey",checkCodeKey);
        return getSuccessResponseVO(result);
    }
    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                               @NotEmpty String nickName,
                               @NotEmpty String checkCode) throws BusinessException {
            try {
                if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey))){
                    throw  new BusinessException("图片验证码错误");
                }
                infoService.register(email,nickName,password);
                return getSuccessResponseVO(null);
            }finally {
                redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
            }
    }
    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String checkCode) throws BusinessException {
        try {
            if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey))){
                throw  new BusinessException("图片验证码错误");
            }
            UserInfoVO userInfoVO=infoService.login(email,password);
            return getSuccessResponseVO(userInfoVO);
        }finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
        }
    }
    @GlobalInterceptor
    @RequestMapping("/getSysSetting")
    public ResponseVO getSysSetting() {
        return getSuccessResponseVO(redisComponent.getSysSetting());
    }

    @RequestMapping("/test")
    public ResponseVO test(){
        MessageSendDto sendDto=new MessageSendDto();
        sendDto.setMessageContent("kkkkkkk"+System.currentTimeMillis());
        messageHandler.sendMessage(sendDto);
        return getSuccessResponseVO(null);
    }
}
