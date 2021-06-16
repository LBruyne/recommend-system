package com.hinsliu.dianping.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * user
 * @author 
 */
@Data
public class UserModel implements Serializable {
    private Integer id;

    private Date createdAt;

    private Date updatedAt;

    private String telphone;

    private String password;

    private String nickName;

    private Integer gender;

    private static final long serialVersionUID = 1L;
}