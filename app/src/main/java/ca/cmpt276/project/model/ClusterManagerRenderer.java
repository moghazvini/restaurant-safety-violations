package ca.cmpt276.project.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import ca.cmpt276.project.R;

public class ClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {

    private final IconGenerator icongen;
    private final ImageView iv;
    private final int width;
    private final int height;

    public ClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);

        icongen = new IconGenerator(context.getApplicationContext());
        iv = new ImageView(context.getApplicationContext());
        width = (int) context.getResources().getDimension(R.dimen.marker_image);
        height = (int) context.getResources().getDimension(R.dimen.marker_image);
        iv.setLayoutParams(new ViewGroup.LayoutParams(width, height));
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
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return false;
    }

}
