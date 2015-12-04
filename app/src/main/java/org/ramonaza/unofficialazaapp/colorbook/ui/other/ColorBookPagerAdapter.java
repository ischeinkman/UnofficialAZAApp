package org.ramonaza.unofficialazaapp.colorbook.ui.other;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.LruCache;
import android.view.Display;
import android.view.WindowManager;

import org.ramonaza.unofficialazaapp.colorbook.backend.ColorbookConstants;
import org.ramonaza.unofficialazaapp.colorbook.ui.fragments.ColorBookPageFragment;

import java.io.IOException;


/**
 * Created by ilan on 11/24/15.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ColorBookPagerAdapter extends FragmentStatePagerAdapter {

    public int[] screenDims;
    PdfRenderer mRenderer;
    LruCache<Integer, Bitmap> pagesCache;
    private ColorBookPageFragment.BookCallbacks callbacks;
    private int count;

    private Context context;

    public ColorBookPagerAdapter(FragmentManager fm, Context context, ColorBookPageFragment.BookCallbacks callbacks) {
        super(fm);
        this.context = context;
        screenDims = calcScreenDims(context);
        pagesCache = new LruCache<>(4);
        count = ColorbookConstants.COLORBOOK_SIZE;
        this.callbacks = callbacks;

    }

    public static int[] calcScreenDims(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display dis = wm.getDefaultDisplay();
        Point size = new Point();
        dis.getSize(size);
        return new int[]{size.x, size.y};
    }

    @Override
    public Fragment getItem(int position) {
        if (pagesCache.get(position) != null) {
            return ColorBookPageFragment.newInstance(position, pagesCache.get(position), callbacks);
        } else {
            ColorBookPageFragment fragment = ColorBookPageFragment.newInstance(position, null, callbacks);
            new ImageviewLoader(fragment).execute(position);
            return fragment;
        }
    }

    @Override
    public int getCount() {
        return count;
    }

    public class ImageviewLoader extends AsyncTask<Integer, Void, Bitmap> {

        private ColorBookPageFragment pageFragment;
        private int page;

        public ImageviewLoader(ColorBookPageFragment toLoad) {
            pageFragment = toLoad;
        }

        @Override
        protected Bitmap doInBackground(Integer... pagenums) {
            page = pagenums[0];
            if (pagesCache.get(page) != null) {
                return pagesCache.get(page);
            }

            if (mRenderer == null) {
                try {
                    ParcelFileDescriptor mFileDescriptor = context.getAssets().openFd("ColorBook.pdf").getParcelFileDescriptor();
                    mRenderer = new PdfRenderer(mFileDescriptor);
                } catch (IOException e) {

                }
            }
            PdfRenderer.Page currentPage = mRenderer.openPage(page);
            int[] imgDims = getImgDimensionsHorizontalFit(currentPage, screenDims);
            Bitmap pageMap = Bitmap.createBitmap(imgDims[0], imgDims[1], Bitmap.Config.ARGB_8888);
            currentPage.render(pageMap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            currentPage.close();
            return pageMap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            pageFragment.setImage(bitmap);
            pagesCache.put(page, bitmap);
        }

        public int[] getImgDimensionsHorizontalFit(PdfRenderer.Page page, int[] screenDims) {
            double coeff = (double) screenDims[0] / (double) page.getWidth();
            return new int[]{(int) (page.getWidth() * coeff), (int) (page.getHeight() * coeff)};
        }

        public int[] getImgDimensionsVerticalFit(PdfRenderer.Page page, int[] screenDims) {
            double coeff = (double) screenDims[1] / (double) page.getHeight();
            return new int[]{(int) (page.getWidth() * coeff), (int) (page.getHeight() * coeff)};
        }
    }


}
