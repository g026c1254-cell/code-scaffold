package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
            throw new ServiceException("401", "请先登录");
        }
        goods.setUserId(currentUser.getId());
        goodsMapper.insert(goods);
    }

    @Override
    public void update(Goods goods) {
        goodsMapper.updateById(goods);
    }

    @Override
    public void remove(Integer id) {
        goodsMapper.deleteById(id);
    }

    @Override
    public List<Goods> selectAll() {
        LambdaQueryWrapper<Goods> queryWrapper = publicGoodsQuery();
        queryWrapper.orderByDesc(Goods::getDate);
        return goodsMapper.selectList(queryWrapper);
    }

    @Override
    public Goods selectById(Integer id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            return null;
        }

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

        Page<Goods> goodsPage = goodsMapper.selectPage(page, queryWrapper);
        enrichGoods(goodsPage.getRecords());
        return goodsPage;
    }

    @Override
    public List<Goods> times() {
        LambdaQueryWrapper<Goods> queryWrapper = publicGoodsQuery();
        queryWrapper.orderByDesc(Goods::getDate).last("LIMIT 4");
        return goodsMapper.selectList(queryWrapper);
    }

    @Override
    public List<Goods> sales() {
        LambdaQueryWrapper<Goods> queryWrapper = publicGoodsQuery();
        queryWrapper.orderByDesc(Goods::getSales).last("LIMIT 4");
        return goodsMapper.selectList(queryWrapper);
    }
    @Override
    public IPage<Goods> selectPageType(Integer pageNum, Integer pageSize, String name, Integer typeId) {
        Page<Goods> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(name), Goods::getName, name);
        queryWrapper.eq(typeId != null && typeId != 0, Goods::getTypeId, typeId);
        queryWrapper.eq(Goods::getState, "上架");
        queryWrapper.orderByDesc(Goods::getDate);

        Page<Goods> goodsPage = goodsMapper.selectPage(page, queryWrapper);
        enrichGoods(goodsPage.getRecords());
        return goodsPage;
    }

    private LambdaQueryWrapper<Goods> publicGoodsQuery() {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getState, "上架");
        return queryWrapper;
    }

    private void enrichGoods(List<Goods> goodsList) {
        goodsList.forEach(goods -> {
            Type type = typeMapper.selectById(goods.getTypeId());
            goods.setTypeName(Objects.nonNull(type) ? type.getName() : "未知类型");

            User user = userMapper.selectById(goods.getUserId());
            goods.setUserName(Objects.nonNull(user) ? user.getName() : "未知用户");
        });
    }
    @Override
    public List<JSONObject> echarts() {
        List<Goods> goods = goodsMapper.selectList(null);
        List<JSONObject> list = new ArrayList<>();
        goods.forEach(good -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("name", Objects.requireNonNullElse(good.getName(), "未命名商品"));
            jsonObject.set("value", Objects.requireNonNullElse(good.getSales(), 0));

            list.add(jsonObject);
        });
        return list;
    }
}