package com.pinyougou.manager.controller;

import com.pinyougou.entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

@RestController
public class UploadController {


    @RequestMapping("/upload.do")
    public Result upload(MultipartFile file){
        //获取上传的文件名
        String filename = file.getOriginalFilename();
        //获取文件的后缀名
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        try {
            FastDFSClient dfsClient=new FastDFSClient("classpath:config/fdfs_client.conf");
            String url = dfsClient.uploadFile(file.getBytes(), suffix, null);
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false,"上传文件失败");
    }
}
