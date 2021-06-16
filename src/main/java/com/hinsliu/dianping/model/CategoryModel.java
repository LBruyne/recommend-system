package com.hinsliu.dianping.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * category
 * @author 
 */
@Data
public class CategoryModel implements Serializable {
    private Integer id;

    private Date createdAt;

    private Date updatedAt;

    private String name;

    private String iconUrl;

    private Integer sort;

    private static final long serialVersionUID = 1L;
}