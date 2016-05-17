package com.lee.springjdbc.group.parse;

/**
 * Created by liwenzhong on 15-12-21.
 */
public enum SqlType {
	OTHER(-1),
	SELECT(0),
	INSERT(1),
	UPDATE(2),
	DELETE(3),
	SELECT_FOR_UPDATE(4);
	private int code;

	private SqlType(int code) {
		this.code = code;
	}

	public int value() {
		return this.code;
	}

	public static SqlType valueOf(int i) {
		for (SqlType t : values()) {
			if (t.value() == i) {
				return t;
			}
		}
		throw new IllegalArgumentException("Invalid SqlType:" + i);
	}
}
