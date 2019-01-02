package com.atguigu.gmall.user.Service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.service.UserAddressService;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Override
    public List<UserAddress> getUserAddressList() {
        return userAddressMapper.selectAll();
    }
}
