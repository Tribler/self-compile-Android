package nl.tudelft.selfcompileapp;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PickAppActivity extends Activity {

	PackageManager packMgr;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_app);

		packMgr = getPackageManager();
		final List<ApplicationInfo> apps = packMgr.getInstalledApplications(0);
		Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(packMgr));

		ListView listView = (ListView) findViewById(R.id.listview);
		listView.setAdapter(new ApplicationInfoAdapter(this, apps));
	}

	class ApplicationInfoAdapter extends ArrayAdapter<ApplicationInfo>implements OnClickListener {

		public ApplicationInfoAdapter(Context context, List<ApplicationInfo> apps) {
			super(context, 0, apps);
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {

			if (view == null) {
				view = LayoutInflater.from(getContext()).inflate(R.layout.item_app_info, parent, false);
			}

			ApplicationInfo info = getItem(position);
			ImageView iconView = (ImageView) view.findViewById(R.id.pick_app_info_icon);
			TextView nameView = (TextView) view.findViewById(R.id.pick_app_info_name);

			iconView.setImageDrawable(packMgr.getApplicationIcon(info));
			nameView.setText(packMgr.getApplicationLabel(info));

			view.setTag(info.packageName);
			view.setOnClickListener(this);

			return view;
		}

		public void onClick(View v) {
			Intent result = new Intent();
			result.putExtra("app", (String) v.getTag());
			setResult(Activity.RESULT_OK, result);
			finish();
		}
	}
}