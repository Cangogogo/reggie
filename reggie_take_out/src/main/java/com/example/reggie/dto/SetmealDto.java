package com.example.reggie.dto;
import java.util.List;

import com.example.reggie.entity.Setmeal;
import com.example.reggie.entity.SetmealDish;

import lombok.Data;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}


