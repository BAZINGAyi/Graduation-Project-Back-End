package com.potflesh.wenda.controller;
import com.potflesh.wenda.storage.StorageFileNotFoundException;
import com.potflesh.wenda.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by bazinga on 11/04/2018.
 */
@Controller
public class FileUploadController {

    @RequestMapping(value = "api/uploadImage", method = {RequestMethod.POST,RequestMethod.GET})
    String uploadImage(RedirectAttributes attr,
                       @RequestParam("editormd-image-file")MultipartFile file,
                       @RequestParam("dialog_id") String dialogId) throws Exception {

        Map map = new HashMap();
        if(!file.isEmpty()){
            try {
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

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @RequestMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @RequestMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @RequestMapping("/hhhhhhh")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
