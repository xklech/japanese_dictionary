package cz.muni.fi.japanesedictionary.main;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TabHost;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.parser.ParserService;

public class MainFragment extends SherlockFragment implements
		TabHost.OnTabChangeListener {

	private TabHost mTabHost;
	private String mLastTabId;
	private ResultFragmentList mSearchFragment;
	private String mSearchInput = null;

	private MenuItem mSearchItem = null;
	private EditText mSearch = null;
	private Button mDelete = null;

	private ResultLoader resultLoader = null;

	
	
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		Log.i("MainFragment", "Saving instance");

		if (mSearch != null && mSearch.length() > 0) {
			Log.i("MainFragment", "text saved");
			bundle.putString(MainActivity.SEARCH_TEXT, mSearch.getText()
					.toString());
		}
		bundle.putString(MainActivity.PART_OF_TEXT, mLastTabId);
		Log.i("MainFragment", "saving fragmen: "+mLastTabId);
		super.onSaveInstanceState(bundle);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		container = new LinearLayout(getActivity().getApplicationContext());
		return inflater.inflate(R.layout.main_fragment, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		Log.i("MainFragment", "Setting Tabs");
		mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);

		mTabHost.setup();
		mTabHost.addTab(mTabHost.newTabSpec("exact").setIndicator(getText(R.string.search_exact))
				.setContent(new TabFactory(getActivity())));
		mTabHost.addTab(mTabHost.newTabSpec("begining").setIndicator(getText(R.string.search_begining))
				.setContent(new TabFactory(getActivity())));
		mTabHost.addTab(mTabHost.newTabSpec("middle").setIndicator(getText(R.string.search_middle))
				.setContent(new TabFactory(getActivity())));
		mTabHost.addTab(mTabHost.newTabSpec("end").setIndicator(getText(R.string.search_end))
				.setContent(new TabFactory(getActivity())));
		mTabHost.setOnTabChangedListener(this);
		mLastTabId = "exact";

		if (savedInstanceState != null) {
			Log.i("MainFragment", "Setting current tab: "+savedInstanceState.getString(MainActivity.PART_OF_TEXT));
			mTabHost.setCurrentTabByTag(savedInstanceState.getString(MainActivity.PART_OF_TEXT));
			
			mLastTabId = savedInstanceState.getString(MainActivity.PART_OF_TEXT);
			mSearchInput = savedInstanceState.getString(MainActivity.SEARCH_TEXT);
			mSearchFragment = (ResultFragmentList) getActivity().getSupportFragmentManager().findFragmentByTag("result_fragment");
		}
		SharedPreferences settings = getActivity().getSharedPreferences(
				ParserService.DICTIONARY_PREFERENCES, 0);
		boolean validDictionary = settings.getBoolean("hasValidDictionary",
				false);
		if (!validDictionary
				&& !MainActivity.isMyServiceRunning(getActivity()
						.getApplicationContext())) {
			DialogFragment newFragment = MyFragmentAlertDialog.newInstance(
					R.string.no_dictionary_found,
					R.string.download_dictionary_question, false);
			newFragment.show(getActivity().getSupportFragmentManager(),
					"dialog");
		}

		if(mSearchFragment == null){
			Log.i("MainFragment","Creating new result fragment");
			mSearchFragment = new ResultFragmentList();
			Log.e("MainFragment", "tab" +mLastTabId);
			Bundle bundleFragment = new Bundle();
			if (mSearchInput != null) {
				bundleFragment.putString(MainActivity.SEARCH_TEXT,
						mSearchInput);
			}
			bundleFragment.putString(MainActivity.PART_OF_TEXT,
					mLastTabId);
			mSearchFragment.setArguments(bundleFragment);
			FragmentTransaction ft = getActivity().getSupportFragmentManager()
					.beginTransaction();
			ft.add(android.R.id.tabcontent, mSearchFragment,"result_fragment");
			ft.commit();
		}else{
			Log.i("MainFragment","Loading old fragment");
		}

		
		view.requestFocus();
		setHasOptionsMenu(true);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.i("MainFragment", "Inflating menu");
		inflater.inflate(R.menu.menu, menu);
		
		Log.i("MainFragment", "Setting menu ");
		((MainActivity) getActivity()).getSupportActionBar()
				.setDisplayShowTitleEnabled(false);
		mSearchItem = (MenuItem)menu.findItem(R.id.menu_search);
		mSearch = (EditText) mSearchItem.getActionView().findViewById(
				R.id.search);
		if (mSearchInput != null) {
			mSearch.setText(mSearchInput);
		}
			mDelete = (Button) mSearchItem.getActionView().findViewById(
					R.id.delete);
			mDelete.setVisibility(mSearch.getText().length() > 0 ? View.VISIBLE
					: View.GONE);
			if(mDelete == null)System.out.println("nulllll");
			mDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mSearch != null) {
						mSearch.setText("");
					}
				}
			});

			mSearch.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					Log.e("MainFragment","text changed");
					mDelete.setVisibility(s.length() > 0 ? View.VISIBLE
							: View.GONE);

					if (mSearchFragment != null) {
						if (resultLoader == null) {
							Loader<List<Translation>> loader = mSearchFragment
									.getLoaderManager().getLoader(0);
							resultLoader = (ResultLoader) loader;
							if (resultLoader == null) {
								return;
							}
						}
						Message msg = new Message();
						Bundle bundle = new Bundle();
						bundle.putString(
								MainActivity.HANDLER_BUNDLE_TRANSLATION,
								s.toString());
						msg.setData(bundle);
						resultLoader.getHandler().sendMessage(msg);

					}
				}
			});
		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onTabChanged(String tabId) {
		Log.i("MainActivity", "Tab changed: " + tabId);
		if (mSearchFragment == null) {
			return;
		}

		mSearchFragment.changePart(tabId);
		mLastTabId = tabId;
	}

	static class TabFactory implements TabHost.TabContentFactory {
		private final Context mContext;

		public TabFactory(Context context) {
			mContext = context;
		}

		@Override
		public View createTabContent(String tag) {
			Log.i("TabFactory", tag);
			View v = new View(mContext);
			
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}
	}

}
