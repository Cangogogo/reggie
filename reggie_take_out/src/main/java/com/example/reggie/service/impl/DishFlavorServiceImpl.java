package com.example.reggie.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.entity.DishFlavor;
import com.example.reggie.mapper.DishFlavorMapper;
import com.example.reggie.service.DishFlavorService;


@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}

