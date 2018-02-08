package com.kimjisub.launchpad;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.kimjisub.launchpad.manage.SaveSetting;
import com.kimjisub.launchpad.manage.UIManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.kimjisub.launchpad.manage.Tools.lang;

/**
 * Created by rlawl ON 2017-01-16.
 */

public class Setting extends PreferenceActivity {
	
	
	static IInAppBillingService mService;
	static ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IInAppBillingService.Stub.asInterface(service);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		BaseActivity.startActivity(this);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
		
		
		//구글플레이 결제
		Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		serviceIntent.setPackage("com.android.vending");
		bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
		
		findPreference("select_theme").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Setting.this, Theme.class));
				return false;
			}
		});
		
		findPreference("use_sd_card").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				BaseActivity.requestRestart(Setting.this);
				return true;
			}
		});
		
		findPreference("community").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				int[] RlistT = {R.string.officialHomepage,
					R.string.officialFacebook,
					R.string.facebookCommunity,
					R.string.naverCafe,
					R.string.discord,
					R.string.kakaotalk,
					R.string.email};
				int[] RlistS = {R.string.officialHomepage_,
					R.string.officialFacebook_,
					R.string.facebookCommunity_,
					R.string.naverCafe_,
					R.string.discord_,
					R.string.kakaotalk_,
					R.string.email_};

				int[] RlistI = {
					R.drawable.web,
					R.drawable.facebook,
					R.drawable.facebook_community,
					R.drawable.cafe,
					R.drawable.discord,
					R.drawable.kakaotalk,
					R.drawable.mail
				};

				final String[] listT = new String[RlistT.length];
				final String[] listS = new String[RlistS.length];
				final int[] listI = new int[RlistI.length];
				for (int i = 0; i < listT.length; i++) {
					listT[i] = lang(Setting.this, RlistT[i]);
					listS[i] = lang(Setting.this, RlistS[i]);
					listI[i] = RlistI[i];
				}
				
				
				ListView listView = new ListView(Setting.this);
				ArrayList<communityItem> data = new ArrayList<>();
				for (int i = 0; i < listT.length; i++)
					data.add(new communityItem(listT[i], listS[i], listI[i]));
				
				listView.setAdapter(new mAdapter(Setting.this, R.layout.setting_community_item, data));
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						String[] urls = {
							"http://unipad.kr",
							"https://www.facebook.com/playunipad",
							"https://www.facebook.com/groups/unipadcommunity",
							"http://cafe.naver.com/unipad",
							"https://discord.gg/ESDgyNs",
							"http://qr.kakao.com/talk/R4p8KwFLXRZsqEjA1FrAnACDyfc-",
							"mailto:0226unipad@gmail.com"
						};
						String[] actions = {
							Intent.ACTION_VIEW,
							Intent.ACTION_VIEW,
							Intent.ACTION_VIEW,
							Intent.ACTION_VIEW,
							Intent.ACTION_VIEW,
							Intent.ACTION_VIEW,
							Intent.ACTION_VIEW,
							Intent.ACTION_SENDTO
						};
						startActivity(new Intent(actions[position], Uri.parse(urls[position])));
					}
				});
				
				AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);
				builder.setTitle(lang(Setting.this, R.string.community));
				builder.setView(listView);
				builder.show();
				return false;
			}
		});
		
		findPreference("removeAds").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				try {
					Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "premium", "inapp", UIManager.DEVELOPERPAYLOAD);
					PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
					
					startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
					
					
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (IntentSender.SendIntentException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
		
	}
	
	
	public class communityItem {
		private String title;
		private String summuary;
		private int icon;
		
		
		public communityItem(String title, String summuary, int icon) {
			setTitle(title);
			setSummuary(summuary);
			setIcon(icon);
		}
		
		
		public void setTitle(String title) {
			this.title = title;
		}

		public void setSummuary(String summuary) {
			this.summuary = summuary;
		}

		public void setIcon(int icon) {
			this.icon = icon;
		}
		
		public String getTitle() {
			return title;
		}

		public String getSummuary() {
			return summuary;
		}

		public int getIcon() {
			return icon;
		}
		
	}
	
	class mAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ArrayList<communityItem> data;
		private int layout;
		
		
		public mAdapter(Context context, int layout, ArrayList<communityItem> data) {
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.data = data;
			this.layout = layout;
		}
		
		@Override
		public int getCount() {
			return data.size();
		}
		
		@Override
		public String getItem(int position) {
			return data.get(position).getTitle();
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = inflater.inflate(layout, parent, false);
			
			communityItem item = data.get(position);
			
			TextView title = convertView.findViewById(R.id.title);
			TextView summary = convertView.findViewById(R.id.summary);
			ImageView icon = convertView.findViewById(R.id.icon);
			
			title.setText(item.getTitle());
			summary.setText(item.getSummuary());
			icon.setBackground(getApplicationContext().getResources().getDrawable(item.getIcon()));
			return convertView;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1001) {
			int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
			String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
			String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
			
			if (resultCode == RESULT_OK) {
				try {
					JSONObject jo = new JSONObject(purchaseData);
					UIManager.isPremium = jo.getBoolean("autoRenewing");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void onResume() {
		findPreference("select_theme").setSummary(SaveSetting.SelectedTheme.load(Setting.this));
		findPreference("use_sd_card").setSummary(SaveSetting.IsUsingSDCard.URL);
		if (UIManager.isPremium)
			findPreference("removeAds").setSummary(lang(Setting.this, R.string.using));
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		BaseActivity.finishActivity(this);
		if (mService != null)
			unbindService(mServiceConn);
	}
}