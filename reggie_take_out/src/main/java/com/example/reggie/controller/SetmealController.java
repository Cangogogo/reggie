package com.example.reggie.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishService;
import com.example.reggie.service.SetmealDishService;
import com.example.reggie.service.SetmealService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private DishService dishService;
    
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    // /**
    //  * 根据条件查询对应的菜品数据
    //  *
    //  * @param dish
    //  * @return
    //  */
    // @GetMapping("/list")
    // public R<List<Dish>> list(Dish dish) {
    //     //构造条件查询对象
    //     LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
    //     queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
    //     //查询状态为1的菜品，也就是查询正在起售的商品
    //     queryWrapper.eq(Dish::getStatus,1);
    //     //添加排序条件
    //     queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

    //     List<Dish> list = dishService.list(queryWrapper);
    //     return R.success(list);
    // }

    @PostMapping
    @CacheEvict(value="setmealCache",allEntries = true)//清理所有数据
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();


        //创建条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，根据name进行like模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);
        //添加排序条件(根据更新时间降序排序)
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        //原来的records不带菜品分类,把之前的records提取出来并加上菜品分类，再注入到DishmealDto所构造的分页查询中
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);

        return R.success(dtoPage);
    }

    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    //CacheEvict:清除缓存中key所对应的数据
    //#root.args[0]获取参数表中的第一个数据
    //#result.id:从返回结果中获得id属性值
    //@CacheEvict(value="userCache",key="")
    @CacheEvict(value="setmealCache",allEntries = true)//清理所有数据
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("删除套餐成功！");
    }

    /**
     * 根据id来查询套餐信息和对应的菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get (@PathVariable Long id){
        SetmealDto setmealDao = setmealService.getByIdWithDish(id);
        return R.success(setmealDao);
    }

    /**
     * 更新套餐信息，同时更新对应的菜品信息
     *
     * @param setmealDto
     */
    @PutMapping
    @CacheEvict(value="setmealCache",allEntries = true)//清理所有数据
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());

        setmealService.updateWithDish(setmealDto);
        return R.success("修改套餐成功");
    }

    /**
     * 修改套餐的售卖状态
     *
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> statusWithIds(@PathVariable("status") Integer status, @RequestParam List<Long> ids) {
        //构造一个条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null, Setmeal::getId, ids);//根据id
        queryWrapper.orderByDesc(Setmeal::getPrice);//排序
        List<Setmeal> list = setmealService.list(queryWrapper);
        for (Setmeal setmeal : list) {
            if (list != null) {
                //把浏览器传入的status参数复制给套餐
                setmeal.setStatus(status);
                setmealService.updateById(setmeal);
            }
        }
        return R.success("售卖状态修改成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    //Cacheable在方法执行前spring会先查看缓存中是否有数据,如果有数据,则直接返回缓存数据,如果没有数据,则会调用方法将方法返回值放到缓存中
    //该注解还可以解决缓存穿透(redis和数据库中),体现在当key所对应的val在数据库中并不存在的时候,这时候会将一个空占位数据缓存到缓存中(缓存空对象,可以预防高并发的时候访问不存在的数据,服务端缓存空数据,客户端访问时直接返回空数据)
    //condition可以用来指定什么情况下缓存数据,这里给的条件是当查询得到的result不为空的时候才把数据缓存到服务端(但Cacheable没有这个上下文对象)
    //unless是除非的意思,也就是除非#result==null的时候就不缓存
    //注意:key应该是有唯一格式,用来规定查询何种缓存
    //@Cacheable(value="userCache",key="",unless = "#result==null")
    @Cacheable(value ="setmealCache",key = "#setmeal.categoryId+'_'+#status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }


}


