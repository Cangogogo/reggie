package com.example.reggie.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.mapper.SetmealMapper;
import com.example.reggie.service.SetmealService;

//SetmealServiceImpl
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
}

