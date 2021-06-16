package com.hinsliu.dianping.common;

/**
 * @Description: 业务异常
 * @author: liuxuanming
 * @date: 2021/06/16 4:51 下午
 */
public class BusinessException extends RuntimeException {

    private CommonError commonError;

    public BusinessException(EmBusinessError emBusinessError) {
        super();
        this.commonError = new CommonError(emBusinessError);
    }

    public BusinessException(EmBusinessError emBusinessError, String errMsg) {
        super();
        this.commonError = new CommonError(emBusinessError);
        this.commonError.setErrMsg(errMsg);
    }

    public CommonError getCommonError() {
        return commonError;
    }

    public void setCommonError(CommonError commonError) {
        this.commonError = commonError;
    }

}
