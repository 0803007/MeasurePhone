package nchc.measurePhone;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore.MediaColumns;
import android.text.SpannableString;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;

import javax.microedition.khronos.opengles.GL10;

import nchc.measurePhone.SemiAutoExtraction;

public class ImageShowActivity extends Activity{
	private static final int
            MENU_AUTO_LASER = Menu.FIRST,
           	MENU_MANU_LASER = Menu.FIRST + 1,
           	MENU_GET_DLTIMG = Menu.FIRST + 2,
           	MENU_PROFILE = Menu.FIRST + 3,
           	MENU_SEMIAUTO = Menu.FIRST + 4,
         	MENU_OPENCLOSE_GRID = Menu.FIRST + 5,
         	MENU_ABOUT = Menu.FIRST + 6,
           	MENU_EXIT = Menu.FIRST + 7,
        	MENU_OPENCLOSE_SENSOR = Menu.FIRST + 8,
            MENU_INIT_AB = Menu.FIRST + 9,
            MENU_GET_AB = Menu.FIRST + 10,
            MENU_GET_THRESH = Menu.FIRST + 11,
	        MENU_DIVIDE_LINE = Menu.FIRST + 12,
	        MENU_GET_A4_CORNER = Menu.FIRST + 13;

	//�D�e��
	private TouchView iv;
	private Vibrator mVibrator;
	private TextView mLogText;
	private TextView mPosText;
	private TextView mRotText;
	private TextView mAccText;
	private TextView mAngText;
	private SlidingDrawer mSlidingDrawer;
	private ProgressDialog mProgressDialog;
	private ImageButton mROILButton;
	private ImageButton mROIRButton;
	private ProgressBar mProgressBar;
	private Button mHorizontalButton;
	private Button mVerticalButton;

	//���ê�button
	private Button mTopButton;
	private Button mDownButton;
	private Button mRightButton;
	private Button mLeftButton;
	private Button mSaveButton;
	private Button mCenterButton;

	//�������
	private Bitmap mBitmap = null;
	private Bitmap mBackupBitmap = null;
	private Bitmap mBinaryBitmap = null;
	private Bitmap mBinary2Bitmap = null;
	ArrayList <PointF> mLaserPointList = null;
	List<MatOfPoint> mContoursList = null;

	//�����P
	private EditText mAlphaText;
	private EditText mBetaText;
	private Button mSetAngleButton;
	private EditText mWText;
	private EditText mHText;
	private EditText mShiftXText;
	private EditText mShiftYText;
	private EditText mScaleText;
	private Button mSetWHButton;
	private Button mSetScaleButton;
	private Button mA4HWButton;

	//�k���P
	private EditText mStdText;
	private EditText mNodeText;
	private EditText mNormalText;

	//�W����P
	private CheckBox mGreenCheckBox;
	private CheckBox mRedCheckBox;
	private SeekBar mRSeekbar;
	private SeekBar mRRGBSeekbar;
	private SeekBar mGRGBSeekbar;
	private SeekBar mBRGBSeekbar;
	private SeekBar mAreaSeekbar;
	private EditText mRText;
	private EditText mRRGBText;
	private EditText mGRGBText;
	private EditText mBRGBText;
	private EditText mAreaText;
	private TextView RTextView;
	private TextView RRGBTextView;
	private TextView GRGBTextView;

	//�U����P
	private Button mContoursButton;
	private Button mThinButton;
	private EditText mNnnText;
	private EditText mBaseLineThresholdText;
	private EditText mDiffRatioText;
	private EditText mAaaText;
	private EditText mGHText;

	//Dialog
	private Spinner mParamSpinner;
	private Spinner mBlockSizeSpinner;
	private Spinner mThresholdIntervalSpinner;
	private Button mAdaptiveButton;
	private EditText mManualText;
	private SeekBar mManualSeekBar;
	private Button mManualButton;
	private SeekBar mLowerSeekBar;
	private SeekBar mUpperSeekBar;
	private EditText mLowerText;
	private EditText mUpperText;
	private EditText mThinContourSizeText;
	private EditText mrThresholdIntervaText;
	private Button mAreaRangeButton;
	private Button mORIButton;
	private Button mDLTButton;
	private Button mBINButton;
	private Button mBINTHRButton;
	private Button mFitEclipseButton;
	private Button mThinContourButton;
	private Button mThresholdIntervalButton;

	//A4ThresholdDialoh
	private SeekBar mA4SeekBar;
	private Button mA4Button;
	private EditText mA4Text;

	//Angle Data
    private int 				   count1 = 0;
    private int 				   count2 = 0;
    private int 				   count3 = 0;
    private double 				   tmp_aAngle = 0.0;  //wyc1,20140622
    private double 				   tmp_bAngle = 0.0;  //wyc1,20140622
    private TextView output;
    private boolean key1 = false; //����getAngle��Akey = true�A�����"Alpha,Beta �g�J�ɮצ��\"��TextView
    private boolean key2 = false;
    private boolean key3 = false;
    private boolean key4 = false;
    private boolean key5 = false;

    private String posA;  //wyc1,20140622
    private String posB;  //wyc1,20140622
    private String posC;  //wyc1,20140622
    private String posD;  //wyc1,20140622
    private String tmpString;  //wyc1,20140622
    private double 				   aAngle = 0.0;  //wyc1,20140622
    private double 				   bAngle = 0.0;  //wyc1,20140622
    private double 				   aInitAngle = 0.0;
    private double 				   bInitAngle = 0.0;
    //private double                 read_aAngle = 0.0;
    //private double                 read_bAngle = 0.0;
    private double 				   W = 155;  //139mm 128mm  155mm(CG200) 297mm(A4)
    private double 				   H = 94;   //82mm  58mm    94mm        210mm
    private int                    mShiftX = 1000;
    private int                    mShiftY = 1000;

    private float 				   pointA_x = (float)0.0;
    private float 				   pointA_y = (float)0.0;
    private float 				   pointB_x = (float)0.0;
    private float 				   pointB_y = (float)0.0;
    private float 				   pointC_x = (float)0.0;
    private float 				   pointC_y = (float)0.0;
    private float 				   pointD_x = (float)0.0;
    private float 				   pointD_y = (float)0.0;
    private float                  pointA_u = (float)0.0;
    private float                  pointA_v = (float)0.0;
    private float                  pointB_u = (float)0.0;
    private float                  pointB_v = (float)0.0;
    private float                  pointC_u = (float)0.0;
    private float                  pointC_v = (float)0.0;
    private float                  pointD_u = (float)0.0;
    private float                  pointD_v = (float)0.0;

    //ortho-rectification
    private Mat                    mLaser;
    private Mat                    mLasertmp;
    private Mat                    imgR;
    private Mat                    imgG;
    private Mat                    imgB;
    private Mat                    mR;
    private Mat                    mG;
    private Mat                    mB;
    private Mat                    temp;
    private Mat                    temp1;
    private Mat                    temp2;
    private Mat                    temp3;
    private Mat                    temp4;
    private Mat                    epsilon;
    private Mat                    zero;
    private int 				   fMinArea;
    private Mat                    mAA;
    private Mat                    mXX;
    private Mat                    mBB;
    private Mat                    ortho_bf;
    private Mat                    ortho_af;

    //NativePIV
    private int 				   PIVFile_count = 0;
    float[] LL={0,0,0,0,0,0,0,0};
	float mScale = (float)5.0;

	//Sensor values
	float[] magneticFieldValues = new float[3];
	float[] accelerometerValues = new float[3];
	float[] gyroscopeValues = new float[3];
	float[] gyroscopeAngles = new float[3];

	//Update Rotation ��T
	boolean bShowSensor = false;
    Handler mHandler;
    Timer updateTimer;
    TimerTask mClock;

    //other
	private SemiAutoExtraction mSemiAuto;
	private int nProgressStatus = 0;
	private float timestamp;
	private static final float NS2S = 1.0f / 1000000000.0f;
	private boolean bManualLaserPoint = false;
	private int nLaserPointIndex = 0;
	private Uri uri;
	private String ImagePath;
	private Dialog mThresholdDlg;
	private Dialog mA4ThresholdDlg;
	private boolean bGreenLaser = true;
	public int mStatusDisplay = 0;
	private boolean bResizeImage = false;

	//classs
	class Vectordata{
		PointF pt = new PointF();
		double value = 0.0f;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setupViewComponent();

		//���o�v���ӷ�
	    Bundle bundle = getIntent().getExtras();
	    ImagePath = bundle.getString("DATA_STRING");
	    uri = Uri.parse(ImagePath);
	    //iv.setImageURI(uri);
	    ContentResolver cr = this.getContentResolver();

		//��k1
		BitmapFactory.Options opt16 = new BitmapFactory.Options();
		//opt16.inSampleSize = 2;
		opt16.inPurgeable = true;
		opt16.inPreferredConfig = Bitmap.Config.RGB_565;
		mBitmap = BitmapFactory.decodeFile(getFilePathFromContentUri(uri,cr),opt16).copy(Bitmap.Config.RGB_565, true);
		//mBitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));

		//��k2
		/*
		Mat m = Highgui.imread(getFilePathFromContentUri(uri,cr));
		mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(m, mBitmap);
		m.release();*/
		
		/*
		//��������
		Canvas canvas = new Canvas(mBitmap);
		Paint paint = new Paint();
		int w = mBitmap.getWidth();
		int h = mBitmap.getHeight();
		//  int w_2 = bitmap2.getWidth();
		//  int h_2 = bitmap2.getHeight();

		paint.setAntiAlias(false);
		paint.setDither(false);  
		paint.setFilterBitmap(false); 
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		*/

		//�d���`�O����
		ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		ConfigurationInfo info = activityManager.getDeviceConfigurationInfo();
		int gl_version = info.reqGlEsVersion;

		//�p�G�v���Ӥj �Y�p�v�� (GC100�u��512mb�O����)
		//��ĳ�ϥ�Webview�L����j�p or ���������v��
		//�O����p��768MB�v�� && ���OOpenGL3.0 && �v���W�X2048 resize
		//if (mBitmap.getWidth()>3000||mBitmap.getHeight()>3000)   // <--gc100�]����
		if (gl_version < 0x00020001)    //0x00020001  OpenGL2.1
			if (mBitmap.getWidth()>2048||mBitmap.getHeight()>2048)
				if(memoryInfo.totalMem < 7680*1024*1024)	{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("OpenGL�������� �O����Ӥp �v���W�X4096 �Y�p�v��?").setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									bResizeImage = true;
									setInit();
								}
							}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							//����AlertDialog����
							bResizeImage = false;
							dialog.cancel();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				}

		setInit();
	}
	private void setInit(){
		//�P�_�O�_�nresize�v��
		if(bResizeImage)
		   mBitmap = reSizeBitmap(mBitmap);

		//IV�]�w
		iv.mPosText = mPosText;
		iv.mImageWidth = mBitmap.getWidth();
		iv.mImageHeight = mBitmap.getHeight();
		//iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		iv.setImageBitmap(mBitmap);
		iv.setVibrator(mVibrator);

		//�ƥ��@��Bitmap ����DLT Image�ɨϥ�
		mBackupBitmap = mBitmap.copy(Bitmap.Config.RGB_565, false); //ARGB_8888 �Ӥj ���RGB_565
		//�ƥ��@��BinaryBitmap �Ƕ��ȹB��ϥ�
		mBinaryBitmap = mBitmap.copy(Bitmap.Config.RGB_565, false);//ARGB_8888 �Ӥj ���RGB_565
		mBinary2Bitmap = mBitmap.copy(Bitmap.Config.RGB_565, false);//ARGB_8888 �Ӥj ���RGB_565

		//ortho-rectification
		mAA = new Mat(8, 8, CvType.CV_32FC1, new Scalar(0));
		mXX = new Mat(8, 1, CvType.CV_32FC1, new Scalar(0));
		mBB = new Mat(8, 1, CvType.CV_32FC1, new Scalar(0));
		ortho_bf = new Mat(mBitmap.getHeight(), mBitmap.getWidth(), CvType.CV_8UC1, new Scalar(0));
		ortho_af = new Mat(mBitmap.getHeight(), mBitmap.getWidth(), CvType.CV_8UC1, new Scalar(0));

		//Ū���w�]��EXIF
		setDefaultEXIF();
	}

	private void setDefaultEXIF() {
		// TODO Auto-generated method stub
	    File file = new File(getFilePathFromContentUri(uri,this.getContentResolver()));
	    getABFormEXIF(file);
		DecimalFormat df = new DecimalFormat("#.##");
		mAlphaText.setText(df.format(aAngle));
		mBetaText.setText(df.format(bAngle));

		return;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (mBitmap!=null)
			mBitmap.recycle();

		if (mBackupBitmap!=null)
			mBackupBitmap.recycle();

		if (mBinaryBitmap!=null)
			mBinaryBitmap.recycle();

		if (mBinary2Bitmap!=null)
			mBinary2Bitmap.recycle();

		if (mAA!=null)
			mAA.release();

		if (mXX!=null)
			mXX.release();

		if (mBB!=null)
			mBB.release();

		if (ortho_bf!=null)
			ortho_bf.release();

		if (ortho_af!=null)
			ortho_af.release();

	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//unregisterReceiver(updateUIReceive);
	}
	private void setupViewComponent(){
		//�q�귽���OR�����o��������
		//�w��[�t
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,	WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		//�]�m���
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.imageshow_activity);

		//����title�]�w
		mThresholdDlg = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
		//mThresholdDlg.setTitle("���e��");

		mThresholdDlg.requestWindowFeature(getWindow().FEATURE_NO_TITLE);
		mThresholdDlg.setContentView(R.layout.threshold_dlg);

		mA4ThresholdDlg = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
		mA4ThresholdDlg.requestWindowFeature(getWindow().FEATURE_NO_TITLE);
		mA4ThresholdDlg.setContentView(R.layout.a4threshold_dlg);

	    //ImageView iv = (ImageView)this.findViewById(R.id.imageView1);
	    iv = (TouchView)this.findViewById(R.id.touchView1);
	    mLogText = (TextView) findViewById(R.id.textView1);
	    mPosText = (TextView) findViewById(R.id.textView2);
	    mRotText = (TextView) findViewById(R.id.textView5);
	    mAccText = (TextView) findViewById(R.id.textView3);
	    mAngText  = (TextView) findViewById(R.id.textView6);
	    //mSlidingDrawer = (SlidingDrawer) this.findViewById(R.id.slidingDrawer1);
	    mAlphaText = (EditText)findViewById(R.id.alphaEditText);
	    mBetaText = (EditText)findViewById(R.id.betaEditText);
	    mWText = (EditText)findViewById(R.id.WEditText);
	    mHText = (EditText)findViewById(R.id.HEditText);
	    mWText.setText(Integer.toString((int)W));//��l��
	    mHText.setText(Integer.toString((int)H));

	    mScaleText = (EditText)findViewById(R.id.ScaleEditText);
	    mShiftXText = (EditText)findViewById(R.id.ShiftXEditText);
	    mShiftYText = (EditText)findViewById(R.id.ShiftYEditText);

	    mStdText = (EditText)findViewById(R.id.stdEditText);
	    mNodeText = (EditText)findViewById(R.id.nodeEditText);
	    mNormalText = (EditText)findViewById(R.id.normalEditText);
		mRSeekbar = (SeekBar)findViewById(R.id.RSeekBar);
		mRRGBSeekbar = (SeekBar)findViewById(R.id.RRGBSeekBar);
		mGRGBSeekbar = (SeekBar)findViewById(R.id.GRGBSeekBar);
		mBRGBSeekbar = (SeekBar)findViewById(R.id.BRGBseekBar);
		mAreaSeekbar = (SeekBar)findViewById(R.id.AREAseekBar);
		mRText = (EditText)findViewById(R.id.REditText);
		mRRGBText = (EditText)findViewById(R.id.RRGBEditText);
		mGRGBText = (EditText)findViewById(R.id.GRGBEditText);
		mBRGBText = (EditText)findViewById(R.id.BRGBEditText);
		mAreaText = (EditText)findViewById(R.id.AreaEditText);
        mTopButton = (Button)findViewById(R.id.buttonPreview);
	    mDownButton = (Button)findViewById(R.id.button2);
		mRightButton = (Button)findViewById(R.id.button4);
		mLeftButton = (Button)findViewById(R.id.button3);
		mSaveButton = (Button)findViewById(R.id.button5);
		mCenterButton = (Button)findViewById(R.id.button6);
		mSetAngleButton = (Button)findViewById(R.id.button7);
		mSetWHButton = (Button)findViewById(R.id.button11);
		mSetScaleButton = (Button)findViewById(R.id.button8);
		mROILButton = (ImageButton)findViewById(R.id.imageButton1);
		mROIRButton = (ImageButton)findViewById(R.id.imageButton2);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
		mContoursButton = (Button)findViewById(R.id.contoursButton);
		mThinButton = (Button)findViewById(R.id.thinButton);
		mHorizontalButton = (Button)findViewById(R.id.buttonHorizontal);
		mVerticalButton = (Button)findViewById(R.id.buttonVertical);
		mGreenCheckBox = (CheckBox)findViewById(R.id.greenCheckBox);
		RTextView = (TextView)findViewById(R.id.RTextView);
		RRGBTextView = (TextView)findViewById(R.id.RRGBTextView);
		GRGBTextView = (TextView)findViewById(R.id.GRGBTextView);
		mA4HWButton = (Button)findViewById(R.id.button12);
		mNnnText = (EditText)findViewById(R.id.nnnEditText);
		mBaseLineThresholdText = (EditText)findViewById(R.id.baseLineThresholdEditText);
		mDiffRatioText = (EditText)findViewById(R.id.diffRatioEditText);
		mAaaText = (EditText)findViewById(R.id.aaaEditText);
		mGHText = (EditText)findViewById(R.id.gHEditText);

		//A4ThresholdDialog
		mA4SeekBar = (SeekBar)mA4ThresholdDlg.findViewById(R.id.a4SeekBar);
		mA4Button = (Button)mA4ThresholdDlg.findViewById(R.id.a4Button);
		mA4Text = (EditText)mA4ThresholdDlg.findViewById(R.id.A4TextView);
		mA4SeekBar.setOnSeekBarChangeListener(a4SeekBarOnChangeLis);

		//ThresholdDialog
		//mAdaptiveSpinner = (Spinner)mThresholdDlg.findViewById(R.id.spinnerBlockSize);
		mParamSpinner = (Spinner)mThresholdDlg.findViewById(R.id.spinnerParam);
		mBlockSizeSpinner = (Spinner)mThresholdDlg.findViewById(R.id.spinnerBlockSize);
		mThresholdIntervalSpinner =  (Spinner)mThresholdDlg.findViewById(R.id.spinnerThreshInterval);
		mAdaptiveButton = (Button)mThresholdDlg.findViewById(R.id.buttonAdaptive);
		mManualButton = (Button)mThresholdDlg.findViewById(R.id.buttonManual);
		mManualText = (EditText)mThresholdDlg.findViewById(R.id.editTextManual);
		mManualSeekBar = (SeekBar)mThresholdDlg.findViewById(R.id.seekBarManual);
		mLowerSeekBar = (SeekBar)mThresholdDlg.findViewById(R.id.seekBarLower);
		mUpperSeekBar = (SeekBar)mThresholdDlg.findViewById(R.id.seekBarUpper);
		mLowerText = (EditText)mThresholdDlg.findViewById(R.id.editTextLower);
		mUpperText = (EditText)mThresholdDlg.findViewById(R.id.editTextUpper);
		mThinContourSizeText = (EditText)mThresholdDlg.findViewById(R.id.editTextThinContourSize);
		mrThresholdIntervaText = (EditText)mThresholdDlg.findViewById(R.id.editTextThreshInterval);
		mAreaRangeButton = (Button)mThresholdDlg.findViewById(R.id.buttonAreaRange);
		mORIButton = (Button)mThresholdDlg.findViewById(R.id.buttonORI);
		mDLTButton = (Button)mThresholdDlg.findViewById(R.id.buttonDLT);
		mBINButton = (Button)mThresholdDlg.findViewById(R.id.buttonBIN);
		mBINTHRButton = (Button)mThresholdDlg.findViewById(R.id.buttonBINTHR);
		mFitEclipseButton = (Button)mThresholdDlg.findViewById(R.id.buttoFitEclipse);
		mThinContourButton =  (Button)mThresholdDlg.findViewById(R.id.buttonThinContour);
		mThresholdIntervalButton =  (Button)mThresholdDlg.findViewById(R.id.buttonThreshInterval);

		mVibrator = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);

		mRSeekbar.setOnSeekBarChangeListener(rseekBarOnChangeLis);
		mRRGBSeekbar.setOnSeekBarChangeListener(rrgbseekBarOnChangeLis);
		mGRGBSeekbar.setOnSeekBarChangeListener(grgbseekBarOnChangeLis);
		mBRGBSeekbar.setOnSeekBarChangeListener(brgbseekBarOnChangeLis);
		mAreaSeekbar.setOnSeekBarChangeListener(areaseekBarOnChangeLis);
		mManualSeekBar.setOnSeekBarChangeListener(manualseekBarOnChangeLis);
		mLowerSeekBar.setOnSeekBarChangeListener(lowerseekBarOnChangeLis);
		mUpperSeekBar.setOnSeekBarChangeListener(upperseekBarOnChangeLis);

		mA4HWButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mWText.setText("297");
				mHText.setText("210");
			}
		});
		mTopButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList == null)
					return;

				int i = (int) mLaserPointList.get(nLaserPointIndex).x;
				int j = (int) mLaserPointList.get(nLaserPointIndex).y;
				mLaserPointList.set(nLaserPointIndex, new PointF(i, j - 3));
				iv.postInvalidate();
			}
		});
		mDownButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;

				int i = (int) mLaserPointList.get(nLaserPointIndex).x;
	       		int j = (int) mLaserPointList.get(nLaserPointIndex).y;
	       		mLaserPointList.set(nLaserPointIndex, new PointF(i,j+3));
	       		iv.postInvalidate();
			}});
		mRightButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;

				int i = (int) mLaserPointList.get(nLaserPointIndex).x;
	       		int j = (int) mLaserPointList.get(nLaserPointIndex).y;
	       		mLaserPointList.set(nLaserPointIndex, new PointF(i+3,j));
	       		iv.postInvalidate();
			}});
		mLeftButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;

				int i = (int) mLaserPointList.get(nLaserPointIndex).x;
	       		int j = (int) mLaserPointList.get(nLaserPointIndex).y;
	       		mLaserPointList.set(nLaserPointIndex, new PointF(i-3,j));
	       		iv.invalidate();
			}});
		mSaveButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;

				//�o��DLT�Ѽ�
		        getDLTParameter();
				//�g�J���I�p�g�v����m
				saveLaserImagePos(mLaserPointList);
                //Auto detect mScale mShiftX mShiftY
				setDLTAutoScale();
			}});
		mCenterButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;

				nLaserPointIndex = nLaserPointIndex + 1;
				if (nLaserPointIndex==4)
					nLaserPointIndex = 0;

				iv.nLaserPointIndex = nLaserPointIndex;
				mCenterButton.setText(Integer.toString(nLaserPointIndex + 1));
			}});
		mSetAngleButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				aAngle = Float.parseFloat(mAlphaText.getText().toString());
				bAngle = Float.parseFloat(mBetaText.getText().toString());

				//�g�J���׼Ц��ɮ�
				saveLaserAngle(aAngle, bAngle);
			}});
		mSetWHButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				W = Float.parseFloat(mWText.getText().toString());
				H = Float.parseFloat(mHText.getText().toString());

				Toast.makeText(getApplicationContext(), "�ק�p�g�e�����\", Toast.LENGTH_SHORT).show();
			}});
		mSetScaleButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mScale = Float.parseFloat(mScaleText.getText().toString());
				mShiftX = Integer.parseInt(mShiftXText.getText().toString());
				mShiftY = Integer.parseInt(mShiftYText.getText().toString());

				Toast.makeText(getApplicationContext(), "�ק�p�g��Ҧ��\", Toast.LENGTH_SHORT).show();
			}});
		mROILButton.setOnTouchListener(new OnTouchListener() {
			int[] temp = new int[]{0, 0};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int eventAction = event.getAction();
				//Log.i("", "OnTouchEvent" + eventAction);

				int x = (int) event.getRawX();
				int y = (int) event.getRawY();
				int id_x = v.getLeft();
				int id_y = v.getTop();
				switch (eventAction) {
					case MotionEvent.ACTION_DOWN:
						temp[0] = (int) event.getX();
						temp[1] = y - v.getTop();
						id_x = v.getLeft();
						id_y = v.getTop();
						if (y > v.getTop() && y < v.getTop() + 100 && x > v.getLeft() && x < v.getLeft() + 100) {
							mROILButton.setVisibility(View.INVISIBLE);
						}
						break;

					case MotionEvent.ACTION_MOVE:
						id_x = v.getLeft();
						id_y = v.getTop();
						v.layout(x - temp[0], y - temp[1], x + mROILButton.getWidth() - temp[0], y - temp[1] + mROILButton.getHeight());

						v.postInvalidate();
					case MotionEvent.ACTION_UP:
						break;
				}
				return false;
			}
		});
		mContoursButton.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		mThinButton.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		mThinContourButton.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//Thin�t��k
				return false;
			}
		});
		mThresholdIntervalButton.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//����Thin �g��ļ��I���t��k
				return false;
			}
		});
		mROIRButton.setOnTouchListener(new OnTouchListener(	){
            int[] temp = new int[]{0,0};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
                int eventAction = event.getAction();
                //Log.i("", "OnTouchEvent"+eventAction);

                int x = (int)event.getRawX();
                int y = (int)event.getRawY();
                int id_x = v.getLeft();
                int id_y = v.getTop();
                switch(eventAction){
                case MotionEvent.ACTION_DOWN:
                        temp[0] = (int)event.getX();
                        temp[1] = y - v.getTop();
                        id_x = v.getLeft();
                        id_y = v.getTop();
                        if(y > v.getTop()&& y < v.getTop()+100&&x>v.getLeft()&&x<v.getLeft()+100)
                        {
                        	mROIRButton.setVisibility(View.INVISIBLE);
                        }
                        break;

                case MotionEvent.ACTION_MOVE:
                        id_x = v.getLeft();
                        id_y = v.getTop();
                        v.layout(x - temp[0], y - temp[1], x + mROIRButton.getWidth()- temp[0], y - temp[1] +  mROIRButton.getHeight());

                        v.postInvalidate();
                case MotionEvent.ACTION_UP:
                        break;
                }
				return false;
			}

		});
		mManualButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//��ܶi��bar
				mProgressBar.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.VISIBLE);
				//mThresholdDlg.cancel();

				new Thread() {
					@Override
					public void run() {
						//��l�ƨ��
						int threshold = Integer.parseInt(mManualText.getText().toString());
						//Ū�Ӥ�
						Mat gray = new Mat();
						Mat img = new Mat();
						Utils.bitmapToMat(mBitmap, img);
						//��Ƕ�
						Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGRA2GRAY, 4);
						//�����e
						Imgproc.threshold(gray, gray, threshold, 255, Imgproc.THRESH_BINARY_INV);

						//�K��bitmap
						Utils.matToBitmap(gray, mBinaryBitmap);

						//Calculate Contours
						mContoursList = new ArrayList<MatOfPoint>();
						Imgproc.findContours(gray, mContoursList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
						iv.vecContoursList = mContoursList;

						//Relase
						gray.release();
						img.release();
						/*
						int width = mBitmap.getWidth();
				    	int height = mBitmap.getHeight();
				    	int threshold = Integer.parseInt(mRText.getText().toString());
				    	if(mBinaryBitmap!=null)
				    		mBinaryBitmap.recycle();
				    	mBinaryBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
					    for(int x = 0; x < width; ++x) {
					        for(int y = 0; y < height; ++y) {
					        	int pixel = mBitmap.getPixel(x, y);
					            int red = Color.red(pixel);

					            //get binary value
					            if(red < threshold){
					            	mBinaryBitmap.setPixel(x, y, 0xFF000000);
					            } else{
					            	mBinaryBitmap.setPixel(x, y, 0xFFFFFFFF);
					            }
					        }
					    }*/
						runOnUiThread(new Runnable() {
							public void run() {
								iv.setImageBitmap(mBinaryBitmap);
								iv.invalidate();
								iv.setScaleFitWin();
								mProgressBar.setVisibility(View.GONE);


							}
						});
					}
				}.start();

			}
		});
		mAdaptiveButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}});
		mAreaRangeButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mContoursList == null)
					return;

				//ŪUI
				float fLowerArea, fUpperArea;
				fLowerArea = Float.parseFloat(mLowerText.getText().toString());
				fUpperArea = Float.parseFloat(mUpperText.getText().toString());
				//Ū�Ӥ�
				Mat img = new Mat();
				Utils.bitmapToMat(mBinaryBitmap, img);
				//��Ƕ�
				Mat gray = new Mat();
				Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGRA2GRAY, 4);

				mContoursList = new ArrayList<MatOfPoint>();

				Imgproc.findContours(gray, mContoursList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
				gray.setTo(new Scalar(0, 0, 0));
				for (int i = 0; i < mContoursList.size(); i++)
					if (fLowerArea < Imgproc.contourArea(mContoursList.get(i)) && Imgproc.contourArea(mContoursList.get(i)) < fUpperArea) {
						Imgproc.drawContours(gray, mContoursList, i, new Scalar(255, 255, 255), -1);
					}

				//�K��bitmap
				Utils.matToBitmap(gray, mBinaryBitmap);

				//�����@��Contours,�A�Ǩ�iv
				mContoursList.clear();
				Imgproc.findContours(gray, mContoursList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
				iv.vecContoursList = mContoursList;

				//Release
				img.release();
				gray.release();

				runOnUiThread(new Runnable() {
					public void run() {
						iv.setImageBitmap(mBinaryBitmap);
						//iv.postInvalidate();
						iv.setScaleFitWin();
						mProgressBar.setVisibility(View.GONE);

					}
				});

			}});
		mHorizontalButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				iv.crackend_y = iv.crackstart_y;
				iv.invalidate();
			}
		});
		mVerticalButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				iv.crackend_x = iv.crackstart_x;
				iv.invalidate();
			}
		});
		mORIButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				iv.setImageBitmap(null);
				iv.destroyDrawingCache();
				iv.setImageBitmap(mBackupBitmap);
				iv.invalidate();
				iv.setScaleFitWin();
				System.gc();
			}
		});
		mDLTButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				iv.setImageBitmap(null);
				iv.destroyDrawingCache();
				iv.setImageBitmap(mBitmap);
				iv.invalidate();
				iv.setScaleFitWin();
				System.gc();
			}
		});
		mBINButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				iv.setImageBitmap(null);
				iv.destroyDrawingCache();
				iv.setImageBitmap(mBinaryBitmap);
				iv.invalidate();
				iv.setScaleFitWin();
				System.gc();
			}
		});
		mBINTHRButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				iv.setImageBitmap(null);
				iv.destroyDrawingCache();
				iv.setImageBitmap(mBinary2Bitmap);
				iv.invalidate();
				iv.setScaleFitWin();
				System.gc();
			}
		});
		mFitEclipseButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ArrayList <RotatedRect> rotatedRectContours = new ArrayList();
				for (int i = 0; i < mContoursList.size(); i++)
				{

					MatOfPoint contours = mContoursList.get(i);
					if (Imgproc.contourArea(contours)>6)
					{
						MatOfPoint2f contours2f = new MatOfPoint2f();
						contours.convertTo(contours2f, CvType.CV_32FC2);
						RotatedRect rect = Imgproc.fitEllipse(contours2f);
						rotatedRectContours.add(rect);
					}
				}
				iv.vecRotatedRectContours = rotatedRectContours;

				//
				iv.invalidate();
				//iv.setScaleFitWin();
			}
		});
		mA4Button.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setA4Corner(Float.parseFloat(mA4Text.getText().toString()));
				mA4ThresholdDlg.hide();
				//��s�e��
				iv.invalidate();
			}
		});
        mGreenCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
        {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					// TODO Auto-generated method stub
					if (isChecked)
					{
						bGreenLaser = true;
						RTextView.setText("G > ");
						RRGBTextView.setText("G / (R + G + B) > ");
						GRGBTextView.setText("R / (R + G + B) < ");
					}
					else
					{
						bGreenLaser = false;
						RTextView.setText("R > ");
						RRGBTextView.setText("R / (R + G + B) > 	");
						GRGBTextView.setText("G / (R + G + B) < ");
					}
				}
        });

	}
	SeekBar.OnSeekBarChangeListener manualseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			mManualText.setText(Integer.toString(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

	};
	SeekBar.OnSeekBarChangeListener rseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			mRText.setText(Integer.toString(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

	};

	SeekBar.OnSeekBarChangeListener rrgbseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			double pos = (double)progress / 100;
			DecimalFormat df = new DecimalFormat("#.##");
			mRRGBText.setText(df.format(pos));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

	};
	SeekBar.OnSeekBarChangeListener grgbseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			double pos = (double)progress / 100;
			DecimalFormat df = new DecimalFormat("#.##");
			mGRGBText.setText(df.format(pos));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub	
		}

	};
	SeekBar.OnSeekBarChangeListener brgbseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			double pos = (double)progress / 100;
			DecimalFormat df = new DecimalFormat("#.##");
			mBRGBText.setText(df.format(pos));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub	
		}

	};
	SeekBar.OnSeekBarChangeListener areaseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			//int pos = progress
			mAreaText.setText(Integer.toString(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

	};
	SeekBar.OnSeekBarChangeListener lowerseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {
			// TODO Auto-generated method stub
			mLowerText.setText(Integer.toString(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

	};
	SeekBar.OnSeekBarChangeListener upperseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {
			// TODO Auto-generated method stub
			mUpperText.setText(Integer.toString(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

	};
	SeekBar.OnSeekBarChangeListener a4SeekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {
			// TODO Auto-generated method stub
			mA4Text.setText(Integer.toString(progress));
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
	};

	private void getAngle(){
		//mRgba = inputFrame.rgba();

		// Log.i("!!!!!!!!!!!!!!!!", Double.toString(aDisplayAngle));

		if ( count1 > 10){   //�֥[
			tmp_aAngle=tmp_aAngle+aAngle;
			tmp_bAngle=tmp_bAngle+bAngle;
		}

		if ( count1 == 30){
			tmp_aAngle=tmp_aAngle/20.;    //��30-10��������
			tmp_bAngle=tmp_bAngle/20.;
			aAngle=tmp_aAngle;
			bAngle=tmp_bAngle;
			posA = "A(0,0)"+"\r\n"; //����;
			posB = "B(" + Double.toString(Math.round(W/Math.cos(Math.toRadians(aAngle))*10000)/10000.0) + "," + Double.toString(Math.round                                        ( W * Math.tan(Math.toRadians(bAngle)) * Math.tan(Math.toRadians(aAngle))*10000)/10000.0) + ")"+"\r\n"; //����;
			posC = "C(" + Double.toString(Math.round(W/Math.cos(Math.toRadians(aAngle))*10000)/10000.0) + "," + Double.toString(Math.round(   (H/Math.cos(Math.toRadians(bAngle)) + W * Math.tan(Math.toRadians(bAngle)) * Math.tan(Math.toRadians(aAngle)))*10000  )/10000.0)+")"+"\r\n"; //����;
			posD = "D(0," + Double.toString(Math.round(H/Math.cos(Math.toRadians(bAngle))*10000)/10000.0) + ")"+"\r\n"; //����;

			key1=false;
		} //if ( count1 == 30)

		count1=count1+1;
	}

	/*�q�v���ӷ��o��Alpha Beta
	 * */
	private void getABFormEXIF(File file){
	    /*
	    //���y�ɮ�  �o��alpha beta
		try {
			String path = Environment.getExternalStorageDirectory().toString ()+"/Crackdata";
			File InFile1 = new File(path,"LaserANGLE.dat");
			Scanner sc = new Scanner(InFile1);
			String input = sc.nextLine();
			aAngle=Double.parseDouble(input);
			//System.out.println(input);
			String input1 = sc.nextLine();
			bAngle=Double.parseDouble(input1);
			//System.out.println(input1);
			sc.close();	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    //���yexif �o��alpha beta
	    /*
		try {
		    TiffImageMetadata exif;
		    IImageMetadata meta = Sanselan.getMetadata(new File((getFilePathFromContentUri(uri,this.getContentResolver()))));
		    if (meta instanceof JpegImageMetadata) {
		        exif = ((JpegImageMetadata)meta).getExif();
		    } else if (meta instanceof TiffImageMetadata) {
		        exif = (TiffImageMetadata)meta;
		    } else {
		        return;
		    }
		    TiffOutputSet outputSet = exif.getOutputSet();
		    TiffOutputDirectory exifDir = outputSet.getOrCreateExifDirectory();
		    exifDir.getFields()
		} catch (ImageReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImageWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//PointF pt = new PointF(0,0);
		//final double alpha = 0.0;
		//final double beta = 0.0;
	    try {
	    	//file = new File(getFilePathFromContentUri(uri,this.getContentResolver()));
	    	boolean abc = file.exists();
	    	IImageMetadata metadata;
	    	metadata = Sanselan.getMetadata(file);
	    	if (metadata != null)
	    	{
	    		JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
	    		TiffField field = jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_USER_COMMENT);
	    		if (field != null){
	    			String str = field.getValueDescription();
	    			str = str.replaceAll("'", "");    //�h����޸�
	    			String[] split = str.split(";");  //alpha beta����
	    			if (split.length == 2)
	    			{
	    				//if (isNumeric(split[0]))
							aAngle = Double.parseDouble(split[0]);
						//if (isNumeric(split[1]))
							bAngle = Double.parseDouble(split[1]);
	    			}
	    			runOnUiThread(new Runnable() {
	                    public void run() {
	                    	Toast.makeText(ImageShowActivity.this, "Alpha=" + aAngle+",Beta=" + bAngle , Toast.LENGTH_SHORT).show();
	                    }
	                });
	    		}
	    		else
	    			runOnUiThread(new Runnable() {
	                    public void run() {
	                    	Toast.makeText(ImageShowActivity.this, "No MetaData" , Toast.LENGTH_SHORT).show();
	                    }
	                });
	    	}
	    	else
    			runOnUiThread(new Runnable() {
                    public void run() {
                    	Toast.makeText(ImageShowActivity.this, "No MetaData" , Toast.LENGTH_SHORT).show();
                    }
                });
	    } catch (ImageReadException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
	}
	public static boolean isNumeric(String str){
		for (int i = str.length();--i>=0;){
			//if (!Character.isDigit(str.charAt(i))){
			if(!Character.isDigit(str.charAt(i)) && str.charAt(i) != '.'){
				return false;
			}
		}
		return true;
	}
	private void getDLTParameter() {
		//	Ū���p�g��g����alpha,beta
		//�@��@��Ū���ɮ�
	    /*
	    //���y�ɮ�  �o��alpha beta
		try {
			String path = Environment.getExternalStorageDirectory().toString ()+"/Crackdata";
			File InFile1 = new File(path,"LaserANGLE.dat");
			Scanner sc = new Scanner(InFile1);
			String input = sc.nextLine();
			read_aAngle=Double.parseDouble(input);
			//System.out.println(input);
			String input1 = sc.nextLine();
			read_bAngle=Double.parseDouble(input1);
			//System.out.println(input1);
			sc.close();	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    //���yexif �o��alpha beta
	    /*
		try {
		    TiffImageMetadata exif;
		    IImageMetadata meta = Sanselan.getMetadata(new File((getFilePathFromContentUri(uri,this.getContentResolver()))));
		    if (meta instanceof JpegImageMetadata) {
		        exif = ((JpegImageMetadata)meta).getExif();
		    } else if (meta instanceof TiffImageMetadata) {
		        exif = (TiffImageMetadata)meta;
		    } else {
		        return;
		    }
		    TiffOutputSet outputSet = exif.getOutputSet();
		    TiffOutputDirectory exifDir = outputSet.getOrCreateExifDirectory();
		    exifDir.getFields()
		} catch (ImageReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImageWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    /*
	    try {				
	    	File file = new File(getFilePathFromContentUri(uri,this.getContentResolver()));
	    	boolean abc = file.exists();
	    	IImageMetadata metadata;
	    	metadata = Sanselan.getMetadata(file);
	    	if (metadata != null)
	    	{
	    		JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
	    		TiffField field = jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_USER_COMMENT);
	    		if (field != null){
	    			String str = field.getValueDescription();
	    			str = str.replaceAll("'", "");    //�h����޸�
	    			String[] split = str.split(";");  //alpha beta����
	    			if (split.length == 2)
	    			{
	    				read_aAngle = Double.parseDouble(split[0]);
	    				read_bAngle = Double.parseDouble(split[1]);
	    			}
	    			Toast.makeText(this, "Alpha=" +read_aAngle+",Beta=" +read_bAngle , 1000).show(); 
	    		}
	    		else
	    			Toast.makeText(this, "No MetaData" , 1000).show(); 
	    		
	    	}
	    	else
	    		Toast.makeText(this, "No MetaData" , 1000).show(); 
	    } catch (ImageReadException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    }  
	    */

		//�p��ABCD�|�I�b�Ŷ��W�������y��
		pointA_x =(float) 0.0;
		pointA_y =(float) 0.0;
		pointB_x =(float) (W/Math.cos(Math.toRadians(aAngle)));
		pointB_y =(float) (W * Math.tan(Math.toRadians(aAngle)) * Math.tan(Math.toRadians(bAngle)));
		pointC_x =(float) (W/Math.cos(Math.toRadians(aAngle)));
		pointC_y =(float) (H/Math.cos(Math.toRadians(bAngle))+W * Math.tan(Math.toRadians(aAngle)) * Math.tan(Math.toRadians(bAngle)));
		pointD_x =(float) 0.0;
		pointD_y =(float) (H/Math.cos(Math.toRadians(bAngle)));

		//�p�G�|�I�����
        //ABCD�b����v�����y��
		if (mLaserPointList.size()==4)
		{
			pointA_u = mLaserPointList.get(0).x;
			pointA_v = mLaserPointList.get(0).y;
			pointB_u = mLaserPointList.get(1).x;
			pointB_v = mLaserPointList.get(1).y;
			pointC_u = mLaserPointList.get(2).x;
			pointC_v = mLaserPointList.get(2).y;
			pointD_u = mLaserPointList.get(3).x;
			pointD_v = mLaserPointList.get(3).y;


			//�D��2D-DLT���Y��
			float[] b={pointA_u,pointB_u,pointC_u,pointD_u,pointA_v,pointB_v,pointC_v,pointD_v};
			mBB.put(0, 0, b);
			float[] a={pointA_x,pointA_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointA_u*pointA_x,-pointA_u*pointA_y,
					pointB_x,pointB_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointB_u*pointB_x,-pointB_u*pointB_y,
					pointC_x,pointC_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointC_u*pointC_x,-pointC_u*pointC_y,
					pointD_x,pointD_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointD_u*pointD_x,-pointD_u*pointD_y,
					(float)0.0,(float)0.0,(float)0.0,pointA_x,pointA_y,(float)1.0,-pointA_v*pointA_x,-pointA_v*pointA_y,
				(float)0.0,(float)0.0,(float)0.0,pointB_x,pointB_y,(float)1.0,-pointB_v*pointB_x,-pointB_v*pointB_y,
				(float)0.0,(float)0.0,(float)0.0,pointC_x,pointC_y,(float)1.0,-pointC_v*pointC_x,-pointC_v*pointC_y,
				(float)0.0,(float)0.0,(float)0.0,pointD_x,pointD_y,(float)1.0,-pointD_v*pointD_x,-pointD_v*pointD_y};

			mAA.put(0, 0, a);
			Core.solve(mAA, mBB, mXX);
			float[] x={0,0,0,0,0,0,0,0};
			mXX.get(0, 0, x);
			System.out.println(Double.toString(x[0]));
			System.out.println(Double.toString(x[1]));
			System.out.println(Double.toString(x[2]));
			System.out.println(Double.toString(x[3]));
			System.out.println(Double.toString(x[4]));
			System.out.println(Double.toString(x[5]));
			System.out.println(Double.toString(x[6]));
			System.out.println(Double.toString(x[7]));

			//DLT�g�J�ɮ�
			saveDLTParameter(x);

			key2=false;
		}else
		{
			runOnUiThread(new Runnable() {
                public void run() {
                	Toast.makeText(ImageShowActivity.this, "DLT�ѼƨS�����\", Toast.LENGTH_SHORT).show();;
                }
            });
		}
	}

	private void setDLTAutoScale(){
		if (mLaserPointList.size()!=4)
		{
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(ImageShowActivity.this, "Scale�ѼƳ]�w�����\!!", Toast.LENGTH_LONG);
				}
			});
			return;
		}
		//�۰ʰ���mSale,mShiftX,mShiftY
		float sx =(pointB_u -pointA_u)/(pointB_x -pointA_x); // �v���W�e��/��ڼe�� or �v���W����/��ڪ���
		float sy =(pointA_v -pointD_v)/(pointD_y -pointA_y); // �v���W�e��/��ڼe�� or �v���W����/��ڪ���
		if (sx>sy)
			mScale = sy;
	    else
			mScale = sx;

		mShiftX = (int)pointA_u; //�Z�����I����
		mShiftY = ortho_bf.height() - (int)pointA_v;

		runOnUiThread(new Runnable() {
			public void run() {
				DecimalFormat df = new DecimalFormat("#.##");
				mScaleText.setText(df.format(mScale));
				mShiftXText.setText(df.format(mShiftX));
				mShiftYText.setText(df.format(mShiftY));
				Toast.makeText(ImageShowActivity.this, "Scale�ѼƦ��\", Toast.LENGTH_SHORT).show();
			}
		});

	}

	private void getDLTImage(){
       	//Ū��DLT�Y�ơA�i�楿���ഫ
		//Ū����l�v��
		Utils.bitmapToMat(mBackupBitmap, ortho_bf);
            //�@��@��Ū���ɮ�
			try {
				String path = Environment.getExternalStorageDirectory().toString ()+"/Crackdata";
       		    File InFile1 = new File(path,"DLT_Coefficients.dat");
       		   // Log.i("!!!!!!!!!!!!!!!!!!!", path);
       		    Scanner sc = new Scanner(InFile1);
				String input0 = sc.nextLine();
				LL[0]=(float)Double.parseDouble(input0);
				String input1 = sc.nextLine();
				LL[1]=(float)Double.parseDouble(input1);
				String input2 = sc.nextLine();
				LL[2]=(float)Double.parseDouble(input2);
				String input3 = sc.nextLine();
				LL[3]=(float)Double.parseDouble(input3);
				String input4 = sc.nextLine();
				LL[4]=(float)Double.parseDouble(input4);
				String input5 = sc.nextLine();
				LL[5]=(float)Double.parseDouble(input5);
				String input6 = sc.nextLine();
				LL[6]=(float)Double.parseDouble(input6);
				String input7 = sc.nextLine();
				LL[7]=(float)Double.parseDouble(input7);
				sc.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//��ܶi��bar
			nProgressStatus = 0;                                        //�i��bar�����A
			mProgressDialog = new ProgressDialog(ImageShowActivity.this);                    //���ͷs��bar
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//�i�׽ժ�����
			mProgressDialog.setTitle("�B�z��");
			mProgressDialog.setProgress(0);
			mProgressDialog.setMax(100);
			mProgressDialog.show();
			new Thread() {
				@Override
				public void run() {
					//mapping ��ڪ��y�Ш�i=ortho_bf.cols(),j=ortho_bf.rows(),�Y1mm=1pixel=1��i��j
					//���o�˼v���Ӥp�A�ҥH�O1��i��j=1/scale mm (�v����j�T��)�A�B����(400,200)pixel
					//�o�˴N��N�ഫ�᪺�v���e�ǩ�(1280*760)�v����
					//long startTime = System.currentTimeMillis();
					Mat bf_gray = new Mat();
					Imgproc.cvtColor(ortho_bf,bf_gray,Imgproc.COLOR_BGRA2GRAY, 1);
					//int bf_widthstep = (bf_gray.width()+3)/4*4;
					//int af_widthstep = (ortho_af.width()+3)/4*4;
					int bf_widthstep = bf_gray.width()* bf_gray.channels();
					int af_widthstep = ortho_af.width()* ortho_af.channels();
					float xx,yy;
					int uu,vv;
					//float shift_x=1000,shift_y=1000;
					//float scale=(float)0.5;
			        int bf_size = (int) (bf_gray.total() * bf_gray.channels());
			        int af_size = (int) (ortho_af.total() * ortho_af.channels());
			        byte[] af_buff = new byte[af_size];
			        byte[] bf_buff = new byte[bf_size];
			        bf_gray.get(0, 0, bf_buff);

					for (int i=0;i<af_size;i++)
						af_buff[i] = (byte)255;

		 	        for (int j=0;j<ortho_af.rows();j++) {
		 	        	for (int i=0;i<ortho_af.cols();i++) {
			        		xx = ((float)i-(float)mShiftX)/mScale;
		 	        		yy = ((float)(ortho_bf.rows()-j)-(float)mShiftY)/mScale;
		 	        	    uu = (int) ((LL[0]*xx+LL[1]*yy+LL[2])/(LL[6]*xx+LL[7]*yy+1));
		           		    vv = (int) ((LL[3]*xx+LL[4]*yy+LL[5])/(LL[6]*xx+LL[7]*yy+1));
		          	        if (uu>=0 +2 && uu<ortho_bf.cols()-2 && vv>=0 +2 && vv<ortho_bf.rows()-2){

								//************************bicubic start*********************
								int  width = ortho_bf.width();
								int ix = (int)uu;
								int iy = (int)vv;
								double p = uu - ix; // sub-pixel offset in the x axis
								double q = vv - iy; // sub-pixel offset in the y axis
								// Pixel neighborhood N;
								int dimN = 4;
								int[] N = new int[dimN*dimN];
								int offset = (iy - 1) * width + (ix - 1); // offset to the top-left neighbor

								//1. FILL IN THE NEIGHBOR SET "N[0] to N[15]" HERE
								for(int ii=0; ii<dimN; ii++)
								{
									for(int jj=0; jj<dimN; jj++)
									{
										N[ii+jj*(ii+1)] = bf_buff[(int)(offset + jj*p + ii*q*width)];
										//N[i+j*(i+1)] = (byte) bf_buff[uu + vv*bf_widthstep];
									}
								}

								int result = 0;
								// loop over channels; r,g,b
								for (int ii=0; ii<3; ++ii) {
									// interpolate in the x direction using the neighbor set N
									// 2. COMPUTE THE INTERPOLATION RESULTS IN THE X-DIRECTION HERE
									// interpolate each line
									double[] L = new double[dimN];
									for(int jj=0; jj<dimN; jj++)
									{
										L[jj] += interpolate_1D(	channel(N[jj*dimN],ii),
												channel(N[jj*dimN+1],ii),
												channel(N[jj*dimN+2],ii),
												channel(N[jj*dimN+3],ii),p) ;
									}
									// interpolate the x-axis results in the y direction and add to "result"
									double interp_res = interpolate_1D(L[0], L[1], L[2], L[3], q) ;// 3. FILL IN THE INTERPOLATION RESULT IN THE Y-DIRECTION

									result += (int)interp_res << (ii*8);
								}
								//*************************bicubic end*******************************
								af_buff[i + j*af_widthstep] = (byte)result;

								/*
								//*************************���u�ʤ���*************************
								double x1 = (Math.floor(uu)+1.0-uu)*bf_buff[uu + vv*bf_widthstep] + (uu -  Math.floor(uu)) *bf_buff[uu+1 + vv*bf_widthstep];//x1��V����
								double x2 = (Math.floor(uu)+1.0-uu)*bf_buff[uu + (vv+1)*bf_widthstep] + (uu -  Math.floor(uu)) * bf_buff[uu+1 + (vv+1)*bf_widthstep];//x2��V����
								double y1 =  (Math.floor(vv)+1.0-vv)*x1 + (vv -  Math.floor(vv)) *x2;//y��V����
								af_buff[i + j*af_widthstep] = (byte)y1;*/

								//af_buff[i + j*af_widthstep] = (byte) bf_buff[uu + vv*bf_widthstep];
		          		    }
			        	}
		 	        	//---�^�Ƕi�ױ�----
		 	        	nProgressStatus = (int) (((j+1)*100)/(double)ortho_bf.rows());//�B�z�i��bar
		 	        	Message msg = new Message();
		 	        	msg.what = nProgressStatus;
		 	        	handler.sendMessage(msg);
		 	        	//-------------
			        }
		 	        /*
			        //���ܬ��I����m 
			        for (int k = 0;k<mLaserPointList.size();k++)
			        {
			       		int i = (int) mLaserPointList.get(k).x;
			       		int j = (int) mLaserPointList.get(k).y;
			        	xx = ((float)i-shift_x)/mScale;
	 	        		yy = ((float)(ortho_bf.rows()-j)-shift_y)/mScale;
	 	        	    uu = (int) ((LL[0]*xx+LL[1]*yy+LL[2])/(LL[6]*xx+LL[7]*yy+1));
	           		    vv = (int) ((LL[3]*xx+LL[4]*yy+LL[5])/(LL[6]*xx+LL[7]*yy+1));
	          	        if (uu>=0 && uu<ortho_bf.cols() && vv>=0 && vv<ortho_bf.rows())
	          	        	mDLTLaserPointList.add(new PointF(uu,vv));
	          	        else
	          	        	mDLTLaserPointList.add(new PointF(0,0));	
	          	      
			        }
			        iv.vecDLTLaserPointList = mDLTLaserPointList;*/

			        ortho_af.put(0, 0, af_buff);
			        bf_gray.release();
			        //mLaserPointList.clear();

			        //long endTime = System.currentTimeMillis();
			        //long totTime = endTime - startTime;
			        //mLogText.setText("time:"+Long.toString(totTime));

				    //Mat�ഫ��Bitmap��iv��,����Jhandler��
					Utils.matToBitmap(ortho_af, mBitmap);
					Message msg = new Message();
                    msg.what = -1;
	 	        	handler.sendMessage(msg);
				}
			}.start();

	   //iv.setImageBitmap(mBitmap);
	}
	double channel(int c, int i) {
		return (double) ((c >> (8*i)) & 0xFF);
	}
	public double interpolate_1D(double N0, double N1, double N2, double N3, double t) {

		double c0 = 2*N1;
		double c1 = N2-N0;
		double c2 = 2*N0-5*N1+4*N2-N3;
		double c3 = -N0+3*N1-3*N2+N3;
		/**** 5. FILL IN THE CUBIC RESULT HERE ****/
		double res = (c3*t*t*t + c2*t*t + c1*t + c0)/2;
		res = Math.round(res);

		// Check for oveflow and underflow in computations
		if(res < 0) res = 0;
		if(res > 255) res = 255;

		return res;
	}
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
            //�B�zthread��,�|�I��UI������
            if (msg.what == -1)
            {
            	iv.setImageBitmap(mBitmap);
            	mProgressDialog.cancel();
            }
            else
				mProgressDialog.setProgress(msg.what);
		}
	};

	private void getLaserDetectionPoint() {
		//Ū�Ӥ�
		Mat img = new Mat();
		Utils.bitmapToMat(mBackupBitmap, img);
		mLaserPointList = new ArrayList ();
		//�ŧi�v���ܼ�
		int w,h;
		w = img.width();
		h = img.height();
		Mat imgR8u = null;
		Mat imgG8u = null;
		Mat imgB8u = null;
		Mat temp8u =  new Mat(h, w, CvType.CV_8UC1);

		Mat imgR =  new Mat(h, w, CvType.CV_32FC1);
		Mat imgG =  new Mat(h, w, CvType.CV_32FC1);
		Mat imgB =  new Mat(h, w, CvType.CV_32FC1);
		Mat temp =  new Mat(h, w, CvType.CV_32FC1);
		Mat temp1 =  new Mat(h, w, CvType.CV_32FC1);
		Mat temp2 =  new Mat(h, w, CvType.CV_32FC1);
		Mat temp3 =  new Mat(h, w, CvType.CV_32FC1);
		Mat temp4 =  new Mat(h, w, CvType.CV_32FC1);

		//�q�D����
		List<Mat> listImg = new ArrayList<Mat>(3);
		Core.split(img, listImg);
		if (bGreenLaser)//R->0 G->1 G ->2,R��G��լ����I����
		{
			imgR8u = listImg.get(1);
			imgG8u = listImg.get(0);
			imgB8u = listImg.get(2);
		}else{
			imgR8u = listImg.get(0);
			imgG8u = listImg.get(1);
			imgB8u = listImg.get(2);
		}
    	//8U->32F
    	//Core.convertScaleAbs(imgR8u, imgR);
    	//Core.convertScaleAbs(imgG8u, imgG);
    	//Core.convertScaleAbs(imgB8u, imgB);
		imgR8u.convertTo(imgR, CvType.CV_32FC1);
		imgG8u.convertTo(imgG, CvType.CV_32FC1);
		imgB8u.convertTo(imgB, CvType.CV_32FC1);

    	//�������W���Ѽ�
        float fR, fRRGB, fGRGB, fBRGB, fMinArea;
        fR = Float.parseFloat(mRText.getText().toString());
        fRRGB = Float.parseFloat(mRRGBText.getText().toString());
        fGRGB = Float.parseFloat(mGRGBText.getText().toString());
        fBRGB = Float.parseFloat(mBRGBText.getText().toString());
        fMinArea = Float.parseFloat(mAreaText.getText().toString());

        //�����e1
        //Imgproc.threshold(imgR,temp1,fR,255,Imgproc.THRESH_BINARY_INV); //��k1
        int size = (int) (imgR.total() * imgR.channels());                //��k2
        float[] buff = new float[size];
        imgR.get(0, 0, buff);
        for (int i = 0; i < size; i++)
        	if (buff[i] > fR)
        		buff[i] = (float) 255;
        	else
        		buff[i] = (float) 0;
        	//buff[i] = (byte) (255 - buff[i]);
        temp1.put(0, 0, buff);
        //�����e2
        Core.add(imgR,imgG,temp);
        Core.add(temp,imgB,temp);
        Core.add(temp, new Scalar(1,1,1), temp);
        Core.divide(imgR,temp,temp2);
        Imgproc.threshold(temp2, temp2,fRRGB,255,Imgproc.THRESH_BINARY);
        //�����e3
        Core.divide(imgG,temp,temp3);
        Imgproc.threshold(temp3,temp3,fGRGB,255,Imgproc.THRESH_BINARY_INV);
        //�����e4
        Core.divide(imgB,temp,temp4);
        Imgproc.threshold(temp4,temp4,fBRGB,255,Imgproc.THRESH_BINARY_INV);
        //�|�Ӫ��e��and�涰
        Core.bitwise_and(temp1,temp2,temp2);
        Core.bitwise_and(temp2,temp3,temp3);
        Core.bitwise_and(temp3,temp4,temp);
        //�ഫ�v����8u
        temp.convertTo(temp8u, CvType.CV_8UC1);
        
        /*
        //���Ƕ����e
        Mat gray = new Mat();
		Imgproc.cvtColor(img,gray,Imgproc.COLOR_BGRA2GRAY, 1);
        Imgproc.threshold(gray,temp8u,230,255,Imgproc.THRESH_BINARY); //��k1,�w����I
        */
        
        /*
        //   debug��
        Bitmap bitmap = Bitmap.createBitmap(temp8u.cols(), temp8u.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(temp8u, bitmap);
        iv.setImageBitmap(bitmap);
        */ 
        
        /*
        //Image inverse �W�C
        for (int i=0;i<temp.rows();i++) { 
        	for (int j=0;j<temp.cols();j++) { 
        		double[] data = temp.get(i, j); 
        		data[0] = (255 - data[0]);
        		temp.put(i, j, data); 
        	} 
        }*/
        /*
        //Image inverse ����֪���k
        int size = (int) (temp.total() * temp.channels());
        byte[] buff = new byte[size];
        temp.get(0, 0, buff);
        for (int i = 0; i < size; i++)
        	buff[i] = (byte) (255- buff[i]);
        temp.put(0, 0, buff); */

        //��X�Ҧ���������
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(temp8u, contours, new Mat(), Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
        for (int i=0; i< contours.size();i++)
        	if (Imgproc.contourArea(contours.get(i)) > fMinArea )
        	{
        		Rect rect = Imgproc.boundingRect(contours.get(i));
        		PointF point = new PointF(rect.x + rect.width/2, rect.y + rect.height/2);
        		mLaserPointList.add(point);
        	}
        //release�O����
        contours.clear();
        imgR8u.release();
        imgG8u.release();
        imgB8u.release();
        temp8u.release();
        imgR.release();
        imgG.release();
        imgB.release();
        temp.release();
        temp1.release();
        temp2.release();
        temp3.release();
        temp4.release();

        //�ƦCABCD�|�I��m
        //getSortLaserPostion(mLaserPointList);
        getSortLaserPostionForSort(mLaserPointList);

        //�o��DLT�Ѽ�
        getDLTParameter();

        //�y�Х��iv�W
        iv.vecLaserPointList = mLaserPointList;

	}
	public static void getSortLaserPostionForSort(ArrayList<PointF> list){
        if (list.size() != 4)
            return;

        PointF pointA = new PointF();
        PointF pointB = new PointF();
        PointF pointC = new PointF();
        PointF pointD = new PointF();
        //�̥��U�����OA�I
        //��sort x����AD���I,�b�P�_Y���oAD,�t�~�����IBC�z�LY���o
        Collections.sort(list, new SortByX());
        if (list.get(0).y > list.get(1).y)
        {
        	pointA = list.get(0);
        	pointD = list.get(1);
        }
        else
        {
        	pointA = list.get(1);
        	pointD = list.get(0);
        }
        if (list.get(2).y > list.get(3).y)
        {
        	pointB = list.get(2);
        	pointC = list.get(3);
        }
        else
        {
        	pointB = list.get(3);
        	pointC = list.get(2);
        }
		list.clear();
		list.add(pointA);
		list.add(pointB);
		list.add(pointC);
		list.add(pointD);

	}
	static class SortByX  implements Comparator<PointF> {

		public int compare(final PointF a, final PointF b) {
			if (a.x < b.x) {
				return -1;
			}
			else if (a.x > b.x) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}
	static class SortByY  implements Comparator<PointF> {

		public int compare(final PointF a, final PointF b) {
			if (a.y < b.y) {
				return -1;
			}
			else if (a.y > b.y) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	private void getSortLaserPostion(ArrayList<PointF> list){
        if (list.size() != 4)
            return;

		//�p��A B C D�|�I����m
		PointF pointA = new PointF();
		float tmp_u=0;
		float tmp_v=0;
		float tmp1_u=0;
		float tmp1_v=0;
		float tmp2_u=0;
		float tmp2_v=0;
		float tmp3_u=0;
		float tmp3_v=0;
		float tmp4_u=0;
		float tmp4_v=0;
/*
		float pointA_u=0;
		float pointA_v=0;
		float pointB_u=0;
		float pointB_v=0;
		float pointC_u=0;
		float pointC_v=0;
		float pointD_u=0;
		float pointD_v=0;*/

		float length_12=0;
		float length_13=0;
		float length_14=0;

		float x1=0;
		float x2=0;
		float y1=0;
		float y2=0;

		pointA=(PointF) mLaserPointList.get(0); //�n��(PointF)�Nobject�ରpoint���榡
		tmp1_u=pointA.x;
		tmp1_v=pointA.y;
		pointA=(PointF) mLaserPointList.get(1);
		tmp2_u=pointA.x;
		tmp2_v=pointA.y;
		pointA=(PointF) mLaserPointList.get(2);
		tmp3_u=pointA.x;
		tmp3_v=pointA.y;
		pointA=(PointF) mLaserPointList.get(3);
		tmp4_u=pointA.x;
		tmp4_v=pointA.y;


		//���N�̥��䪺�I�s��tmp1_u,tmp1_v
		if (tmp2_u <= tmp1_u) {
			tmp_u=tmp1_u;
			tmp_v=tmp1_v;
			tmp1_u=tmp2_u;
			tmp1_v=tmp2_v;
			tmp2_u=tmp_u;
			tmp2_v=tmp_v;
		}
		if (tmp3_u <= tmp1_u) {
			tmp_u=tmp1_u;
			tmp_v=tmp1_v;
			tmp1_u=tmp3_u;
			tmp1_v=tmp3_v;
			tmp3_u=tmp_u;
			tmp3_v=tmp_v;
		}
		if (tmp4_u <= tmp1_u) {
			tmp_u=tmp1_u;
			tmp_v=tmp1_v;
			tmp1_u=tmp4_u;
			tmp1_v=tmp4_v;
			tmp4_u=tmp_u;
			tmp4_v=tmp_v;
		}


		//tmp1���̥��䪺�I
		x1=tmp1_u; //�﨤�u�������I
		y1=tmp1_v;
		length_12=(tmp1_u-tmp2_u)*(tmp1_u-tmp2_u); //1-2�u�q
		length_13=(tmp1_u-tmp3_u)*(tmp1_u-tmp3_u); //1-3�u�q
		length_14=(tmp1_u-tmp4_u)*(tmp1_u-tmp4_u); //1-4�u�q
		if (length_12 >= length_13 && length_12 >= length_14) {  //1-2�u�q�̪�
			x2=tmp2_u; //�﨤�u���k���I
			y2=tmp2_v;
			if (tmp3_v < (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_13 > length_14){  //3�b�﨤�u�W��    �B    1-3�u�q>1-4�u�q
				pointD_u=tmp1_u; //�̥��I
				pointD_v=tmp1_v;
				pointB_u=tmp2_u; //�﨤�I
				pointB_v=tmp2_v;
				pointC_u=tmp3_u; //�﨤�u�W�I
				pointC_v=tmp3_v;
				pointA_u=tmp4_u; //�﨤�u�U�I
				pointA_v=tmp4_v;
			}
			else if(tmp3_v < (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_13 < length_14){  //3�b�﨤�u�W��    �B     1-3�u�q<1-4�u�q
				pointA_u=tmp1_u; //�̥��I
				pointA_v=tmp1_v;
				pointC_u=tmp2_u; //�﨤�I
				pointC_v=tmp2_v;
				pointD_u=tmp3_u; //�﨤�u�W�I
				pointD_v=tmp3_v;
				pointB_u=tmp4_u; //�﨤�u�U�I
				pointB_v=tmp4_v;
			}
			else if(tmp3_v > (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_13 > length_14){  //3�b�﨤�u�U��    �B    1-3�u�q>1-4�u�q
				pointA_u=tmp1_u; //�̥��I
				pointA_v=tmp1_v;
				pointC_u=tmp2_u; //�﨤�I
				pointC_v=tmp2_v;
				pointB_u=tmp3_u; //�﨤�u�U�I
				pointB_v=tmp3_v;
				pointD_u=tmp4_u; //�﨤�u�W�I
				pointD_v=tmp4_v;
			}
			else if(tmp3_v > (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_13 < length_14){  //3�b�﨤�u�U��    �B    1-3�u�q<1-4�u�q
				pointD_u=tmp1_u; //�̥��I
				pointD_v=tmp1_v;
				pointB_u=tmp2_u; //�﨤�I
				pointB_v=tmp2_v;
				pointA_u=tmp3_u; //�﨤�u�U�I
				pointA_v=tmp3_v;
				pointC_u=tmp4_u; //�﨤�u�W�I
				pointC_v=tmp4_v;
			}
		}
		if (length_13 >= length_12 && length_13 >= length_14) {  //1-3�u�q�̪�
			x2=tmp3_u; //�﨤�u���k���I
			y2=tmp3_v;
			if (tmp2_v < (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 > length_14){  //2�b�﨤�u�W��    �B    1-2�u�q>1-4�u�q
				pointD_u=tmp1_u; //�̥��I
				pointD_v=tmp1_v;
				pointB_u=tmp3_u; //�﨤�I
				pointB_v=tmp3_v;
				pointC_u=tmp2_u; //�﨤�u�W�I
				pointC_v=tmp2_v;
				pointA_u=tmp4_u; //�﨤�u�U�I
				pointA_v=tmp4_v;
			}
			else if(tmp2_v < (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 < length_14){  //2�b�﨤�u�W��    �B     1-2�u�q<1-4�u�q
				pointA_u=tmp1_u; //�̥��I
				pointA_v=tmp1_v;
				pointC_u=tmp3_u; //�﨤�I
				pointC_v=tmp3_v;
				pointD_u=tmp2_u; //�﨤�u�W�I
				pointD_v=tmp2_v;
				pointB_u=tmp4_u; //�﨤�u�U�I
				pointB_v=tmp4_v;
			}
			else if(tmp2_v > (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 > length_14){  //2�b�﨤�u�U��    �B    1-2�u�q>1-4�u�q
				pointA_u=tmp1_u; //�̥��I
				pointA_v=tmp1_v;
				pointC_u=tmp3_u; //�﨤�I
				pointC_v=tmp3_v;
				pointB_u=tmp2_u; //�﨤�u�U�I
				pointB_v=tmp2_v;
				pointD_u=tmp4_u; //�﨤�u�W�I
				pointD_v=tmp4_v;
			}
			else if(tmp2_v > (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_12 < length_14){  //2�b�﨤�u�U��    �B    1-2�u�q<1-4�u�q
				pointD_u=tmp1_u; //�̥��I
				pointD_v=tmp1_v;
				pointB_u=tmp3_u; //�﨤�I
				pointB_v=tmp3_v;
				pointA_u=tmp2_u; //�﨤�u�U�I
				pointA_v=tmp2_v;
				pointC_u=tmp4_u; //�﨤�u�W�I
				pointC_v=tmp4_v;
			}
		}
		if (length_14 >= length_12 && length_14 >= length_13) {  //1-4�u�q�̪�
			x2=tmp4_u; //�﨤�u���k���I
			y2=tmp4_v;
			if (tmp2_v < (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 > length_13){  //2�b�﨤�u�W��    �B    1-2�u�q>1-3�u�q
				pointD_u=tmp1_u; //�̥��I
				pointD_v=tmp1_v;
				pointB_u=tmp4_u; //�﨤�I
				pointB_v=tmp4_v;
				pointC_u=tmp2_u; //�﨤�u�W�I
				pointC_v=tmp2_v;
				pointA_u=tmp3_u; //�﨤�u�U�I
				pointA_v=tmp3_v;
			}
			else if(tmp2_v < (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 < length_13){  //2�b�﨤�u�W��    �B     1-2�u�q<1-3�u�q
				pointA_u=tmp1_u; //�̥��I
				pointA_v=tmp1_v;
				pointC_u=tmp4_u; //�﨤�I
				pointC_v=tmp4_v;
				pointD_u=tmp2_u; //�﨤�u�W�I
				pointD_v=tmp2_v;
				pointB_u=tmp3_u; //�﨤�u�U�I
				pointB_v=tmp3_v;
			}
			else if(tmp2_v > (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 > length_13){  //2�b�﨤�u�U��    �B    1-2�u�q>1-3�u�q
				pointA_u=tmp1_u; //�̥��I
				pointA_v=tmp1_v;
				pointC_u=tmp4_u; //�﨤�I
				pointC_v=tmp4_v;
				pointB_u=tmp2_u; //�﨤�u�U�I
				pointB_v=tmp2_v;
				pointD_u=tmp3_u; //�﨤�u�W�I
				pointD_v=tmp3_v;
			}
			else if(tmp2_v > (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_12 < length_13){  //2�b�﨤�u�U��    �B    1-2�u�q<1-3�u�q
				pointD_u=tmp1_u; //�̥��I
				pointD_v=tmp1_v;
				pointB_u=tmp4_u; //�﨤�I
				pointB_v=tmp4_v;
				pointA_u=tmp2_u; //�﨤�u�U�I
				pointA_v=tmp2_v;
				pointC_u=tmp3_u; //�﨤�u�W�I
				pointC_v=tmp3_v;
			}
		}
		//ABCDList
		list.clear();
		list.add(new PointF(pointA_u, pointA_v));
		list.add(new PointF(pointB_u, pointB_v));
		list.add(new PointF(pointC_u, pointC_v));
		list.add(new PointF(pointD_u, pointD_v));

		//******************************************************************************
		// putText   //�ٵL�k�b�ϤW�Х�A  B  C  D�|�I
		// Point point9 = new Point(); 
		// point9.x=pointA_u;
		// point9.y=pointA_v;
		// Core.putText(mLaser, "A", point9, 8, 3.0, new Scalar(0, 0, 255), 8);
		//********************************************************************************
/*
		//	Ū���p�g��g����alpha,beta
		//�@��@��Ū���ɮ�
		try {
			String path = Environment.getExternalStorageDirectory().toString ()+"/Crackdata";
			File InFile1 = new File(path,"LaserANGLE.dat");
			Scanner sc = new Scanner(InFile1);
			String input = sc.nextLine();
			read_aAngle=Double.parseDouble(input);
			//System.out.println(input);
			String input1 = sc.nextLine();
			read_bAngle=Double.parseDouble(input1);
			//System.out.println(input1);
			sc.close();	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//�p��ABCD�|�I�b�Ŷ��W�������y��
		pointA_x =(float) 0.0;
		pointA_y =(float) 0.0;
		pointB_x =(float) (W/Math.cos(Math.toRadians(aAngle)));
		pointB_y =(float) (W * Math.tan(Math.toRadians(aAngle)) * Math.tan(Math.toRadians(bAngle)));
		pointC_x =(float) (W/Math.cos(Math.toRadians(aAngle)));
		pointC_y =(float) (H/Math.cos(Math.toRadians(bAngle))+W * Math.tan(Math.toRadians(aAngle)) * Math.tan(Math.toRadians(bAngle)));
		pointD_x =(float) 0.0;
		pointD_y =(float) (H/Math.cos(Math.toRadians(bAngle)));  	

		//�D��2D-DLT���Y��
		float[] b={pointA_u,pointB_u,pointC_u,pointD_u,pointA_v,pointB_v,pointC_v,pointD_v};
		mBB.put(0, 0, b);
		float[] a={pointA_x,pointA_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointA_u*pointA_x,-pointA_u*pointA_y,
				pointB_x,pointB_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointB_u*pointB_x,-pointB_u*pointB_y,
				pointC_x,pointC_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointC_u*pointC_x,-pointC_u*pointC_y,
				pointD_x,pointD_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointD_u*pointD_x,-pointD_u*pointD_y,
				(float)0.0,(float)0.0,(float)0.0,pointA_x,pointA_y,(float)1.0,-pointA_v*pointA_x,-pointA_v*pointA_y,
				(float)0.0,(float)0.0,(float)0.0,pointB_x,pointB_y,(float)1.0,-pointB_v*pointB_x,-pointB_v*pointB_y,
				(float)0.0,(float)0.0,(float)0.0,pointC_x,pointC_y,(float)1.0,-pointC_v*pointC_x,-pointC_v*pointC_y,
				(float)0.0,(float)0.0,(float)0.0,pointD_x,pointD_y,(float)1.0,-pointD_v*pointD_x,-pointD_v*pointD_y};
		mAA.put(0, 0, a);
		Core.solve(mAA, mBB, mXX);
		float[] x={0,0,0,0,0,0,0,0};
		mXX.get(0, 0, x);
		System.out.println(Double.toString(x[0]));
		System.out.println(Double.toString(x[1]));
		System.out.println(Double.toString(x[2]));
		System.out.println(Double.toString(x[3]));
		System.out.println(Double.toString(x[4]));
		System.out.println(Double.toString(x[5]));
		System.out.println(Double.toString(x[6]));
		System.out.println(Double.toString(x[7]));



		//output 2D-DLT�Y��
		try {				
			String path = Environment.getExternalStorageDirectory().toString ()+"/Crackdata";
			File outFile1 = new File(path,"DLT_Coefficients.dat"); 
			FileOutputStream fileos1 = new FileOutputStream(outFile1);

			tmpString=Double.toString(x[0])+"\r\n"; 
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[1])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[2])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[3])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[4])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[5])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[6])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[7])+"\r\n"; 
			fileos1.write(tmpString.getBytes());


//			//���Y��	  
//	          double tmptmp;    		  
//     		  //pointA_u, pointA_v
//     		  tmptmp=(x[0]*pointA_x+x[1]*pointA_y+x[2])/(x[6]*pointA_x+x[7]*pointA_y+1);
//     		  tmpString=Double.toString(tmptmp)+"\r\n";  
//     		  fileos1.write(tmpString.getBytes());
//     		  tmptmp=(x[3]*pointA_x+x[4]*pointA_y+x[5])/(x[6]*pointA_x+x[7]*pointA_y+1);
//     		  tmpString=Double.toString(tmptmp)+"\r\n"; 
//     		  fileos1.write(tmpString.getBytes());
//
//     		  //pointB_u, pointB_v
//     		  tmptmp=(x[0]*pointB_x+x[1]*pointB_y+x[2])/(x[6]*pointB_x+x[7]*pointB_y+1);
//     		  tmpString=Double.toString(tmptmp)+"\r\n";  
//     		  fileos1.write(tmpString.getBytes());
//     		  tmptmp=(x[3]*pointB_x+x[4]*pointB_y+x[5])/(x[6]*pointB_x+x[7]*pointB_y+1);
//     		  tmpString=Double.toString(tmptmp)+"\r\n"; 
//     		  fileos1.write(tmpString.getBytes());
			   
			fileos1.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		key2=false;    	*/

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, MENU_AUTO_LASER, 0, "�۰ʰ����p�g");
		menu.add(0, MENU_MANU_LASER, 0, "��ʰ����p�g");
		menu.add(0, MENU_GET_A4_CORNER, 0, "A4�Ȱ���");
		menu.add(0, MENU_GET_DLTIMG, 0, "�i��v���ե�");
		//menu.add(,MENU_DIVIDE_LINE, 0, "--------------------------");
		//menu.add(0, MENU_SEMIAUTO, 0, "�q�����_���");
		menu.add(0, MENU_PROFILE, 0, "�q����T");

		//menu.add(0, MENU_OPENCLOSE_SENSOR, 0, "Open/Close Sensor");
		//menu.add(0, MENU_INIT_AB, 0, "Get InitAlphaBeta");
		//menu.add(0, MENU_GET_AB, 0, "Get AlphaBeta");

		menu.add(0, MENU_OPENCLOSE_GRID,0, "Open/Close Grid");
		menu.add(0, MENU_GET_THRESH, 0, "������_(������)");
		//menu.add(2, MENU_ABOUT, 0, "About");
		menu.add(0, MENU_EXIT, 0, "Exit");

		/*
		//�]�wMenu�Ǧ�r�� ****android�ª������******
		MenuItem item1 = menu.getItem(1);
		SpannableString s1 = new SpannableString("��ʰ����p�g");
		s1.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s1.length(), 0);
		item1.setTitle(s1);

		MenuItem item4 = menu.getItem(4);
		SpannableString s4 = new SpannableString("�q�����I���_");
		s4.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s4.length(), 0);
		item4.setTitle(s4);

		MenuItem item3 = menu.getItem(4);
		SpannableString s3 = new SpannableString("A4����");
		s3.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s3.length(), 0);
		item3.setTitle(s3);*/

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case MENU_OPENCLOSE_SENSOR:
			//�������{��
			//Rotation��T
			if (bShowSensor == false)
			{
				mHandler = new Handler();
				updateTimer = new Timer();
				mClock = new TimerTask() {
					public void run()
					{
						mHandler.post(new Runnable() {
							@Override
							public void run()
							{
								DecimalFormat df4 = new DecimalFormat("#.##");
								String str4 = "Gyroscope: " + df4.format(gyroscopeAngles[0]) + " , " + df4.format(gyroscopeAngles[1]) + " , " + df4.format(gyroscopeAngles[2]);
								String str5 = "Accelerometer:" + df4.format(accelerometerValues[0]) + " , "+ df4.format(accelerometerValues[1]) + " , "+ df4.format(accelerometerValues[2]);
								String str6 = "Angle: " + df4.format(aAngle) + " , " + df4.format(bAngle);
								mRotText.setText(str4);
								mAccText.setText(str5);
								mAngText.setText(str6);

							}
						});
					}
				};
				updateTimer.schedule(mClock, 0, 100);
				bShowSensor = true;
	        }
			else
			{
				updateTimer.cancel();
				mRotText.setText("");
				mAccText.setText("");
				bShowSensor = false;
			}
			break;
		case MENU_INIT_AB:
			//���o initial Alpah Beta
			//���oAlpah Beta
			tmp_aAngle = 0;
			tmp_bAngle = 0;
			for (int i=0;i<31;i++)
			{
			    getAngle();
			}
			count1 = 0;

			//���oInitial Alpha Beta
			aInitAngle = 0;
			bInitAngle = 0;
			gyroscopeValues[0] = 0;
			gyroscopeValues[1] = 0;
			gyroscopeValues[2] = 0;

			//���
			DecimalFormat df2 = new DecimalFormat("#.##");
			String str2 = "Initial Alpha = " + df2.format(aInitAngle) + " ,Beta =" + df2.format(bInitAngle);
			mLogText.setText(str2);
			break;
		case MENU_GET_AB:
			//���oAlpah Beta
			tmp_aAngle = 0;
			tmp_bAngle = 0;
			for (int i=0;i<31;i++)
			    getAngle();

			count1 = 0;

			//���oAlpha�PBeta���t
			//aAngle = aAngle - aInitAngle;
			//bAngle = bAngle - bInitAngle;

			//���
			DecimalFormat df3 = new DecimalFormat("#.##");
			String str3 = "Alpha = " + df3.format(aAngle) + " ,Beta =" + df3.format(bAngle);
			mLogText.setText(str3);

			//�g�J���׼Ц��ɮ�
			saveLaserAngle(aAngle,bAngle);

			break;
		case MENU_AUTO_LASER:
			//�������sensor
			if (bShowSensor)
			{
				updateTimer.cancel();
				mRotText.setText("");
				mAccText.setText("");
				mAngText.setText("");
				bShowSensor = true;
			}
			//��ܦ��L��
			mProgressDialog = new ProgressDialog(ImageShowActivity.this);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setMessage("���椤.....");
			mProgressDialog.show();

			//�D�n���檺�{��
			new Thread()
		      {
		        public void run()
		        {
					//���I�����{��
				    getLaserDetectionPoint();
					//�g�J���I�p�g�v����m
					saveLaserImagePos(mLaserPointList);
					//Auto detect mScale mShiftX mShiftY
					setDLTAutoScale();

					runOnUiThread(new Runnable() {
                        public void run() {
                        	//�����L��
        				    mProgressDialog.dismiss();

        					//��s�e��
        				    iv.invalidate();
                        }
                   });

		        }
		      }.start(); //�}�l��������

			//��s�e��
			iv.isDLTMode = false;
		    iv.invalidate();

			break;
		case MENU_GET_A4_CORNER:
			mA4ThresholdDlg.show();
			//��s�e��
			iv.invalidate();
			break;
		case MENU_MANU_LASER:
			if (bManualLaserPoint == false)
			{
				//�إ߷s��4�I(0,0)��array
				mLaserPointList = new ArrayList ();
				mLaserPointList.add(new PointF(0,0));
				mLaserPointList.add(new PointF(0,0));
				mLaserPointList.add(new PointF(0,0));
				mLaserPointList.add(new PointF(0,0));
				iv.vecLaserPointList = mLaserPointList;
				iv.nLaserPointIndex = nLaserPointIndex;

				mTopButton.setVisibility(View.VISIBLE);
				mDownButton.setVisibility(View.VISIBLE);
				mRightButton.setVisibility(View.VISIBLE);
				mLeftButton.setVisibility(View.VISIBLE);
				mSaveButton.setVisibility(View.VISIBLE);
				mCenterButton.setVisibility(View.VISIBLE);

				iv.isLaserPointMode = true;
				bManualLaserPoint = true;
			}else
			{
				mTopButton.setVisibility(View.GONE);
				mDownButton.setVisibility(View.GONE);
				mRightButton.setVisibility(View.GONE);
				mLeftButton.setVisibility(View.GONE);
				mSaveButton.setVisibility(View.GONE);
				mCenterButton.setVisibility(View.GONE);

				iv.isLaserPointMode = false;
				bManualLaserPoint = false;
			}
			//��s�e��
			iv.isDLTMode = false;
			iv.postInvalidate();
			break;
		case MENU_GET_DLTIMG:
			//DLT Image Transfer
			getDLTImage();

			//�ק窱�A
			mTopButton.setVisibility(View.GONE);
			mDownButton.setVisibility(View.GONE);
			mRightButton.setVisibility(View.GONE);
			mLeftButton.setVisibility(View.GONE);
			mSaveButton.setVisibility(View.GONE);
			mCenterButton.setVisibility(View.GONE);

			iv.isLaserPointMode = false;
			bManualLaserPoint = false;
			iv.isDLTMode = true;
			//mLogText.setText("DLT");
			break;
		case MENU_PROFILE:
			if (iv.crackstart_x==0 && iv.crackstart_y==0 && iv.crackmiddle_x==0 && iv.crackmiddle_y==0 && iv.crackend_x==0 && iv.crackend_y==0 )
			{
				Toast.makeText(this, "�L�I��T�I!!!!", Toast.LENGTH_SHORT).show();
				break;
			}
			//���׭p��
			iv.mLineLength1 = Math.sqrt((iv.crackstart_x-iv.crackmiddle_x)*(iv.crackstart_x-iv.crackmiddle_x) + (iv.crackstart_y-iv.crackmiddle_y)*(iv.crackstart_y-iv.crackmiddle_y));
			iv.mLineLength2 = Math.sqrt((iv.crackmiddle_x-iv.crackend_x)*(iv.crackmiddle_x-iv.crackend_x) + (iv.crackmiddle_y-iv.crackend_y)*(iv.crackmiddle_y-iv.crackend_y));
			iv.mLineLength3 = Math.sqrt((iv.crackstart_x-iv.crackend_x)*(iv.crackstart_x-iv.crackend_x) + (iv.crackstart_y-iv.crackend_y)*(iv.crackstart_y-iv.crackend_y));
			iv.mLineDegree = Math.acos((iv.mLineLength1*iv.mLineLength1+iv.mLineLength2*iv.mLineLength2-iv.mLineLength3*iv.mLineLength3)/(2*iv.mLineLength1*iv.mLineLength2)) * 180 / 3.1415;

			//���׭p��
			iv.mLineLengthScale = iv.mLineLength1 / mScale; // the same with c
			//�������L�p�_�� - 1pixel
			//len_crack = len_crack -1;
			//�ǰe����
			DecimalFormat df_ = new DecimalFormat("#.##");
			String str_ = "�e�� = " +  df_.format(iv.mLineLengthScale) + "mm , ���� = " +  df_.format(iv.mLineDegree);
			mLogText.setText(str_);

            iv.invalidate();
			break;
		case MENU_SEMIAUTO:
			mSemiAuto = new SemiAutoExtraction();
			Mat img = new Mat();
			Mat temp = new Mat();
			Utils.bitmapToMat(mBitmap, img);
			Imgproc.cvtColor(img,temp,Imgproc.COLOR_BGRA2GRAY, 4);
			mSemiAuto.image = temp;
			mSemiAuto.startPoint.x = (float)iv.crackstart_x;
			mSemiAuto.startPoint.y = (float)iv.crackstart_y;
			mSemiAuto.endPoint.x = (float)iv.crackend_x;
			mSemiAuto.endPoint.y = (float)iv.crackend_y;
			mSemiAuto.m_fpNodeLen = Double.parseDouble(mNodeText.getText().toString());   //�qUI�^����
			mSemiAuto.m_fpNormalLen = Double.parseDouble(mNormalText.getText().toString());   //�qUI�^����
			//mSemiAuto.m_fpNodeLen = 20.0f;
			//mSemiAuto.m_fpNormalLen = 20.0f;
			mSemiAuto.m_nSmoothPara = 1;
			mSemiAuto.m_nProfileHeight = 100;
			mSemiAuto.run();

			//�������
			iv.vecSemiAutoBounUpPoint = mSemiAuto.vecBounUpPoint;
			iv.vecSemiAutoBounDownPoint = mSemiAuto.vecBounDownPoint;
			iv.vecSemiAutoNodePoint = mSemiAuto.vecNodePoint;
			iv.vecSemiAutoNodeWidth = mSemiAuto.vecNodeWidth;

			//�ǰe���׻P�e��
			double wid_crack = 0.0f;
			double max_crack = 0.0f;

			for (int i=0;i<iv.vecSemiAutoNodeWidth.size();i++) {
				wid_crack = wid_crack + iv.vecSemiAutoNodeWidth.get(i);
				if (iv.vecSemiAutoNodeWidth.get(i) > max_crack)
					max_crack = iv.vecSemiAutoNodeWidth.get(i);
			}
			if (iv.vecSemiAutoNodeWidth.size()!=0)
				wid_crack = wid_crack/iv.vecSemiAutoNodeWidth.size();

			double len_crack =  mSemiAuto.getCrackLength(mSemiAuto.vecNodePoint);
			DecimalFormat df = new DecimalFormat("#.##");
			String str = "���� = " + df.format(len_crack / mScale) + "mm \n�����e�� = " + df.format(wid_crack / mScale) + "mm \n"+ "�̤j�e�� =  " + df.format(max_crack / mScale) +" mm";
			mLogText.setText(str);

			iv.invalidate();
			break;
		case MENU_OPENCLOSE_GRID:
			if (iv.isGridMode)
				iv.isGridMode = false;
			else
				iv.isGridMode = true;

			iv.invalidate();
			break;
		case MENU_ABOUT:
			//�������{��
			mLogText.setText("��a���t�����P�p�⤤��............");
			break;
		case MENU_EXIT:
			//�������{��
			finish();
			break;
		case MENU_GET_THRESH:
			mThresholdDlg.show();

		    break;
		default:

		}
		return super.onOptionsItemSelected(item);

	}
	/*
	���I�s�u�j�M���_�e��(�s��k)
	 */
	public void lineCalculate2() {
		if (iv.crackstart_x==0 && iv.crackstart_y==0 && iv.crackend_x==0 && iv.crackend_y==0 )
		{
			Toast.makeText(this, "�L�]�w�_���I�y��!!!!", Toast.LENGTH_SHORT).show();
			return;
		}
		//default value
		int nnn = Integer.parseInt(mNnnText.getText().toString());  //�Ѽ�1 default 1000
        double base_line_threshold = Double.parseDouble(mBaseLineThresholdText.getText().toString()); //�Ѽ�2 default 0.2
		int xx[]=new int[nnn];
		int yy[]=new int[nnn];
		int zz[]=new int[nnn];

		ArrayList <PointF> vecLinePos = new ArrayList ();
		ArrayList <Double> vecLineValue = new ArrayList ();
		Mat gray = new Mat();
		Mat img = new Mat();
		Utils.bitmapToMat(mBitmap, img);
		Imgproc.cvtColor(img,gray,Imgproc.COLOR_BGRA2GRAY, 4);
		PointF s = new PointF(iv.crackstart_x, iv.crackstart_y);
		PointF e = new PointF(iv.crackend_x, iv.crackend_y);
		//�����I
		double LL2 = Math.sqrt((s.x - e.x)*(s.x - e.x) +  (s.y - e.y)*(s.y - e.y));
		//�q�_�l�I�}�l�������I 1cm 2cm 3cm.....
		PointF pt;
		double si = Double.parseDouble(mStdText.getText().toString());   //�qUI�^����
		//Imgproc.equalizeHist(gray,gray);//�]�i�H��Imgproc.medianBlur(gray, gray, 5);
		Imgproc.medianBlur(gray, gray, 5);
		ArrayList<Double> vecSortValue = new ArrayList();

		//���u�ʤ���
		for (int i = 0;i<nnn;i++){
			pt = lineInterp(s, e, i*LL2/(double)nnn);
			double x1 = (Math.floor(pt.x)+1.0-pt.x)*gray.get((int)Math.floor(pt.y), (int)Math.floor(pt.x))[0] + (pt.x -  Math.floor(pt.x)) * gray.get((int)Math.floor(pt.y), (int)Math.floor(pt.x)+1)[0];//x1��V����
			double x2 = (Math.floor(pt.x)+1.0-pt.x)*gray.get((int)Math.floor(pt.y)+1, (int)Math.floor(pt.x))[0] + (pt.x -  Math.floor(pt.x)) * gray.get((int)Math.floor(pt.y+1), (int)Math.floor(pt.x)+1)[0];//x2��V����
			double y1 =  (Math.floor(pt.y)+1.0-pt.y)*x1 + (pt.y -  Math.floor(pt.y)) *x2;//y��V����

			vecSortValue.add(255-y1); //inverse image 255<->0�@�¦�=255,�զ�=0
			vecLinePos.add(pt);
			vecLineValue.add(255-y1); //inverse image 255<->0�@�¦�=255,�զ�=0
		}

		//Sorting����˫e20%������ �A���̤j�ȧ@�����
		double endsize = base_line_threshold * nnn;
		double total = 0.0f;
		double max_invzz = 0.0f;
		double base_invzz = 0.0f;
		//Sorting
		Collections.sort(vecSortValue, new Comparator<Double>() {
			@Override
			public int compare(Double f1, Double f2) {  //smallsun->large
			if(f1>f2) {
				return 1;
			}
			return -1;
			}
		});
		//�P�_�����I����
		if (vecSortValue.size()<endsize)	{
			Toast.makeText(this, "�����I����200��,��ĳ�Ԫ�", Toast.LENGTH_SHORT).show();
			endsize = vecSortValue.size();
		}

		//Max Value & average
		for (int i=0;i<nnn;i++){
			if (max_invzz < vecSortValue.get(i))
				max_invzz =  vecSortValue.get(i);
		}

		for (int i=0;i<endsize;i++){
			total+=vecSortValue.get(i);
		}
		base_invzz = total / endsize;

		//diffusion point R & L (=0.5(peak+base))
		double diff_ratio = Double.parseDouble(mDiffRatioText.getText().toString()); //�Ѽ�3: ����ɭץ�  default 0.45f
		double diff_invzz = diff_ratio*(max_invzz-base_invzz)+base_invzz;
		int diff_pt_L=1;
		int diff_pt_R=1;

		//�q����j�M
		int index_L = 0;
		int num = vecLineValue.size();
		for (int i = 3; i < num-3; i++)
		{
			if (vecLineValue.get(i) < diff_invzz && vecLineValue.get(i+1) >= diff_invzz && index_L==0)
			{
				diff_pt_L = i+1; //diffusion pt ��������I
				index_L = 1;
			}
		}

		//�q�k��j�M
		int index_R = 0;
		for (int i = num-3; i > 3; i--)
		{
			if (vecLineValue.get(i) >= diff_invzz && vecLineValue.get(i+1) < diff_invzz && index_R==0)
			{
                diff_pt_R = i; // %diffusion pt ���k����I
				index_R = 1;
			}
		}

		//non-linear container, diffusion �ץ�
		// �p��diffusion point �d�򤺪��e�n (base_invzz�H�W)
		double aaa = Double.parseDouble(mAaaText.getText().toString()); // �Ѽ�4: non-linear factor default:10
		double GH = Double.parseDouble(mGHText.getText().toString()); // �Ѽ�5: �j��GH�~�ץ�(�j��GH�Y�������_�A���ץ�) default :210
		int dw_pt_L = 1;
		int dw_pt_R = nnn;
		double total_dw = 0.0f;
		double dw_A1 = 0.0f;
		for (int i=diff_pt_L; i<diff_pt_R; i++){
			if (vecLineValue.get(i)  > GH) {
				dw_A1 = LL2; //��ܤw�g�O���M�����_�A���ΦA�ɺ�
				total_dw = total_dw + dw_A1;
			}else{
				double GP = vecLineValue.get(i) ;
				double R = (GP - base_invzz) / (GH - base_invzz);
				dw_A1 = (2 + (aaa - 1) * R) * R / (aaa + 1) * LL2 ; //LL2�O�@��di�ҥN��mm; %���_�e(mm)
				total_dw = total_dw + dw_A1;
			}
		}
		total_dw = total_dw / (nnn-1); //�ץ��֥[

		//�аO���_��m
		PointF L = new PointF();
		PointF R = new PointF();
		L.x =  vecLinePos.get(diff_pt_L).x;
		L.y =  vecLinePos.get(diff_pt_L).y;
		R.x =  vecLinePos.get(diff_pt_R).x;
		R.y =  vecLinePos.get(diff_pt_R).y;
        iv.mInterpPTLeft.x = L.x;
        iv.mInterpPTLeft.y =  L.y;
        iv.mInterpPTRight.x =  R.x;
        iv.mInterpPTRight.y =  R.y;

		//���_���׭p��
        double len_crack = total_dw; //�쥻�O�Υ��ץ� �{�b�אּ�ץ���
		//�������L�p�_�� - 1pixel
		//len_crack = len_crack -1;
		//�ǰe����
		DecimalFormat df = new DecimalFormat("#.##");
		String str = "�e�� = " +  df.format(len_crack / mScale) + "mm";
		mLogText.setText(str);

		//�M���O����
		gray.release();
		img.release();
		vecSortValue.clear();
	}
	/*
	 ���I�s�u�j�M���_�e��
	 */
	public void lineCalculate() {
		if (iv.crackstart_x==0 && iv.crackstart_y==0 && iv.crackend_x==0 && iv.crackend_y==0 )
		{
			Toast.makeText(this, "�L�]�w�_���I�y��", Toast.LENGTH_SHORT).show();
			return;
		}
		ArrayList <PointF> vecLinePos = new ArrayList ();
		ArrayList <Double> vecLineValue = new ArrayList ();
		Mat gray = new Mat();
		Mat img = new Mat();
		Utils.bitmapToMat(mBitmap, img);
		Imgproc.cvtColor(img,gray,Imgproc.COLOR_BGRA2GRAY, 4);
		PointF s = new PointF(iv.crackstart_x, iv.crackstart_y);
		PointF e = new PointF(iv.crackend_x, iv.crackend_y);
		//����u�q����
		double len = Math.sqrt((s.x - e.x)*(s.x - e.x) + (s.y - e.y)*(s.y - e.y));
		//�q�_�l�I�}�l�������I 1cm 2cm 3cm.....
		PointF pt;
		double si = Double.parseDouble(mStdText.getText().toString());   //�qUI�^����
		Imgproc.equalizeHist(gray,gray);//�]�i�H��Imgproc.medianBlur(gray, gray, 5);
		ArrayList<Double> vecTotalValue = new ArrayList();

		for (int i=0; i<len*10; i++){
			pt = lineInterp(s, e, i*0.1);

			//�h���˪���K�I������ (2017/05/03�R��)

			//���u�ʤ���
			double x1 = (Math.floor(pt.x)+1.0-pt.x)*gray.get((int)Math.floor(pt.y), (int)Math.floor(pt.x))[0] + (pt.x -  Math.floor(pt.x)) * gray.get((int)Math.floor(pt.y), (int)Math.floor(pt.x)+1)[0];//x1��V����
			double x2 = (Math.floor(pt.x)+1.0-pt.x)*gray.get((int)Math.floor(pt.y)+1, (int)Math.floor(pt.x))[0] + (pt.x -  Math.floor(pt.x)) * gray.get((int)Math.floor(pt.y+1), (int)Math.floor(pt.x)+1)[0];//x2��V����
			double y1 =  (Math.floor(pt.y)+1.0-pt.y)*x1 + (pt.y -  Math.floor(pt.y)) *x2;//y��V����
			vecTotalValue.add(y1);
			vecLinePos.add(pt);
			vecLineValue.add(y1);
		}

		//���_���׭p��
		double len_crack = SemiAutoExtraction.getProfileCrackWidth(vecLineValue,vecLinePos, si, iv.mInterpPTLeft, iv.mInterpPTRight);
		//�������L�p�_�� - 1pixel
		len_crack = len_crack -1;
		//�ǰe����
		DecimalFormat df = new DecimalFormat("#.##");
		String str = "�e�� = " +  df.format(len_crack / mScale) + "mm";
		mLogText.setText(str);

		//�M���O����
		gray.release();
		img.release();
		vecTotalValue.clear();
	}
    /**
     * �u�ʤ��t�k
     * @param start �_�l�I
     * @param end ���I
     * @param length ���t������
     * @return �^�Ǥ��t���I
     */
	public PointF lineInterp(PointF start, PointF end, double length) {
		/*
		if (length == 0)
			return start;

		double dist = 0.0f;
		double x = 0.0f;
		double y = 0.0f;

		//�|�ئ��i�઺���γ��n�Ҽ{
		if (Math.abs(start.x - end.x) > Math.abs(start.y - end.y))
		{
			x = start.x;
			while (dist < length)
			{
				if (start.x < end.x)
					x = x+1;
				else
					x = x-1;
				//�קK������0
				if(end.x == start.x)
					end.x += 1;

				y = ((end.y - start.y) / (end.x - start.x)) * (x - start.x) + start.y;
				dist = calDist(start, new PointF((float)x,(float)y));
			}
		}
		else
		{
				y = start.y;
				while (dist < length)
				{
					if (start.y < end.y)
						y = y+1;
					else
						y = y-1;
	                //�קK������0
					if(end.y == start.y)
						end.y += 1;

					x = ((end.x - start.x) / (end.y - start.y))  * (y - start.y) + start.x;

					dist = calDist(start, new PointF((float)x,(float)y));
				}
		}
		return new PointF((float)x,(float)y);*/
		if (length == 0)
			return start;

		double dist = calDist(start, end);
		if (dist==0)
		   dist=length;
		double x = 0.0f;
		double y = 0.0f;

        x = start.x + (end.x -start.x)*length/dist;
		y = start.y + (end.y -start.y)*length/dist;

		return new PointF((float)x,(float)y);
	}
    /**
     * ���u�Z��
     * @param start �_�l�I
     * @param end ���I
     * @return �Z��
     */
	public double calDist(PointF start,PointF end){
		return Math.sqrt((start.x - end.x)*(start.x - end.x) + (start.y - end.y)*(start.y - end.y));

	}
	private void saveDLTParameter(float[] x){
		//output 2D-DLT�Y��
		try {
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				String path = Environment.getExternalStorageDirectory().toString ()+"/Crackdata";
				File outFile1 = new File(path);
				if( !outFile1.exists() )
					outFile1.mkdirs();

				FileWriter fileos1 = new FileWriter(path + "/DLT_Coefficients.dat");
				fileos1.write(Double.toString(x[0])+"\r\n");
				fileos1.write(Double.toString(x[1])+"\r\n");
				fileos1.write(Double.toString(x[2])+"\r\n");
				fileos1.write(Double.toString(x[3])+"\r\n");
				fileos1.write(Double.toString(x[4])+"\r\n");
				fileos1.write(Double.toString(x[5])+"\r\n");
				fileos1.write(Double.toString(x[6])+"\r\n");
				fileos1.write(Double.toString(x[7])+"\r\n");
				fileos1.close();

				//------------------------------start--------------------------
				//�ƻs�@����PIVdata����� �i�Ω�PIVCamera��
				String path2 = Environment.getExternalStorageDirectory().toString ()+"/PIVdata";
				File outFile2 = new File(path2);
				if( !outFile2.exists() )
					outFile2.mkdirs();

				FileWriter fileos2 = new FileWriter(path2 + "/DLT_Coefficients.dat");
				fileos2.write(Double.toString(x[0])+"\r\n");
				fileos2.write(Double.toString(x[1])+"\r\n");
				fileos2.write(Double.toString(x[2])+"\r\n");
				fileos2.write(Double.toString(x[3])+"\r\n");
				fileos2.write(Double.toString(x[4])+"\r\n");
				fileos2.write(Double.toString(x[5])+"\r\n");
				fileos2.write(Double.toString(x[6])+"\r\n");
				fileos2.write(Double.toString(x[7])+"\r\n");
				fileos2.close();
				//------------------------------end----------------------------

				//���Y��
				/*	  
	              double tmptmp;    		  
     		  //pointA_u, pointA_v
     		  tmptmp=(x[0]*pointA_x+x[1]*pointA_y+x[2])/(x[6]*pointA_x+x[7]*pointA_y+1);
     		  tmpString=Double.toString(tmptmp)+"\r\n";  
     		  fileos1.write(tmpString.getBytes());
     		  tmptmp=(x[3]*pointA_x+x[4]*pointA_y+x[5])/(x[6]*pointA_x+x[7]*pointA_y+1);
     		  tmpString=Double.toString(tmptmp)+"\r\n"; 
     		  fileos1.write(tmpString.getBytes());

     		  //pointB_u, pointB_v
     		  tmptmp=(x[0]*pointB_x+x[1]*pointB_y+x[2])/(x[6]*pointB_x+x[7]*pointB_y+1);
     		  tmpString=Double.toString(tmptmp)+"\r\n";  
     		  fileos1.write(tmpString.getBytes());
     		  tmptmp=(x[3]*pointB_x+x[4]*pointB_y+x[5])/(x[6]*pointB_x+x[7]*pointB_y+1);
     		  tmpString=Double.toString(tmptmp)+"\r\n"; 
     		  fileos1.write(tmpString.getBytes());
				 */

				runOnUiThread(new Runnable() {
	                public void run() {
	                	Toast.makeText(ImageShowActivity.this, "DLT�Ѽ��ɮ׼g�J���\", Toast.LENGTH_SHORT).show();
	                }
	            });
			}
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void saveLaserAngle(double aAng, double bAng){
		//�x�йp�g���׻P�y�Ц��ɮ�
		pointA_x =(float) 0.0;
		pointA_y =(float) 0.0;
		pointB_x =(float) (W/Math.cos(Math.toRadians(aAng)));
		pointB_y =(float) (W * Math.tan(Math.toRadians(aAng)) * Math.tan(Math.toRadians(bAng)));
		pointC_x =(float) (W/Math.cos(Math.toRadians(aAng)));
		pointC_y =(float) (H/Math.cos(Math.toRadians(bAng))+W * Math.tan(Math.toRadians(aAng)) * Math.tan(Math.toRadians(bAng)));
		pointD_x =(float) 0.0;
		pointD_y =(float) (H/Math.cos(Math.toRadians(bAng)));

		//output data
		try {
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				String path = Environment.getExternalStorageDirectory().toString ()+"/Crackdata";
				File outFile1 = new File(path);
				if( !outFile1.exists() )
					outFile1.mkdirs();

				FileWriter fileos1 = new FileWriter(path + "/LaserANGLE.dat");
				fileos1.write(Double.toString(aAng)+"\r\n");
				fileos1.write(Double.toString(bAng)+"\r\n");

				fileos1.write(Double.toString(pointA_x)+"\r\n");
				fileos1.write(Double.toString(pointA_y)+"\r\n");
				fileos1.write(Double.toString(pointB_x)+"\r\n");
				fileos1.write(Double.toString(pointB_y)+"\r\n");
				fileos1.write(Double.toString(pointC_x)+"\r\n");
				fileos1.write(Double.toString(pointC_y)+"\r\n");
				fileos1.write(Double.toString(pointD_x) + "\r\n");
				fileos1.write(Double.toString(pointD_y)+"\r\n");

				fileos1.close();

				//------------------------------start--------------------------
				//�ƻs�@����PIVdata����� �i�Ω�PIVCamera��
				String path2 = Environment.getExternalStorageDirectory().toString ()+"/PIVdata";
				File outFile2 = new File(path2);
				if( !outFile2.exists() )
					outFile2.mkdirs();

				FileWriter fileos2 = new FileWriter(path2 + "/LaserANGLE.dat");
				fileos2.write(Double.toString(aAng)+"\r\n");
				fileos2.write(Double.toString(bAng)+"\r\n");

				fileos2.write(Double.toString(pointA_x)+"\r\n");
				fileos2.write(Double.toString(pointA_y)+"\r\n");
				fileos2.write(Double.toString(pointB_x)+"\r\n");
				fileos2.write(Double.toString(pointB_y)+"\r\n");
				fileos2.write(Double.toString(pointC_x)+"\r\n");
				fileos2.write(Double.toString(pointC_y)+"\r\n");
				fileos2.write(Double.toString(pointD_x)+"\r\n");
				fileos2.write(Double.toString(pointD_y)+"\r\n");

				fileos2.close();
				//------------------------------end----------------------------
				
				/*
			String path = Environment.getExternalStorageDirectory().toString ()+"/";
			FileWriter fw = new FileWriter(path + "/LaserANGLE.txt", true);
		    BufferedWriter fileos1 = new BufferedWriter(fw); //�NBufferedWeiter�PFileWrite���󰵳s��

		    tmpString=Double.toString(aAngle)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(bAngle)+"\r\n";
			fileos1.write(tmpString);

			tmpString=Double.toString(pointA_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointA_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointB_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointB_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointC_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointC_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointD_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointD_y)+"\r\n";
			fileos1.write(tmpString);        

			fileos1.close();*/
				Toast.makeText(this, "�p�g�����ɮ׼g�J���\", Toast.LENGTH_SHORT).show();
			}else
				Toast.makeText(this, "�p�g�����ɮ׼g�J����", Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			Toast.makeText(this, "�p�g�����ɮ׼g�J����", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "�p�g�����ɮ׼g�J����", Toast.LENGTH_SHORT).show();
		}
	}
	private void saveLaserImagePos(ArrayList <PointF> laserpoint){

		if (laserpoint.size()!=4)
		{
			runOnUiThread(new Runnable() {
                public void run() {
                	Toast.makeText(ImageShowActivity.this, "�����X�D�|�Ӭ��I!���g�J�v����m�ɮ�!!", Toast.LENGTH_LONG);
                }
            });
			return;
		}
		//output data
		try {
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				String path = Environment.getExternalStorageDirectory().toString ()+"/Crackdata";
				File outFile1 = new File(path);
				if( !outFile1.exists() )
					outFile1.mkdirs();

				FileWriter fileos1 = new FileWriter(path + "/LaserImagePos.dat");
				for (int i=0;i<laserpoint.size();i++)
				{
					fileos1.write(Double.toString(laserpoint.get(i).x)+"\r\n");
					fileos1.write(Double.toString(laserpoint.get(i).y)+"\r\n");
				}

				fileos1.close();
				//------------------------------start--------------------------
				//�ƻs�@����PIVdata����� �i�Ω�PIVCamera��
				String path2 = Environment.getExternalStorageDirectory().toString ()+"/PIVdata";
				File outFile2 = new File(path2);
				if( !outFile2.exists() )
					outFile2.mkdirs();

				FileWriter fileos2 = new FileWriter(path2 + "/LaserImagePos.dat");
				for (int i=0;i<laserpoint.size();i++)
				{
					fileos2.write(Double.toString(laserpoint.get(i).x)+"\r\n");
					fileos2.write(Double.toString(laserpoint.get(i).y)+"\r\n");
				}

				fileos2.close();
				//------------------------------end----------------------------
				
				/*
			String path = Environment.getExternalStorageDirectory().toString ()+"/";
			FileWriter fw = new FileWriter(path + "/LaserANGLE.txt", true);
		    BufferedWriter fileos1 = new BufferedWriter(fw); //�NBufferedWeiter�PFileWrite���󰵳s��

		    tmpString=Double.toString(aAngle)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(bAngle)+"\r\n";
			fileos1.write(tmpString);

			tmpString=Double.toString(pointA_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointA_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointB_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointB_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointC_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointC_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointD_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointD_y)+"\r\n";
			fileos1.write(tmpString);        

			fileos1.close();*/
				runOnUiThread(new Runnable() {
	                public void run() {
	                	Toast.makeText(ImageShowActivity.this, "���I�v����m�ɮ׼g�J���\", Toast.LENGTH_SHORT).show();;
	                }
	            });
			}else
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ImageShowActivity.this, "���I�v����m�ɮ׼g�J����", Toast.LENGTH_SHORT);
					}
				});
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(ImageShowActivity.this, "���I�v����m�ɮ׼g�J����", Toast.LENGTH_SHORT);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(ImageShowActivity.this, "���I�v����m�ɮ׼g�J����", Toast.LENGTH_SHORT);
				}
			});
		}
	}
	private void saveExif(String filename) throws ImageReadException, ImageWriteException, IOException
	{
        /*
		try {
			ExifInterface exif = new ExifInterface(filename);
			exif.setAttribute("UserComment", "�x�sexit:alpha = 60, beta = 60");
			exif.saveAttributes();
	        String str = exif.getAttribute(ExifInterface.TAG_MAKE);
	        
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "exif�g�J����", 1000).show(); 
			e1.printStackTrace();
		}*/

		OutputStream os = null;
		try
		{
			TiffOutputSet outputSet = null;

			// note that metadata might be null if no metadata is found.
			IImageMetadata metadata = Sanselan.getMetadata(new File(filename));
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (null != jpegMetadata)
			{
				// note that exif might be null if no Exif metadata is found.
				TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif)
				{
					// TiffImageMetadata class is immutable (read-only).
					// TiffOutputSet class represents the Exif data to write.
					//
					// Usually, we want to update existing Exif metadata by
					// changing
					// the values of a few fields, or adding a field.
					// In these cases, it is easiest to use getOutputSet() to
					// start with a "copy" of the fields read from the image.
					outputSet = exif.getOutputSet();
				}
			}

			// if file does not contain any exif metadata, we create an empty
			// set of exif metadata. Otherwise, we keep all of the other
			// existing tags.
			if (null == outputSet)
				outputSet = new TiffOutputSet();

			{
				// Example of how to add a field/tag to the output set.
				//
				// Note that you should first remove the field/tag if it already
				// exists in this directory, or you may end up with duplicate
				// tags. See above.
				//
				// Certain fields/tags are expected in certain Exif directories;
				// Others can occur in more than one directory (and often have a
				// different meaning in different directories).
				//
				// TagInfo constants often contain a description of what
				// directories are associated with a given tag.
				//
				// see
				// org.apache.sanselan.formats.tiff.constants.AllTagConstants
				//
				byte[] bytesUserComment = ExifTagConstants.EXIF_TAG_USER_COMMENT.encodeValue(TiffFieldTypeConstants.FIELD_TYPE_ASCII,"i Love 5566", outputSet.byteOrder);

				TiffOutputField aperture = new TiffOutputField(ExifTagConstants.EXIF_TAG_USER_COMMENT
						                                     ,ExifTagConstants.EXIF_TAG_USER_COMMENT.dataTypes[0]
						                                     ,bytesUserComment.length
						                                     ,bytesUserComment);

				TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
				// make sure to remove old value if present (this method will
				// not fail if the tag does not exist).
				exifDirectory.removeField(TiffConstants.EXIF_TAG_USER_COMMENT);
				exifDirectory.add(aperture);
			}


			// printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);
			File tempFile = new File(Environment.getExternalStorageDirectory()+ "/tempImage.jpg");
			os = new FileOutputStream(tempFile);
			os = new BufferedOutputStream(os);

			try {
				new ExifRewriter().updateExifMetadataLossless(new File(filename), os, outputSet);//�L�k�g�b�P�@���ɮפW��!
			} finally {
				os.close();
				//�N�Ȧs�ɦWRename�����ɦW!
				tempFile.renameTo(new File(filename));
			}
			Toast.makeText(this, "exif�g�J���\", Toast.LENGTH_SHORT).show();
			os.close();
			os = null;
		} finally
		{
			if (os != null)
				try
				{
					os.close();
				} catch (IOException e)
				{

				}
		}

	}
    public static String getFilePathFromContentUri(Uri selectedVideoUri, ContentResolver contentResolver) {
        String filePath;
        String[] filePathColumn = {MediaColumns.DATA};

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);
//      �]�i�ΤU������k����cursor  
//      Cursor cursor = this.context.managedQuery(selectedVideoUri, filePathColumn, null, null, null);  

        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }
    private static Bitmap reSizeBitmap(Bitmap bmp) {
    	//�W�L4096�n�Y�p
		//�W�L2900�n�Y�p
    	int width = bmp.getWidth();
    	int height = bmp.getHeight();
    	Matrix matrix = new Matrix();
    	Bitmap resizedBitmap;
    	if (width>height)
    	{
    		//matrix.postScale(3000/(float)width, 3000/(float)width);
			matrix.postScale(2000/(float)width, 2000/(float)width);
    		resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

    	}else
    	{
    		//matrix.postScale(3000/(float)height, 3000/(float)height);
			matrix.postScale(2000/(float)height, 2000/(float)height);
    		resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
    	}
    	bmp.recycle();
    	System.gc();

    	return resizedBitmap;
    }

	public void setA4Corner(float thresh){
		//�إ߷s��4�I��array
		mLaserPointList = new ArrayList ();
		iv.vecLaserPointList = mLaserPointList;
		iv.nLaserPointIndex = nLaserPointIndex;

		Mat gray = new Mat();
		Mat img = new Mat();
		Mat hsv = new Mat();
		Utils.bitmapToMat(mBitmap, img);
		//Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGRA2GRAY, 1);
		//Imgproc.threshold(gray, gray, thresh, 255, Imgproc.THRESH_BINARY);
		Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV, 3);
		Core.inRange(hsv, new Scalar(thresh, 0, 0), new Scalar(120, 256, 256), gray);  //H:0~1

		Utils.matToBitmap(gray, mBitmap);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		if (contours.size()==0)
			return;

		gray.setTo(new Scalar(0, 0, 0));
		double fMaxArea = 0.0;
		int nMaxIndex = 0;
		for (int i=0; i< contours.size();i++)
			if (Imgproc.contourArea(contours.get(i)) > fMaxArea )
			{
				fMaxArea = Imgproc.contourArea(contours.get(i));
				nMaxIndex = i;
			}

		MatOfPoint2f curContour2f = new MatOfPoint2f(contours.get(nMaxIndex).toArray());
		Imgproc.approxPolyDP(curContour2f, curContour2f, 0.04 * Imgproc.arcLength(curContour2f, true), true);
		Point[] pointArray = curContour2f.toArray();
		if (pointArray.length == 4) {
			for (int i = 0;i<pointArray.length ; i++)
				mLaserPointList.add(new PointF((float)pointArray[i].x,(float)pointArray[i].y));

			Toast.makeText(this, "���A4���|���I", Toast.LENGTH_SHORT).show();
			//Sor 4 points
			getSortLaserPostionForSort(mLaserPointList);
            //�o��DLT�Ѽ�
			getDLTParameter();
			//Auto detect mScale mShiftX mShiftY
			setDLTAutoScale();

			//�y�Х��iv�W
			iv.vecLaserPointList = mLaserPointList;
		}else
			Toast.makeText(this, "�줣��A4�I", Toast.LENGTH_SHORT).show();

		img.release();
		gray.release();
		hsv.release();
	}

}
