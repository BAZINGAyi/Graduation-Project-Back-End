package com.potflesh.wenda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bazinga on 11/04/2018.
 */
@Controller
public class UtilController {

    @RequestMapping(value = "api/uploadImage", method = {RequestMethod.POST,RequestMethod.GET})
    String uploadImage(RedirectAttributes attr,
                       @RequestParam("editormd-image-file")MultipartFile file,
                       @RequestParam("dialog_id") String dialogId) throws Exception {
        Map map = new HashMap();
        if(!file.isEmpty()){
            try {
              /*
               * 这段代码执行完毕之后，图片上传到了工程的跟路径；
               * 大家自己扩散下思维，如果我们想把图片上传到 d:/files大家是否能实现呢？
               * 等等;
               * 这里只是简单一个例子,请自行参考，融入到实际中可能需要大家自己做一些思考，比如：
               * 1、文件路径；
               * 2、文件名；
               * 3、文件格式;
               * 4、文件大小的限制;
               */
                System.out.println(file.getOriginalFilename());
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file.getOriginalFilename())));
//				out.write(file.getBytes());
//				out.flush();
//				out.close();
                attr.addAttribute("dialog_id", dialogId);
                attr.addAttribute("message", "hello");
                attr.addAttribute("success", 1);
                attr.addAttribute("url", "yyyyyyy");
                return  "redirect:/api/testMarkdownZHANG";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                //return"上传失败,"+e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                //return"上传失败,"+e.getMessage();
            }
            //return"上传成功";
        }else{
            System.out.println("图片为空");
            //return"上传失败，因为文件是空的.";
        }
        return "redirect:/api/testMarkdownZHANG";
    }

    @RequestMapping(value = "api/testMarkdownZHANG", method = {RequestMethod.POST,RequestMethod.GET})
    String uploadImage(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestParam("dialog_id") String dialogId
            ) throws Exception {

        return  "api/testMarkdownZHANG";
    }

    @RequestMapping(value = "api/testMarkdown2", method = {RequestMethod.POST,RequestMethod.GET})
    String testMarkdown2(HttpServletRequest request,
                        HttpServletResponse response,
                        Model model,
                         RedirectAttributes attr,
                        @RequestParam("dialog_id") String dialogId
    ) throws Exception {
        attr.addAttribute("dialog_id", dialogId);
        attr.addAttribute("message", "hello");
        attr.addAttribute("success", "1");
        attr.addAttribute("url", "/api/yuwei");
        return  "redirect:/api/testMarkdownZHANG";
    }

}
