package nchc.measurePhone;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class TouchView extends ImageView {

    static final int NONE = 0;
    static final int DRAG = 1; //��ʤ�
    static final int ZOOM = 2; //�Y��
    static final int BIGGER = 3; //��jing
    static final int SMALLER = 4; //�Y�ping
    private int mode = NONE; //��e���ƥ�
   
    private float beforeLenght; //��Ĳ�I�Z��
    private float afterLenght; //��Ĳ�I�Z��
    private float scale = 0.04f; //�Y�񪺤��XY��V���O�o�ӭȶV�j�Y�񪺶V��
    
    private int screenW;
    private int screenH;
    public int mImageWidth = 0;
    public int mImageHeight = 0;

    /*�B�z��� �ܶq */
    private int start_x;
    private int start_y;
    private int stop_x ;
    private int stop_y ;
    
    Vibrator vibrator;
    public float crackstart_x = 0;
    public float crackstart_y = 0;
    public float crackmiddle_x = 0;
    public float crackmiddle_y = 0;
    public float crackend_x = 0;
    public float crackend_y = 0;
	public float mLastMotionX = 0f;
	public float mLastMotionY = 0f;
    
    /*����
	private boolean mIsLongPressed = false;
	private float mLastMotionX = 0f;
	private float mLastMotionY = 0f;
	private long lastDownTime = 0;
	private long eventTime = 0;*/
    boolean isLongClickModule = false;
    boolean isTouchOne = false;
	boolean isStartPoint = false;
    int nWhichPoint = 0;
    
    //���_�����G
	public PointF mInterpPTLeft = new PointF(0.0f,0.0f);
	public PointF mInterpPTRight = new PointF(0.0f,0.0f);
	ArrayList <PointF> vecSemiAutoBounUpPoint;
	ArrayList <PointF> vecSemiAutoBounDownPoint;
	ArrayList <PointF> vecSemiAutoNodePoint;
	ArrayList <Double> vecSemiAutoNodeWidth;
    List<MatOfPoint> vecContoursList = null;
    ArrayList <RotatedRect> vecRotatedRectContours = null;
	
	//���I�������I����
	ArrayList <PointF> vecLaserPointList;
    boolean isLaserPointMode = false;
    int nLaserPointIndex = 0;
    boolean isDLTMode = false;
    boolean isGridMode = false;
    float fScale = 1;
    
	//��L����
	public TextView mPosText;
    Timer timer;
    private Paint paint;//�n���e��
    private TranslateAnimation trans; //�B�z�W�X��ɪ��ʵe
    Matrix mNewTrx = new Matrix();
    
    /**
     * �q�{�c�y���
     * @param context
     */
	public TouchView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
    /**
     * �Ӻc�y��k�b�R�A�ޤJXML��󤤬O������
     * @param context
     * @param paramAttributeSet
     */
    public TouchView(Context context,AttributeSet paramAttributeSet){
        super(context, paramAttributeSet);
        /*super.setLongClickable(true);
        super.setClickable(true);
        super.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				vibrator.vibrate(1000);
				return false;
			}});*/
    }
    public void myInvalidate(){
    	this.invalidate();
    }
    @Override
	protected boolean setFrame(int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		return super.setFrame(l, t, r, b);
	}
	/**
     * �Ӻc�y��Ʀb�ʺA�ЫخɡA���w�Ϥ�����l���e
     * @param context
     * @param w
     * @param h
     */
    public TouchView(Context context,int w,int h) {
        super(context);
        
        this.setPadding(0, 0, 0, 0);
        screenW = w;
        screenH = h;
    }
    /**
     * �N����I�����Z��
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
    
    /**
     * �B�zĲ�I..
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
   
    	switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
        	
                mode = DRAG;

                stop_x = (int) event.getRawX();
                stop_y = (int) event.getRawY();
                start_x = (int) event.getX();
                //start_y = (int) event.getY();
                start_y = stop_y - this.getTop();
                if(event.getPointerCount()==2)
                    beforeLenght = spacing(event);
               
                mNewTrx.set(this.getImageMatrix());
                mNewTrx.invert(mNewTrx);
                
	            mLastMotionX = event.getX();
	            mLastMotionY = event.getY();
	            
                /*����
	            mLastMotionX = event.getX();
	            mLastMotionY = event.getY();
	            lastDownTime = event.getEventTime() ;*/
                timer = new Timer();
                isLongClickModule = true;
                
                timer.schedule(new TimerTask(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
			            if (isLongClickModule){
			            	//�����Ҧ��U�����Ʊ�
			            	vibrator.vibrate(100);
			            	
	            			float pts [] = {mLastMotionX, mLastMotionY};       			
	            			mNewTrx.mapPoints(pts);
	            			
			            	if (isLaserPointMode)
			            	{	            			
		            			vecLaserPointList.set(nLaserPointIndex, new PointF(pts[0],pts[1]));
			            	}
			            	else
			            	{

                                switch (nWhichPoint) {
                                case 0:
                                    crackstart_x = (int)pts[0];
                                    crackstart_y = (int)pts[1];
                                    nWhichPoint++;
									break;
                                case 1:
                                    crackmiddle_x = (int)pts[0];
                                    crackmiddle_y = (int)pts[1];
                                    nWhichPoint++;
									break;
                                case 2:
                                    crackend_x = (int)pts[0];
                                    crackend_y = (int)pts[1];
                                    nWhichPoint = 0;
									break;
                                }

								/*
                                if (isStartPoint){
			            			crackstart_x = (int)pts[0];
			            			crackstart_y = (int)pts[1];

			            			isStartPoint = false;
			            		}
			            		else{
			            			crackend_x = (int)pts[0];
			            			crackend_y = (int)pts[1];

			            			isStartPoint = true;
			            		}*/
			            	}
			            	postInvalidate();
			            	isTouchOne = true;
			            }
					}}, 1000);
    			float pts [] = {event.getX(), event.getY()};
    			mNewTrx.mapPoints(pts);
    		
    			int pixel = 0;
    	        int redValue = 0;
    	        int blueValue = 0;
    	        int greenValue = 0;
    			Bitmap bitmap = ((BitmapDrawable)this.getDrawable()).getBitmap();
    			if (0<=pts[0]&&pts[0]<bitmap.getWidth()&&0<=pts[1]&&pts[1]<bitmap.getHeight())
    			{
    				pixel = bitmap.getPixel((int)pts[0],(int)pts[1]);
    				redValue = Color.red(pixel);
    				blueValue = Color.blue(pixel);
    				greenValue = Color.green(pixel);
    			}

    			String str = "(X,Y)=("+(int)pts[0] + ","+ (int)pts[1] + "),RGB=(" + redValue + ","+ blueValue +","+ greenValue + ")";
                //String str = (int)event.getX() + ","+ (int)event.getY();
                mPosText.setText(str);

                invalidate();
                break;
        case MotionEvent.ACTION_POINTER_DOWN:
                if (spacing(event) > 10f) {
                    mode = ZOOM;
                    beforeLenght = spacing(event);
                }
                break;
        case MotionEvent.ACTION_UP:
            /*�P�_�O�_�W�X�d�� �óB�z*/
        	/*
                int disX = 0;
                int disY = 0;
                if(getHeight()<=screenH || this.getTop()<0)
                {
                    if(this.getTop()<0 )
                    {
                        int dis = getTop();
                        this.layout(this.getLeft(), 0, this.getRight(), 0 + this.getHeight());
                        disY = dis - getTop();
                    }
                    else if(this.getBottom()>screenH)
                    {
                        disY = getHeight()- screenH+getTop();
                        this.layout(this.getLeft(), screenH-getHeight(), this.getRight(), screenH);
                    }
                }
                if(getWidth()<=screenW)
                {
                    if(this.getLeft()<0)
                    {
                        disX = getLeft();
                        this.layout(0, this.getTop(), 0+getWidth(), this.getBottom());
                    }
                    else if(this.getRight()>screenW)
                    {
                        disX = getWidth()-screenW+getLeft();
                        this.layout(screenW-getWidth(), this.getTop(), screenW, this.getBottom());
                    }
                }
                if(disX!=0 || disY!=0)
                {
                    trans = new TranslateAnimation(disX, 0, disY, 0);
                    trans.setDuration(500);
                    this.startAnimation(trans);
                }
                mode = NONE;*/
             	/*����
        	    mIsLongPressed = false;*/
        	    isLongClickModule = false;
                break;
        case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        case MotionEvent.ACTION_MOVE:
        	    /*����
                float x = event.getX();
                float y = event.getY();
        	    eventTime = event.getEventTime();
        	    if (!mIsLongPressed){
        	         mIsLongPressed = isLongPressed(mLastMotionX, mLastMotionY, x, y, lastDownTime,eventTime,500);
              	}
        	    if (mIsLongPressed){
	        	    //�����Ҧ��U�����Ʊ�
	        	    vibrator.vibrate(1000);
        	    }
        	    else
        	    {}*/
        	    //double deltaX = Math.sqrt((event.getX() - start_x) * (event.getX() - start_x) + (event.getY() - start_y) * (event.getY() - start_y));
        	double deltaX = Math.sqrt((event.getX() - mLastMotionX) * (event.getX() - mLastMotionX) + (event.getY() - mLastMotionY) * (event.getY() - mLastMotionY));
        	    if (deltaX > 20 && timer != null) { // ���ʤj��5��pixel
            		timer.cancel();
            		timer = null;
            		isLongClickModule = false;
            	}
   	
                /*�B�z���*/
                if (mode == DRAG) {
                    if (Math.abs(stop_x-start_x-getLeft())<200 && Math.abs(stop_y - start_y-getTop())<200)//88,85
                    {
                        //this.setPosition(stop_x - start_x, stop_y - start_y, stop_x + this.getWidth() - start_x, stop_y - start_y + this.getHeight());
                        Matrix mtrx = this.getImageMatrix();
                        mtrx.postTranslate((int) event.getRawX()- stop_x , (int) event.getRawY() - stop_y);
                    	//mNewTrx.set(mtrx);
                    	
                        stop_x = (int) event.getRawX();
                        stop_y = (int) event.getRawY();
                    }
                }
                /*�B�z�Y��*/
                else if (mode == ZOOM) {
                    if (spacing(event)>10f)
                    {
                        afterLenght = spacing(event);
                        float gapLenght = afterLenght - beforeLenght;
                        if(gapLenght == 0) {
                           break;
                        }
                        else if(Math.abs(gapLenght)>5f)
                        {
                            if(gapLenght>0) {  
                                this.setScale(scale,BIGGER);
 
                            }else {
                                this.setScale(scale,SMALLER);
                                
                            }
                            beforeLenght = afterLenght;
                        }
                    }
                }     
                /*
                String str2 = (int)event.getX() + ","+ (int)event.getX();
                mPosText.setText(str2);*/
                postInvalidate();
                break;
        }
        return true;
    }
    /**
     * ��{�A�X�����j�p,�w�ﰪ�Y�i
     */
    public void setScaleFitWin() {
        //�Y�p�v�����
        float yScale;
        float screenwidth = (float)this.getWidth();
        float screenheight = (float)this.getHeight();
        yScale = screenheight/(float)mImageHeight;

        //���Y�p�v��
        Matrix mtrx = this.getImageMatrix();
        mtrx.postScale(yScale, yScale, 0, 0);

        //�����v��
        float xShift = (screenwidth - (float)mImageWidth*yScale)/2;
        float yShift = (screenheight - (float)mImageHeight*yScale)/2;
        float []pts = new float[]{0,0};
        float []dst = new float[]{0,0};
        mtrx.mapPoints(dst, pts);//�v���y�д��⦨�ù��y��
        mtrx.postTranslate(xShift - dst[0], yShift - dst[1]);
    }
    /**
     * ��{�B�z�Y��
     */
    private void setScale(float temp,int flag) {
        /*    
        if (flag==BIGGER) {
            this.setFrame(this.getLeft()-(int)(temp*this.getWidth()),
                          this.getTop()-(int)(temp*this.getHeight()),
                          this.getRight()+(int)(temp*this.getWidth()),
                          this.getBottom()+(int)(temp*this.getHeight()));
        	
            scaler = scaler + 2*scale;
        }else if (flag==SMALLER){
            this.setFrame(this.getLeft()+(int)(temp*this.getWidth()),
                          this.getTop()+(int)(temp*this.getHeight()),
                          this.getRight()-(int)(temp*this.getWidth()),
                          this.getBottom()-(int)(temp*this.getHeight()));
            scaler = scaler - 2*scale; 
        }*/
    	Matrix mtrx = this.getImageMatrix(); 
    	if (flag==BIGGER) {
        	mtrx.postScale(1.05f,1.05f,stop_x,stop_y);
        	//mNewTrx.set(mtrx);
    	}else if (flag==SMALLER){
        	mtrx.postScale(0.95f,0.95f,stop_x,stop_y);	
        	//mNewTrx.set(mtrx);
    	}

    }
       
    /**
     * ��{�B�z���
     */
    private void setPosition(int left,int top,int right,int bottom) {
        this.layout(left,top,right,bottom);
    }
    /**
     *�������禡
     */
    private boolean isLongPressed(float lastX, float lastY, float thisX,float thisY, long lastDownTime,long thisEventTime, long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <=10 && offsetY<=10 && intervalTime >= longPressTime){
            return true;
        }
        return false;
    }
    @Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
    	super.onDraw(canvas);
    	canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG, 0));

        if (isDLTMode == false)
        	drawLaserNode(vecLaserPointList, canvas, Color.BLUE);

        drawRotatedRect(canvas);
        //drawContours(canvas);
    	/*
    	if (isDLTMode)
    	    drawScaler(1000, 1,canvas);
    	*/
    	if (isGridMode)
    	    drawGrid(canvas);
    	
    	if (isTouchOne){
    		drawTouchRect(canvas);
            drawTouchRect2(canvas);
    		drawInterpPT(mInterpPTLeft, mInterpPTRight, canvas);
    		drawNode(vecSemiAutoNodePoint, canvas);
    		//drawBoundary(vecSemiAutoBounUpPoint,vecSemiAutoBounDownPoint,canvas);
    	}
	}
	/**
     * �q�{�c�y���
     * @param
     * @return 
     */
	public void setVibrator(Vibrator vibrator) {
		this.vibrator = vibrator;
	}
	
	public void drawBoundary(ArrayList <PointF> up, ArrayList <PointF> down, Canvas canvas) {
		if (up == null || down == null)
			return;
		
		//�]�m�e��
		paint = new Paint();
		paint.setStrokeWidth(5);//���e5�Ϥ�
		paint.setStyle(Paint.Style.STROKE);//�Ť�  
		paint.setColor(Color.YELLOW);//�]�m������
		paint.setAntiAlias(true);//���������
		//�y���ഫ+�e�W���
		Matrix mtrx = this.getImageMatrix();
		float[] pts = new float[] {0, 0};
		float[] dst = new float[] {0, 0};
		for (int i=0;i<up.size();i++)
		{
			 pts[0] = up.get(i).x;
			 pts[1] = up.get(i).y;
			 //�y���ഫ
			 mtrx.mapPoints(dst, pts);
			 //�e��
			 canvas.drawLine(dst[0]-5, dst[1]-5, dst[0]+5, dst[1]+5, paint);
			 canvas.drawLine(dst[0]+5, dst[1]-5, dst[0]-5, dst[1]+5, paint);
		}		
		//�y���ഫ+�e�U���
		mtrx = this.getImageMatrix();
		paint.setColor(Color.RED);//�]�m������
		float[] pts2 = new float[] {0, 0};
		float[] dst2 = new float[] {0, 0};
		for (int i=0;i<down.size();i++)
		{
			 pts2[0] = down.get(i).x;
			 pts2[1] = down.get(i).y;
			 //�y���ഫ
			 mtrx.mapPoints(dst2, pts2);
			 //�e��
			 canvas.drawLine(dst2[0] - 5, dst2[1] - 5, dst2[0] + 5, dst2[1] + 5, paint);
			 canvas.drawLine(dst2[0] + 5, dst2[1] - 5, dst2[0] - 5, dst2[1] + 5, paint);
		}	
	}
    /**
     * ø�s�����ת�Rect
     * @param
     * @return
     */
    public void drawRotatedRect(Canvas canvas) {
        if (vecRotatedRectContours == null)
            return;
        //�]�m�e��
        paint = new Paint();
        paint.setStrokeWidth(2);//���e5�Ϥ�
        paint.setStyle(Paint.Style.STROKE);//�Ť�
        paint.setColor(Color.RED);//�]�m������
        paint.setAntiAlias(true);//���������

        for (int i=0;i<vecRotatedRectContours.size();i++)
        {
            org.opencv.core.Point [] rect_points = new org.opencv.core.Point[4];
            vecRotatedRectContours.get(i).points(rect_points);

            //�y���ഫ
            Matrix mtrx = this.getImageMatrix();
            float[] pts = new float[]{(float)rect_points[0].x, (float)rect_points[0].y,
                                      (float)rect_points[1].x, (float)rect_points[1].y,
                                      (float)rect_points[1].x, (float)rect_points[1].y,
                                      (float)rect_points[2].x, (float)rect_points[2].y,
                                      (float)rect_points[2].x, (float)rect_points[2].y,
                                      (float)rect_points[3].x, (float)rect_points[3].y,
                                      (float)rect_points[3].x, (float)rect_points[3].y,
                                      (float)rect_points[0].x, (float)rect_points[0].y};
            float[] dst = new float[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mtrx.mapPoints(dst, pts);
            canvas.drawLines(dst, paint);
        }

    }
	public void drawTouchRect(Canvas canvas) {
		//�]�m�e��
		paint = new Paint();
		paint.setStrokeWidth(5);//���e5�Ϥ�
		paint.setStyle(Paint.Style.STROKE);//�Ť�  
		paint.setColor(Color.RED);//�]�m������
		paint.setAntiAlias(true);//���������
        //�y���ഫ
		Matrix mtrx = this.getImageMatrix(); 
		float[] pts = new float[]{crackstart_x, crackstart_y, crackmiddle_x, crackmiddle_y};
		float[] dst = new float[] {0, 0, 0, 0};
		mtrx.mapPoints(dst, pts);
		//�e�W��
		if (crackmiddle_y!=0 || crackmiddle_y!=0)  //End�I����0�ɤ��e
			canvas.drawLine(dst[0], dst[1], dst[2], dst[3], paint);

		if (dst[0] > 7 && dst[1] > 7)
			canvas.drawRect(dst[0]-7, dst[1]-7, dst[0]+7, dst[1]+7, paint);

		if (dst[2] > 7 && dst[3] > 7)
			canvas.drawRect(dst[2]-7, dst[3]-7, dst[2]+7, dst[3]+7, paint);

	}
    public void drawTouchRect2(Canvas canvas) {
        //�]�m�e��
        paint = new Paint();
        paint.setStrokeWidth(5);//���e5�Ϥ�
        paint.setStyle(Paint.Style.STROKE);//�Ť�
        paint.setColor(Color.RED);//�]�m������
        paint.setAntiAlias(true);//���������
        //�y���ഫ
        Matrix mtrx = this.getImageMatrix();
        float[] pts = new float[]{crackmiddle_x, crackmiddle_y, crackend_x, crackend_y};
        float[] dst = new float[] {0, 0, 0, 0};
        mtrx.mapPoints(dst, pts);
        //�e�W��
        if (crackend_x!=0 || crackend_y!=0)  //End�I����0�ɤ��e
            canvas.drawLine(dst[0], dst[1], dst[2], dst[3], paint);

        if (dst[0] > 7 && dst[1] > 7)
            canvas.drawRect(dst[0]-7, dst[1]-7, dst[0]+7, dst[1]+7, paint);

        if (dst[2] > 7 && dst[3] > 7)
            canvas.drawRect(dst[2]-7, dst[3]-7, dst[2]+7, dst[3]+7, paint);
    }
	public void drawInterpPT(PointF left,PointF right, Canvas canvas) {
		//�]�m�e��
		paint = new Paint();
		paint.setStrokeWidth(5);//���e5�Ϥ�
		paint.setStyle(Paint.Style.STROKE);//�Ť�  
		paint.setColor(Color.YELLOW);//�]�m������
		paint.setAntiAlias(true);//���������
		//�y���ഫ
		Matrix mtrx = this.getImageMatrix(); 
		float[] pts =new float[]{left.x+0.5f, left.y+0.5f, right.x+0.5f, right.y+0.5f};  //�ե��^pixel��������,���v�T�e�׭�
		float[] dst = new float[] {0, 0, 0, 0};
		mtrx.mapPoints(dst, pts);
		//�e�WXX
		canvas.drawLine(dst[0]+5, dst[1]-5, dst[0]-5, dst[1]+5, paint);
		canvas.drawLine(dst[0]-5, dst[1]-5, dst[0]+5, dst[1]+5, paint);
		canvas.drawLine(dst[2]-5, dst[3]-5, dst[2]+5, dst[3]+5, paint);
		canvas.drawLine(dst[2]+5, dst[3]-5, dst[2]-5, dst[3]+5, paint);
	}
	public void drawNode(ArrayList <PointF> nodePoint, Canvas canvas) {
		if (nodePoint == null)
			return;
		
		//�]�m�e��
		paint = new Paint();
		paint.setStrokeWidth(5);//���e5�Ϥ�
		paint.setStyle(Paint.Style.STROKE);//�Ť�  
		paint.setColor(Color.GREEN);//�]�m������
		paint.setAntiAlias(true);//���������
		//�y���ഫ+�e��
		Matrix mtrx = this.getImageMatrix();
		float[] pts = new float[] {0, 0};
		float[] dst = new float[] {0, 0};
		for (int i=0;i<nodePoint.size();i++)
		{
			 pts[0] = nodePoint.get(i).x;
			 pts[1] = nodePoint.get(i).y;
			 //�y���ഫ
			 mtrx.mapPoints(dst, pts);
			 //�e��
			 canvas.drawLine(dst[0]-5, dst[1]-5, dst[0]+5, dst[1]+5, paint);
			 canvas.drawLine(dst[0]+5, dst[1]-5, dst[0]-5, dst[1]+5, paint);
		}
	}
	public void drawLaserNode(ArrayList <PointF> nodePoint, Canvas canvas, int color) {	
		if (nodePoint == null)
			return;
	
		//�]�m�e��
		paint = new Paint();
		paint.setStrokeWidth(3);//���e5�Ϥ�
		paint.setStyle(Paint.Style.STROKE);//�Ť�  
		paint.setColor(Color.BLUE);//�]�m������
		paint.setAntiAlias(true);//���������
		
		//�]�m�r
		Paint paintWord = new Paint();
		paintWord.setTextSize(54);//�]�w�r��j�p
		paintWord.setStrokeWidth(3);//���e5�Ϥ�
		paintWord.setStrokeWidth(24);//���e24�Ϥ�
		paintWord.setColor(color);//�]�m������

		//�y���ഫ+�e��
		Matrix mtrx = this.getImageMatrix();
		float[] pts = new float[] {0, 0};
		float[] dst = new float[] {0, 0};
		for (int i=0;i<nodePoint.size();i++)
		{
			pts[0] = nodePoint.get(i).x;
			pts[1] = nodePoint.get(i).y;
			//�y���ഫ
			mtrx.mapPoints(dst, pts);
			//�e��
			canvas.drawLine(dst[0]-8, dst[1]-8, dst[0]+8, dst[1]+8, paint);
			canvas.drawLine(dst[0]+8, dst[1]-8, dst[0]-8, dst[1]+8, paint);
			//�p�G���|�� �e�WABCD
			if (nodePoint.size() == 4)
			{
				if (i==0)
				    canvas.drawText("A", dst[0]-20, dst[1]-20, paintWord); 	
				if (i==1)
				    canvas.drawText("B", dst[0]-20, dst[1]-20, paintWord); 
				if (i==2)
				    canvas.drawText("C", dst[0]-20, dst[1]-20, paintWord); 
				if (i==3)
				    canvas.drawText("D", dst[0]-20, dst[1]-20, paintWord); 
				
			}
		}
	}
	public void drawScaler(int length, float scale, Canvas canvas) {
		//�e�@��10cm*10cm����Ҥ�
		//�]�m�e��
		paint = new Paint();
		paint.setStrokeWidth(5);//���e5�Ϥ�
		paint.setStyle(Paint.Style.STROKE);//�Ť�  
		paint.setColor(Color.RED);//�]�m������
		paint.setAntiAlias(true);//���������
		//�]�m�r
		Paint paintWord = new Paint();
		paintWord.setTextSize(36);//�]�w�r��j�p
		paintWord.setStrokeWidth(3);//���e5�Ϥ�
		paintWord.setStrokeWidth(24);//���e24�Ϥ�
		paintWord.setColor(Color.BLUE);//�]�m������
		//�]�m�ഫ�x�}
		Matrix mtrx = this.getImageMatrix();
		PointF xstart = new PointF(mImageWidth/7,mImageHeight/7*6);
		PointF xend = new PointF(mImageWidth/7 + length,mImageHeight/7*6);
		float[] pts = new float[] {xstart.x, xstart.y, xend.x, xend.y};
		float[] dst = new float[] {0, 0, 0, 0};
		mtrx.mapPoints(dst, pts);
		//�e��
		canvas.drawLine(dst[0], dst[1], dst[2], dst[3], paint);//�D��
		canvas.drawLine(dst[0], dst[1]-10, dst[0], dst[1]+10, paint);//�Y��
		canvas.drawLine(dst[2], dst[3]-10, dst[2], dst[3]+10, paint);//����
		//�g�r
		canvas.drawText("100mm", dst[0], dst[1]-10, paintWord); 	
		
	}
    public void drawContours(Canvas canvas) {
        if (vecContoursList == null)
            return;
        //�]�m�e��
        paint = new Paint();
        paint.setStrokeWidth(1);//���e5�Ϥ�
        paint.setStyle(Paint.Style.STROKE);//�Ť�
        paint.setColor(Color.RED);//�]�m������
        paint.setAntiAlias(true);//���������

        //�]�m�ഫ�x�}
        Matrix mtrx = this.getImageMatrix();

        //trace every contour
        for (int i = 0; i < vecContoursList.size(); i++)
        {
            MatOfPoint contours =  vecContoursList.get(i);
            org.opencv.core.Point[] points_contour = contours.toArray();

            float[] pts = new float[points_contour.length*2];
            float[] dst = new float[points_contour.length*2];

            for (int j =0; j < points_contour.length; j++)
            {
                pts[j*2] = (float)points_contour[j].x;
                pts[j*2+1] = (float)points_contour[j].y;
            }
            //pts[(points_contour.length)*2] = (float)points_contour[0].x;
            //pts[(points_contour.length)*2+1] = (float)points_contour[0].y;
            //�y���ഫ
            mtrx.mapPoints(dst, pts);
            //�e��
            if (points_contour.length>2)
               canvas.drawLines(dst,paint);

            //release
            pts = null;
            dst = null;
        }
    }
	public void drawGrid(Canvas canvas) {
		//�]�m�e��
		paint = new Paint();
		paint.setStrokeWidth(1);//���e5�Ϥ�
		paint.setStyle(Paint.Style.STROKE);//�Ť�  
		paint.setColor(Color.RED);//�]�m������
		paint.setAntiAlias(true);//���������
		//�]�m�ഫ�x�}
		Matrix mtrx = this.getImageMatrix();
		
		//�e���u
		for (int i=0;i<mImageWidth;i++)
		{
			PointF xstart = new PointF(i,0);
			PointF xend = new PointF(i,mImageHeight);
			float[] pts = new float[] {xstart.x, xstart.y, xend.x, xend.y};
			float[] dst = new float[] {0, 0, 0, 0};
			
			mtrx.mapPoints(dst, pts);
			canvas.drawLine(dst[0], dst[1], dst[2], dst[3], paint);//�D��
		}
		//�e��u
		for (int j=0;j<mImageHeight;j++)
		{
			PointF xstart = new PointF(0,j);
			PointF xend = new PointF(mImageWidth,j);
			float[] pts = new float[] {xstart.x, xstart.y, xend.x, xend.y};
			float[] dst = new float[] {0, 0, 0, 0};
			
			mtrx.mapPoints(dst, pts);
			canvas.drawLine(dst[0], dst[1], dst[2], dst[3], paint);//�D��
		}
		
	}

}
