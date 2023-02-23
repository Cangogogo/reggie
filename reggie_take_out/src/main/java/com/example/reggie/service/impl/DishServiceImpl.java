package com.example.reggie.service.impl;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.dto.DishDto;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.DishFlavor;
import com.example.reggie.mapper.DishMapper;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;

//DishServiceImpl
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品同时保存对应的口味数据
     *
     * @param dishDto
     */
    @Override
    @Transactional  //涉及到多张表，事务管理
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishID = dishDto.getId();//菜品id

        List<DishFlavor> flavors = dishDto.getFlavors();    //菜品口味
        //遍历flavor，并为每一个赋id
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishID);
            return item;
        }).collect(Collectors.toList());//重新转换成list

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);//saveBatch批量保存
    }

}

