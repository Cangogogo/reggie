package com.example.reggie.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.entity.Orders;


public interface OrderService  extends IService<Orders>{
    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    public Page<OrdersDto> empPage(int page, int pageSize, String number, String beginTime, String endTime);
    
}
