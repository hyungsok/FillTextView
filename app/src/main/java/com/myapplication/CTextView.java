package com.myapplication;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by hyungsoklee on 2015. 10. 29..
 */
public class CTextView extends TextView {
    private static final String LOG_TAG = CTextView.class.getSimpleName();

    public CTextView(Context context) {
        super(context);
    }

    public CTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            setBreakText(getText());
        }
    }

    /**
     *
     * @param text
     */
    private void setBreakText(CharSequence text) {
        if (text == null)
            return;

        String breakText = breakText(
                getPaint(),
                text.toString(),
                getWidth() - this.getPaddingLeft() - this.getPaddingRight());

        if (breakText.equals(getText()) == false) {
            setText(breakText);
        }
    }

    /**
     * 줄바꿈
     *
     * @param textPaint  TextView의 Paint 객체
     * @param strText    문자열
     * @param breakWidth 줄바꿈 하고 싶은 width값 지정
     * @return
     */
    private String breakText(Paint textPaint, String strText, int breakWidth) {
        Log.d(LOG_TAG, "breakText width : " + breakWidth);
        if (breakWidth == 0) {
            return strText;
        }
        Log.d(LOG_TAG, "\t-Ellipsize : " + getEllipsize());
        Log.d(LOG_TAG, "\t-LineCount : " + getLineCount());

        if (getEllipsize() == TextUtils.TruncateAt.START) {
            // 해당 속성은 지원 안함
            return strText;
        }

        StringBuilder sb = new StringBuilder();
        int endValue;

        int line = 1;
        String tempStrText = strText;
        do {
            // 입력한 텍스트를 지정한 길이에 맞게 계산하여 길이를 리턴
            endValue = textPaint.breakText(tempStrText, true, breakWidth, null);
            if (endValue > 0) {
                if (getEllipsize() == TextUtils.TruncateAt.END &&
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ) {
                    // 진져브래드 버전에서이고 TextUtils.TruncateAt.END 속성이면 뒤에 ... 붙여주기
                    if (line == getLineCount()) {
                        // 맨마지막 줄
                        sb.append(tempStrText.substring(0, endValue - 2)).append("...");
                        break;
                    } else {
                        sb.append(tempStrText.substring(0, endValue)).append("\n");
                    }
                } else {
                    if (getEllipsize() == TextUtils.TruncateAt.MIDDLE &&
                            line == getLineCount()) {
                        // MIDDLE이면 중간에 ...표시 해주고
                        int halfValue = endValue / 2;
                        sb.append(tempStrText.substring(0, halfValue - 1)).append("...");
                        // 맨마지막 텍스트를 짤라서 붙여줌
                        sb.append(strText.substring((strText.length() - halfValue), strText.length())).append("\n");
                    } else {
                        sb.append(tempStrText.substring(0, endValue)).append("\n");
                    }
                }
                tempStrText = tempStrText.substring(endValue);
            }
            line++;
        } while (endValue > 0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && getEllipsize() == TextUtils.TruncateAt.END) {
            // 줄바꿈기호가 없기 때문에 그대로 텍스트 올려주기
            return sb.toString();
        }

        // 마지막 "\n"를 제거
        String text = sb.toString().substring(0, sb.length() - 1);
        return text;
    }
}
