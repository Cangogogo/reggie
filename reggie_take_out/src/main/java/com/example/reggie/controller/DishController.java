package com.example.reggie.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.DishDto;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;

import lombok.extern.slf4j.Slf4j;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;


    
    /**
     * 新增菜品
     *1、页面（backend/page/food/add.html）发送ajax请求，请求服务端获取菜品分类数据并展示到下拉框中，可以看到该功能是写在CategoryController控制器里的
     *2、页面发送请求进行图片上传，请求服务器端将图片保存到服务器
     *3、页面发送请求进行图片下载，将上传的图片进行回显（直接使用上边开发的CommonController ）
     *4、点击保存按钮，发送ajax请求，将菜品相关数据以json形式提交到服务端
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }
    
    /**
     * 菜品信息分页
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dtopageInfo = new Page(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        lambdaQueryWrapper.like(name != null, Dish::getName, name);

        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, lambdaQueryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dtopageInfo, "records");//除了records

        //原来的records不带菜品分类,把之前的records提取出来并加上菜品分类，再注入到DishDto所构造的分页查询中
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();//分类名称
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());


        dtopageInfo.setRecords(list);

        return R.success(dtopageInfo);
        //return R.success(pageInfo);
    }




}

