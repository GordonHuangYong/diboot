/*
 * Copyright (c) 2015-2020, www.dibo.ltd (service@dibo.ltd).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.diboot.iam.starter;

import com.diboot.core.plugin.PluginManager;
import com.diboot.core.starter.SqlHandler;
import com.diboot.core.util.ContextHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

/**
 * IAM组件相关的初始化
 * @author mazc@dibo.ltd
 * @version v2.0
 * @date 2019/12/23
 */
@Slf4j
public class IamBasePluginManager implements PluginManager {

    public void initPlugin(IamBaseProperties iamBaseProperties){
        // 检查数据库字典是否已存在
        if(iamBaseProperties.isInitSql()){
            Environment environment = ContextHelper.getApplicationContext().getEnvironment();
            SqlHandler.init(environment);
            // 验证SQL
            String initDetectSql = "SELECT id FROM ${SCHEMA}.iam_role WHERE id=0";
            if(SqlHandler.checkSqlExecutable(initDetectSql) == false){
                log.info("diboot-IAM-base 初始化SQL ...");
                // 执行初始化SQL
                SqlHandler.initBootstrapSql(this.getClass(), environment, "iam-base");
                // 插入相关数据：Dict，Role等
                ContextHelper.getBean(IamBaseInitializer.class).insertInitData();
                log.info("diboot-IAM-base 初始化SQL完成.");
            }
            else{
                String upgradeDetectSql = "SELECT tenant_id FROM ${SCHEMA}.iam_role WHERE id=0";
                if(SqlHandler.checkSqlExecutable(upgradeDetectSql) == false){
                    SqlHandler.initUpgradeSql(this.getClass(), environment, "iam-base");
                    log.info("diboot-IAM-base SQL更新完成.");
                }
            }
        }
    }
}