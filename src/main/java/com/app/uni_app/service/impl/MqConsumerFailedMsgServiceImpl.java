package com.app.uni_app.service.impl;

import com.app.uni_app.mapper.MqConsumerFailedMsgMapper;
import com.app.uni_app.pojo.entity.MqConsumerFailedMsg;
import com.app.uni_app.service.MqConsumerFailedMsgService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MqConsumerFailedMsgServiceImpl extends ServiceImpl<MqConsumerFailedMsgMapper, MqConsumerFailedMsg> implements MqConsumerFailedMsgService {
}
