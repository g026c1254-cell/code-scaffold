package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Type;
import com.example.springboot.service.ITypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/type")
public class TypeController {

    @Autowired
    private ITypeService typeService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Type type) {
        typeService.save(type);
        return Result.success();
    }
    /**
     * 更新/修改分类数据
     */
    @PutMapping("/update")
    public Result update(@RequestBody Type type) {
        typeService.update(type);
        return Result.success();
    }
    /**
     * 删除
     */
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        typeService.remove(id);
        return Result.success();
    }

    /**
     * 查询全部数据
     */
    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(typeService.selectAll());
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        return Result.success(typeService.selectById(id));
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "") String name,
                             @RequestParam Integer pageNum,
                             @RequestParam Integer pageSize) {
        return Result.success(typeService.selectPage(pageNum, pageSize, name));
    }
}