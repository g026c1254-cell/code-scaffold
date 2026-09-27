package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean save(User entity) {
        if (StrUtil.isBlank(entity.getName())) {
            entity.setName(entity.getUsername());
        }
        if (StrUtil.isBlank(entity.getPassword())) {
            entity.setPassword("123");
        }
        if (StrUtil.isBlank(entity.getRole())) {
            entity.setRole("USER");
        }
        return super.save(entity);
    }

    public User selectByUsername(String username, String role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("role", role);
        return getOne(queryWrapper);
    }

    // 验证用户账户是否合法
    public User login(User user) {
        User dbUser = selectByUsername(user.getUsername(), user.getRole());
        if (dbUser == null) {
            throw new ServiceException("ユーザー名またはパスワードが正しくありません");
        }
        if (!user.getPassword().equals(dbUser.getPassword())) {
            throw new ServiceException("ユーザー名またはパスワードが正しくありません");
        }
        // 生成token
        String token = TokenUtils.createToken(dbUser.getId().toString(), dbUser.getPassword());
        dbUser.setToken(token);
        // 优化：返回给前端前脱敏密码，杜绝明文密码泄露及本地缓存风险
        dbUser.setPassword(null);
        return dbUser;
    }

    public User register(User user) {
        User dbUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (dbUser != null) {
            throw new ServiceException("ユーザー名は既に使用されています");
        }
        user.setName(user.getUsername());
        userMapper.insert(user);
        // 优化：脱敏密码返回
        user.setPassword(null);
        return user;
    }

    public void resetPassword(User user) {
        User dbUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (dbUser == null) {
            throw new ServiceException("ユーザーが存在しません");
        }
        // 优化：避免 null 对象 equals 导致的空指针异常
        if (!StrUtil.equals(user.getPhone(), dbUser.getPhone())) {
            throw new ServiceException("電話番号の認証に失敗しました");
        }
        dbUser.setPassword("123");
        updateById(dbUser);
    }

    @Override
    public void updatePassword(User user) {
        int update = userMapper.updatePassword(user);
        if (update < 1) {
            throw new ServiceException("現在のパスワードが正しくありません");
        }
    }
}
