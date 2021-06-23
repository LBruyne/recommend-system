package com.hinsliu.dianping.model;

import java.io.Serializable;
import lombok.Data;

/**
 * recommend
 * @author 
 */
@Data
public class RecommendDO implements Serializable {
    private Integer id;

    private String recommend;

    private static final long serialVersionUID = 1L;
}