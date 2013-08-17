package views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Alexandre
 * 
 * ViewPager to avoid having tabs with swipe gesture
 *
 */
public class CustomViewPager extends ViewPager {

	public CustomViewPager(Context context) {
		super(context);
	}
	
	public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

	   @Override
	    public boolean onInterceptTouchEvent(MotionEvent ev){
	        return false;
	    }

	    @Override
	    public boolean onTouchEvent(MotionEvent ev){
	        return false;
	    }
}
