package org.ramonaza.unofficialazaapp.colorbook.other;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import org.ramonaza.unofficialazaapp.R;

/**
 * Created by ilan on 10/14/15.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ColorBookViewAdapter extends BaseAdapter {

    PdfRenderer mRenderer;
    Context context;
    Bitmap[] allImages;
    int[] screenDims;

    public ColorBookViewAdapter(Context context, int resource, PdfRenderer renderer, int[] screenDims) {
        mRenderer = renderer;
        allImages = new Bitmap[renderer.getPageCount()];
        this.context = context;
        this.screenDims = screenDims;
    }

    @Override
    public int getCount() {
        return allImages.length;
    }

    @Override
    public Bitmap getItem(int i) {
        if (allImages[i] == null) loadImage(i);
        return allImages[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public Context getContext() {
        return context;
    }

    private Bitmap loadImage(int index) {
        PdfRenderer.Page currentPage = mRenderer.openPage(index);
        int pgWidth = currentPage.getWidth();
        int pgHeight = currentPage.getHeight();
        float widthRadio = screenDims[0] / (float) pgWidth;
        Bitmap pageMap = Bitmap.createBitmap(((int) (currentPage.getWidth() * widthRadio)), ((int) (currentPage.getHeight() * widthRadio)), Bitmap.Config.ARGB_8888);
        currentPage.render(pageMap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        currentPage.close();
        return pageMap;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bluebook_adapter_layout, parent, false);
            viewHolder.content = (ImageView) convertView.findViewById(R.id.contentID);
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.content.setImageBitmap(loadImage(position));
        return convertView;
    }

    private static class ViewHolder {
        public ImageView content;
    }
}
