package com.lee.springjdbc.group.template;

import com.lee.springjdbc.group.parse.DefaultSqlParser;
import com.lee.springjdbc.group.parse.ISqlParser;
import com.lee.springjdbc.group.parse.SqlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

/**
 * Created by liwenzhong on 15-12-21.
 */
public class GroupJdbcTemplate extends JdbcTemplate implements JdbcOperations {

	private static final Logger LOGGER = LoggerFactory.getLogger(GroupJdbcTemplate.class);

	private Map<DataSource, Integer> readDataSources;

	private Map<DataSource, Integer> writeDataSources;

	private ISqlParser sqlParser;

	private JdbcTemplateSelecttor templateSelecttor;

	public Map<DataSource, Integer> getReadDataSources() {
		return Collections.unmodifiableMap(readDataSources);
	}

	public Map<DataSource, Integer> getWriteDataSources() {
		return Collections.unmodifiableMap(writeDataSources);
	}

	public void setReadDataSources(Map<DataSource, Integer> readDataSources) {
		this.readDataSources = readDataSources;
	}

	public void setWriteDataSources(Map<DataSource, Integer> writeDataSources) {
		this.writeDataSources = writeDataSources;
	}

	public ISqlParser getSqlParser() {
		return sqlParser;
	}

	public void setSqlParser(ISqlParser sqlParser) {
		this.sqlParser = sqlParser;
	}

	/**
	 * 兼容JdbcTemplate的方法，为了保险，默认返回写的dataSource;
	 *
	 * @return
	 */
	@Override
	public DataSource getDataSource() {
		return this.templateSelecttor.getWriteJdbcTemplate().getDataSource();
	}

	/**
	 * 获取真正使用的jdbcTemplate
	 *
	 * @param sql
	 * @return
	 */
	protected JdbcTemplate getJdbcTemplate(String sql) {
		SqlType sqlType = this.sqlParser.parse(sql);
		JdbcTemplate template = this.templateSelecttor.getJdbcTemplate(sqlType);
		LOGGER.info("[group template] sql={}, type={}, template={}", sql, sqlType, template);

		return template;
	}

	/**
	 * 初始化
	 */
	@Override
	public void afterPropertiesSet() {

		Assert.notEmpty(readDataSources);
		Assert.notEmpty(writeDataSources);

		if (this.sqlParser == null) {
			sqlParser = new DefaultSqlParser();
		}

		this.templateSelecttor = new JdbcTemplateSelecttor();
		for (Map.Entry<DataSource, Integer> readEntry : readDataSources.entrySet()) {
			JdbcTemplate template = new JdbcTemplate(readEntry.getKey());
			LOGGER.info("[group template] read dataSource={}, template={}, weight={}", readEntry.getKey(), template, readEntry.getValue());
			templateSelecttor.addReadDataSources(readEntry.getValue(), template);
		}

		for (Map.Entry<DataSource, Integer> writeEntry : writeDataSources.entrySet()) {
			JdbcTemplate template = new JdbcTemplate(writeEntry.getKey());
			LOGGER.info("[group template] write dataSource={}, template={}, weight={}", writeEntry.getKey(), template, writeEntry.getValue());
			templateSelecttor.addWriteDataSource(writeEntry.getValue(), template);
		}

		//因为父类在中调用了getDataSource方法，所以这个地方需要初始化之后才进行验证
		super.afterPropertiesSet();
	}

	//////////////////////////////////////// override mthod in JdbcTemplate /////////////////////////////////////
	@Override
	public void execute(final String sql) throws DataAccessException {
		this.getJdbcTemplate(sql).execute(sql);
	}

	@Override
	public <T> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException {
		return this.getJdbcTemplate(sql).execute(sql, action);
	}

	@Override
	public <T> T query(final String sql, final ResultSetExtractor<T> rse) throws DataAccessException {
		return this.getJdbcTemplate(sql).query(sql, rse);
	}

	@Override
	public <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException {
		return this.getJdbcTemplate(sql).query(sql, pss, rse);
	}

}
