package com.example.reggie.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.common.CustomException;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.mapper.CategoryMapper;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishService;
import com.example.reggie.service.SetmealService;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

   /**
     * 根据id删除分类，删除之前需要进行判断
     *
     * @param ids
     */
    @Override
    public void remove(Long ids) {

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件,根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        int count1 = dishService.count(dishLambdaQueryWrapper);


        //查询当前分类是否关联了菜品，如果已经关联就抛出一个业务异常
        if (count1 > 0) {
            //已经关联菜品，抛出一个业务异常
            throw  new CustomException("当前分类下关联了菜品，不能删除");
        }

        //查询当前分类是否关联了套餐，如果已经关联就抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if (count2 > 0){
            //已经关联套餐，抛出一个业务异常
            throw  new CustomException("当前分类下关联了套餐，不能删除");
        }
        
        //两次查询如果都没有关联，正常删除
        super.removeById(ids);
      
     }
}


