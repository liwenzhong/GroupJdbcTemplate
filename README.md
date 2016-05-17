# GroupJdbcTemplate
基于spring-jdbc 中template的封装，提供了多数据源支持以及简单的读写分离的支持:
1. 多数据源时，可以指定每个数据源的配置权重
2. 读写分离: 对于sql进行解析sql操作类型, 重载了JdbcTemplate的关键方法，对于JdbcTemplate的带有sql入餐的函数，提供读写分离支持。
　　　　　　　同时默认兼容JdbcTemplate的getDataSource()方法，默认返回master的数据源。
　　其中提供了默认的简单地sql解析: DefaultSqlParser.java，　当然你可以自己实现ISqlParser.java来获得自己的sql解析语法

3. demo如下: 一个典型的配置如下: (这里demo 用的pg的database, 以及c3p0连接池实现)

	<bean id="masterDataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource" destroy-method="close">
		<property name="driverClass" value="org.postgresql.Driver" />
		<property name="jdbcUrl">
			<value><![CDATA[${db.url}]]></value>
		</property>
		<property name="user" value="${db.username}" />
		<property name="password" value="${db.password}" />
		<property name="initialPoolSize" value="${db.initialPoolSize}" />
        <property name="minPoolSize" value="${db.initialPoolSize}"/>
		<property name="maxPoolSize" value="${db.maxPoolSize}" />
		<property name="idleConnectionTestPeriod" value="60" />
        <property name="maxIdleTime" value="30" />
        <property name="numHelperThreads" value="3" />
        <property name="preferredTestQuery" value="select 1" />
	</bean>

    <bean id="slaveDataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource" destroy-method="close">
        <property name="driverClass" value="org.postgresql.Driver" />
        <property name="jdbcUrl">
            <value><![CDATA[${slave.db.url}]]></value>
        </property>
        <property name="user" value="${slave.db.username}" />
        <property name="password" value="${slave.db.password}" />
        <property name="initialPoolSize" value="${slave.db.initialPoolSize}" />
        <property name="minPoolSize" value="${slave.db.initialPoolSize}" />
        <property name="maxPoolSize" value="${slave.db.maxPoolSize}" />
        <property name="idleConnectionTestPeriod" value="120" />
        <property name="maxIdleTime" value="30" />
        <property name="numHelperThreads" value="3" />
        <property name="preferredTestQuery" value="select 1" />
    </bean>

    <!-- group dataSource -->
    <bean id="groupJdbcTemplate" name="groupJdbcTemplate" class="com.qunar.piao.sight.dao.base.GroupJdbcTemplate">
        <property name="writeDataSources">
            <map key-type="javax.sql.DataSource" value-type="java.lang.Integer">
                <entry key-ref="masterDataSource" value="1"/>
            </map>
        </property>
        <property name="readDataSources">
            <map key-type="javax.sql.DataSource" value-type="java.lang.Integer">
                <entry key-ref="masterDataSource" value="1"/>
                <entry key-ref="slaveDataSource" value="2"/>
            </map>
        </property>
    </bean>

    然后在你的项目中，使用JdbcTemplate的地方，都可以换成GroupJdbcTemplate的实例。

４．由于这是基于spring-jdbc做的简单的包装，验证对于3.x版本是OK的，不排除以后版本的spring-jdbc的源码实现发生改变，对于不同版本，读写分离实现可能会不工作，请测试后使用。
