package com.my.recordplayer.b;

public class d {
	private int num;
	private int progress;

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int pregress) {
		this.progress = pregress;
	}

	public int calcuLatePer(int per) {
		return per * num + per * progress / 100;
	}
}
