package com.potflesh.wenda.controller;

import com.potflesh.wenda.async.EventProducer;
import com.potflesh.wenda.dao.LoginTicketDAO;
import com.potflesh.wenda.model.LoginTicket;
import com.potflesh.wenda.service.UserService;
import com.potflesh.wenda.utils.WendaUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

            Map<String,String> map = userService.register(username,password,null,null);

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


////////////////////////////////////////////////////////////////////////// api interface ////////////////////////////////////////////////////

    // 登录
    @RequestMapping(path = {"api/login/"}, method = {RequestMethod.POST})
    @ResponseBody
    public String userLogin(@RequestBody Map<String, Object> reqMap,
                        HttpServletResponse response) {
        try {
            String username = reqMap.get("username").toString();
            String password = reqMap.get("password").toString();
            //  登录成功后返回 ticket 用于返回 cookie 标示用户
            Map<String, String> map = userService.login(username, password);
            Map<String, Object> msgMaps = new HashedMap();
            if (map.containsKey("ticket")) {
                //  将 cookie 下发到客户端
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                response.addCookie(cookie);
                // 返回登录成功消息
                msgMaps.put("status", "success");
                msgMaps.put("msg", "登录成功");
                msgMaps.put("user", userService.selectByName(username));
                msgMaps.put("token", map.get("ticket").toString());
                System.out.println("login:" + map.get("ticket").toString());
                return WendaUtil.getJSONString(200, msgMaps);
            } else {
                // 登录失败
                msgMaps.put("status", "fail");
                msgMaps.put("msg", "密码或用户名不匹配");
                return WendaUtil.getJSONString(200, msgMaps);
            }
        } catch (Exception e) {
            logger.error("登陆异常" + e.getMessage());
            Map<String, Object> msgMaps = new HashedMap();
            msgMaps.put("status", "fail");
            msgMaps.put("msg", "登录状态异常");
            return WendaUtil.getJSONString(200, msgMaps);
        }
    }

    // 注册
    @RequestMapping(path = {"api/reg/"}, method = {RequestMethod.POST})
    @ResponseBody
    public String userRegister(@RequestBody Map<String, Object> reqMap,
                        HttpServletResponse response) {

        try{
            String username = reqMap.get("username").toString();
            String password = reqMap.get("password").toString();
            String mail = reqMap.get("mail").toString();
            String describe = reqMap.get("describe").toString();
            Map<String,String> map = userService.register(username, password, mail, describe);
            Map<String, Object> msgMaps = new HashedMap();
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                msgMaps.put("status", "success");
                msgMaps.put("msg", "注册成功");
                return WendaUtil.getJSONString(200, msgMaps);
            } else {
                msgMaps.put("status", "success");
                msgMaps.put("msg", "注册失败");
                return WendaUtil.getJSONString(200, msgMaps);
            }
        }catch (Exception e){
            logger.error("注册异常" + e.getMessage());
            Map<String, Object> msgMaps = new HashedMap();
            msgMaps.put("status", "fail");
            msgMaps.put("msg", "注册状态异常");
            return WendaUtil.getJSONString(200, msgMaps);
        }
    }
}
