package com.atguigu.gmall.user.Service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.utils.RedisUtil;
import com.atguigu.gmall.utils.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import javax.jms.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceInfoImpl implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public List<UserInfo> getUserInfoList() {

        return userInfoMapper.selectAll();
    }

    @Override
    public int delUserInfoById(String id) {

        System.out.print(id);
        return userInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int addUser(UserInfo userInfo) {

        return userInfoMapper.insert(userInfo);
    }

    @Override
    public int updateUser(Long id, UserInfo userInfo) {
        return userInfoMapper.updateByPrimaryKey(userInfo);
    }

    @Override
    public int remove(UserInfo userInfo) {
        return userInfoMapper.delete(userInfo);
    }

    public UserInfo getUserInfoById(Long id) {
        return userInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        UserInfo userInfoDB = userInfoMapper.selectOne(userInfo);
        System.out.println(redisUtil);
        //将登陆之后的用户信息存入到redis缓存中
        Jedis jedis = redisUtil.getJedis();
        if(userInfoDB!=null){
            String userId = userInfoDB.getId();
            //导入缓存，序列化，使用hash
            Map map = new HashMap<>();
            map.put(userInfoDB.getId(), JSON.toJSONString(userInfoDB));
            jedis.hmset("user:"+userId+":info",map);
//            下面两句是测试能不能再redis中取出
//            String s = jedis.hvals("user:" + userId + ":info").get(0);
//            UserInfo userInfo1 = JSON.parseObject(s, UserInfo.class);

            jedis.close();
        }
        return userInfoDB;
    }

    @Override
    public List<UserAddress> getAddressListByUserId(String userId) {
//        UserAddress userAddress = new UserAddress();
//        userAddress.setUserId(userId);
//        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);
//        return userAddressList;
        List<UserAddress> userAddressList =  userAddressMapper.selectUserAddressListByUserId(userId);
        return userAddressList;
    }

    @Override
    public UserAddress getAddressById(String addressId) {
        Long id = Long.valueOf(addressId);
        UserAddress userAddress = userAddressMapper.selectByPrimaryKey(id);
        return userAddress;
    }

    @Override
    public void sendLoginSuccess(String userId, List<CartInfo> cartInfosCookie) {
            //发送支付成功的消息队列
            //建立mq工厂
            ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
            Connection connection=null;
            try {
                //如果第一个值是true，就代表是开启mq事务
                connection = connectionFactory.createConnection();
                connection.start();
                Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
                Queue loginResultQueue = session.createQueue("LOGIN_RESULT_QUEUE");
                MapMessage mapMessage=new ActiveMQMapMessage();
                mapMessage.setString("userId",userId);
                mapMessage.setString("cartInfosCookie",JSON.toJSONString(cartInfosCookie));
                MessageProducer producer = session.createProducer(loginResultQueue);

                producer.setDeliveryMode(DeliveryMode.PERSISTENT);

                producer.send(mapMessage);
                session.commit();

                producer.close();
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
    }
}
