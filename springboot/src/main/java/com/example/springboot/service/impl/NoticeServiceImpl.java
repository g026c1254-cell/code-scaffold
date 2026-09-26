package com.example.springboot.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Notice;
import com.example.springboot.mapper.NoticeMapper;
import com.example.springboot.service.INoticeService;
import com.example.springboot.utils.TokenUtils;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements INoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public void save(Notice notice) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "请先登录");
        }
        notice.setTime(DateUtil.now());
        notice.setUserId(currentUser.getId());
        notice.setUserName(currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername());
        noticeMapper.insert(notice);
    }

    @Override
    public void update(Notice notice) {
        User currentUser = requireCurrentUser();
        Notice existing = noticeMapper.selectById(notice.getId());
        if (existing == null) {
            throw new ServiceException("404", "公告不存在");
        }
        if (!isAdmin(currentUser) && !currentUser.getId().equals(existing.getUserId())) {
            throw new ServiceException("403", "只能修改自己发布的公告");
        }
        if (notice.getName() == null || notice.getName().trim().isEmpty()
                || notice.getContent() == null || notice.getContent().trim().isEmpty()) {
            throw new ServiceException("400", "公告标题和内容不能为空");
        }
        notice.setUserId(existing.getUserId());
        notice.setUserName(existing.getUserName());
        notice.setTime(DateUtil.now());
        noticeMapper.updateById(notice);
    }

    @Override
    public void remove(Integer id) {
        noticeMapper.deleteById(id);
    }

    @Override
    public List<Notice> myNotices() {
        User currentUser = requireCurrentUser();
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getUserId, currentUser.getId());
        queryWrapper.orderByDesc(Notice::getTime);
        return noticeMapper.selectList(queryWrapper);
    }

    @Override
    public List<Notice> selectAll() {
        return noticeMapper.selectAllWithPublisher();
    }

    @Override
    public Notice selectById(Integer id) {
        return noticeMapper.selectById(id);
    }

    @Override
    public IPage<Notice> selectPage(Integer pageNum, Integer pageSize, String name) {
        Page<Notice> page = new Page<>(pageNum, pageSize);

        return noticeMapper.selectPageWithPublisher(page, name);
    }

    private User requireCurrentUser() {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "请先登录");
        }
        return currentUser;
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }
}