package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.entity.Notice;

import java.util.List;
import java.util.Map;

public interface INoticeService {
    void save(Notice notice);
    void update(Notice notice);
    void remove(Integer id);
    List<Notice> myNotices();
    List<Notice> selectAll();
    Notice selectById(Integer id);
    IPage<Notice> selectPage(Integer pageNum, Integer pageSize, String name);
    Map<String, Object> toggleLike(Integer noticeId);
}
