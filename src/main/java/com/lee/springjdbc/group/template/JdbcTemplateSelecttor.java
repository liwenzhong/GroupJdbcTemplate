package com.lee.springjdbc.group.template;

import com.google.common.base.Objects;
import com.google.common.primitives.Ints;
import com.lee.springjdbc.group.parse.SqlType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Comparator;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by liwenzhong on 16-5-16.
 */
public class JdbcTemplateSelecttor {

	/**
	 * 只读的数据源
	 */
	private SortedMap<Integer, JdbcTemplate> reads;

	private int readSumScore = 1;

	/**
	 * 可写的数据源
	 */
	private SortedMap<Integer, JdbcTemplate> writes;

	private int writeSumScore = 1;

	private Weight weight = new Weight();

	public JdbcTemplateSelecttor() {
		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Ints.compare(o1, o2);
			}
		};

		this.reads = new TreeMap<Integer, JdbcTemplate>(comparator);

		this.writes = new TreeMap<Integer, JdbcTemplate>(comparator);
	}

	public JdbcTemplate getJdbcTemplate(SqlType sqlType) {
		if (Objects.equal(SqlType.SELECT, sqlType)) {
			return this.getReadJdbcTemplate();
		}

		return this.getWriteJdbcTemplate();
	}

	public void addReadDataSources(int score, JdbcTemplate jdbcTemplate) {
		if (score == 0 || jdbcTemplate == null) {
			throw new IllegalArgumentException("score must be greater than 0 and jdbcTemplate must not be null!");
		}
		this.readSumScore += Math.abs(score);
		this.reads.put(readSumScore, jdbcTemplate);
	}

	public void addWriteDataSource(int score, JdbcTemplate jdbcTemplate) {
		if (score == 0 || jdbcTemplate == null) {
			throw new IllegalArgumentException("score must be greater than 0 and jdbcTemplate must not be null!");
		}
		this.writeSumScore += Math.abs(score);
		this.writes.put(writeSumScore, jdbcTemplate);

	}

	public JdbcTemplate getReadJdbcTemplate() {
		int w = this.weight.next(this.readSumScore);
		SortedMap<Integer, JdbcTemplate> readMap = this.reads.tailMap(w);
		return readMap.get(readMap.firstKey());
	}

	public JdbcTemplate getWriteJdbcTemplate() {
		int w = this.weight.next(this.writeSumScore);
		SortedMap<Integer, JdbcTemplate> writeMap = this.writes.tailMap(w);
		return writeMap.get(writeMap.firstKey());
	}

}

class Weight {
	private final Random random;

	public Weight() {
		this.random = new Random();
	}

	public Weight(long seed) {
		this.random = new Random(seed);
	}

	/**
	 * return between 1 (inclusive) and bound (inclusive)
	 *
	 * @param bound
	 * @return
	 */
	public int next(int bound) {
		return random.nextInt(bound) + 1;
	}
}
