package com.lhh.serverinfocommon.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自动填充实体
 *
 * @author Rona
 * @date 2019/4/12
 */
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetaObjectHandlerConfig.class);

  /**
   * 保存自动填充实体
   * @param metaObject
   */
    @Override
    public void insertFill(MetaObject metaObject) {
      this.setFieldValByName("createTime", new Date(), metaObject);
      this.setFieldValByName("updateTime", new Date(), metaObject);
      this.setFieldValByName("flg", 0, metaObject);
      this.setFieldValByName("createId", 1L, metaObject);
      this.setFieldValByName("updateId", 1L, metaObject);
    }

  /**
   * 更新自动填充实体
   * @param metaObject
   */
  @Override
    public void updateFill(MetaObject metaObject) {
      this.setFieldValByName("updateTime", new Date(), metaObject);
    }

}
