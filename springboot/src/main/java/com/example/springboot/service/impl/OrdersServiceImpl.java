package com.example.springboot.service.impl;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class OrdersServiceImpl implements IOrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void save(Orders orders) {
        // 1、判断商品库存是否充足，如果不充足，给提示
        Goods goods = goodsMapper.selectById(orders.getGoodsId());
        // 2、如果商品库存充足，就下单（数据库新增一条订单）
        if (goods.getStore() == null || orders.getNums() == null || goods.getStore() <= orders.getNums()){
            throw new ServiceException("201", goods.getName() + "商品库存不足");
        }

        // 3、新增订单
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmsss");
        orders.setOrderNo(sdf.format(new Date()));
        orders.setTime(DateUtil.now());
        orders.setState("待支付");
        ordersMapper.insert(orders);

        // 4、商品库存减去对应的数量
        goods.setStore(goods.getStore() - orders.getNums());
        // 5、更新商品库存
        goodsMapper.updateById(goods);
    }

    @Override
    public void update(Orders orders) {
        ordersMapper.updateById(orders);
    }

    @Override
    public void remove(Integer id) {
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

        // select * from orders where name like %name% or order_no = xx
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null && !name.trim().isEmpty(), Orders::getName, name);
        queryWrapper.like(orderNo != null && !orderNo.trim().isEmpty(), Orders::getOrderNo, orderNo);

        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser != null && !"ADMIN".equals(currentUser.getRole())) {
            queryWrapper.eq(Orders::getUserId, currentUser.getId());
        }

        Page<Orders> ordersPage = ordersMapper.selectPage(page, queryWrapper);
        ordersPage.getRecords().stream().forEach(orders -> {
            orders.setGoods(goodsMapper.selectById(orders.getGoodsId()));
            orders.setUser(userMapper.selectById(orders.getUserId()));
        });
        return ordersPage;
    }
    @Override
    public void pay(Orders orders) {
        if (orders == null || orders.getId() == null) {
            throw new ServiceException("201", "订单信息无效");
        }

        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("401", "请先登录");
        }

        Orders storedOrder = ordersMapper.selectById(orders.getId());
        if (storedOrder == null || !currentUser.getId().equals(storedOrder.getUserId())) {
            throw new ServiceException("201", "订单不存在或无权操作");
        }
        if ("已支付".equals(storedOrder.getState())) {
            throw new ServiceException("201", "订单已经支付");
        }

        User user = userMapper.selectById(currentUser.getId());
        if (user == null || user.getAccount() == null || storedOrder.getPrice() == null
                || user.getAccount() < storedOrder.getPrice()) {
            throw new ServiceException("201", "余额不足，请充值~");
        }

        storedOrder.setState("已支付");
        ordersMapper.updateById(storedOrder);
        user.setAccount(user.getAccount() - storedOrder.getPrice());
        userMapper.updateById(user);
    }
}