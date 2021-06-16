package com.hinsliu.dianping.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * seller
 * @author 
 */
@Data
public class SellerModel implements Serializable {
    private Integer id;

    private String name;

    private Date createdAt;

    private Date updatedAt;

    private BigDecimal remarkScore;

    private Integer disabledFlag;

    private static final long serialVersionUID = 1L;
}