package com.hinsliu.dianping.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * shop
 * @author 
 */
@Data
public class ShopModel implements Serializable {
    private Integer id;

    private Date createdAt;

    private Date updatedAt;

    private String name;

    private BigDecimal remarkScore;

    private Integer pricePerMan;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer categoryId;

    private String tags;

    private String startTime;

    private String endTime;

    private String address;

    private Integer sellerId;

    private String iconUrl;

    private static final long serialVersionUID = 1L;
}