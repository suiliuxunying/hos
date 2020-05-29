package com.imooc.bigdata.hos.web.security;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.imooc.bigdata.hos.core.authmgr.IAuthService;
import com.imooc.bigdata.hos.core.authmgr.model.TokenInfo;
import com.imooc.bigdata.hos.core.usermgr.IUserService;
import com.imooc.bigdata.hos.core.usermgr.model.SystemRole;
import com.imooc.bigdata.hos.core.usermgr.model.UserInfo;

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
        // 如果是登录就拦截了
    if (request.getRequestURI().equals("/loginPost")) {
      return true;
    }
    String token = "";
    HttpSession session = request.getSession();
    // 如果有session 从session获取token
    if (session.getAttribute(ContextUtil.SESSION_KEY) != null) {
      token = session.getAttribute(ContextUtil.SESSION_KEY).toString();
    } else {//没有就从headr获得
      token = request.getHeader("X-Auth-Token");
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

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {

  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {

  }
}
