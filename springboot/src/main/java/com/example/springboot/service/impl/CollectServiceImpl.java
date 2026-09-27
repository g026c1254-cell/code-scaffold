package com.example.springboot.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Collect;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.CollectMapper;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.ICollectService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CollectServiceImpl implements ICollectService {

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void save(Collect collect) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "ログインしてください");
        }
        Integer userId = currentUser.getId();
        // 1、判断该用户是否之前收藏过该商品
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getUserId, userId);
        queryWrapper.eq(Collect::getGoodsId, collect.getGoodsId());
        Collect one = collectMapper.selectOne(queryWrapper);
        // 2、如果收藏过就删除之前的记录（取消收藏）
        if (Objects.nonNull(one)) {
            collectMapper.delete(queryWrapper);
            throw new ServiceException("201", "お気に入りを解除しました");
        }
        // 3、如果没收藏，则新增收藏
        collect.setUserId(userId);
        collect.setTime(DateUtil.now());
        collectMapper.insert(collect);
    }

    @Override
    public void update(Collect collect) {
        collectMapper.updateById(collect);
    }

    @Override
    public void remove(Integer id) {
        Collect collect = collectMapper.selectById(id);
        if (collect == null) {
            return;
        }
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "ログインしてください");
        }
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole()) && !currentUser.getId().equals(collect.getUserId())) {
            throw new ServiceException("403", "他人のお気に入りを削除する権限がありません");
        }
        collectMapper.deleteById(id);
    }

    @Override
    public List<Collect> selectAll() {
        return collectMapper.selectList(null);
    }

    @Override
    public Collect selectById(Integer id) {
        return collectMapper.selectById(id);
    }

    @Override
    public IPage<Collect> selectPage(Integer pageNum, Integer pageSize, String name) {
        Page<Collect> page = new Page<>(pageNum, pageSize);

        Page<Collect> collectPage = collectMapper.selectPage(page, null);
        List<Collect> records = collectPage.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            Set<Integer> userIds = records.stream().map(Collect::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<Integer> goodsIds = records.stream().map(Collect::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());

            Map<Integer, String> userMap = CollUtil.isEmpty(userIds) ? Map.of() :
                    userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getName, (k1, k2) -> k1));
            Map<Integer, String> goodsMap = CollUtil.isEmpty(goodsIds) ? Map.of() :
                    goodsMapper.selectBatchIds(goodsIds).stream().collect(Collectors.toMap(Goods::getId, Goods::getName, (k1, k2) -> k1));

            for (Collect item : records) {
                item.setUserName(userMap.getOrDefault(item.getUserId(), "未知ユーザー"));
                item.setGoodsName(goodsMap.getOrDefault(item.getGoodsId(), "未知商品"));
            }
        }
        return collectPage;
    }

    @Override
    public List<Collect> myCollect() {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "ログインしてください");
        }
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getUserId, currentUser.getId());
        List<Collect> collects = collectMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(collects)) {
            return new ArrayList<>();
        }

        // 优化：批量填充关联商品，并自动过滤掉已删除商品失效的数据，防止前端出现 NPE
        Set<Integer> goodsIds = collects.stream().map(Collect::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, Goods> goodsMap = CollUtil.isEmpty(goodsIds) ? Map.of() :
                goodsMapper.selectBatchIds(goodsIds).stream().collect(Collectors.toMap(Goods::getId, g -> g));

        List<Collect> validCollects = new ArrayList<>();
        for (Collect collect : collects) {
            Goods g = goodsMap.get(collect.getGoodsId());
            if (g != null) {
                collect.setGoods(g);
                validCollects.add(collect);
            }
        }
        return validCollects;
    }
}
