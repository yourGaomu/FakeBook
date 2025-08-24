package com.zhangzc.bookauth.MQ.Service;

import com.zhangzc.bookauth.Pojo.Vo.MailVo;

public interface RabbitMQService {

   void sendCode(MailVo mailVo);
}
