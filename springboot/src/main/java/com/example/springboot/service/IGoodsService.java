package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.entity.Goods;

import java.util.List;

public interface IGoodsService {

    /**
      * 新增
      */
    void save(Goods goods);

    /**
      * 修改
      */
    void update(Goods goods);

    /**
      * 删除
      */
    void remove(Integer id);

    /**
      * 查询全部数据
      */
    List<Goods> selectAll();

    /**
      * 根据ID查询数据
      */
    Goods selectById(Integer id);

    /**
      * 分页查询数据
      */
    IPage<Goods> selectPage(Integer pageNum, Integer pageSize, String name);
    /**
     * 新品上架
     */
    List<Goods> times();

    IPage<Goods> selectPageType(Integer pageNum, Integer pageSize, String name, Integer typeId);

    /**
     * 查询当前用户发布的商品
     */
    List<Goods> myGoods();
}