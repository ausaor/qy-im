package xyz.qy.implatform.config.intercept;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.qy.implatform.util.PageUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static xyz.qy.implatform.contant.Constant.DEFAULT_PAGE_NO;
import static xyz.qy.implatform.contant.Constant.DEFAULT_SIZE;
import static xyz.qy.implatform.contant.Constant.PAGE_NO;
import static xyz.qy.implatform.contant.Constant.PAGE_SIZE;

/**
 * 分页拦截器
 **/
public class PageableInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String currentPage = Optional.ofNullable(request.getParameter(PAGE_NO)).orElse(DEFAULT_PAGE_NO);
        String pageSize = Optional.ofNullable(request.getParameter(PAGE_SIZE)).orElse(DEFAULT_SIZE);
        if (StringUtils.isNoneBlank(currentPage)) {
            PageUtils.setCurrentPage(new Page<>(Long.parseLong(currentPage), Long.parseLong(pageSize)));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        PageUtils.remove();
    }

}
