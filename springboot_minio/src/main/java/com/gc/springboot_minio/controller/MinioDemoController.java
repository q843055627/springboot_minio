package com.gc.springboot_minio.controller;

import com.alibaba.fastjson.JSONObject;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/minioDemo")
public class MinioDemoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioDemoController.class);

    @Value("${minio.endpoint}")
    private  String ENDPOINT;
    @Value("${minio.bucketName}")
    private  String BUCKETNAME;
    @Value("${minio.accessKey}")
    private  String ACCESSKEY;
    @Value("${minio.secretKey}")
    private  String SECRETKEY;

    //文件创建
    @RequestMapping(value = "/upload")
    public String upload(MultipartFile file) {
        String s=null;
        try {
            MinioClient minioClient = new MinioClient(ENDPOINT, ACCESSKEY, SECRETKEY);
            //存入bucket不存在则创建
            if (!minioClient.bucketExists(BUCKETNAME)) {
                minioClient.makeBucket(BUCKETNAME);
            }
            String filename = file.getOriginalFilename();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            // 文件存储的目录结构
            String objectName = sdf.format(new Date()) + "/" + filename;
            // 存储文件
            minioClient.putObject(BUCKETNAME, objectName, file.getInputStream(),
                    new PutObjectOptions(file.getInputStream().available(), -1));
            LOGGER.info("文件上传成功!");
            s=ENDPOINT + "/" + BUCKETNAME + "/" + objectName;
        } catch (Exception e) {
            LOGGER.info("上传发生错误: {}！", e.getMessage());
            e.printStackTrace();
        }
        return s;
    }

    @RequestMapping(value = "/download")
    public void downloadFiles(@RequestBody JSONObject param, HttpServletResponse httpResponse) {
        try {
            String filename = param.getString("filename");
            MinioClient minioClient = new MinioClient(ENDPOINT, ACCESSKEY, SECRETKEY);
            InputStream object = minioClient.getObject(BUCKETNAME, filename);
            byte buf[] = new byte[1024];
            int length = 0;
            httpResponse.reset();

            httpResponse.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            httpResponse.setContentType("application/octet-stream");
            httpResponse.setCharacterEncoding("utf-8");
            OutputStream outputStream = httpResponse.getOutputStream();
            while ((length = object.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
            outputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.info("导出失败：", ex.getMessage());
        }
    }
    //文件删除
    @RequestMapping(value = "/delete")
    public String delete(@RequestBody JSONObject param) {
        try {
            MinioClient minioClient = new MinioClient(ENDPOINT, ACCESSKEY, SECRETKEY);
            minioClient.removeObject(BUCKETNAME, param.getString("filename"));
        } catch (Exception e) {
            return "删除失败"+e.getMessage();
        }
        return "删除成功";
    }

//    Iterable<Result<Item>> results = minioClient.listObjects(BUCKETNAME);
//    Iterator<Result<Item>> iterator = results.iterator();
//            while(iterator.hasNext()){
//        Item item = iterator.next().get();
//        System.out.println(item.objectName());
//    }
//

}

