package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.config.AppConfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.entity.po.Message;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.MessageService;
import com.easychat.service.SessionUserService;
import com.easychat.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

@Component
@RequestMapping("/chat")
public class ChatController extends ABaseController{
    private static final Logger logger= LoggerFactory.getLogger(ChatController.class);

    @Resource
    private MessageService messageService;

    @Resource
    private SessionUserService sessionUserService;

    @Resource
    private AppConfig appConfig;

    @RequestMapping("/sendMessage")
    @GlobalInterceptor
    public ResponseVO sendMessage(HttpServletRequest request,
                                  @NotEmpty String contactId,
                                  @NotEmpty @Max(500) String messageContent,
                                  @NotNull Integer messageType,
                                  Long fileSize,
                                  String fileName,
                                  Integer fileType) throws BusinessException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        Message chatMessage=new Message();
        chatMessage.setContactId(contactId);
        chatMessage.setContent(messageContent);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setFileType(fileType);
        chatMessage.setType(messageType);
        MessageSendDto messageSendDto=messageService.saveMessage(chatMessage,tokenUserInfoDto);
        return getSuccessResponseVO(messageSendDto);
    }

    @RequestMapping("/upLoadFile")
    @GlobalInterceptor
    public ResponseVO upLoadFile(HttpServletRequest request, @NotNull Long messageId,
                                 @NotNull MultipartFile file,
                                 @NotNull MultipartFile cover) throws BusinessException {
        TokenUserInfoDto userInfoDto=getTokenUserInfo(request);
        messageService.saveMessageFile(userInfoDto.getUserId(),messageId,file,cover);
        return getSuccessResponseVO(null);
    }
    @RequestMapping("/downloadFile")
    @GlobalInterceptor
    public void downloadFile(HttpServletRequest request, HttpServletResponse response,
                                   @NotEmpty String fileId,@NotNull Boolean showCover) {

        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        OutputStream outputStream=null;
        FileInputStream inputStream=null;
        try {
            File file=null;
            if (StringTools.isNumber(fileId)){
                String avatarFolderName= Constants.FILE_FOLDER_FILE+Constants.FILE_FOLDER_AVATAR_NAME;
                String avatarPath=appConfig.getProjectFolder()+avatarFolderName+fileId+Constants.IMAGE_SUFFIX;
                if (showCover) {
                    avatarPath=avatarPath+Constants.COVER_IMAGE_SUFFIX;
                }
                file=new File(avatarPath);
                if (!file.exists()) {
                    throw new BusinessException(ResponseCodeEnum.CODE_602);
                }else {
                  file=messageService.downloadFile(tokenUserInfoDto,Long.parseLong(fileId),showCover);
                }
            }
            response.setContentType("application/x-msdownload/charset=UTF-8");
            response.setHeader("Content-Disposition","attachment");
            response.setContentLengthLong(file.length());
            inputStream=new FileInputStream(file);
            byte[] byteData=new byte[1024];
            outputStream=response.getOutputStream();
            int len;
            while ((len=inputStream.read(byteData))!=-1){
                outputStream.write(byteData,0,len);
            }
            outputStream.flush();
        }catch (Exception e){
            logger.error("下载文件失败",e);
        }finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                }catch (Exception e){
                    logger.error("Io异常",e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                }catch (Exception e){
                    logger.error("Io异常",e);
                }
            }
        }
    }
}
