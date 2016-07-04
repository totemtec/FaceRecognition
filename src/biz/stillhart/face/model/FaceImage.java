package biz.stillhart.face.model;

import jjil.algorithm.RgbAvgGray;
import jjil.core.Rect;
import jjil.core.RgbImage;
import jjil.j2se.RgbImageJ2se;
import biz.stillhart.face.util.Gray8DetectHaarMultiScale;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *   Copyright 2014 Patrick Stillhart
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
public class FaceImage {

    private InputStream mHaarSource = FaceImage.class.getResourceAsStream("/haar/HCSB.txt");
    private int mMinScale = 1;
    private int mMaxScale = 30;
    private int mNoCropMultiplier = 3;
    private int mAdditionPadding = 40;
    private Dimension mDimension = new Dimension(150, 150);

    private BufferedImage mOriginalFace;
    private BufferedImage mProfileFace;
    private BufferedImage mProfileFaceScaled;

    private Rect mFace;
    private boolean mFoundFace;
    private boolean mChangedSettings;

    /**
     * Creates a new FaceImage object
     * @param inputImage the image containing a biz.stillhart.face
     * @throws IOException couldn't read file or don't contain a biz.stillhart.face
     */
    public FaceImage(BufferedImage inputImage) throws IOException {
        update(inputImage);
    }

    /**
     * Switch current image with a new one
     * @param inputImage The new image
     * @throws IOException couldn't read file or don't contain a biz.stillhart.face
     */
    public void update(BufferedImage inputImage) throws IOException {
        // Reset
        mFace = null;
        mFoundFace = false;
        mChangedSettings = true;

        mOriginalFace = inputImage;
        findFace();
    }

    /**
     * Get original image
     * @return original image
     */
    public BufferedImage getOriginalFace() {
        return mOriginalFace;
    }

    /**
     * Return a square profile picture
     * @return square profile picture
     */
    public BufferedImage getProfileFace() {
        if((mChangedSettings || mProfileFace == null) && mFoundFace) createCutFace();

        return mProfileFace;
    }

    /**
     * Return a square scaled profile picture
     * @return square profile picture
     */
    public BufferedImage getScaledProfileFace() {
        if(mProfileFaceScaled == null) updateScaledImage();

        return mProfileFaceScaled;
    }

    /**
     * Return true if biz.stillhart.face is found
     * @return isFaceFound
     */
    public boolean foundFace() {
        return mFoundFace;
    }

    /**
     * The finest scale -- a scale factor of 1 corresponds to the full image resolution.
     * Default: 1
     * @param minScale The finest scale
     */
    public void setMinScale(int minScale) {
        this.mChangedSettings = true;
        this.mMinScale = minScale;
    }

    /**
     * The coarsest scale. A scale factor equal to the image width (for a square image) would mean the entire image is reduced to a single pixel
     * Default: 30
     * @param maxScale The coarsest scale
     */
    public void setMaxScale(int maxScale) {
        this.mChangedSettings = true;
        this.mMaxScale = maxScale;
    }

    /**
     * Threshold multiplier for when to start cropping a Picture
     * Default: 3
     * @param noCropMultiplier a threshold value
     */
    public void setNoCropMultiplier(int noCropMultiplier) {
        this.mChangedSettings = true;
        this.mNoCropMultiplier = noCropMultiplier;
    }

    /**
     * Additional padding for the cropped profile picture
     * Default: 40
     * @param additionPadding additional padding
     */
    public void setAdditionPadding(int additionPadding) {
        this.mChangedSettings = true;
        this.mAdditionPadding = additionPadding;
    }

    /**
     * Set the scaling factor
     * Default 150, 150
     * @param mDimension the scaling factor
     */
    public void setDimension(Dimension mDimension) {
        this.mDimension = mDimension;
        if(mProfileFaceScaled == null) updateScaledImage();
    }

    /**
     * Set the scaling factor
     * Default 150, 150
     * @param width width
     * @param height height
     */
    public void setDimension(int width, int height) {
        setDimension(new Dimension(width, height));
    }

    /**
     * Set the haar source
     * @param haarSource the source
     */
    public void setHaarSource(InputStream haarSource) throws IOException{
        this.mHaarSource = haarSource;
        findFace();
    }


    private void findFace() throws IOException{
        try {
            Gray8DetectHaarMultiScale detectHaar = new Gray8DetectHaarMultiScale(mHaarSource, mMinScale, mMaxScale);
            RgbImage im = RgbImageJ2se.toRgbImage(mOriginalFace);
            RgbAvgGray toGray = new RgbAvgGray();
            toGray.push(im);
            List<Rect> results = detectHaar.pushAndReturn(toGray.getFront());

            if(results.size() <= 0) {
                mFoundFace = false;
            } else {
                mFoundFace = true;
/*
                for(Rect rect : results) System.out.println(rect);

                List<Rect> singleFace = new ArrayList<Rect>();
                singleFace.add(results.get(0));

                for(int j = 1; j <results.size(); j++) {
                    Rect temp = results.get(j);
                    Point tempTopLeft = new Point(temp.getTopLeft().getX(), temp.getTopLeft().getY());

                    boolean end = false;
                  //  for(Rect comp : singleFace) {
                    for(int i = 0; i < singleFace.size() && !end; i++) {
                        Rect comp = singleFace.get(i);
                        Point compTopLeft = new Point(comp.getTopLeft().getX(), comp.getTopLeft().getY());

                        // If founds doesn't contain that new face
                        if (tempTopLeft.getX() >= compTopLeft.getX() && tempTopLeft.getX() + temp.getWidth() <= compTopLeft.getX() + comp.getWidth() &&
                            tempTopLeft.getY() >= compTopLeft.getX() && tempTopLeft.getY() + temp.getHeight() <= compTopLeft.getY() + comp.getHeight()) {
                            System.out.println("in");


                        } else {
                            System.out.println("add");
                            singleFace.add(temp);
                            end = true;
                        }

                    }


                }
*/

                mFace = results.get(0);
            }

        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    public Rect getFaceRect()
    {
        return mFace;
    }

    private void createCutFace() {
        if(mFoundFace && mFace.getHeight()*mFace.getWidth()* mNoCropMultiplier > mOriginalFace.getHeight()*mOriginalFace.getWidth()) {
            mProfileFace = mOriginalFace;
        } else if(mFoundFace) {

            int topX = mFace.getTopLeft().getX();
            int topY = mFace.getTopLeft().getY();
            int width = mFace.getWidth();
            int height = mFace.getHeight();

            int padding = mAdditionPadding;
            // ToDo: Do this better
            while(topX + width + padding > mOriginalFace.getWidth() ||
                  topY + height + padding > mOriginalFace.getHeight() ||
                  topX - padding < 0 ||
                  topY - padding < 0) padding--;

            topX = mFace.getTopLeft().getX()-padding;
            topY = mFace.getTopLeft().getY()-padding;

            width = mFace.getWidth()+padding*2;
            height = mFace.getHeight()+padding*2;


            mProfileFace = mOriginalFace.getSubimage(topX, topY, width, height);
        }
    }

    private void updateScaledImage() {
        mProfileFaceScaled = getScaledImage(getProfileFace(), mDimension.width, mDimension.height);
    }

    private BufferedImage getScaledImage(BufferedImage src, int width, int height){
        double factor = 1.0d;
        if(src.getWidth() > src.getHeight()){
            factor = ((double)src.getHeight()/(double)src.getWidth());
            height = (int)(width * factor);
        }else{
            factor = ((double)src.getWidth()/(double)src.getHeight());
            width = (int)(height * factor);
        }

        BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, width, height, null);
        g2.dispose();
        return resizedImg;
    }

}
