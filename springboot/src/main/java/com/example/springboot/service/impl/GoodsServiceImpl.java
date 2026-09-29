package com.example.springboot.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Collect;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Type;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.CollectMapper;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.TypeMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements IGoodsService {

    private final GoodsMapper goodsMapper;

    private final TypeMapper typeMapper;

    private final UserMapper userMapper;

    private final CollectMapper collectMapper;

    public GoodsServiceImpl(GoodsMapper goodsMapper, TypeMapper typeMapper, UserMapper userMapper,
                            CollectMapper collectMapper) {
        this.goodsMapper = goodsMapper;
        this.typeMapper = typeMapper;
        this.userMapper = userMapper;
        this.collectMapper = collectMapper;
    }

    @Override
    public void save(Goods goods) {
        User currentUser = TokenUtils.getCurrentUser();
        if (Objects.isNull(currentUser)) {
            throw new ServiceException("401", "ログインしてください");
        }
        if (StrUtil.isBlank(goods.getName()) || goods.getTypeId() == null || goods.getPrice() == null) {
            throw new ServiceException("400", "商品名、カテゴリ、価格は必須です");
        }
        goods.setUserId(currentUser.getId());
        if (StrUtil.isBlank(goods.getDate())) {
            goods.setDate(cn.hutool.core.date.DateUtil.now());
        }
        if (StrUtil.isBlank(goods.getState())) {
            goods.setState("上架");
        }
        if (goods.getStore() == null || goods.getStore() < 1) {
            goods.setStore(1);
        }
        goodsMapper.insert(goods);
    }

    @Override
    public void update(Goods goods) {
        User currentUser = requireCurrentUser();
        Goods existing = goodsMapper.selectById(goods.getId());
        if (existing == null) {
            throw new ServiceException("404", "商品が存在しません");
        }
        if (!isAdmin(currentUser) && !currentUser.getId().equals(existing.getUserId())) {
            throw new ServiceException("403", "自身が出品した商品のみ編集可能です");
        }
        if (StrUtil.isBlank(goods.getName()) || goods.getTypeId() == null || goods.getPrice() == null) {
            throw new ServiceException("400", "商品名、カテゴリ、価格は必須です");
        }
        goods.setUserId(existing.getUserId());
        if (goods.getStore() == null || goods.getStore() < 1) {
            goods.setStore(1);
        }
        goods.setDate(cn.hutool.core.date.DateUtil.now());
        goods.setState("上架");
        goodsMapper.updateById(goods);
    }

    @Override
    @Transactional
    public void remove(Integer id) {
        User currentUser = requireCurrentUser();
        Goods existing = goodsMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("404", "商品が存在しません");
        }
        if (!isAdmin(currentUser) && !currentUser.getId().equals(existing.getUserId())) {
            throw new ServiceException("403", "自身が出品した商品のみ削除可能です");
        }
        // 优化：删除商品时，一并级联删除关联的收藏记录，防止脏数据与页面 NPE
        collectMapper.delete(new LambdaQueryWrapper<Collect>().eq(Collect::getGoodsId, id));
        goodsMapper.deleteById(id);
    }

    @Override
    public List<Goods> selectAll() {
        LambdaQueryWrapper<Goods> queryWrapper = publicGoodsQuery();
        queryWrapper.orderByDesc(Goods::getDate);
        List<Goods> goodsList = goodsMapper.selectList(queryWrapper);
        enrichGoods(goodsList);
        return goodsList;
    }

    @Override
    public Goods selectById(Integer id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            return null;
        }
        enrichGoods(List.of(goods));

        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser != null) {
            LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Collect::getUserId, currentUser.getId());
            queryWrapper.eq(Collect::getGoodsId, id);
            goods.setIsCollect(collectMapper.selectOne(queryWrapper) != null);
        } else {
            goods.setIsCollect(false);
        }
        return goods;
    }

    @Override
    public IPage<Goods> selectPage(Integer pageNum, Integer pageSize, String name) {
        Page<Goods> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(name), Goods::getName, name);
        queryWrapper.orderByDesc(Goods::getDate);

        Page<Goods> goodsPage = goodsMapper.selectPage(page, queryWrapper);
        enrichGoods(goodsPage.getRecords());
        return goodsPage;
    }

    @Override
    public List<Goods> times() {
        LambdaQueryWrapper<Goods> queryWrapper = publicGoodsQuery();
        queryWrapper.orderByDesc(Goods::getDate).last("LIMIT 12");
        List<Goods> goods = goodsMapper.selectList(queryWrapper);
        enrichGoods(goods);
        return goods;
    }

    @Override
    public IPage<Goods> selectPageType(Integer pageNum, Integer pageSize, String name, Integer typeId) {
        Page<Goods> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(name), Goods::getName, name);
        queryWrapper.eq(typeId != null && typeId != 0, Goods::getTypeId, typeId);
        queryWrapper.eq(Goods::getState, "上架");
        queryWrapper.ge(Goods::getStore, 1);
        queryWrapper.orderByDesc(Goods::getDate);

        Page<Goods> goodsPage = goodsMapper.selectPage(page, queryWrapper);
        enrichGoods(goodsPage.getRecords());
        return goodsPage;
    }

    @Override
    public List<Goods> myGoods() {
        User currentUser = requireCurrentUser();
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getUserId, currentUser.getId());
        queryWrapper.orderByDesc(Goods::getDate);
        List<Goods> goods = goodsMapper.selectList(queryWrapper);
        enrichGoods(goods);
        return goods;
    }

    private User requireCurrentUser() {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "ログインしてください");
        }
        return currentUser;
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private LambdaQueryWrapper<Goods> publicGoodsQuery() {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getState, "上架");
        queryWrapper.ge(Goods::getStore, 1);
        return queryWrapper;
    }

    /**
     * 批量填充类型名称与发布者姓名，解决 N+1 查询隐患
     */
    private void enrichGoods(List<Goods> goodsList) {
        if (CollUtil.isEmpty(goodsList)) {
            return;
        }

        Set<Integer> typeIds = goodsList.stream()
                .map(Goods::getTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Integer> userIds = goodsList.stream()
                .map(Goods::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, String> typeNameMap = CollUtil.isEmpty(typeIds) ? Collections.emptyMap() :
                typeMapper.selectBatchIds(typeIds).stream()
                        .collect(Collectors.toMap(Type::getId, Type::getName, (k1, k2) -> k1));

        Map<Integer, String> userNameMap = CollUtil.isEmpty(userIds) ? Collections.emptyMap() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getName, (k1, k2) -> k1));

        for (Goods goods : goodsList) {
            goods.setTypeName(typeNameMap.getOrDefault(goods.getTypeId(), "未分類"));
            goods.setUserName(userNameMap.getOrDefault(goods.getUserId(), "匿名ユーザー"));
        }
    }
}
