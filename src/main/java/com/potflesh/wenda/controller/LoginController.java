package com.potflesh.wenda.controller;

import com.potflesh.wenda.async.EventProducer;
import com.potflesh.wenda.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by bazinga on 2017/4/11.
 */
@Controller
public class LoginController {

    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;

    private static final Logger logger= LoggerFactory.getLogger(LoginController.class);

    // 注册
    @RequestMapping(path = {"/reg/"}, method = {RequestMethod.POST})
    public String index(Model model,
                        @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam("next") String next,
                        @RequestParam(value="rememberme", defaultValue = "false") boolean rememberme,
                        HttpServletResponse response) {

        try{

            Map<String,String> map = userService.register(username,password);

            if (map.containsKey("ticket")) {

                Cookie cookie =
                        new Cookie("ticket", map.get("ticket").toString());

                cookie.setPath("/");

                if (rememberme) {
                    cookie.setMaxAge(3600*24*5);
                }
                response.addCookie(cookie);

                if (StringUtils.isNotBlank(next)) {
                    return "redirect:" + next;
                }

                return "redirect:/";

            } else {

                model.addAttribute("msg", map.get("msg"));

                return "login";
            }

        }catch (Exception e){

                logger.error("注册异常" + e.getMessage());

                return "login";
        }
    }


    // 注册页面 注意后面一定要跟上 '／'
    @RequestMapping(path = {"/reglogin"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String reg(Model model,
                      @RequestParam(value="next", required = false) String next) {
        // 设置返回的 next 的地址
        model.addAttribute("next",next);
        return "login";
    }



    // 登录
    @RequestMapping(path = {"/login/"}, method = {RequestMethod.POST})
    public String login(Model model,
                        @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value="next", required = false) String next,
                        @RequestParam(value = "rememberme",defaultValue = "false")
                        boolean rememberme,
                        HttpServletResponse response) {
        try {

            Map<String, String> map = userService.login(username, password);

            if (map.containsKey("ticket")) {
                //  将 cookie 下发到客户端
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");

                // 如果用户点击 记住我
                if (rememberme) {
                    cookie.setMaxAge(3600*24*5);
                }

                response.addCookie(cookie);

                //  下面为发送邮件的部分
               /** eventProducer.fireEvent(new EventModel(EventType.LOGIN)
                        .setExt("username", username).setExt("email", "xxxx@qq.com")
                        .setActorId(WendaUtil.SYSTEM_USERID));**/

                // 如果包含 next 值证明用户是从别的页面跳过来的，再跳回去
                if (StringUtils.isNotBlank(next)) {
                    return "redirect:" + next;
                }
                return "redirect:/";

            } else {
                model.addAttribute("msg", map.get("msg"));
                return "login";
            }

        } catch (Exception e) {
            logger.error("登陆异常" + e.getMessage());
            return "login";
        }
    }

    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/";
    }


}
