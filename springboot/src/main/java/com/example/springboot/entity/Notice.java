package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
@TableName("notice")
public class Notice {

    @TableId(type= IdType.AUTO)
    private Integer id;
    private String name;
    private String content;
    private String time;
    private Integer userId;
    @TableField("user_name")
    private String userName;

}