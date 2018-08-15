# 组件模块

模块化开发过程中相对独立的功能，并抽象成相对一致的功能组件。

## http
封装底层细节，提供更好的使用抽象。

## mybatis增强
增强mybatis的使用体验，提升效率。

### 分页插件
    <plugin type="com.feixue.assembly.mybatis.MysqlLimitPlugin"></plugin>
### 插入忽略插件
基于唯一键冲突原理，insert ignore的语法实现。

    <plugin type="com.feixue.assembly.mybatis.InsertIgnorePlugin"></plugin>
### 悲观锁插件
基于select for update的语法实现。

    <plugin type="com.feixue.assembly.mybatis.SelectForUpdatePlugin"></plugin>
### 文档插件
数据库表映射JavaBean实体，同时将comment处理成字段注释文档

    <plugin type="com.feixue.assembly.mybatis.GeneratorSwagger2Doc"></plugin>
### 全局时间格式化插件
支持jackson，fastJson等序列化框架。识别表中的timestamp类型字段，然后生成相应的格式化注解

    <plugin type="com.feixue.assembly.mybatis.GeneratorGlobalJSONFormat">
        <property name="jsonType" value="jackson"/>
        <property name="datePattern" value="yyyy-MM-dd HH:mm:ss"/>
    </plugin>
### Component注解插件
在mapper的接口上添加此注解，提升idea内的bean注入可读性。

    <plugin type="com.feixue.assembly.mybatis.ComponentAnnotationPlugin"></plugin>
### 类型处理
将tinyint(1)处理成Boolean，而不是Byte。方便使用。

    <javaTypeResolver type="com.feixue.assembly.mybatis.JavaTypeResolverExtendImpl">
        <!--
            true：使用BigDecimal对应DECIMAL和 NUMERIC数据类型
            false：默认,
                scale>0;length>18：使用BigDecimal;
                scale=0;length[10,18]：使用Long；
                scale=0;length[5,9]：使用Integer；
                scale=0;length<5：使用Short；
         -->
        <property name="forceBigDecimals" value="false"/>
        <property name="tinyint2Boolean" value="true"/>
    </javaTypeResolver>
## 调度
### 简单调度
基于线程池机制，将任务调度编程化处理。支持调度周期性任务，一次性任务。

### 分布式调度
#### 最少一次策略
思路：当多个节点相互竞争时，无论网络节点出现什么问题，只要有执行节点与zk保持相连，那就可以保证最少有一个节点会执行。此时，不保证不会并行执行，业务方需要自行做幂等处理。
实现原理：基于zookeeper的选举机制，多节点竞选执行权，竞选成功则执行任务。执行完毕则释放节点占用，进行下一轮竞争。