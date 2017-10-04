package nchc.measurePhone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.graphics.Point;
import android.graphics.PointF;

public class SemiAutoExtraction {
    public double m_fpNodeLen;
    public double m_fpNormalLen;
    public int m_nSmoothPara;
    public int m_nProfileHeight;
    public Mat image;

    public PointF startPoint = new PointF(0, 0);
    public PointF endPoint = new PointF(0, 0);
    public PointF interpPoint = new PointF(0, 0);

    private ArrayList<PointF> vecNormalPoint = new ArrayList();

    public ArrayList<PointF> vecBounUpPoint = new ArrayList();
    public ArrayList<PointF> vecBounDownPoint = new ArrayList();
    public ArrayList<PointF> vecNodePoint = new ArrayList();
    public ArrayList<Double> vecNodeWidth = new ArrayList();

    public double M;

    public SemiAutoExtraction() {

    }

    /**
     * ���u�Z��
     *
     * @param start �_�l�I
     * @param end   ���I
     * @return �Z��
     */
    public double calDist(PointF start, PointF end) {
        return Math.sqrt((start.x - end.x) * (start.x - end.x) + (start.y - end.y) * (start.y - end.y));

    }

    /**
     * �u�ʤ��t�k
     *
     * @param start  �_�l�I
     * @param end    ���I
     * @param length ���t������
     * @return �^�Ǥ��t���I
     */
    public PointF lineInterp(PointF start, PointF end, double length) {
        /*
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
        if (dist == 0)
            return start;
        //dist=length;

        double x = 0.0f;
        double y = 0.0f;

        x = start.x + (end.x - start.x) * length / dist;
        y = start.y + (end.y - start.y) * length / dist;

        return new PointF((float) x, (float) y);
    }

    /**
     * ���I�󦡧�k�V�q y-b = m(x-a)
     * [fx,fy] ��쪺�k�V�q�I
     *
     * @param point  �I
     * @param M      slope �ײv
     * @param length �ҭn���k�V�q����
     * @return �^���I
     */
    public ArrayList<PointF> getNormal(PointF point, double M, double length) {
        double dist = 0;
        ArrayList<PointF> f = new ArrayList();
        ;

        double a = point.x;
        double b = point.y;
        double d = 0;

        //�|�ئ��i�઺���γ��n�Ҽ{
        if (0 <= Math.abs(M) && Math.abs(M) <= 1) {
            while (dist < length) {
                //�Ĥ@��
                double x = a - d;
                double y = M * (x - a) + b;
                dist = calDist(new PointF((float) a, (float) b), new PointF((float) x, (float) y));
                f.add(0, new PointF((float) x, (float) y));//���J��Ĥ@��
                //fx = [x;fx];
                //fy = [y;fy];
                //�ĤG��
                x = a + d;
                y = M * (x - a) + b;
                dist = calDist(new PointF((float) a, (float) b), new PointF((float) x, (float) y));
                f.add(new PointF((float) x, (float) y));
                //fx = [fx;x];
                //fy = [fy;y];

                d = d + 1;
            }
        } else {
            while (dist < length) {
                //�ĤT��
                double y = b - d;
                double x = (y - b) / M + a;
                dist = calDist(new PointF((float) a, (float) b), new PointF((float) x, (float) y));
                f.add(0, new PointF((float) x, (float) y));
                //fx = [x;fx];
                //fy = [y;fy];
                //�ĥ|��
                y = b + d;
                x = (y - b) / M + a;
                dist = calDist(new PointF((float) a, (float) b), new PointF((float) x, (float) y));
                f.add(new PointF((float) x, (float) y));
                //fx = [fx;x];
                //fy = [fy;y];

                d = d + 1;
            }
        }
        return f;
    }

    /**
     * ���t�ܤlpixel
     *
     * @param arr ��v�v��
     * @param loc pixel��m
     * @return pixel��m
     */
    public PointF getSubpixel(Mat arr, Point loc) {
        //x��V��subpixel
        double[] if1;
        double[] f0;
        double[] f1;
        double dx, dy;
        PointF cp = new PointF(0.0f, 0.0f);
        if (loc.x - 1 < 0 || loc.x + 1 > image.width())                  //�P�_�W�X���
        {
            cp.x = loc.x;
        } else {
            if1 = arr.get(loc.y, loc.x - 1);
            f0 = arr.get(loc.y, loc.x);
            f1 = arr.get(loc.y, loc.x + 1);

            dx = (f1[0] - if1[0]) / (2 * (2 * f0[0] - if1[0] - f1[0]));
            if (dx < -1 || dx > 1)                                                   //�W�X�@��pixel�k0
                dx = 0;

            cp.x = (float) (loc.x + dx);
        }

        //y��V��subpixel
        if (loc.y - 1 < 0 || loc.y + 1 > arr.height())               //�P�_�W�X���
        {
            cp.y = loc.y;
        } else {
            if1 = arr.get(loc.y - 1, loc.x);
            f0 = arr.get(loc.y, loc.x);
            f1 = arr.get(loc.y + 1, loc.x);

            dy = (f1[0] - if1[0]) / (2 * (2 * f0[0] - if1[0] - f1[0]));
            if (dy < -1 || dy > 1)                                                 //�W�X�@��pixel�k0
                dy = 0;

            cp.y = (float) (loc.y + dy);
        }
        return cp;
    }

    /**
     * �p�⦹���_������
     *
     * @param vecPoint �x�}�I
     * @return ���_����
     */
    public double getCrackLength(ArrayList<PointF> vecPoint) {
        double length = 0;
        double dist;
        PointF s, e;
        int num = vecPoint.size();
        for (int i = 0; i < num - 1; i++) {
            s = vecPoint.get(i);
            e = vecPoint.get(i + 1);
            dist = Math.sqrt(Math.pow((s.x - e.x), 2) + Math.pow((s.y - e.y), 2));
            length = length + dist;
        }
        return length;

    }

    /**
     * �����۰ʰ���
     */
    public void run() {

        //---- ��l�Ѽ�----
        if (vecNodePoint.size() != 0)
            vecNodePoint.clear();

        if (vecBounUpPoint.size() != 0)
            vecBounUpPoint.clear();

        if (vecBounDownPoint.size() != 0)
            vecBounDownPoint.clear();

        if (vecNodeWidth.size() != 0)
            vecNodeWidth.clear();


        //m_fpNodeLen = 30;
        //m_fpNormalLen = 30;
        int m_nMaxNodePointNum = 1000;

        //------------�DNode�I-----------
        //���t��m
        interpPoint = lineInterp(startPoint, endPoint, m_fpNodeLen);
        //�D�ײvM
        if (endPoint.x == startPoint.x)
            M = -100.0;//�������ײvM���L�u�p
        else if (endPoint.y == startPoint.y)
            //M = 100.0;//�������ײvM���L���j ����0
            M = (endPoint.y - startPoint.y + 0.01) / (endPoint.x - startPoint.x);
        else
            M = (endPoint.y - startPoint.y) / (endPoint.x - startPoint.x);

        //��k�V�q
        if (vecNormalPoint.size() != 0)
            vecNormalPoint.clear();

        if (M == 0)
            M = -100.0;
        vecNormalPoint = getNormal(interpPoint, -1 / M, m_fpNormalLen);

        //�q�k�V�q�y�Ш���pixel
        Mat array = new Mat(1, vecNormalPoint.size(), CvType.CV_8UC1);
        Mat array_smooth = new Mat(1, vecNormalPoint.size(), CvType.CV_8UC1);

        //IplImage * array = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
        //IplImage * array_smooth = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
        int num = vecNormalPoint.size();
        for (int i = 0; i < num; i++) {
            int x = (int) vecNormalPoint.get(i).x;
            int y = (int) vecNormalPoint.get(i).y;
            if (x < 0 || y < 0 || x > image.width() || y > image.height())  //�W�X�v���d��
            {
                array.release();
                array_smooth.release();
                //cvReleaseImage(&array);
                //cvReleaseImage(&array_smooth);
                //ShowMessage("Outside the Image!!");
                return;
            }
            //**************�U�����i��X��*****************
            double[] temp = image.get(y - 1, x - 1);
            //int temp = CV_IMAGE_ELEM(image,uchar,y-1,x-1);

            array.put(0, i, temp);
            //CV_IMAGE_ELEM(array,uchar,0,i) = temp;
        }
        //��Xpeak�I
        //cvSmooth(array, array_smooth, CV_MEDIAN,3,0);
        Point minLoc = new Point(0, 0);
        Point maxLoc = new Point(0, 0);

        MinMaxLocResult mmr = Core.minMaxLoc(array);
        minLoc.x = (int) mmr.minLoc.x;
        minLoc.y = (int) mmr.minLoc.y;
        //cvMinMaxLoc(array,NULL,NULL,&minLoc,NULL,NULL);

        PointF pt = vecNormalPoint.get(minLoc.x);
        vecNodePoint.add(startPoint);
        vecNodePoint.add(pt);

        double end_dist = calDist(pt, endPoint);

        //�M���ŧi��
        array.release();
        array_smooth.release();
        //cvReleaseImage(&array);
        //cvReleaseImage(&array_smooth);

        //�`����Node�I��End�I���פp��Node��
        while (end_dist > m_fpNodeLen) {
            //���t��m
            interpPoint = lineInterp(startPoint, pt, m_fpNodeLen + calDist(startPoint, pt));
            //��������m�W�X�v���d��N���X
            if (interpPoint.x < 0 || interpPoint.y < 0 || interpPoint.x > image.width() - 1 || interpPoint.y > image.height() - 1)  //�W�X�v���d��
            {
                //ShowMessage("Node Outside the Image!!");
                break;
            }
            //Node�ƶq�W�X�]�w��m_nMaxModePointNum ���X�j��
            if (vecNodePoint.size() > m_nMaxNodePointNum)
                break;

            //�D�ײvM
            if (endPoint.x == pt.x)
                M = -100.0;//�������ײvM���L���p
            else if (endPoint.y == pt.y)
                //M = 100.0;//�������ײvM���L���j ����0
                M = (endPoint.y - pt.y + 0.01) / (endPoint.x - pt.x);
            else
                M = (endPoint.y - pt.y) / (endPoint.x - pt.x);

            //��k�V�q
            if (vecNormalPoint.size() != 0)
                vecNormalPoint.clear();

            if (M == 0)
                M = -100.0;
            vecNormalPoint = getNormal(interpPoint, -1 / M, m_fpNormalLen);

            //�q�k�V�q�y�Ш���pixel
            array.create(1, vecNormalPoint.size(), CvType.CV_8UC1);
            array_smooth.create(1, vecNormalPoint.size(), CvType.CV_8UC1);
            //IplImage * array = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
            //IplImage * array_smooth = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
            num = vecNormalPoint.size();
            for (int i = 0; i < num; i++) {
                int x = (int) vecNormalPoint.get(i).x;
                int y = (int) vecNormalPoint.get(i).y;
                if (x < 0 || y < 0 || x > image.width() || y > image.height())  //�W�X�v���d��
                {
                    array.release();
                    array_smooth.release();
                    //cvReleaseImage(&array);
                    //cvReleaseImage(&array_smooth);
                    //ShowMessage("Normal Outside the Image!!");
                    return;
                }
                //**************�U�����i��X��*****************
                double[] temp = image.get(y - 1, x - 1);
                //int temp = CV_IMAGE_ELEM(image,uchar,y-1,x-1);
                array.put(0, i, temp);
                //CV_IMAGE_ELEM(array,uchar,0,i) = temp;
            }
            //��Xpeak�I
            MinMaxLocResult mmr2 = Core.minMaxLoc(array);
            //cvMinMaxLoc(array,NULL,NULL,&minLoc,NULL,NULL);
            minLoc.x = (int) mmr2.minLoc.x;
            minLoc.y = (int) mmr2.minLoc.y;
            pt = vecNormalPoint.get(minLoc.x);
            vecNodePoint.add(pt);

            end_dist = calDist(pt, endPoint);

            //�M���ŧi��
            array.release();
            array_smooth.release();
            //cvReleaseImage(&array);
            //cvReleaseImage(&array_smooth);

        }
        //vecNodePoint.add(endPoint);
        //------------�D���------------

        for (int i = 0; i < vecNodePoint.size() - 1; i++) {
            PointF s = vecNodePoint.get(i);
            PointF e = vecNodePoint.get(i + 1);
            PointF up_pt = new PointF(0.0f, 0.0f);
            PointF down_pt = new PointF(0.0f, 0.0f);

            double dist = calDist(s, e);
            //for (int j = 1; j < dist + 1; j += (dist / 2)) {
            //	//���t��m
            //	interpPoint = lineInterp(s, e, j);
            interpPoint.x = (s.x + e.x) / 2;
            interpPoint.y = (s.y + e.y) / 2;
            //�D�ײvM
            if (e.x == s.x)
                M = -100.0;//�������ײvM���L���p
            else if (e.y == s.y)
                //M = 100.0;//�������ײvM���L���j ����0
                M = (e.y - s.y + 0.01) / (e.x - s.x);
            else
                M = (e.y - s.y) / (e.x - s.x);

            //��k�V�q
            if (vecNormalPoint.size() != 0)
                vecNormalPoint.clear();

            if (M == 0)
                M = -100;

            vecNormalPoint = getNormal(interpPoint, -1 / M, m_nProfileHeight);    //  default:m_fpNodeLen/10

				/*
				//�q�k�V�q�y�Ш���pixel
				array.create(1, vecNormalPoint.size(), CvType.CV_8UC1);
				array_smooth.create(1, vecNormalPoint.size(), CvType.CV_8UC1);
				Mat array_diff = new Mat(1, vecNormalPoint.size(), CvType.CV_32FC1);
				//IplImage * array = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
				//IplImage * array_smooth = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
				//IplImage * array_diff = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_64F,1);
				num = vecNormalPoint.size();
				for (int k=0; k<num; k++)
				{
					int x = (int) vecNormalPoint.get(k).x;
					int y = (int) vecNormalPoint.get(k).y;
	                if (x<0 || y<0 || x>image.width() || y>image.height())  //�W�X�v���d��
	                {
	                	array.release();
	                	array_smooth.release();
	                	array_diff.release();
	                	//cvReleaseImage(&array);
	                    //cvReleaseImage(&array_smooth);
	                    //cvReleaseImage(&array_diff);
						//ShowMessage("Normal Outside the Image!!");
	                    return;
	                }
	              //**************�U�����i��X��*****************
	                double [] temp = image.get(y-1,x-1);
					//int temp = CV_IMAGE_ELEM(image,uchar,y-1,x-1);
                    array.put(0,k,temp);
					//CV_IMAGE_ELEM(array,uchar,0,k) = temp;
				}
				//Imgproc.medianBlur(array, array, 5);
				//cvSmooth(array, array, CV_MEDIAN, m_nSmoothPara,0);
				//��Xpeak�I�����
				for (int k=0; k<array_diff.width() - 1; k++)
				{
					double [] x1 = array.get(0,k);
					double [] x2 = array.get(0,k+1);
					double [] x3 = {0,0,0};  
					x3[0] = x1[0] - x2[0];
					array_diff.put(0,k,x3);
					//CV_IMAGE_ELEM(array_diff,double,0,k) = (double)CV_IMAGE_ELEM(array,uchar,0,k)
					//									- (double)CV_IMAGE_ELEM(array,uchar,0,k+1);
				}
				double [] x4 = {0,0,0};
				array_diff.put(0, 0, x4);
				array_diff.put(0, array_diff.width()-1, x4);
				//CV_IMAGE_ELEM(array_diff,double,0,0) = 0.0;
				//CV_IMAGE_ELEM(array_diff,double,0,array_diff->width - 1) = 0.0;
				MinMaxLocResult mmr2 = Core.minMaxLoc(array_diff);
				minLoc.x = (int) mmr2.minLoc.x; 
				minLoc.y = (int) mmr2.minLoc.y;
				maxLoc.x = (int) mmr2.maxLoc.x;
				maxLoc.y = (int) mmr2.maxLoc.y;
				//cvMinMaxLoc(array_diff,NULL,NULL,&minLoc,&maxLoc,NULL);
				up_pt = vecNormalPoint.get(maxLoc.x);
				down_pt = vecNormalPoint.get(minLoc.x);
				//up_pt = vecNormalPoint[maxLoc.x];
				//down_pt = vecNormalPoint[minLoc.x];
				*/
            //}
            //�q�y��Array->Pixel Array
            ArrayList<Double> vecNewLineValue = new ArrayList();
            ArrayList<PointF> vecNewNormalPoint = new ArrayList();
            for (int k = 0; k < vecNormalPoint.size(); k++) {
                int x = (int) vecNormalPoint.get(k).x;
                int y = (int) vecNormalPoint.get(k).y;
                if (x >= 0 && y >= 0 && x < image.width() - 1 && y < image.height() - 1)  //�W�X�v���d��
                {
                    vecNewLineValue.add(image.get(y, x)[0]);
                    vecNewNormalPoint.add(vecNormalPoint.get(k));
                    //vecLineValue.add(new PointF(vecNormalPoint.get(k.x, vecNormalPoint.get(k).y));
                }
            }
            double crackwidth = getProfileCrackWidth(vecNewLineValue, vecNewNormalPoint, 0.7f, up_pt, down_pt);

            //�������L�p�_�� - 1pixel
            crackwidth = crackwidth - 1;

            vecNewLineValue.clear();
            vecNewNormalPoint.clear();

            //��ɥ�J�e����
            vecBounUpPoint.add(up_pt);
            vecBounDownPoint.add(down_pt);
            vecNodeWidth.add(crackwidth);
            //}
            //�e��J�e��
            //double h = calDist(up_pt,down_pt);
            //vecNodeWidth.add(h);
        }
    }

    static public double getProfileCrackWidth(ArrayList<Double> vecLineValue, ArrayList<PointF> vecLinePos, double fSTD, PointF ptLeft, PointF ptRight) {
        //���˫e30*10������  (10�Osubpixel)
        ArrayList<Double> vecTotalValue = new ArrayList();
        for (int i = 0; i < vecLineValue.size(); i++)
            vecTotalValue.add(vecLineValue.get(i));

        Collections.sort(vecTotalValue);
        int endsize = 200 * 10;
        if (vecTotalValue.size() < 200 * 10) {
            endsize = vecTotalValue.size();
        }
        if (endsize == 0)
            endsize = 1;

        double total = 0.0f;
        for (int i = 0; i < endsize; i++) {
            total = total + vecTotalValue.get(i);
        }
        double avg = total / endsize;                 //������
        total = 0;
        for (int i = 0; i < endsize; i++)
            total = total + (avg - vecTotalValue.get(i)) * (avg - vecTotalValue.get(i));

        double si = Math.sqrt(total / endsize);    //�зǮt
        //�p����_���d��,��ؤ�k�������I
        int num = vecLineValue.size();
        int posL = 0;
        int posR = 0;
        //double fSTD = 1.0f;         //�w�]�@�ӼзǮt
        //double fSTD = Double.parseDouble(mStdText.getText().toString());
        boolean isMinToBorder = true;

        if (isMinToBorder == true) {   //�Ĥ@�ؤ�k:�ѳ̧C�I�V�~�j�M
            int posMin = 0;
            double fValuse = 255.0;
            for (int i = 1; i < num - 1; i++) {
                if (vecLineValue.get(i) < fValuse)                  //�p��vector�����̤p��
                {
                    fValuse = vecLineValue.get(i);
                    posMin = i;
                }
            }
            for (int i = posMin; i > 1; i--) {
                if (vecLineValue.get(i) > vecLineValue.get(posMin) + si * fSTD)     //�P�_�O�_�j��N�ӼзǮt
                //if (vecLineValue.get(i) > avg - si*fSTD )     //�P�_�O�_�j��N�ӼзǮt
                {
                    posL = i + 1;                                 //�n�[�^��
                    //posL = i;
                    break;
                }
            }
            for (int i = posMin; i < num; i++) {
                if (vecLineValue.get(i) > vecLineValue.get(posMin) + si * fSTD)     //�P�_�O�_�j��N�ӼзǮt
                //if (vecLineValue.get(i) > avg - si*fSTD )     //�P�_�O�_�j��N�ӼзǮt
                {
                    posR = i;                                //�n��^��
                    //posR = i;                                    //�@��ߦ^�Ӥ@��N���Υ[
                    break;
                }
            }
        } else {    //�ĤG�ؤ�k:���Y���I�V���j�M
            for (int i = 3; i < num - 3; i++) {
                if (vecLineValue.get(i) < avg - si * fSTD)     //�P�_�O�_�j��N�ӼзǮt
                {
                    posL = i - 1;                                 //�n��^��
                    //posL = i;                                     //�@��ߦ^�Ӥ@��N���Υ[
                    break;
                }
            }
            for (int i = num - 3; i > 3; i--) {
                if (vecLineValue.get(i) < avg - si * fSTD)     //�P�_�O�_�j��N�ӼзǮt
                {
                    posR = i + 1;                                 //�n�[�^��
                    break;
                }
            }
        }
        //���_������
        PointF L = new PointF();
        PointF R = new PointF();
        boolean isSubpixel = false;
        if (isSubpixel == true)    //�P�_�O�_�ݭn���t��subpixel
        {
            if (vecLineValue.get(posL - 1) == vecLineValue.get(posL) || vecLineValue.get(posR + 1) == vecLineValue.get(posR))   //�קK������0
            {
                L.x = vecLinePos.get(posL).x; //�L����
                L.y = vecLinePos.get(posL).y;
                R.x = vecLinePos.get(posR).x;
                R.y = vecLinePos.get(posR).y;
            } else {
                double tempLX = (vecLineValue.get(posL - 1) - (avg - si * fSTD)) / (vecLineValue.get(posL - 1) - vecLineValue.get(posL) * (vecLinePos.get(posL).x - vecLinePos.get(posL - 1).x));
                double tempLY = (vecLineValue.get(posL - 1) - (avg - si * fSTD)) / (vecLineValue.get(posL - 1) - vecLineValue.get(posL) * (vecLinePos.get(posL).y - vecLinePos.get(posL - 1).y));
                double tempRX = ((avg - si * fSTD) - vecLineValue.get(posR)) / (vecLineValue.get(posR + 1) - vecLineValue.get(posR)) * (vecLinePos.get(posR + 1).x - vecLinePos.get(posR).x);
                double tempRY = ((avg - si * fSTD) - vecLineValue.get(posR)) / (vecLineValue.get(posR + 1) - vecLineValue.get(posR)) * (vecLinePos.get(posR + 1).y - vecLinePos.get(posR).y);
                L.x = (float) (vecLinePos.get(posL - 1).x + tempLX);       //������subpixel
                L.y = (float) (vecLinePos.get(posL - 1).y + tempLY);
                R.x = (float) (vecLinePos.get(posR).x + tempRX);
                R.y = (float) (vecLinePos.get(posR).y + tempRY);
            }
        } else {
            L.x = vecLinePos.get(posL).x;                                                                                                                                           //�L����
            L.y = vecLinePos.get(posL).y;
            R.x = vecLinePos.get(posR).x;
            R.y = vecLinePos.get(posR).y;
        }
        double fLenCrack = Math.sqrt((L.x - R.x) * (L.x - R.x) + (L.y - R.y) * (L.y - R.y));

        ptLeft.x = L.x;
        ptLeft.y = L.y;
        ptRight.x = R.x;
        ptRight.y = R.y;

        return fLenCrack;
    }
}
