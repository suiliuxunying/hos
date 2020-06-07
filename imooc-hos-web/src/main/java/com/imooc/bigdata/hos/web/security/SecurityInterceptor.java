package com.imooc.bigdata.hos.web.security;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.imooc.bigdata.hos.core.authmgr.IAuthService;
import com.imooc.bigdata.hos.core.authmgr.model.TokenInfo;
import com.imooc.bigdata.hos.core.usermgr.IUserService;
import com.imooc.bigdata.hos.core.usermgr.model.SystemRole;
import com.imooc.bigdata.hos.core.usermgr.model.UserInfo;
@CrossOrigin 
@Component //（把普通pojo实例化到spring容器中，相当于配置文件中的<bean id="" class=""/>）
public class SecurityInterceptor implements HandlerInterceptor {


  @Autowired
  @Qualifier("authServiceImpl")
  private IAuthService authService;

  @Autowired
  @Qualifier("userServiceImpl")
  //Qualifier的意思是合格者，通过这个标示，表明了哪个实现类才是我们所需要的，
  //添加@Qualifier注解，需要注意的是@Qualifier的参数名称为我们之前定义@Service注解的名称之一。
  private IUserService userService;

  private Cache<String, UserInfo> userInfoCache =
      CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();


  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
      if (request.getMethod().equals("OPTIONS")){
        //跨域资源共享标准新增了一组 HTTP 首部字段，允许服务器声明哪些源站有权限访问哪些资源。
        // 另外，规范要求，对那些可能对服务器数据产生副作用的 HTTP 请求方法（特别是 GET 以外的 HTTP 请求，或者搭配某些 MIME 类型的 POST 请求），
        // 浏览器必须首先使用 OPTIONS 方法发起一个预检请求（preflight request），
        // 从而获知服务端是否允许该跨域请求。服务器确认允许之后，才发起实际的 HTTP 请求。
        // 在预检请求的返回中，服务器端也可以通知客户端，是否需要携带身份凭证（包括 Cookies 和 HTTP 认证相关数据）。
        // 参考：https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Access_control_CORS
        response.setStatus(HttpServletResponse.SC_OK);
        return true;
      }
      else {
        System.out.println(request.getHeader("token"));
        if (request.getRequestURI().equals("/loginPost")||request.getRequestURI().equals("/loginPost1")) {
          return true;
        }
        String token = "";
        HttpSession session = request.getSession();
        // 如果有session 从session获取token
        if (session.getAttribute(ContextUtil.SESSION_KEY) != null) {
          token = session.getAttribute(ContextUtil.SESSION_KEY).toString();
        } else {//没有就从headr获得
          token = request.getHeader("token");
        }
        TokenInfo tokenInfo = authService.getTokenInfo(token);
        if (tokenInfo == null) {
          String url = "/loginPost";
          response.sendRedirect(url);//重定向
          //response.setStatus(403);
          return false;
        }
        UserInfo userInfo = userInfoCache.getIfPresent(tokenInfo.getToken());
        if (userInfo == null) {
          userInfo = userService.getUserInfo(token);
          if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setUserId(token);
            userInfo.setUserName("NOT_EXIST_USER");
            userInfo.setDetail("a temporary visitor");
            userInfo.setSystemRole(SystemRole.VISITER);
          }
          userInfoCache.put(tokenInfo.getToken(), userInfo);
        }
        ContextUtil.setCurrentUser(userInfo);
        return true;
    }
    
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {
        System.out.println("response: "+ response.getStatus());
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {

  }
}
