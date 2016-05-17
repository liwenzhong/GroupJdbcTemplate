package com.lee.springjdbc.group.parse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by liwenzhong on 15-12-21.
 */
public class DefaultSqlParser implements ISqlParser {

	private static final Pattern SELECT_FOR_UPDATE_PATTERN = Pattern.compile("^[(\\s]*select\\s+.*\\s+for\\s+update.*$",
			Pattern.CASE_INSENSITIVE);

	private static final char[] ignoreChars = new char[]{'(', ' '};

	@Override
	public SqlType parse(String sql) {
		return getSqlType(sql);
	}

	protected static SqlType getSqlType(String sql) {
		SqlType sqlType = null;
		sql = StringUtils.trim(sql);
		if (startsWithIgnoreCase(sql, "select", ignoreChars)) {
			if (SELECT_FOR_UPDATE_PATTERN.matcher(sql).matches()) {
				sqlType = SqlType.SELECT_FOR_UPDATE;
			} else {
				sqlType = SqlType.SELECT;
			}
		} else if (startsWithIgnoreCase(sql, "insert", ignoreChars)) {
			sqlType = SqlType.INSERT;
		} else if (startsWithIgnoreCase(sql, "update", ignoreChars)) {
			sqlType = SqlType.UPDATE;
		} else if (startsWithIgnoreCase(sql, "delete", ignoreChars)) {
			sqlType = SqlType.DELETE;
		} else {
			sqlType = SqlType.OTHER;
		}
		return sqlType;
	}

	protected static boolean startsWithIgnoreCase(String str, String targetStr, char[] ignoreChars){
		if(StringUtils.isBlank(str)){
			return StringUtils.isBlank(targetStr);
		}

		if(StringUtils.isBlank(targetStr)){
			return false;
		}

		int pos = 0;
		int len = str.length();
		if(ignoreChars !=null && ignoreChars.length>0){
			for(;pos<len;pos++){
				if (!ArrayUtils.contains(ignoreChars, str.charAt(pos))){
					break;
				}
			}
		}

		return str.regionMatches(true, pos, targetStr, 0, targetStr.length());
	}

}
