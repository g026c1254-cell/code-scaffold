package com.example.springboot.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.OrdersMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.IOrdersService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl implements IOrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void save(Orders orders) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "ログインしてください");
        }
        orders.setUserId(currentUser.getId());

        // 1、判断商品是否存在及库存是否充足
        Goods goods = goodsMapper.selectById(orders.getGoodsId());
        if (goods == null) {
            throw new ServiceException("201", "商品が存在しません");
        }
        if (goods.getStore() == null || orders.getNums() == null || goods.getStore() < orders.getNums()) {
            throw new ServiceException("201", goods.getName() + " の在庫が不足しています");
        }

        // 2、新增订单
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        orders.setOrderNo(sdf.format(new Date()));
        orders.setTime(DateUtil.now());
        orders.setState("待支付");
        ordersMapper.insert(orders);

        // 3、商品库存减去对应的数量
        goods.setStore(goods.getStore() - orders.getNums());
        goodsMapper.updateById(goods);
    }

    @Override
    public void update(Orders orders) {
        ordersMapper.updateById(orders);
    }

    @Override
    @Transactional
    public void remove(Integer id) {
        Orders orders = ordersMapper.selectById(id);
        if (orders == null) {
            return;
        }
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "ログインしてください");
        }
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole()) && !currentUser.getId().equals(orders.getUserId())) {
            throw new ServiceException("403", "他人の注文を削除する権限がありません");
        }

        // 优化：未支付（待支付）订单取消或删除时，将扣减的库存原样返还给商品，防止库存永久泄漏
        if ("待支付".equals(orders.getState()) && orders.getGoodsId() != null && orders.getNums() != null && orders.getNums() > 0) {
            Goods goods = goodsMapper.selectById(orders.getGoodsId());
            if (goods != null) {
                goods.setStore((goods.getStore() == null ? 0 : goods.getStore()) + orders.getNums());
                goodsMapper.updateById(goods);
            }
        }

        ordersMapper.deleteById(id);
    }

    @Override
    public List<Orders> selectAll() {
        return ordersMapper.selectList(null);
    }

    @Override
    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    @Override
    public IPage<Orders> selectPage(Integer pageNum, Integer pageSize, String name, String orderNo) {
        Page<Orders> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null && !name.trim().isEmpty(), Orders::getName, name);
        queryWrapper.like(orderNo != null && !orderNo.trim().isEmpty(), Orders::getOrderNo, orderNo);

        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser != null && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            queryWrapper.eq(Orders::getUserId, currentUser.getId());
        }
        queryWrapper.orderByDesc(Orders::getId);

        Page<Orders> ordersPage = ordersMapper.selectPage(page, queryWrapper);
        List<Orders> records = ordersPage.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            // 批量查询避免 N+1 问题
            Set<Integer> goodsIds = records.stream().map(Orders::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<Integer> userIds = records.stream().map(Orders::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());

            Map<Integer, Goods> goodsMap = CollUtil.isEmpty(goodsIds) ? Map.of() :
                    goodsMapper.selectBatchIds(goodsIds).stream().collect(Collectors.toMap(Goods::getId, g -> g));
            Map<Integer, User> userMap = CollUtil.isEmpty(userIds) ? Map.of() :
                    userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

            for (Orders item : records) {
                item.setGoods(goodsMap.get(item.getGoodsId()));
                item.setUser(userMap.get(item.getUserId()));
            }
        }
        return ordersPage;
    }

    @Override
    @Transactional
    public void pay(Orders orders) {
        if (orders == null || orders.getId() == null) {
            throw new ServiceException("201", "注文情報が無効です");
        }

        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "ログインしてください");
        }

        Orders storedOrder = ordersMapper.selectById(orders.getId());
        if (storedOrder == null || !currentUser.getId().equals(storedOrder.getUserId())) {
            throw new ServiceException("201", "注文が存在しないか、操作権限がありません");
        }
        if ("已支付".equals(storedOrder.getState())) {
            throw new ServiceException("201", "この注文は既に支払済みです");
        }

        User buyer = userMapper.selectById(currentUser.getId());
        if (buyer == null || buyer.getAccount() == null || storedOrder.getPrice() == null
                || buyer.getAccount() < storedOrder.getPrice()) {
            throw new ServiceException("201", "残高が不足しています。チャージしてください");
        }

        // 1、更新订单状态
        storedOrder.setState("已支付");
        ordersMapper.updateById(storedOrder);

        // 2、扣减买家账户余额
        buyer.setAccount(buyer.getAccount() - storedOrder.getPrice());
        userMapper.updateById(buyer);

        // 3、优化：结算给卖家（发布商品的发布者），实现完整的买卖资金交易闭环
        if (storedOrder.getGoodsId() != null) {
            Goods goods = goodsMapper.selectById(storedOrder.getGoodsId());
            if (goods != null && goods.getUserId() != null) {
                User seller = userMapper.selectById(goods.getUserId());
                if (seller != null) {
                    double sellerCurrentAccount = seller.getAccount() == null ? 0.0 : seller.getAccount();
                    seller.setAccount(sellerCurrentAccount + storedOrder.getPrice());
                    userMapper.updateById(seller);
                }
            }
        }
    }
}
