package caltrack.blub.com.caltrack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        Bundle extras = getIntent().getExtras();
        File file = (File) extras.get("picture");
        PhotoView image = findViewById(R.id.fullscreenImg);

        Glide.with(this).load(file).into(image);
    }
}
