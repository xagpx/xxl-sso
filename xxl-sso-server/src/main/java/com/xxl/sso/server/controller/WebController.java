package com.xxl.sso.server.controller;
import com.xxl.sso.core.conf.Conf;
import com.xxl.sso.core.login.SsoWebLoginHelper;
import com.xxl.sso.core.user.XxlSsoUser;
import com.xxl.sso.core.util.StringUtils;
import com.xxl.sso.server.core.model.UserInfo;
import com.xxl.sso.server.core.result.ReturnT;
import com.xxl.sso.server.core.store.SsoLoginStore;
import com.xxl.sso.server.core.util.JwtTokenUtil;
import com.xxl.sso.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * sso server (for web)
 *
 * @author xuxueli 2017-08-01 21:39:47
 */
@Controller
public class WebController {

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {

        // login check
    	String access_token = request.getParameter(Conf.ACCESS_TOKEN);
        XxlSsoUser xxlUser = SsoWebLoginHelper.loginCheck(access_token);

        if (xxlUser == null) {
            return "login";
//            return "redirect:/login";
        } else {
            model.addAttribute("xxlUser", xxlUser);
            model.addAttribute(Conf.ACCESS_TOKEN, access_token);
            return "index";
        }
    }

    /**
     * Login page
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(Conf.SSO_LOGIN)
    public String login(Model model, HttpServletRequest request, HttpServletResponse response) {

        // login check
    	String token = request.getParameter(Conf.ACCESS_TOKEN);
        XxlSsoUser xxlUser = SsoWebLoginHelper.loginCheck(token);

        if (xxlUser != null) {
            // success redirect
            String redirectUrl = request.getParameter(Conf.REDIRECT_URL);
            if (redirectUrl!=null && redirectUrl.trim().length()>0) {
            	String access_token = request.getParameter(Conf.ACCESS_TOKEN);
            	String redirectUrlFinal = redirectUrl + "?" + Conf.ACCESS_TOKEN + "=" + access_token;;
                return "redirect:" + redirectUrlFinal;
            } else {
                return "redirect:/";
            }
        }

        model.addAttribute("errorMsg", request.getParameter("errorMsg"));
        model.addAttribute(Conf.REDIRECT_URL, request.getParameter(Conf.REDIRECT_URL));
        return "login";
    }

    /**
     * Login
     *
     * @param request
     * @param redirectAttributes
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/doLogin")
    public String doLogin(HttpServletRequest request,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes,
                        String username,
                        String password,
                        String ifRemember) {

        boolean ifRem = (ifRemember!=null&&"on".equals(ifRemember))?true:false;

        // valid login
        ReturnT<UserInfo> result = userService.findUser(username, password);
        if (result.getCode() != ReturnT.SUCCESS_CODE) {
            redirectAttributes.addAttribute("errorMsg", result.getMsg());

            redirectAttributes.addAttribute(Conf.REDIRECT_URL, request.getParameter(Conf.REDIRECT_URL));
//            return "redirect:/login";
            return "login";
        }
        String ip=StringUtils.getIpAddr(request);
        // 1、make xxl-sso user
        XxlSsoUser xxlUser = new XxlSsoUser();
        xxlUser.setUserid(String.valueOf(result.getData().getUserid()));
        xxlUser.setUsername(result.getData().getUsername());
        xxlUser.setVersion(UUID.randomUUID().toString().replaceAll("-", ""));
        xxlUser.setExpireMinute(SsoLoginStore.getRedisExpireMinute());
        xxlUser.setExpireFreshTime(System.currentTimeMillis());
        xxlUser.setIp(ip);

        String sessionId = JwtTokenUtil.createJWT(xxlUser);

        SsoWebLoginHelper.login(response, sessionId, xxlUser, ifRem);

        // 4、return, redirect sessionId
        String redirectUrl = request.getParameter(Conf.REDIRECT_URL);
        if (redirectUrl!=null && redirectUrl.trim().length()>0) {
            String redirectUrlFinal = redirectUrl + "?"+ Conf.ACCESS_TOKEN + "=" + sessionId;
            return "redirect:" + redirectUrlFinal;
        } else {
            return "redirect:/?"+ Conf.ACCESS_TOKEN + "=" + sessionId;
        }

    }

    /**
     * Logout
     *
     * @param request
     * @param redirectAttributes
     * @return
     */
    @RequestMapping(Conf.SSO_LOGOUT)
    public String logout(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
    	String sessionId = request.getHeader(Conf.ACCESS_TOKEN);
        // logout
        SsoWebLoginHelper.logout(sessionId);

        redirectAttributes.addAttribute(Conf.REDIRECT_URL, request.getParameter(Conf.REDIRECT_URL));
//        return "redirect:/login";
        return "login";
    }
}