package com.example.reggie.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.common.CustomException;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.entity.SetmealDish;
import com.example.reggie.mapper.SetmealMapper;
import com.example.reggie.service.SetmealDishService;
import com.example.reggie.service.SetmealService;

//SetmealServiceImpl
@Service

public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    
    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional  //操作两张表 加入事物
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);
        Long setmealId = setmealDto.getId();    //套餐id

        //保存套餐和菜品的关联关系，操作setmeal_dish，执行insert操作
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        dishes.stream().map((item) -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(dishes);
    }

    /**
     * 删除套餐,同时删除套餐和菜品的关联数据
     *
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐的状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null, Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);
        if (count > 0) {
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在使用中！");
        }
        //如果可以删除，先删除套餐表中的数据--setmeal
        this.removeByIds(ids);
        //然后删除关联数据--setmeal_dish
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 根据id来查询套餐信息和对应的菜品信息
     *
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //查询套餐的基本信息，从setmeal表中查询
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        //查询当前套餐对应的菜品信息，从setmeal_dish表中查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //根据套餐的id查对应的菜品
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());

        List<SetmealDish> dishs = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(dishs);
        return setmealDto;
    }

    /**
     * 更新套餐信息，同时更新对应的菜品信息
     *
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新setmeal表基本信息
        this.updateById(setmealDto);
        //清理当前套餐对应菜品数据--》setmeal_dish表中的delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //根据dto的id查对应的菜品信息
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //添加当前提交过来的菜品数据--setmeal_dish表的insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }






    


}

