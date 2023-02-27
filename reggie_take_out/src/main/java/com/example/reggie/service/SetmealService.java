package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Setmeal;


//SetmealService
public interface SetmealService extends IService<Setmeal> {

    void saveWithDish(SetmealDto setmealDto);
}

