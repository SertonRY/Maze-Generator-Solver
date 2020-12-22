import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Frame extends JFrame {
    Frame(){
        this.add(new Panel());
        this.setTitle("Maze Game");
        //define the close operation
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        //adaptive size
        this.pack();
        this.setVisible(true);
        //set location to center
        this.setLocationRelativeTo(null);
        BufferedImage image = null;
        try {
            image = ImageIO.read(this.getClass()
                    .getResource("./icon3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setIconImage(image);
    }
}
