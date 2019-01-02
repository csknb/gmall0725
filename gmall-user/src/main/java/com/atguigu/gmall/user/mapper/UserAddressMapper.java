package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.bean.UserAddress;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserAddressMapper extends Mapper<UserAddress> {


    List<UserAddress> selectUserAddressListByUserId(String userId);
}
