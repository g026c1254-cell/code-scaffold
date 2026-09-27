package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Type;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.TypeMapper;
import com.example.springboot.service.ITypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TypeServiceImpl implements ITypeService {

    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public void save(Type type) {
        if (type == null || StrUtil.isBlank(type.getName())) {
            throw new ServiceException("400", "カテゴリ名を入力してください");
        }
        typeMapper.insert(type);
    }

    @Override
    public void update(Type type) {
        if (type == null || type.getId() == null || StrUtil.isBlank(type.getName())) {
            throw new ServiceException("400", "カテゴリ情報が無効です");
        }
        typeMapper.updateById(type);
    }

    @Override
    public void remove(Integer id) {
        // 优化：删除分类时增加完整性校验，若该分类下仍有关联商品，禁止直接删除，防止孤儿数据
        Long goodsCount = goodsMapper.selectCount(new LambdaQueryWrapper<Goods>().eq(Goods::getTypeId, id));
        if (goodsCount != null && goodsCount > 0) {
            throw new ServiceException("400", "このカテゴリに属する商品が存在するため削除できません");
        }
        typeMapper.deleteById(id);
    }

    @Override
    public List<Type> selectAll() {
        return typeMapper.selectList(null);
    }

    @Override
    public Type selectById(Integer id) {
        return typeMapper.selectById(id);
    }

    @Override
    public IPage<Type> selectPage(Integer pageNum, Integer pageSize, String name) {
        Page<Type> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Type> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(name), Type::getName, name);

        return typeMapper.selectPage(page, queryWrapper);
    }
}
