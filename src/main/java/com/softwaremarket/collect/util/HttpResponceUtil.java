package com.softwaremarket.collect.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.CollectionUtils;


public class HttpResponceUtil {
    public static Boolean requestSoftIsSuccess(JSONObject response) {

        return (!CollectionUtils.isEmpty(response))&& response.getIntValue("code")==200 && response.containsKey("data");
    }
}
