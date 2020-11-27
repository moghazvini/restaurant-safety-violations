package ca.cmpt276.project.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.project.R;

/**
 * Draws the markers on the map.
 **/
public class ClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {

    private final IconGenerator icongen;
    private final ImageView iv;
    private final int mWidth;
    private final int mHeight;
    private GoogleMap mMap;

    public ClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);

        mMap = map;
        icongen = new IconGenerator(context.getApplicationContext());
        iv = new ImageView(context.getApplicationContext());
        mWidth = (int) context.getResources().getDimension(R.dimen.marker_image);
        mHeight = (int) context.getResources().getDimension(R.dimen.marker_image);
        iv.setLayoutParams(new ViewGroup.LayoutParams(mWidth, mHeight));
        int padding = (int) context.getResources().getDimension(R.dimen.marker_padding);
        iv.setPadding(padding, padding, padding, padding);
        icongen.setContentView(iv);
    }
    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {

        iv.setImageResource(item.getIcon());
        Bitmap icon = icongen.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<ClusterMarker> cluster, MarkerOptions markerOptions) {
        markerOptions.icon(getClusterIcon(cluster));
    }

    private BitmapDescriptor getClusterIcon(Cluster<ClusterMarker> cluster) {
        List<Drawable> icon_Cluster = new ArrayList<>(Math.min(4, cluster.getSize()));
        int width = mWidth;
        int height = mHeight;

        for (ClusterMarker marker : cluster.getItems()) {
            // Draw 4 at most.
            if (icon_Cluster.size() == 4)
                break;
            Drawable drawable = iv.getResources().getDrawable(marker.getIcon());
            drawable.setBounds(0, 0, width, height);
            icon_Cluster.add(drawable);
        }
        MultiDrawable multiDrawable = new MultiDrawable(icon_Cluster);
        multiDrawable.setBounds(0, 0, width, height);

        iv.setImageDrawable(multiDrawable);
        Bitmap icon = icongen.makeIcon(String.valueOf(cluster.getSize()));
        return BitmapDescriptorFactory.fromBitmap(icon);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize()>1;
    }

}
