package com.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
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
    // [final/static_property]=====================[END]====================[final/static_property]
    // [private/protected/public_property]========[START]=======[private/protected/public_property]
    private int mMaxLines;
    // [private/protected/public_property]=========[END]========[private/protected/public_property]
    // [interface/enum/inner_class]===============[START]==============[interface/enum/inner_class]
    // [interface/enum/inner_class]================[END]===============[interface/enum/inner_class]
    // [inherited/listener_method]================[START]===============[inherited/listener_method]
    public CTextView(Context context) {
        super(context);
    }

    public CTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Target Api(16) JELLY_BEAN
        TypedArray a = context.obtainStyledAttributes(attrs,
                new int[]{ android.R.attr.maxLines }, defStyleAttr, 0);
        setMaxLines(a.getInt(0, Integer.MAX_VALUE));
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            setBreakText(getText());
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(breakText(getPaint(), text, getBreakWidth()), type);
    }

    // [inherited/listener_method]=================[END]================[inherited/listener_method]
    // [private_method]===========================[START]==========================[private_method]
    /**
     *
     * @return
     */
    private int getBreakWidth() {
        return getWidth() - this.getPaddingLeft() - this.getPaddingRight();
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

        if (breakText.equals(getText()) == false) {
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
        Log.d(LOG_TAG, "breakText width : " + breakWidth + " : " + charSequence);
        if (charSequence == null) {
            return null;
        }

        String texts = charSequence.toString();
        if (breakWidth == 0) {
            return texts;
        }

        if (getEllipsize() == TextUtils.TruncateAt.START) {
            // 해당 속성은 지원 안함
            return texts;
        }

        List<String> textList = new ArrayList<String>();
        int endValue;
        String tempText = texts;
        do {
            // 입력한 텍스트를 지정한 길이에 맞게 계산하여 길이를 리턴
            endValue = textPaint.breakText(tempText, true, breakWidth, null);
            Log.d(LOG_TAG, "\t> endValue : " + endValue);
            if (endValue > 0) {
                String text = tempText.substring(0, endValue).replace(NEW_LINE, "") + NEW_LINE;
                textList.add(text);
                tempText = tempText.substring(endValue);
            }
        } while (endValue > 0);

        setMiddleText(textList, texts, textPaint, breakWidth);

        StringBuilder sb = new StringBuilder();
        for(String text : textList) {
            Log.d(LOG_TAG, "\t> text : " + text);
            if (BLANK.equals(text.substring(0, 1))) {
                text = text.substring(1);
            }
            sb.append(text);
        }

        // 마지막 "\n"를 제거
        String string = sb.toString().substring(0, sb.length() - 1);
        Log.d(LOG_TAG, "result : " + string);
        return string;
    }

    /**
     * TextView Middle 텍스트 처리
     * @param textList
     * @param texts
     * @param textPaint
     * @param breakWidth
     */
    private void setMiddleText(List<String> textList, String texts, Paint textPaint, int breakWidth) {
        int maxLine = getMaxLines();
        Log.d(LOG_TAG, "setMiddleText() MaxLine : " + maxLine);
        if (getEllipsize() == TextUtils.TruncateAt.MIDDLE) {
            if (textList.size() > maxLine && maxLine > 0) {
                // 마지막 줄에서의 어디까지 글자를 쓸수있는지 확인
                String endString = textList.get(maxLine - 1);
                int endValue = textPaint.breakText(endString, true, breakWidth, null);
                Log.d(LOG_TAG, "\t> endValue[middle] : " + endValue);

                StringBuilder sb = new StringBuilder();
                // MIDDLE이면 중간에 ...표시 해주고
                int halfValue = endValue / 2;
                sb.append(endString.substring(0, halfValue - 2)).append("...");
                // 맨마지막 텍스트를 짤라서 붙여줌
                sb.append(texts.substring((texts.length() + 2 - halfValue)).replace(NEW_LINE, ""));
                sb.append(NEW_LINE);
                Log.d(LOG_TAG, "\t- middle : " + sb.toString());

                // Set Middle
                textList.set(maxLine - 1, sb.toString());
            }
        }
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
    // [private_method]============================[END]===========================[private_method]
    // [life_cycle_method]========================[START]=======================[life_cycle_method]
    // [life_cycle_method]=========================[END]========================[life_cycle_method]
    // [public_method]============================[START]===========================[public_method]
    // [public_method]=============================[END]============================[public_method]
    // [get/set]==================================[START]=================================[get/set]
    // [get/set]===================================[END]==================================[get/set]
}
