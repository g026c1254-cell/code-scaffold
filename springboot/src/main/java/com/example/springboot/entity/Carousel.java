package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("carousel")
public class Carousel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String cover;
    private Integer goodsId;

    // 表示该字段在 carousel 数据库表中不存在，仅用于业务层传输
    @TableField(exist = false)
    private String goodsName;
}