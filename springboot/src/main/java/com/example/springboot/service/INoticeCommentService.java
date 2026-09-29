package com.example.springboot.service;

import com.example.springboot.entity.NoticeComment;

import java.util.List;

public interface INoticeCommentService {
    List<NoticeComment> selectByNoticeId(Integer noticeId);
    void add(NoticeComment comment);
    void delete(Integer id);
}
