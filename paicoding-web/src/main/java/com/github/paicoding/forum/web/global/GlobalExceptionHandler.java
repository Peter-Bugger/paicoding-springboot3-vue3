package com.github.paicoding.forum.web.global;

import com.github.paicoding.forum.api.model.exception.ForumAdviceException;
import com.github.paicoding.forum.api.model.exception.ForumException;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/17/23
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ForumAdviceException.class)
    public ResVo<String> handleForumAdviceException(ForumAdviceException e, HttpServletResponse response) {
        log.error("ForumAdviceException: [{}] {}", e.getStatus().getCode(), e.getMessage(), e);
        int httpStatus = (e.getStatus().getCode() / 1000) % 1000;
        if (httpStatus >= 100 && httpStatus < 600) {
            response.setStatus(httpStatus);
        }
        return ResVo.fail(e.getStatus());
    }

    @ExceptionHandler(value = ForumException.class)
    public ResVo<String> handleForumException(ForumException e, HttpServletResponse response) {
        log.error("ForumException: [{}] {}", e.getStatus().getCode(), e.getMessage(), e);
        int httpStatus = (e.getStatus().getCode() / 1000) % 1000;
        if (httpStatus >= 100 && httpStatus < 600) {
            response.setStatus(httpStatus);
        }
        return ResVo.fail(e.getStatus());
    }

    @ExceptionHandler(value = Exception.class)
    public ResVo<String> handleException(Exception e, HttpServletResponse response) {
        log.error("Unexpected exception", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return ResVo.fail(StatusEnum.UNEXPECT_ERROR);
    }
}
