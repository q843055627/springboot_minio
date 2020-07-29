package com.gc.springboot_minio.aspect;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Component
@Aspect
public class logAspect {

    private static Logger logger = LoggerFactory.getLogger(logAspect.class);

    @Autowired
    private HttpServletRequest request;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void logPoingcut(){
    }

    @Before("logPoingcut()")
    public void doBefore(JoinPoint joinPoint) throws Exception{
        Object[] args = joinPoint.getArgs();
        if(null != args[0]){            //记录请求地址和参数
            if(args[0] instanceof MultipartFile){           //上传文件格式
                String filanme = ((MultipartFile)args[0]).getOriginalFilename();
                logger.info("请求地址：" + request.getRequestURI() + ",请求参数：文件【" + filanme + "】");
            }else{                //请求参数为String、Map、JSON等格式
                logger.info("请求地址：" + request.getRequestURI() + ",请求参数：" + JSON.toJSON(args[0]));
            }
        }else{
            logger.info("请求地址：" + request.getRequestURI() + ",请求参数：" + null);
        }
    }

    @AfterReturning(pointcut = "logPoingcut()",returning = "ret")
    public void doAfter(Object ret) throws Exception{
        //执行结果
        logger.info("执行地址：" + request.getRequestURI() + ",执行结果：" + JSON.toJSON(ret));
    }

}
