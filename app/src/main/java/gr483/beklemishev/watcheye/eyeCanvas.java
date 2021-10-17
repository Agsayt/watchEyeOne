package gr483.beklemishev.watcheye;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class eyeCanvas extends SurfaceView {

    int width = 0;
    int height = 0;

    int[] image = null;

    Paint p;

    public eyeCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);

       p = new Paint();
       p.setStyle(Paint.Style.FILL);
       setWillNotDraw(false);
    }


    void update(int w, int h, byte[] arr, int ofs)
    {
        int n = w * h;

        if (w != width || h != height){
            width = w;
            height = h;

            image = new int[n];
        }

        for (int i = 0; i < n; i++){
            image[i] = (arr[i + ofs] & 0x3f) * 4;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        if (image == null) return;

        float sx = canvas.getWidth() / (float) width;
        float sy = canvas.getHeight() / (float) height;

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int v = image[x + (height - 1 - y) * width];
                p.setColor(Color.rgb(v,v,v));

                float x0 = x * sx;
                float y0 = y * sy;
                float x1 = x0 + sx;
                float y1 = y0 * sy;

                canvas.drawRect(x0,y0,x1,y1, p);
            }
        }

    }
}
