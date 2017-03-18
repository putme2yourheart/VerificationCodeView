package io.github.putme2yourheart.verificationcodeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

/**
 * Created by Frank on 2017/3/17.
 * 生成不同难度的验证码控件
 */

public class VerificationCodeView extends View {

    private static final int easy = 0;
    private static final int mid = 1;
    private static final int diff = 2;

    // 验证码
    private String mCodeText;
    // 文本大小
    private int mCodeTextSize;
    // 背景颜色
    private int mCodeBackground;
    // 验证码长度
    private int mCodeLength;
    // 识别难度
    private int mVerifyDegree;

    // 画笔
    private Paint mPaint;
    // 随机数
    private static Random random = new Random();

    // 控件宽度
    private static int mWidth;
    // 控件高度
    private static int mHeight;

    public VerificationCodeView(Context context) {
        super(context);
    }

    public VerificationCodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initAttrValue(context, attrs);
        initData();
    }

    private void initAttrValue(Context context, AttributeSet attrs) {
        // 获取在values/attrs中定义VerificationCodeView的属性集合
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeView, 0, R.style.VerificationCodeView);

        int count = typedArray.getIndexCount();

        for (int i = 0; i < count; i++) {
            int index = typedArray.getIndex(i);

            switch (index) {
                case R.styleable.VerificationCodeView_codeTextSize:
                    // 默认设置为16sp，TypeValue类将px转成sp
                    mCodeTextSize = typedArray.getDimensionPixelSize(index, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 32.0f, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.VerificationCodeView_codeBackground:
                    mCodeBackground = typedArray.getColor(index, Color.BLACK);
                    break;
                case R.styleable.VerificationCodeView_codeLength:
                    mCodeLength = typedArray.getInteger(index, 4);
                    break;
                case R.styleable.VerificationCodeView_verifyDegree:
                    mVerifyDegree = typedArray.getInteger(index, mid);
                    break;
            }
        }

        //Recycles the TypedArray, to be re-used by a later caller
        typedArray.recycle();
    }

    private void initData() {
        // 获取验证码
        mCodeText = getCodeText();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);      // 取出宽度的确切数值
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);      // 取出宽度的测量模式

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);    // 取出高度的确切数值
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);    // 取出高度的测量模式

        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else {
            mPaint.setTextSize(mCodeTextSize);
            mWidth = Math.round(mPaint.measureText(mCodeText)) + Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, 16.0f, getResources().getDisplayMetrics()) * 2);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else {
            mPaint.setTextSize(mCodeTextSize);
            Paint.FontMetricsInt fm = mPaint.getFontMetricsInt();
            mHeight = fm.descent - fm.ascent + getPaddingTop() + getPaddingBottom();
        }

        // 设置宽高
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(createBitmap(), 0, 0, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                refresh();
                break;
        }
        return super.onTouchEvent(event);
    }

    // 获取验证码图片
    private Bitmap createBitmap() {

        Bitmap b = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);  //设置画笔模式为填充
        mPaint.setStrokeWidth(1f);
        mPaint.setTextSize(mCodeTextSize);

        Canvas canvas = new Canvas(b);
        // 背景色
        canvas.drawColor(mCodeBackground);

        Paint.FontMetricsInt fm = mPaint.getFontMetricsInt();
        // 绘制文字的高度
        int height = mHeight / 2 - fm.descent + (fm.bottom - fm.top) / 2;

        // 初始化水平居中的起始位置，累加每个字符的宽度
        int length = mWidth / 2 - Math.round(mPaint.measureText(mCodeText) / 2);

        int nBessell = 0;
        int nLine = 0;
        int nPoint = 0;

        switch (mVerifyDegree) {
            case easy:
                nLine = 1;
                nPoint = 30;
                break;
            case mid:
                nBessell = 1;
                nLine = 2;
                nPoint = 50;
                break;
            case diff:
                nBessell = 2;
                nLine = 3;
                nPoint = 80;
                break;
        }

        for (int i = 0; i < mCodeLength; i++) {

            canvas.save();

            int r = random.nextInt(2);
            if (r % 2 == 0) {
                canvas.rotate(-15, length, Math.abs(fm.ascent));
            } else {
                canvas.rotate(15, length, Math.abs(fm.ascent));
            }

            canvas.skew(0.5f, 0);

            canvas.drawText(String.valueOf(mCodeText.charAt(i)),
                    length, height, mPaint);

            // 累计下一个字的起始位置
            length += Math.round(mPaint.measureText(String.valueOf(mCodeText.charAt(i))));

            canvas.restore();
        }

        drawPoint(canvas, mPaint, nPoint);

        drawLine(canvas, mPaint, nLine);

        drawBessell(canvas, mPaint, nBessell);

        return b;
    }

    // 画二阶贝塞尔曲线
    private void drawBessell(Canvas canvas, Paint paint, int count) {
        for (int i = 0; i < count; i++) {
            drawBessell(canvas, paint);
        }
    }

    // 画二阶贝塞尔曲线
    private void drawBessell(Canvas canvas, Paint paint) {

        paint.setStrokeWidth(2f);
        paint.setStyle(Paint.Style.STROKE);

        int startX = random.nextInt(mWidth);
        int startY = random.nextInt(mHeight);

        int controlX = random.nextInt(mWidth);
        int controlY = random.nextInt(mHeight);

        int endX = random.nextInt(mWidth);
        int endY = random.nextInt(mHeight);

        Path path = new Path();
        path.moveTo(startX, startY);
        path.quadTo(controlX, controlY, endX, endY);

        canvas.drawPath(path, paint);
    }

    // 画直线
    private void drawLine(Canvas canvas, Paint paint, int count) {
        for (int i = 0; i < count; i++) {
            drawLine(canvas, paint);
        }
    }

    // 画直线
    private void drawLine(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(2f);
        paint.setStyle(Paint.Style.STROKE);

        int startX = random.nextInt(mWidth);
        int startY = random.nextInt(mHeight);

        int endX = random.nextInt(mWidth);
        int endY = random.nextInt(mHeight);

        canvas.drawLine(startX, startY, endX, endY, paint);
    }

    // 画干扰点
    private void drawPoint(Canvas canvas, Paint paint, int count) {
        for (int i = 0; i < count; i++) {
            drawPoint(canvas, paint);
        }
    }

    // 画干扰点
    private void drawPoint(Canvas canvas, Paint paint) {

        paint.setStrokeWidth(2f);
        paint.setStyle(Paint.Style.FILL);

        int x = random.nextInt(mWidth);
        int y = random.nextInt(mHeight);

        canvas.drawPoint(x, y, paint);
    }

    public void setCodeText(String codeText) {
        mCodeText = codeText;
    }

    public int getCodeBackground() {
        return mCodeBackground;
    }

    public void setCodeBackground(int codeBackground) {
        mCodeBackground = codeBackground;
    }

    public int getCodeLength() {
        return mCodeLength;
    }

    public void setCodeLength(int codeLength) {
        mCodeLength = codeLength;
    }

    public int getVerifyDegree() {
        return mVerifyDegree;
    }

    public void setVerifyDegree(int verifyDegree) {
        mVerifyDegree = verifyDegree;
    }

    /**
     * 生成验证码
     *
     * @return String
     */
    private String getCodeText() {
        String s = "";
        Random random = new Random();

        for (int i = 0; i < mCodeLength; i++) {
            s += random.nextInt(2) % 2 == 0                     // 随机生成数字或者字母
                    ? String.valueOf(random.nextInt(10))        // 生成数字
                    : random.nextInt(2) % 2 == 0                // 随机生成字母
                    ? (char) (random.nextInt(26) + 65) : (char) (random.nextInt(26) + 97);        // 生成大小写字母
        }
        return s;
    }

    /**
     * 检测验证码是否一致，忽略大小写
     *
     * @param code 传入的验证码
     * @return boolean
     */
    public boolean isEqualIgnoreCase(String code) {
        return mCodeText.equalsIgnoreCase(code);
    }

    /**
     * 检测验证码是否一致，不忽略大小写
     *
     * @param code 传入的验证码
     * @return boolean
     */
    public boolean isEqual(String code) {
        return mCodeText.equals(code);
    }

    public void refresh() {

        mCodeText = getCodeText();

        invalidate();
    }

}
