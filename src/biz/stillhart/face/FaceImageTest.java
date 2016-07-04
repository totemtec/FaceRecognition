package biz.stillhart.face;

import biz.stillhart.face.model.FaceImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * jjil包来自https://www.github.com/sexyideas/moosificator
 * 参考https://github.com/arcs-/FaceDetection
 * https://github.com/NeuronRobotics/sample-java-bowler
 */
public class FaceImageTest {


    public static void main(String[] args) throws Exception {
//          BufferedImage bi = ImageIO.read(FaceImageTest.class.getResourceAsStream("111.jpg"));
        BufferedImage bi = ImageIO.read(new File("/Users/majianglin/Desktop/111.jpg"));


        FaceImage face = new FaceImage(bi);

        System.out.println(face.getFaceRect());

//
//        File outputfile = new File("/Users/majianglin/Desktop/result.png");
//        ImageIO.write(face.getScaledProfileFace(), "png", outputfile);
    }

}