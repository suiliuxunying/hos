package com.imooc.bigdata.hos.web.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.bigdata.hos.core.ErrorCodes;
import com.imooc.bigdata.hos.core.HosException;
import com.imooc.bigdata.hos.web.security.IOperationAccessControl;

@CrossOrigin // 解决跨域问题！原来以为米有用 在delete请求中发现了极大的用处 （原来一直报403）
 /**
 * Created by jixin on 17-3-6.
 */
@ControllerAdvice //全局异常捕获的类 专用注解
public class BaseController {
  @Autowired
  @Qualifier("DefaultAccessControl")
  protected IOperationAccessControl operationAccessControl;
 
  /**
   * 异常处理.
   */
  @ExceptionHandler //全局异常捕获的类 专用注解
  @ResponseBody       
  //  参数 ex 就是捕获到的异常
  public Map<String, Object> exceptionHandle(Exception ex, HttpServletResponse response) {
    ex.printStackTrace();//打印异常信息到控制台
    if (HosException.class.isAssignableFrom(ex.getClass())) {
      HosException hosException = (HosException) ex; 

      if (hosException.errorCode() == ErrorCodes.ERROR_PERMISSION_DENIED) {
        response.setStatus(403);
      } else {
        response.setStatus(500);
      }
      return getResultMap(hosException.errorCode(), null, hosException.errorMessage(), null);
    } else {
      response.setStatus(500);
      return getResultMap(500, null, ex.getMessage(), null);
    }
  }
//成功仅仅传数据 （自动加上状态码20000）
  protected Map<String, Object> getResult(Object object) {
    return getResultMap(null, object, null, null);
  }

//除去数据之外还有其他应答（自动加上状态码20000）
  protected Map<String, Object> getResult(Object object, Map<String, Object> extraMap) {
    return getResultMap(null, object, null, extraMap);
  }

  //成功调用 新加的 （自动加上状态码20000）
  protected Map<String, Object> getSuccess(Object object, String successMsg,  Map<String, Object> extraMap) {
    return getResultMap(null, object, successMsg, extraMap);
  }
  
//错误 调用此应答（需要自定传入code 单页加上了400状态码 为了方便前端分类）
  protected Map<String, Object> getError(int errCode, String errMsg) {
    return getResultMap(errCode, null, errMsg, null);
  }

  private Map<String, Object> getResultMap(Integer code, Object data, String msg,
      Map<String, Object> extraMap) {
    Map<String, Object> map = new HashMap<String, Object>();
    if (code == null || code.equals(200)) { //没问题 返回200+数据
      map.put("code", 20000);
      map.put("data", data);
      map.put("message", msg);
    } else {                                //有问题 就是这个 
      map.put("Code", code);
      map.put("code", 400);
      map.put("message", msg);
    }
    if (extraMap != null && !extraMap.isEmpty()) {
      map.putAll(extraMap);                //有数据全部都放进去
    }
    System.out.println(map);
    return map;
  }

}
