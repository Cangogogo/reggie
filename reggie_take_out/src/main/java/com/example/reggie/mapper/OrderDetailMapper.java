package com.example.reggie.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.reggie.entity.OrderDetail;


@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}