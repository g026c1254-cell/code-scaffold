package com.example.springboot.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Notice;
import com.example.springboot.entity.NoticeLike;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.NoticeLikeMapper;
import com.example.springboot.mapper.NoticeMapper;
import com.example.springboot.service.INoticeService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl implements INoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private NoticeLikeMapper noticeLikeMapper;

    @Override
    public void save(Notice notice) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "请先登录");
        }
        notice.setTime(DateUtil.now());
        notice.setUserId(currentUser.getId());
        notice.setUserName(currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername());
        notice.setViews(0);
        notice.setLikes(0);
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
        User currentUser = requireCurrentUser();
        Notice existing = noticeMapper.selectById(id);
        if (existing == null) {
            return;
        }
        if (!isAdmin(currentUser) && !currentUser.getId().equals(existing.getUserId())) {
            throw new ServiceException("403", "只能删除自己发布的公告");
        }
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
        List<Notice> notices = noticeMapper.selectAllWithPublisher();
        if (notices == null || notices.isEmpty()) {
            return notices;
        }
        User currentUser = TokenUtils.getCurrentUser();
        Set<Integer> likedNoticeIds = Collections.emptySet();
        if (currentUser != null) {
            LambdaQueryWrapper<NoticeLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(NoticeLike::getUserId, currentUser.getId());
            List<NoticeLike> likes = noticeLikeMapper.selectList(wrapper);
            if (likes != null && !likes.isEmpty()) {
                likedNoticeIds = likes.stream().map(NoticeLike::getNoticeId).collect(Collectors.toSet());
            }
        }
        for (Notice notice : notices) {
            notice.setIsLiked(likedNoticeIds.contains(notice.getId()));
            if (notice.getViews() == null) {
                notice.setViews(0);
            }
            if (notice.getLikes() == null) {
                notice.setLikes(0);
            }
        }
        return notices;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Notice selectById(Integer id) {
        if (id == null) {
            return null;
        }
        noticeMapper.incrementViews(id);
        Notice notice = noticeMapper.selectByIdWithPublisher(id);
        if (notice == null) {
            notice = noticeMapper.selectById(id);
        }
        if (notice != null) {
            User currentUser = TokenUtils.getCurrentUser();
            if (currentUser != null) {
                LambdaQueryWrapper<NoticeLike> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(NoticeLike::getNoticeId, id)
                        .eq(NoticeLike::getUserId, currentUser.getId());
                notice.setIsLiked(noticeLikeMapper.selectCount(wrapper) > 0);
            } else {
                notice.setIsLiked(false);
            }
            if (notice.getViews() == null) {
                notice.setViews(0);
            }
            if (notice.getLikes() == null) {
                notice.setLikes(0);
            }
        }
        return notice;
    }

    @Override
    public IPage<Notice> selectPage(Integer pageNum, Integer pageSize, String name) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        return noticeMapper.selectPageWithPublisher(page, name);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> toggleLike(Integer noticeId) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "请先登录");
        }
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new ServiceException("404", "公告不存在");
        }

        LambdaQueryWrapper<NoticeLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoticeLike::getNoticeId, noticeId)
                .eq(NoticeLike::getUserId, currentUser.getId());
        NoticeLike existing = noticeLikeMapper.selectOne(queryWrapper);

        boolean isLiked;
        if (existing != null) {
            noticeLikeMapper.deleteById(existing.getId());
            noticeMapper.decrementLikes(noticeId);
            isLiked = false;
        } else {
            try {
                NoticeLike like = new NoticeLike();
                like.setNoticeId(noticeId);
                like.setUserId(currentUser.getId());
                like.setCreateTime(DateUtil.now());
                noticeLikeMapper.insert(like);
                noticeMapper.incrementLikes(noticeId);
                isLiked = true;
            } catch (Exception e) {
                isLiked = true;
            }
        }

        Notice updated = noticeMapper.selectById(noticeId);
        int likes = (updated != null && updated.getLikes() != null) ? updated.getLikes() : 0;
        if (likes < 0) {
            likes = 0;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likes", likes);
        return result;
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
