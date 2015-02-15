package com.freezingwind.animereleasenotifier.ui.animelist;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
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

import com.freezingwind.animereleasenotifier.updater.AnimeListUpdateCallBack;
import com.freezingwind.animereleasenotifier.updater.AnimeUpdater;
import com.freezingwind.animereleasenotifier.data.Anime;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnimeListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
	static final AnimeUpdater animeUpdater = new AnimeUpdater();

	protected Activity activity;
	protected ListView animeListView;

	protected AnimeAdapter adapter;

	protected SharedPreferences sharedPrefs;

	public AnimeListFragment() {
		// ...
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		activity = getActivity();

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);

		adapter = new AnimeAdapter(activity, animeUpdater.getAnimeList());

		if(animeListView == null) {
			createAnimeListView();
		}

		update();

		return animeListView;
	}

	// Create anime list view
	private void createAnimeListView() {
		animeListView = new ListView(activity);
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
			}
		});
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		switch(key) {
			case "userName":
				update();
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	// Update
	void update() {
		String userName = sharedPrefs.getString("userName", "");

		animeUpdater.updateByUser(userName, activity, new AnimeListUpdateCallBack() {
			@Override
			public void execute() {
				adapter.notifyDataSetChanged();
			}
		});
	}
}
