package com.lhh.serverbase.utils;

import com.lhh.serverbase.common.constant.TokenConstants;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class HttpContextUtils {

	public static HttpServletRequest getHttpServletRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}

	public static String getToken(){
		HttpServletRequest request = getHttpServletRequest();
		return request.getHeader(TokenConstants.AUTHENTICATION);
	}

}
