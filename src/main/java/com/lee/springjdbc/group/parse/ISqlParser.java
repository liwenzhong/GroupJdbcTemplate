package com.lee.springjdbc.group.parse;

/**
 * Created by liwenzhong on 15-12-21.
 */
public interface ISqlParser {

	SqlType parse(String sql);

}
