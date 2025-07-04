package xyz.qy.implatform.exception;

import cn.hutool.json.JSONException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常捕获
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public Result handleException(Exception e) {
        if (e instanceof GlobalException) {
            GlobalException ex = (GlobalException) e;
            // token过期是正常情况,不打印
            if (!ex.getCode().equals(ResultCode.INVALID_TOKEN.getCode())) {
                log.error("全局异常捕获:msg:{}", ex.getMessage());
            }
            return ResultUtils.error(ex.getCode(), ex.getMessage());
        } else if (e instanceof UndeclaredThrowableException) {
            GlobalException ex = (GlobalException) e.getCause();
            log.error("全局异常捕获:msg:{}", ex.getMessage());
            return ResultUtils.error(ex.getCode(), ex.getMessage());
        } else {
            log.error("全局异常捕获:msg:{}", e.getMessage());
            return ResultUtils.error(ResultCode.PROGRAM_ERROR);
        }
    }

    /**
     * 数据解析错误
     **/
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Result handleMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("全局异常捕获:msg:{}", e.getMessage());
        Throwable t = e.getCause();
        if (t instanceof JSONException) {
            t = t.getCause();
            if (t instanceof DateTimeParseException) {
                return ResultUtils.error(ResultCode.PROGRAM_ERROR, "日期格式不正确");
            }
            return ResultUtils.error(ResultCode.PROGRAM_ERROR, "数据格式不正确");
        }
        return ResultUtils.error(ResultCode.PROGRAM_ERROR);
    }

    /**
     * 处理请求参数格式错误 @RequestBody上validate失败后抛出的异常
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.OK)
    public Result handleValidationExceptionHandler(MethodArgumentNotValidException exception) {
        BindingResult bindResult = exception.getBindingResult();
        String msg;
        if (bindResult.hasErrors()) {
            msg = bindResult.getAllErrors().get(0).getDefaultMessage();
            assert msg != null;
            if (msg.contains("NumberFormatException")) {
                msg = "参数类型错误！";
            }
        } else {
            msg = "系统繁忙，请稍后重试...";
        }
        log.error("全局异常捕获:msg:{}", msg);
        return ResultUtils.error(ResultCode.PROGRAM_ERROR, msg);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result handleBindException(BindException e) {
        //抛出异常可能不止一个 输出为一个List集合
        List<ObjectError> errors = e.getAllErrors();
        //取第一个异常
        ObjectError error = errors.get(0);
        //获取异常信息
        String errorMsg = error.getDefaultMessage();
        log.error("全局异常捕获:msg:{}", errorMsg);
        return ResultUtils.error(ResultCode.PROGRAM_ERROR, errorMsg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());
        log.error("全局异常捕获:msg:{}", errors);
        return ResultUtils.error(ResultCode.PROGRAM_ERROR, "参数校验失败");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String error = "参数 '" + ex.getName() + "' 类型错误: " + ex.getMessage();
        return ResultUtils.error(ResultCode.PROGRAM_ERROR, error);
    }
}
