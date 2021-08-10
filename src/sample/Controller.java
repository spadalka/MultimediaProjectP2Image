package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.*;

public class Controller {

    final FileChooser fileChooser = new FileChooser();
    private final int[][] quantizationMatrix = {{1, 1, 2, 4, 8, 16, 32, 64},
            {1, 1, 2, 4, 8, 16, 32, 64},
            {2, 2, 2, 4, 8, 16, 32, 64},
            {4, 4, 4, 4, 8, 16, 32, 64},
            {8, 8, 8, 8, 8, 16, 32, 64},
            {16, 16, 16, 16, 16, 16, 32, 64},
            {32, 32, 32, 32, 32, 32, 32, 64},
            {64, 64, 64, 64, 64, 64, 64, 64}};

    BufferedImage originalImage;
    private Stage stage;
    @FXML
    private AnchorPane originalPane;
    @FXML
    private AnchorPane compressedPane;
    private File file;

    public void openFileImage(ActionEvent event) throws IOException {
        file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);
            originalPane.getChildren().set(0, imageView);
        }
    }


    public double[][] getTransformMatrix() {
        double[][] transformMatrix = new double[8][8];
        double a = sqrt((double) 1 / 8);
        for (int i = 0; i < 8; i++) {
            if (i == 1) {
                a = sqrt((double) 2 / 8);
            }
            for (int j = 0; j < 8; j++) {
                transformMatrix[i][j] = a * cos(((2 * j + 1) * i * PI) / (2 * 8));
            }
        }
        return transformMatrix;
    }

    public double[][] product(double[][] A, double[][] B) {
        double[][] productMatrix = new double[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    productMatrix[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return productMatrix;
    }

    public double[][] product(double[][] A, int[][] B) {
        double[][] productMatrix = new double[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    productMatrix[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return productMatrix;
    }

    public double[][] transpose(double[][] A) {
        double[][] transposedMatrix = new double[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                transposedMatrix[i][j] = A[j][i];
            }
        }
        return transposedMatrix;
    }

    public int[][] roundOff(double[][] A) {
        int[][] roundedMatrix = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                roundedMatrix[i][j] = (int) round(A[i][j]);
            }
        }
        return roundedMatrix;
    }

    public void quantize(int[][] A) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                A[i][j] = A[i][j] / quantizationMatrix[i][j];
            }
        }
    }

    public void dequantize(int[][] A) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                A[i][j] = A[i][j] * quantizationMatrix[i][j];
            }
        }
    }

    // https://stackoverflow.com/questions/5061912/printing-out-a-2d-array-in-matrix-format
    public void printMatrix(double[][] matrix) {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                System.out.printf("%10f", matrix[row][col]);
            }
            System.out.println();
        }
    }

    public void printMatrix(int[][] matrix) {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                System.out.printf("%6d", matrix[row][col]);
            }
            System.out.println();
        }
    }

    public int normalize(int colorVal) {
        if (colorVal < 0) {
            return 0;
        } else if (colorVal > 255) {
            return 255;
        }
        return colorVal;
    }

    public void initiateCompression(ActionEvent event) {
        compress(file);
    }

    public void compress(File file) {

        originalImage = null;
        try {
            originalImage = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert originalImage != null;

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        int numHorizontalChunks = width / 8;
        int numVerticalChunks = height / 8;

        double[][] transformMatrix = getTransformMatrix();
        double[][] transposedMatrix = transpose(transformMatrix);

        double[][] Y_Matrix = new double[8][8];
        double[][] Co_Matrix = new double[8][8];
        double[][] Cg_Matrix = new double[8][8];


        double temp = 0;
        int R = 0;
        int G = 0;
        int B = 0;

        for (int y_factor = 0; y_factor < numVerticalChunks; y_factor++) {
            for (int x_factor = 0; x_factor < numHorizontalChunks; x_factor++) {

                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        int row = 8 * x_factor + x;
                        int col = 8 * y_factor + y;

                        Color colorAtPixel = new Color(originalImage.getRGB(row, col));
                        R = colorAtPixel.getRed();
                        G = colorAtPixel.getGreen();
                        B = colorAtPixel.getBlue();

                        Co_Matrix[x][y] = R - B;
                        temp = B + Co_Matrix[x][y] / 2;
                        Cg_Matrix[x][y] = G - temp;
                        Y_Matrix[x][y] = temp + Cg_Matrix[x][y] / 2;
                    }
                }

                Y_Matrix = compressColorBlock(transformMatrix, transposedMatrix, Y_Matrix);
                Co_Matrix = compressColorBlock(transformMatrix, transposedMatrix, Co_Matrix);
                Cg_Matrix = compressColorBlock(transformMatrix, transposedMatrix, Cg_Matrix);

                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        int row = 8 * x_factor + x;
                        int col = 8 * y_factor + y;

                        temp = Y_Matrix[x][y] - Cg_Matrix[x][y] / 2;
                        G = (int) (Cg_Matrix[x][y] + temp);
                        B = (int) (temp - Co_Matrix[x][y] / 2);
                        R = (int) (B + Co_Matrix[x][y]);

                        R = normalize(R);
                        G = normalize(G);
                        B = normalize(B);

                        Color compressedColorForPixel = new Color(R, G, B);
                        originalImage.setRGB(row, col, compressedColorForPixel.getRGB());
                    }
                }
            }
        }
        Image displayImage = SwingFXUtils.toFXImage(originalImage, null);
        ImageView imageView = new ImageView(displayImage);
        compressedPane.getChildren().set(0, imageView);

        File output = new File("compressedImage.bmp");
        try {
            ImageIO.write(originalImage, "bmp", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double[][] compressColorBlock(double[][] transformMatrix, double[][] transposedMatrix, double[][] layerMatrix) {
        double[][] intermediateMatrix = product(transformMatrix, layerMatrix);
        double[][] DCTCoefficients = product(intermediateMatrix, transposedMatrix);

        int[][] DCT_rounded = roundOff(DCTCoefficients);
        quantize(DCT_rounded);
        dequantize(DCT_rounded);
        intermediateMatrix = product(transposedMatrix, DCT_rounded);
        layerMatrix = product(intermediateMatrix, transformMatrix);
        return layerMatrix;
    }
}
