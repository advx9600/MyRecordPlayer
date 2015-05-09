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

	public int calcuLatePer(double per, boolean isStart) {
		int val = 0;
		if (isStart) {
			val = (int) Math.floor(per * num + per * progress / 100);
		} else {
			val = (int) Math.ceil(per * num + per * progress / 100);
			val = val > 100 ? 100 : val;
		}
		return val;
	}
}
