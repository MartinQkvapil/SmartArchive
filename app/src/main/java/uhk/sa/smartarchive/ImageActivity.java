package uhk.sa.smartarchive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.util.List;

public class ImageActivity extends AppCompatActivity {

    ImageView imageView;
    Button btn_detect_faces;
    TextView imageText;
    TextView faceCount;

    private InputImage image = null;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image);
        btn_detect_faces = findViewById(R.id.btn_detect_faces);

        Intent intent = getIntent();
        imageView = findViewById(R.id.imageView);
        imageText = findViewById(R.id.imageText);
        faceCount = findViewById(R.id.tv_faces);
        Bundle b = intent.getBundleExtra("bundle");
        path = b.getString("path");

        try {
            if (path != null) {
                File f = new File(path);
                Uri uri = Uri.fromFile(f);
                imageView.setImageBitmap(BitmapFactory.decodeFile(path));
                image = InputImage.fromFilePath(ImageActivity.this, uri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        btn_detect_faces.setOnClickListener(v -> {
            if (image != null) {
                FaceDetectorOptions highAccuracyOpts =
                        new FaceDetectorOptions.Builder()
                                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                .build();
                FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);
                Task<List<Face>> results =
                        detector.process(image)
                                .addOnSuccessListener(
                                        faces -> {
                                            Bitmap pic = BitmapFactory.decodeFile(path);
                                            Bitmap bmp = Bitmap.createBitmap(pic.getWidth(), pic.getHeight(), Bitmap.Config.RGB_565);
                                            Canvas cnvs = new Canvas(bmp);
                                            cnvs.drawBitmap(BitmapFactory.decodeFile(path), 0, 0, null);

                                            if (faces.size() == 0) {
                                                Toast.makeText(ImageActivity.this, "Faces not detected.", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            for (Face face : faces) {
                                                Rect bounds = face.getBoundingBox();

                                                Paint paint = new Paint();
                                                paint.setColor(Color.RED);
                                                paint.setStyle(Paint.Style.STROKE);
                                                paint.setStrokeWidth(2);

                                                cnvs.drawRect(bounds.left, bounds.top, bounds.right, bounds.bottom, paint);
                                            }
                                            faceCount.setText("Detected faces: " + faces.size());
                                            imageView.setImageBitmap(bmp);
                                            Toast.makeText(ImageActivity.this, "Faces detected.", Toast.LENGTH_SHORT).show();
                                        })
                                .addOnFailureListener(
                                        e -> Toast.makeText(ImageActivity.this, "Faces not detected.", Toast.LENGTH_SHORT).show());
            }
            // detekce textových materiálů za pomoci ML Kit API firebase
            if (image != null) {
                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                Task<Text> result =
                        recognizer.process(image)
                                .addOnSuccessListener(visionText -> {
                                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                                        imageText.setText(block.getText());
                                    }
                                })
                                .addOnFailureListener(
                                        e -> Toast.makeText(ImageActivity.this, "Text not detected.", Toast.LENGTH_SHORT).show());
            }
        });
    }
}