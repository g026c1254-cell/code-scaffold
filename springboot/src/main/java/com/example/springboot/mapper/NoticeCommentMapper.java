package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.NoticeComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoticeCommentMapper extends BaseMapper<NoticeComment> {
    List<NoticeComment> selectByNoticeId(@Param("noticeId") Integer noticeId);
}
