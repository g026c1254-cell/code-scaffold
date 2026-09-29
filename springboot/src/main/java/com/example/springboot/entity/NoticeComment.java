package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("notice_comment")
public class NoticeComment {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("notice_id")
    private Integer noticeId;

    @TableField("user_id")
    private Integer userId;

    @TableField("user_name")
    private String userName;

    @TableField("user_avatar")
    private String userAvatar;

    private String content;

    @TableField("create_time")
    private String createTime;
}
