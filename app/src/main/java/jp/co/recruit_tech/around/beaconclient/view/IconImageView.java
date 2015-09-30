package jp.co.recruit_tech.around.beaconclient.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

/**
 * Metadataアイコン表示用のView。maxWidth,maxHeightに強制的にリサイズされます
 *
 * Created by Hideaki on 15/03/05.
 */
public class IconImageView extends ImageView {
    private ImageLoader.ImageContainer imageContainer;

    public IconImageView(Context context) {
        super(context);
    }
    public IconImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public IconImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void loadImageUrl(final String url, final Bitmap defaultImage, ImageLoader imageLoader) {
        if (imageContainer != null) {
            imageContainer.cancelRequest();
        }
        final int maxWidth = getMaxWidth();
        final int maxHeight = getMaxHeight();
        imageContainer = imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                if (bitmap != null) {
                    if (bitmap.getWidth() < maxWidth || bitmap.getHeight() < maxHeight) {
                        bitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, false);
                    }
                    setImageBitmap(bitmap);
                } else {
                    setImageBitmap(defaultImage);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }, maxWidth, maxHeight);
    }

}
