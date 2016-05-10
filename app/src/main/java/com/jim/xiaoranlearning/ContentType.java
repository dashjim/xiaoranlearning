package com.jim.xiaoranlearning;

public enum ContentType{
	
	CHINESE_265,ENGLISH_WORDS, ENGLISH_CHANT;
	
	/**use to get the instance from a ordinal() values*/
    public static ContentType valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}