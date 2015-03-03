package com.freezingwind.animereleasenotifier.ui.animelist;

import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.net.Uri;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.freezingwind.animereleasenotifier.R;
import com.freezingwind.animereleasenotifier.ui.adapter.AnimeAdapter;
import com.freezingwind.animereleasenotifier.ui.settings.SettingsActivity;
import com.freezingwind.animereleasenotifier.updater.AnimeListUpdateCallBack;
import com.freezingwind.animereleasenotifier.updater.AnimeUpdater;
import com.freezingwind.animereleasenotifier.data.Anime;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnimeListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
	static final AnimeUpdater animeUpdater = new AnimeUpdater(false);

	protected AnimeListActivity activity;
	protected ListView animeListView;

	protected AnimeAdapter adapter;

	protected SharedPreferences sharedPrefs;

	protected View view;

	protected ProgressBar loadingSpinner;

	protected Intent showSettingsIntent;

	public AnimeListFragment() {
		// ...
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_animelist, container, false);
		animeListView = (ListView) view.findViewById(R.id.animeList);
		loadingSpinner = (ProgressBar) view.findViewById(R.id.loadingSpinner);

		activity = (AnimeListActivity) getActivity();

		showSettingsIntent = new Intent(activity, SettingsActivity.class);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);

		adapter = new AnimeAdapter(activity, getAnimeUpdater().getAnimeList());

		setupAnimeListView();
		update();

		return view;
	}

	// Get anime list
	protected AnimeUpdater getAnimeUpdater() {
		return animeUpdater;
	}

	// Create anime list view
	protected void setupAnimeListView() {
		animeListView.setVisibility(View.GONE);
		animeListView.setAdapter(adapter);
		animeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
			                        long id) {
				Anime anime = (Anime) parent.getItemAtPosition(position);

				Intent intent;

				// Video URL
				if(anime.videoURL != null && !anime.videoURL.isEmpty()) {
					Uri videoURI = Uri.parse(anime.videoURL);
					intent = new Intent(Intent.ACTION_VIEW, videoURI);
					intent.setDataAndType(videoURI, "video/*");

					try {
						startActivity(intent);
						return;
					} catch (ActivityNotFoundException e) {
						Log.d("AnimeListFragment", e.toString());
					}
				}

				// Next episode URL
				if(anime.nextEpisodeURL != null && !anime.nextEpisodeURL.isEmpty()) {
					Uri uri = Uri.parse(anime.nextEpisodeURL);
					intent = new Intent(Intent.ACTION_VIEW, uri);

					try {
						startActivity(intent);
						return;
					} catch (ActivityNotFoundException e) {
						Log.d("AnimeListFragment", e.toString());
					}
				}

				// Anime provider URL
				if(anime.animeProviderURL != null && !anime.animeProviderURL.isEmpty()) {
					Uri uri = Uri.parse(anime.animeProviderURL);
					intent = new Intent(Intent.ACTION_VIEW, uri);

					try {
						startActivity(intent);
						return;
					} catch (ActivityNotFoundException e) {
						Log.d("AnimeListFragment", e.toString());
					}
				}

				// List provider URL
				if(anime.url != null && !anime.url.isEmpty()) {
					Uri uri = Uri.parse(anime.url);
					intent = new Intent(Intent.ACTION_VIEW, uri);

					try {
						startActivity(intent);
						return;
					} catch (ActivityNotFoundException e) {
						Log.d("AnimeListFragment", e.toString());
					}
				}
			}
		});
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		switch(key) {
			case "animeProvider":
			case "userName":
				update();
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	protected String getCacheKey() {
		return "cachedAnimeListJSON";
	}

	protected void displayUserNameMissingNotification() {
		Toast.makeText(activity, "Please enter your ARN username", Toast.LENGTH_SHORT).show();

		try {
			startActivity(showSettingsIntent);
		} catch(IllegalStateException e) {
			Log.d("AnimeListFragment", "Not attached to activity");
		}
	}

	// Update
	protected void update() {
		String userName = sharedPrefs.getString("userName", "");
		String cachedJSON = sharedPrefs.getString(getCacheKey(), "");

		final AnimeUpdater updater = getAnimeUpdater();

		if(userName.length() == 0) {
			displayUserNameMissingNotification();
			return;
		}

		if(cachedJSON.length() > 0) {
			updater.update(cachedJSON, activity, new AnimeListUpdateCallBack() {
				@Override
				public void execute() {
					onReceiveAnimeList();
				}
			});
		} else {
			loadingSpinner.setVisibility(View.VISIBLE);
		}

		// Update in the background
		updater.updateByUser(userName, activity, new AnimeListUpdateCallBack() {
			@Override
			public void execute() {
				onReceiveAnimeList();
			}
		});
	}

	protected void onReceiveAnimeList() {
		loadingSpinner.setVisibility(View.INVISIBLE);
		animeListView.setVisibility(View.VISIBLE);
		adapter.notifyDataSetChanged();
	}
}
