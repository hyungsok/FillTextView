package com.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hyungsoklee on 2015. 10. 29..
 */
public class CTextView extends TextView {
    // [final/static_property]====================[START]===================[final/static_property]
    private static final String LOG_TAG = CTextView.class.getSimpleName();
    private static final String NEW_LINE = "\n";
    private static final String BLANK = " ";
    private static String ELLIPSIS;
    // [final/static_property]=====================[END]====================[final/static_property]
    // [private/protected/public_property]========[START]=======[private/protected/public_property]
    private int mMaxLines; // Target Api(16) JELLY_BEAN
    // UnitText : "X 개" 관련 보장 텍스트
    private String mUnitText;
    private boolean mEnableUnitText = false;
    // [private/protected/public_property]=========[END]========[private/protected/public_property]
    // [interface/enum/inner_class]===============[START]==============[interface/enum/inner_class]
    // [interface/enum/inner_class]================[END]===============[interface/enum/inner_class]
    // [inherited/listener_method]================[START]===============[inherited/listener_method]
    public CTextView(Context context) {
        super(context);
        ELLIPSIS = context.getString(R.string.ellipsis);
    }

    public CTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ELLIPSIS = context.getString(R.string.ellipsis);
    }

    public CTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Target Api(16) JELLY_BEAN
        TypedArray a = context.obtainStyledAttributes(attrs,
                new int[]{ android.R.attr.maxLines }, defStyleAttr, 0);
        setMaxLines(a.getInt(0, Integer.MAX_VALUE));
        a.recycle();
        ELLIPSIS = context.getString(R.string.ellipsis);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(LOG_TAG, "onSizeChanged() " + w + ", " + h + ", " + oldw + ", " + oldh);
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            setBreakText(getText());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getEllipsize() == TextUtils.TruncateAt.START) {
            return;
        }

        // 커스텀뷰의 크기 설정
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;
        int height = 0;
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
                width = widthMeasureSpec;
                break;
            case MeasureSpec.AT_MOST:  // wrap_content
                width = MeasureSpec.getSize(widthMeasureSpec);
                break;
            case MeasureSpec.EXACTLY:  // match_parent
                width = MeasureSpec.getSize(widthMeasureSpec);
                break;
        }

        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
                height = heightMeasureSpec;
                if (height == 0) {
                    height = getBreakHeight(getText(), MeasureSpec.getSize(widthMeasureSpec), heightMeasureSpec);
                }
                break;
            case MeasureSpec.AT_MOST:  // wrap_content
                height = getBreakHeight(getText(), MeasureSpec.getSize(widthMeasureSpec), heightMeasureSpec);
                break;
            case MeasureSpec.EXACTLY:  // match_parent
                height = MeasureSpec.getSize(heightMeasureSpec);
                break;
        }

        Log.d(LOG_TAG, "onMeasure() " + width + ", " + height);
        setMeasuredDimension(width, height);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        Log.d(LOG_TAG, "setText(" + type + ") " + text);
        super.setText(breakText(getPaint(), text, getBreakWidth()), type);
    }


    // [inherited/listener_method]=================[END]================[inherited/listener_method]
    // [private_method]===========================[START]==========================[private_method]
    /**
     * 실제 TextView 넓이
     *
     * @return
     */
    private int getBreakWidth() {
        return getWidth() - this.getPaddingLeft() - this.getPaddingRight();
    }

    /**
     * 실제 TextView 높이
     *
     * @param charSequence
     * @param width
     * @param heightMeasureSpec
     * @return
     */
    private int getBreakHeight(CharSequence charSequence, int width, int heightMeasureSpec) {
        if (charSequence == null) {
            return MeasureSpec.getSize(heightMeasureSpec);
        }

        String texts = charSequence.toString();
        if (width == 0) {
            return MeasureSpec.getSize(heightMeasureSpec);
        }

        int endValue;
        int breakHeight = 0;
        int lineCount = 0;
        do {
            endValue = getPaint().breakText(texts, true, width, null);
            if (endValue > 0) {
                texts = texts.substring(endValue);
                if (lineCount >= getMaxLines()) {
                    break;
                }
                breakHeight += getLineHeight();
                lineCount ++;
            }
        } while (endValue > 0);

        breakHeight += getPaddingTop() + getPaddingBottom() + getPixelFromDip(getContext(), lineCount + 2);

        return breakHeight;
    }


    /**
     *
     * @param charSequence
     */
    private void setBreakText(CharSequence charSequence) {
        if (charSequence == null)
            return;

        String breakText = breakText(
                getPaint(),
                charSequence,
                getBreakWidth());

        if (!TextUtils.isEmpty(breakText) && !breakText.equals(getText())) {
            super.setText(breakText);
        }
    }

    /**
     * 줄바꿈
     *
     * @param textPaint  TextView의 Paint 객체
     * @param charSequence    문자열
     * @param breakWidth 줄바꿈 하고 싶은 width값 지정
     * @return
     */
    private String breakText(Paint textPaint, CharSequence charSequence, int breakWidth) {
        if (charSequence == null) {
            return null;
        }

        String texts = charSequence.toString();
        if (breakWidth == 0) {
            return texts;
        }

        if (getEllipsize() == TextUtils.TruncateAt.START) {
            return texts;
        }

        Log.d(LOG_TAG, "breakText width : " + breakWidth + " : " + charSequence);
        List<String> textList = new ArrayList<String>();
        int endValue;
        String tempText = texts.trim();
        do {
            // 입력한 텍스트를 지정한 길이에 맞게 계산하여 길이를 리턴
            endValue = textPaint.breakText(tempText, true, breakWidth, null);
            if (endValue > 0) {
                String text = tempText.substring(0, endValue).replace(NEW_LINE, "") + NEW_LINE;
                textList.add(text);
                tempText = tempText.substring(endValue);
                // 앞뒤 공백제거
                tempText = tempText.trim();
            }
        } while (endValue > 0);

        setMiddleText(textList, texts);
        setEndText(textList);

        StringBuilder sb = new StringBuilder();
        final int lineCount = textList.size();
        final int maxLineCount = getMaxLines();
        for (int i = 0; i < lineCount; i++) {
            if (i == maxLineCount)
                break;
            String text = textList.get(i);
            Log.d(LOG_TAG, "\t> " + text);
            sb.append(text);
        }

        // 마지막 "\n"를 제거
        if (sb.toString().length() > 0) {
            String string = sb.toString().substring(0, sb.length() - 1);
            return string;
        } else {
            return sb.toString();
        }
    }

    /**
     * TextView Middle 텍스트 처리
     *
     * @param textList
     */
    private void setMiddleText(List<String> textList, String texts) {
        int maxLine = getMaxLines();
        if (getEllipsize() == TextUtils.TruncateAt.MIDDLE) {
            if (textList.size() > maxLine && maxLine > 0) {
                if (TextUtils.isEmpty(textList.get(maxLine - 1))) {
                    return;
                }
                StringBuilder sb = new StringBuilder();
                String endString = textList.get(maxLine - 1).replace(NEW_LINE, "");
                if (mEnableUnitText) {
                    if (TextUtils.isEmpty(mUnitText)) {
                        // 끝에 ...표시
                        sb.append(endString.substring(0, endString.length() - ELLIPSIS.length() - 1)).append(ELLIPSIS);
                    } else {
                        // UnitText 에 ...표시
                        int startIndex = endString.length() - (mUnitText.length() + ELLIPSIS.length());
                        if (startIndex < 0) {
                            sb.append(ELLIPSIS);
                            // -1 각각의 단어마다의 여유 스페이스
                            sb.append(mUnitText.substring(ELLIPSIS.length() - 1));
                        } else {
                            // -2 각각의 단어마다의 여유 스페이스
                            sb.append(endString.substring(0, startIndex - 2));
                            sb.append(ELLIPSIS);
                            sb.append(mUnitText);
                        }
                    }
                } else {
                    // MIDDLE이면 중간에 ...표시 해주고
                    int halfIndex = endString.length() / 2;
                    sb.append(endString.substring(0, halfIndex - ELLIPSIS.length() - 2)).append(ELLIPSIS); // -2는 space 여유
                    // 맨마지막 텍스트를 짤라서 붙여줌
                    sb.append(texts.substring((texts.length() - halfIndex)));
                }
                sb.append(NEW_LINE);
                // Set Middle
                textList.set(maxLine - 1, sb.toString());
            }
        }
    }

    /**
     * TextView End 텍스트 처리
     *
     * @param textList
     */
    private void setEndText(List<String> textList) {
        int maxLine = getMaxLines();
        if (getEllipsize() == TextUtils.TruncateAt.END) {
            if (textList.size() > maxLine && maxLine > 0) {
                if (TextUtils.isEmpty(textList.get(maxLine - 1))) {
                    return;
                }
                StringBuilder sb = new StringBuilder();
                String endString = textList.get(maxLine - 1).replace(NEW_LINE, "");
                // 끝에 ...표시
                sb.append(endString.substring(0, endString.length() - ELLIPSIS.length() - 1)).append(ELLIPSIS);
                sb.append(NEW_LINE);
                // Set Middle
                textList.set(maxLine - 1, sb.toString());
            }
        }
    }

    /**
     * 현재 디스플레이 화면에 비례한 DP단위를 픽셀 크기로 반환합니다.
     *
     * @param context
     * @param dip 픽셀
     * @return 변환된 값 (pixel)
     */
    public int getPixelFromDip(Context context, float dip) {
        if (context == null) {
            return 0;
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, dm);
    }

    // Target Api(16) JELLY_BEAN
    @SuppressLint("Override")
    public int getMaxLines() {
        return mMaxLines;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        mMaxLines = maxLines;
    }

    /**
     *
     * @param unitText
     */
    public void setUnitText(String unitText) {
        this.mUnitText = unitText;
        setEnableUnitText(!TextUtils.isEmpty(unitText));
    }

    /**
     *
     * @param enableUnitText
     */
    public void setEnableUnitText(boolean enableUnitText) {
        this.mEnableUnitText = enableUnitText;
    }
    // [private_method]============================[END]===========================[private_method]
    // [life_cycle_method]========================[START]=======================[life_cycle_method]
    // [life_cycle_method]=========================[END]========================[life_cycle_method]
    // [public_method]============================[START]===========================[public_method]
    // [public_method]=============================[END]============================[public_method]
    // [get/set]==================================[START]=================================[get/set]
    // [get/set]===================================[END]==================================[get/set]
}
