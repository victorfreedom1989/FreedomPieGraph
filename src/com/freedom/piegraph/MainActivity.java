package com.freedom.piegraph;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private PiegraphView view;
	private TextView text;
	private TextView back;
	private int radius;
	private int strokeWidth;
	private String strokeColor = "#ffffff";
	private float animSpeed = (float) 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		view = (PiegraphView) findViewById(R.id.piechar_view);
		text = (TextView) findViewById(R.id.content);
		back = (TextView) findViewById(R.id.back);
		back.getBackground().setAlpha(180);
		text.setText("第一块");
		radius = ScreenUtil.dip2px(this, 140);
		strokeWidth = ScreenUtil.dip2px(this, 3);
		view.setItemsSizes(new Double[] { 10d, 20d, 30d, 20d, 40d });
		// pieChart.setTotal(total);//设置整体的值, 默认为和
		// pieChart.setItemsColors(colors);//设置各个块的颜色
		view.setAnimSpeed(animSpeed);// 设置旋转速度
		view.setRaduis(radius);// 设置饼状图半径，不包含边缘的圆环
		view.setStrokeWidth(strokeWidth);// 设置边缘的圆环粗度
		view.setStrokeColor(strokeColor);// 设置边缘的圆环颜色
		// pieChart.setRotateWhere(PieChartView.TO_RIGHT);//设置选中的item停靠的位置，默认在右侧
		view.setItemSelectedListener(new OnPiegraphItemSelectedListener() {

			@Override
			public void onPieChartItemSelected(int position, String colorRgb,
					double size, float rate, boolean isFreePart,
					float rotateTime) {

				Toast.makeText(MainActivity.this, "第" + position + "块",
						Toast.LENGTH_SHORT).show();
				text.setText("第" + (position + 1) + "块");
			}
		});
	}

}
