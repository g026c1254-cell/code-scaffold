package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoticeMapper extends BaseMapper<Notice> {
    List<Notice> selectAllWithPublisher();

    IPage<Notice> selectPageWithPublisher(Page<Notice> page, @Param("name") String name);
}