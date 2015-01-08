package com.freedom.piegraph;

public abstract interface OnPiegraphItemSelectedListener {
	public void onPieChartItemSelected(int position, String colorRgb,
			double size, float rate, boolean isFreePart, float rotateTime);
}