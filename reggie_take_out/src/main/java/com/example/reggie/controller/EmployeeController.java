package com.example.reggie.controller;
import com.example.reggie.entity.Employee;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.reggie.service.EmployeeService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

    /*员工登录
     * @param request
     * @param employee
     * @return 
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //将接收到的密码进行加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //根据接收到的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        if(emp == null){
            return R.error("登录失败");
        }

        //密码对比
        if(!emp.getPassword().equals(password)){return R.error("登录失败");}

        //查看员工状态
        if(emp.getStatus()==0){return R.error("账号已禁用");}

        //登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);



    }

    
    /*
     * 员工退出
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中的用户id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    
    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());
        //设置初始密码为,进行MD5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //获得当前登录用户的id
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);

        //employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息的分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    //Page范性有前端需要的data.total  data.record
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, pageSize, name);

        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //构造条件构造器，封装过滤条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加一个过滤条件 （当name不为空是添加条件） like是相似度查询
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加一个排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id来修改员工信息
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empId);

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }
    
    /**
     * 根据id查询员工信息 
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息");
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }








}
