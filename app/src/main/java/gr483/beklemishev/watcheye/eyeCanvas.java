package gr483.beklemishev.watcheye;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class eyeCanvas extends SurfaceView {

    Paint p;

    public eyeCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);

       setWillNotDraw(false);
       p = new Paint();
       p.setColor((Color.RED));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.RED);




    }
}
