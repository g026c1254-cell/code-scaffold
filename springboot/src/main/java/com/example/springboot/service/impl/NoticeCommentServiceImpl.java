package com.example.springboot.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.springboot.entity.NoticeComment;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.NoticeCommentMapper;
import com.example.springboot.service.INoticeCommentService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class NoticeCommentServiceImpl implements INoticeCommentService {

    @Autowired
    private NoticeCommentMapper noticeCommentMapper;

    @Override
    public List<NoticeComment> selectByNoticeId(Integer noticeId) {
        if (noticeId == null) {
            return Collections.emptyList();
        }
        return noticeCommentMapper.selectByNoticeId(noticeId);
    }

    @Override
    public void add(NoticeComment comment) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "请先登录");
        }
        if (comment == null || comment.getNoticeId() == null) {
            throw new ServiceException("400", "公告ID不能为空");
        }
        if (comment.getContent() == null || StrUtil.isBlank(comment.getContent())) {
            throw new ServiceException("400", "评论内容不能为空");
        }
        comment.setUserId(currentUser.getId());
        comment.setUserName(StrUtil.isNotBlank(currentUser.getName()) ? currentUser.getName() : currentUser.getUsername());
        comment.setUserAvatar(currentUser.getAvatar());
        comment.setCreateTime(DateUtil.now());
        noticeCommentMapper.insert(comment);
    }

    @Override
    public void delete(Integer id) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "请先登录");
        }
        NoticeComment comment = noticeCommentMapper.selectById(id);
        if (comment == null) {
            return;
        }
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole()) && !currentUser.getId().equals(comment.getUserId())) {
            throw new ServiceException("403", "只能删除自己的评论");
        }
        noticeCommentMapper.deleteById(id);
    }
}
