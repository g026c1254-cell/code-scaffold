package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Notice;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface NoticeMapper extends BaseMapper<Notice> {
    List<Notice> selectAllWithPublisher();

    Notice selectByIdWithPublisher(@Param("id") Integer id);

    IPage<Notice> selectPageWithPublisher(Page<Notice> page, @Param("name") String name);

    @Update("UPDATE notice SET views = COALESCE(views, 0) + 1 WHERE id = #{id}")
    int incrementViews(@Param("id") Integer id);

    @Update("UPDATE notice SET likes = COALESCE(likes, 0) + 1 WHERE id = #{id}")
    int incrementLikes(@Param("id") Integer id);

    @Update("UPDATE notice SET likes = GREATEST(COALESCE(likes, 0) - 1, 0) WHERE id = #{id}")
    int decrementLikes(@Param("id") Integer id);
}
