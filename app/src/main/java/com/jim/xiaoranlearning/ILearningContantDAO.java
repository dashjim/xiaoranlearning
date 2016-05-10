package com.jim.xiaoranlearning;

import android.content.Context;

public interface ILearningContantDAO {

	public abstract void init(Context aContext);

	public abstract void loadNewContent(ContentType contentType);

	/**
	 * save current position, current vo array, how much marked as Known. //TODO
	 * @param position
	 */
	public abstract void saveLastStatus(int position);

	/**
	 * Will be called from UI onStart().
	 * @return last position
	 */
	public abstract int getLastStatus();

	public abstract ContentVO getCurrentDisplay(int position);

	public abstract void markKnown(int position);

	public abstract void markUnKnown(int position);

	public abstract void increaseDisplayTimes(int pos);

	public abstract void increaseLearned();

	public abstract int getContentLength();

	void addContent(ContentVO vo);

	void delete(int currentItem);
}