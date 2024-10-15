package com.easychat.utils;

import com.easychat.entity.constants.Constants;
import com.easychat.entity.enums.UserContactTypeEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;

public class StringTools {

    public static boolean isEmpty(String str){
        if (null ==str  ||"".equals(str)||"null".equals(str)||"\u0000".equals(str)) {
            return true;
        }else if ("".equals(str.trim())){
            return true;
        }
        return false;
    }

    public static String getUserId(){
        return UserContactTypeEnum.USER.getPrefix() +getRandomNumber(Constants.LENGTH_11);
    }

    public static String getGroupId(){
        return UserContactTypeEnum.GROUP.getPrefix() +getRandomNumber(Constants.LENGTH_11);
    }

    public static String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }

    public static String getRandomString(Integer count){
        return RandomStringUtils.random(count,true,true);
    }

    public static final String encodeMd5(String originString){
        return StringUtils.isEmpty(originString) ? null: DigestUtils.md5Hex(originString);
    }

    public static String cleanHtmlTag(String content){
        if (isEmpty(content)) {
            return content;
        }
        content=content.replace("<","&lt");
        content=content.replace("\r\n","<br>");
        content=content.replace("\n","<br>");
        return content;
    }
    public static final String getChatSessionId4User(String[] userId){
        Arrays.sort(userId);
        return encodeMd5(StringUtils.join(userId,""));
    }
    public static final String getChatSessionId4Group(String groupId){
        return encodeMd5(groupId);
    }

    public static String getFileSuffix(String fileName){
        if (isEmpty(fileName)) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static boolean isNumber(String str){
        String checkNumber="^[0-9]+$";
        if (str == null) {
            return false;
        }
        if (!str.matches(checkNumber)) {
            return false;
        }
        return true;
    }

}
