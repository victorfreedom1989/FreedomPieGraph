package com.freedom.piegraph;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint({ "DrawAllocation" })
public class PiegraphView extends View implements Runnable {

	// 旋转方向
	public static final int TO_RIGHT = 0;
	public static final int TO_BOTTOM = 1;
	public static final int TO_LEFT = 2;
	public static final int TO_TOP = 3;

	// 颜色值
	private final String[] DEFAULT_ITEMS_COLORS = { "#FF0000", "#FFFF01",
			"#FF9933", "#9967CC", "#00CCCC", "#00CC33", "#0066CC", "#FF6799",
			"#99FF01", "#FF67FF", "#4876FF", "#FF00FF", "#FF83FA", "#0000FF",
			"#363636", "#FFDAB9", "#90EE90", "#8B008B", "#00BFFF", "#FFFF00",
			"#00FF00", "#006400", "#00FFFF", "#00FFFF", "#668B8B", "#000080",
			"#008B8B" };

	// 动画速度
	private float animSpeed = 3.0F;
	// 总数值
	private double total;
	// 各饼块对应的数值
	private Double[] itemSizesTemp;
	// 各饼块对应的数值
	private Double[] itemsSizes;
	// 各饼块对应的颜色
	private String[] itemsColors;
	// 各饼块的角度
	private float[] itemsAngle;
	// 各饼块的起始角度
	private float[] itemsBeginAngle;
	// 各饼块的占比
	private float[] itemsRate;
	// 起始角度
	private float rotateAng = 0.0F;
	// 结束角度
	private float lastAng = 0.0F;
	// 正转还是反转
	private boolean bClockWise;
	// 正在旋转
	private boolean isRotating;
	// 是否开启动画
	private boolean isAnimEnabled = true;
	// 边缘圆环的颜色
	private String radiusBorderStrokeColor;
	// 边缘圆环的宽度
	private float strokeWidth = 0.0F;
	// 饼图半径，不包括圆环
	private float radius;
	// 当前item的位置
	private int itemPostion = -1;
	// 停靠位置
	private int rotateWhere = 0;

	// 消息接收器
	private Handler rotateHandler = new Handler();
	private static final String TAG = "ParBarView";

	// 监听器集合
	private OnPiegraphItemSelectedListener itemSelectedListener;

	public PiegraphView(Context context, String[] itemColors,
			Double[] itemSizes, float total, int radius, int strokeWidth,
			String strokeColor, int rotateWhere, int separateDistence) {
		super(context);

		this.rotateWhere = rotateWhere;

		if ((itemSizes != null) && (itemSizes.length > 0)) {
			this.itemSizesTemp = itemSizes;
			this.total = total;
			reSetTotal();
			refreshItemsAngs();
		}

		if (radius < 0)
			this.radius = 100.0F;
		else {
			this.radius = radius;
		}
		if (strokeWidth < 0)
			strokeWidth = 2;
		else {
			this.strokeWidth = strokeWidth;
		}

		this.radiusBorderStrokeColor = strokeColor;

		if (itemColors == null) {
			setDefaultColor();
		} else if (itemColors.length < itemSizes.length) {
			this.itemsColors = itemColors;
			setLeftColor();
		} else {
			this.itemsColors = itemColors;
		}

		invalidate();
	}

	public PiegraphView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.radiusBorderStrokeColor = "#000000";
		invalidate();
	}

	public void setRaduis(int radius) {
		if (radius < 0)
			this.radius = 100.0F;
		else {
			this.radius = radius;
		}
		invalidate();
	}

	public float getRaduis() {
		return this.radius;
	}

	public void setStrokeWidth(int strokeWidth) {
		if (strokeWidth < 0)
			strokeWidth = 2;
		else {
			this.strokeWidth = strokeWidth;
		}
		invalidate();
	}

	public float getStrokeWidth() {
		return this.strokeWidth;
	}

	public void setStrokeColor(String strokeColor) {
		this.radiusBorderStrokeColor = strokeColor;

		invalidate();
	}

	public String getStrokeColor() {
		return this.radiusBorderStrokeColor;
	}

	/**
	 * @Title: setItemsColors
	 * @Description: 设置个饼块的颜色
	 * @param colors
	 * @throws
	 */
	public void setItemsColors(String[] colors) {
		if ((this.itemsSizes != null) && (this.itemsSizes.length > 0)) {
			// 如果传入值未null，则使用默认的颜色
			if (colors == null) {
				setDefaultColor();
			} else if (colors.length < this.itemsSizes.length) {
				// 如果传入颜色不够，则从默认颜色中填补
				this.itemsColors = colors;
				setLeftColor();
			} else {
				this.itemsColors = colors;
			}
		}

		invalidate();
	}

	public String[] getItemsColors() {
		return this.itemsColors;
	}

	/**
	 * @Title: setItemsSizes
	 * @Description: 设置各饼块数据
	 * @param items
	 * @throws
	 */
	public void setItemsSizes(Double[] items) {
		if ((items != null) && (items.length > 0)) {
			this.itemSizesTemp = items;
			// 重设总值，默认为所有值的和
			reSetTotal();
			refreshItemsAngs();
			setItemsColors(this.itemsColors);
		}
		invalidate();
	}

	public Double[] getItemsSizes() {
		return this.itemSizesTemp;
	}

	public void setTotal(int total) {
		this.total = total;
		reSetTotal();

		invalidate();
	}

	public double getTotal() {
		return this.total;
	}

	public void setAnimEnabled(boolean isAnimEnabled) {
		this.isAnimEnabled = isAnimEnabled;
		invalidate();
	}

	public boolean isAnimEnabled() {
		return this.isAnimEnabled;
	}

	public void setAnimSpeed(float animSpeed) {
		if (animSpeed < 0.5F) {
			animSpeed = 0.5F;
		}
		if (animSpeed > 5.0F) {
			animSpeed = 5.0F;
		}
		this.animSpeed = animSpeed;
	}

	public float getAnimSpeed() {
		if (isAnimEnabled()) {
			return this.animSpeed;
		}
		return 0.0F;
	}

	public void setShowItem(int position, boolean anim, boolean listen) {
		if ((this.itemsSizes != null) && (position < this.itemsSizes.length)
				&& (position >= 0)) {
			this.lastAng = getLastRotateAngle(position);
			this.itemPostion = position;

			if (anim) {
				this.rotateAng = 0.0F;
				if (this.lastAng > 0.0F) {
					this.bClockWise = true;
				} else {
					this.bClockWise = false;
				}
				this.isRotating = true;
			} else {
				this.rotateAng = this.lastAng;
			}

			if (listen) {
				itemSelectedListener.onPieChartItemSelected(position,
						this.itemsColors[position], this.itemsSizes[position],
						this.itemsRate[position], isPositionFree(position),
						getAnimTime(Math.abs(this.lastAng - this.rotateAng)));
				// notifySelectedListeners(position, this.itemsColors[position],
				// this.itemsSizes[position], this.itemsRate[position],
				// isPositionFree(position),
				// getAnimTime(Math.abs(this.lastAng - this.rotateAng)));
			}
			this.rotateHandler.postDelayed(this, 1L);
		}
	}

	public int getShowItem() {
		return this.itemPostion;
	}

	public void setRotateWhere(int rotateWhere) {
		this.rotateWhere = rotateWhere;
	}

	public int getRotateWhere() {
		return this.rotateWhere;
	}

	@SuppressLint({ "DrawAllocation" })
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float bigRadius = this.radius + this.strokeWidth;
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		float lineLength = 2.0F * this.radius + this.strokeWidth;
		if (this.strokeWidth != 0.0F) {
			// 空心的画笔
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.parseColor(this.radiusBorderStrokeColor));
			paint.setStrokeWidth(this.strokeWidth);
			canvas.drawCircle(bigRadius, bigRadius, bigRadius - 5, paint);
		}

		if ((this.itemsAngle != null) && (this.itemsBeginAngle != null)) {

			canvas.rotate(this.rotateAng, bigRadius, bigRadius);

			RectF oval = new RectF(this.strokeWidth, this.strokeWidth,
					lineLength, lineLength);
			for (int i = 0; i < this.itemsAngle.length; i++) {
				if ((this.itemPostion == i) && (!this.isRotating))
					;
				switch (this.rotateWhere) {
				case 0:
					oval = new RectF(this.strokeWidth, this.strokeWidth,
							lineLength, lineLength);
					break;
				case 3:
					oval = new RectF(this.strokeWidth, this.strokeWidth,
							lineLength, lineLength);
					break;
				case 1:
					oval = new RectF(this.strokeWidth, this.strokeWidth,
							lineLength, lineLength);
					break;
				case 2:
					oval = new RectF(this.strokeWidth, this.strokeWidth,
							lineLength, lineLength);
					break;
				default:
					oval = new RectF(this.strokeWidth, this.strokeWidth,
							lineLength, lineLength);
					break;

				}
				// 填充的画笔
				paint.setStyle(Paint.Style.FILL);
				paint.setColor(Color.parseColor(this.itemsColors[i]));
				canvas.drawArc(oval, this.itemsBeginAngle[i],
						this.itemsAngle[i], true, paint);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(strokeWidth/2);
				paint.setColor(Color.WHITE);
				canvas.drawArc(oval, this.itemsBeginAngle[i],
						this.itemsAngle[i], true, paint);

			}
		}
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.LTGRAY);
		canvas.drawCircle(bigRadius, bigRadius,
				ScreenUtil.dip2px(getContext(), 40), paint);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(this.strokeWidth);
		canvas.drawCircle(bigRadius, bigRadius,
				ScreenUtil.dip2px(getContext(), 40), paint);

	}

	public boolean onTouchEvent(MotionEvent event) {
		if ((!this.isRotating) && (this.itemsSizes != null)
				&& (this.itemsSizes.length > 0)) {
			float x1 = 0.0F;
			float y1 = 0.0F;
			switch (event.getAction()) {
			case 0:
				x1 = event.getX();
				y1 = event.getY();
				float r = this.radius + this.strokeWidth;
				if ((x1 - r) * (x1 - r) + (y1 - r) * (y1 - r) - r * r <= 0.0F) {
					int position = getShowItem(getTouchedPointAngle(r, r, x1,
							y1));
					setShowItem(position, isAnimEnabled(), true);
				}
				break;
			case 2:
				break;
			case 1:
				break;
			}

		}

		return super.onTouchEvent(event);
	}

	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		this.rotateHandler.removeCallbacks(this);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		float widthHeight = 2.0F * (this.radius + this.strokeWidth + 1.0F);
		setMeasuredDimension((int) widthHeight, (int) widthHeight);
	}

	public void run() {
		if (this.bClockWise) {
			this.rotateAng += this.animSpeed;
			invalidate();
			this.rotateHandler.postDelayed(this, 10L);
			if (this.rotateAng - this.lastAng >= 0.0F) {
				this.rotateAng = 0.0F;
				this.rotateHandler.removeCallbacks(this);
				resetBeginAngle(this.lastAng);

				this.isRotating = false;
			}
		} else {
			this.rotateAng -= this.animSpeed;
			invalidate();
			this.rotateHandler.postDelayed(this, 10L);
			if (this.rotateAng - this.lastAng <= 0.0F) {
				this.rotateAng = 0.0F;
				this.rotateHandler.removeCallbacks(this);
				resetBeginAngle(this.lastAng);

				this.isRotating = false;
			}
		}
	}

	/**
	 * @Title: refreshItemsAngs
	 * @Description: 重设角度
	 * @throws
	 */
	private void refreshItemsAngs() {
		if ((this.itemSizesTemp != null) && (this.itemSizesTemp.length > 0)) {
			if (getTotal() > getAllSizes()) {
				this.itemsSizes = new Double[this.itemSizesTemp.length + 1];
				for (int m = 0; m < this.itemSizesTemp.length; m++) {
					this.itemsSizes[m] = this.itemSizesTemp[m];
				}
				this.itemsSizes[(this.itemsSizes.length - 1)] = (getTotal() - getAllSizes());
			} else {
				this.itemsSizes = new Double[this.itemSizesTemp.length];
				this.itemsSizes = this.itemSizesTemp;
			}

			this.itemsRate = new float[this.itemsSizes.length];
			this.itemsBeginAngle = new float[this.itemsSizes.length];
			this.itemsAngle = new float[this.itemsSizes.length];
			float beginAngle = 0.0F;

			for (int i = 0; i < this.itemsSizes.length; i++) {
				this.itemsRate[i] = ((float) (this.itemsSizes[i] * 1.0D
						/ getTotal() * 1.0D));
			}

			for (int i = 0; i < this.itemsRate.length; i++) {
				// if (i == 1)
				// beginAngle = 360.0F * this.itemsRate[(i - 1)];
				// else if (i > 1) {
				// beginAngle = 360.0F * this.itemsRate[(i - 1)] + beginAngle;
				// }
				// this.itemsBeginAngle[i] = beginAngle;
				this.itemsAngle[i] = (360.0F * this.itemsRate[i]);
				if (i != 0) {
					this.itemsBeginAngle[i] = beginAngle + itemsAngle[i - 1];
					beginAngle = 360.0F * this.itemsRate[(i - 1)] + beginAngle;
				} else {
					this.itemsBeginAngle[i] = -itemsAngle[i] / 2;
					beginAngle = this.itemsBeginAngle[i];
				}
			}
		}
	}

	private boolean isPositionFree(int position) {
		if ((position == this.itemsSizes.length - 1)
				&& (getTotal() > getAllSizes())) {
			return true;
		}
		return false;
	}

	private float getAnimTime(float ang) {
		return (int) Math.floor(ang / getAnimSpeed() * 10.0F);
	}

	private float getTouchedPointAngle(float radiusX, float radiusY, float x1,
			float y1) {
		float ax = x1 - radiusX;
		float ay = y1 - radiusY;

		// ay = -ay;
		double a = 0.0D;
		double t = ay / Math.sqrt(ax * ax + ay * ay);

		if (ax > 0.0F) {
			// 0~90
			if (ay > 0.0F)
				a = 6.283185307179586D - Math.asin(t);
			else
				// 270~360
				a = -Math.asin(t);
		} else if (ay > 0.0F)
			// 90~180
			a = 3.141592653589793D + Math.asin(t);
		else {
			// 180~270
			a = 3.141592653589793D + Math.asin(t);
		}
		return (float) (360.0D - a * 180.0D / 3.141592653589793D % 360.0D);
	}

	private int getShowItem(float touchAngle) {
		int position = 0;

		for (int i = 0; i < this.itemsBeginAngle.length; i++) {
			if (i != this.itemsBeginAngle.length - 1) {
				if ((touchAngle >= this.itemsBeginAngle[i])
						&& (touchAngle < this.itemsBeginAngle[(i + 1)])) {
					position = i;
					break;
				}

			} else if ((touchAngle > this.itemsBeginAngle[(this.itemsBeginAngle.length - 1)])
					&& (touchAngle < this.itemsBeginAngle[0])) {
				position = this.itemsSizes.length - 1;
			} else if ((isUpperSort(this.itemsBeginAngle))
					|| (isLowerSort(this.itemsBeginAngle))) {
				position = this.itemsSizes.length - 1;
			} else {
				position = getPointItem(this.itemsBeginAngle);
			}

		}

		return position;
	}

	private float getLastRotateAngle(int position) {
		float result = 0.0F;

		// result = this.itemsBeginAngle[position];

		result = this.itemsBeginAngle[position] + this.itemsAngle[position]
				/ 2.0F + getRotateWhereAngle();
		if (result >= 360.0F) {
			result -= 360.0F;
		}

		if (result <= 180.0F)
			result = -result;
		else {
			result = 360.0F - result;
		}

		return result;
	}

	private boolean isUpperSort(float[] all) {
		boolean result = true;
		float temp = all[0];
		for (int a = 0; a < all.length - 1; a++) {
			if (all[(a + 1)] - temp > 0.0F)
				temp = all[(a + 1)];
			else {
				return false;
			}
		}

		return result;
	}

	private boolean isLowerSort(float[] all) {
		boolean result = true;
		float temp = all[0];
		for (int a = 0; a < all.length - 1; a++) {
			if (all[(a + 1)] - temp < 0.0F)
				temp = all[(a + 1)];
			else {
				return false;
			}
		}

		return result;
	}

	private int getPointItem(float[] all) {
		int item = 0;

		float temp = all[0];
		for (int a = 0; a < all.length - 1; a++) {
			if (all[(a + 1)] - temp > 0.0F)
				temp = all[a];
			else {
				return a;
			}
		}

		return item;
	}

	private void resetBeginAngle(float angle) {
		for (int i = 0; i < this.itemsBeginAngle.length; i++) {
			float newBeginAngle = this.itemsBeginAngle[i] + angle;

			if (newBeginAngle < 0.0F)
				this.itemsBeginAngle[i] = (newBeginAngle + 360.0F);
			else if (newBeginAngle > 360.0F)
				this.itemsBeginAngle[i] = (newBeginAngle - 360.0F);
			else
				this.itemsBeginAngle[i] = newBeginAngle;
		}
	}

	private void setDefaultColor() {
		if ((this.itemsSizes != null) && (this.itemsSizes.length > 0)
				&& (this.itemsColors == null)) {
			this.itemsColors = new String[this.itemsSizes.length];
			if (this.itemsColors.length <= DEFAULT_ITEMS_COLORS.length) {
				System.arraycopy(DEFAULT_ITEMS_COLORS, 0, this.itemsColors, 0,
						this.itemsColors.length);
			} else {
				int multiple = this.itemsColors.length
						/ DEFAULT_ITEMS_COLORS.length;
				int left = this.itemsColors.length
						% DEFAULT_ITEMS_COLORS.length;

				for (int a = 0; a < multiple; a++) {
					System.arraycopy(DEFAULT_ITEMS_COLORS, 0, this.itemsColors,
							a * DEFAULT_ITEMS_COLORS.length,
							DEFAULT_ITEMS_COLORS.length);
				}
				if (left > 0)
					System.arraycopy(DEFAULT_ITEMS_COLORS, 0, this.itemsColors,
							multiple * DEFAULT_ITEMS_COLORS.length, left);
			}
		}
	}

	private void setLeftColor() {
		if ((this.itemsSizes != null)
				&& (this.itemsSizes.length > this.itemsColors.length)) {
			String[] preItemsColors = new String[this.itemsColors.length];
			preItemsColors = this.itemsColors;
			int leftall = this.itemsSizes.length - this.itemsColors.length;
			this.itemsColors = new String[this.itemsSizes.length];
			System.arraycopy(preItemsColors, 0, this.itemsColors, 0,
					preItemsColors.length);

			if (leftall <= DEFAULT_ITEMS_COLORS.length) {
				System.arraycopy(DEFAULT_ITEMS_COLORS, 0, this.itemsColors,
						preItemsColors.length, leftall);
			} else {
				int multiple = leftall / DEFAULT_ITEMS_COLORS.length;
				int left = leftall % DEFAULT_ITEMS_COLORS.length;
				for (int a = 0; a < multiple; a++) {
					System.arraycopy(DEFAULT_ITEMS_COLORS, 0, this.itemsColors,
							a * DEFAULT_ITEMS_COLORS.length,
							DEFAULT_ITEMS_COLORS.length);
				}
				if (left > 0) {
					System.arraycopy(DEFAULT_ITEMS_COLORS, 0, this.itemsColors,
							multiple * DEFAULT_ITEMS_COLORS.length, left);
				}
			}
			preItemsColors = null;
		}
	}

	private void reSetTotal() {
		double totalSizes = getAllSizes();
		if (getTotal() < totalSizes)
			this.total = totalSizes;
	}

	private double getAllSizes() {
		float tempAll = 0.0F;
		if ((this.itemSizesTemp != null) && (this.itemSizesTemp.length > 0)) {
			for (double itemsize : this.itemSizesTemp) {
				tempAll += itemsize;
			}
		}

		return tempAll;
	}

	private float getRotateWhereAngle() {
		float result = 0.0F;
		switch (this.rotateWhere) {
		case 0:
			result = 0.0F;
			break;
		case 2:
			result = 180.0F;
			break;
		case 3:
			result = 90.0F;
			break;
		case 1:
			result = 270.0F;
			break;
		}

		return result;
	}

	public OnPiegraphItemSelectedListener getItemSelectedListener() {
		return itemSelectedListener;
	}

	public void setItemSelectedListener(
			OnPiegraphItemSelectedListener itemSelectedListener) {
		this.itemSelectedListener = itemSelectedListener;
	}

	// protected void notifySelectedListeners(int position, String colorRgb,
	// double itemsSizes2, float rate, boolean isFreePart, float animTime) {
	// for (OnPiegraphItemSelectedListener listeners :
	// this.itemSelectedListeners)
	// listeners.onPieChartItemSelected(this, position, colorRgb,
	// itemsSizes2, rate, isFreePart, animTime);
	// }
}
