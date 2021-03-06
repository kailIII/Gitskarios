package com.alorma.github.ui.fragment.detail.repo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alorma.github.R;
import com.alorma.github.sdk.bean.dto.response.Branch;
import com.alorma.github.sdk.bean.info.RepoInfo;
import com.alorma.gitskarios.basesdk.client.BaseClient;
import com.alorma.github.sdk.services.repo.GetReadmeContentsClient;
import com.alorma.github.ui.ErrorHandler;
import com.alorma.github.ui.fragment.base.BaseFragment;
import com.alorma.github.ui.listeners.TitleProvider;
import com.alorma.github.utils.AttributesUtils;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Bernat on 22/07/2014.
 */
public class ReadmeFragment extends BaseFragment implements BaseClient.OnResultCallback<String>, BranchManager, TitleProvider {

	private static final String REPO_INFO = "REPO_INFO";
	private RepoInfo repoInfo;

	private WebView webview;

	private UpdateReceiver updateReceiver;
	private SmoothProgressBar progressBar;

	public static ReadmeFragment newInstance(RepoInfo repoInfo) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(REPO_INFO, repoInfo);

		ReadmeFragment f = new ReadmeFragment();
		f.setArguments(bundle);
		return f;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.readme_fragment, null);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (getArguments() != null) {
			loadArguments();

			progressBar = (SmoothProgressBar) view.findViewById(R.id.progress);

			int color = AttributesUtils.getPrimaryColor(getActivity(), R.style.AppTheme_Repos);

			progressBar.setSmoothProgressDrawableColor(color);

			webview = (WebView) view.findViewById(R.id.webContainer);
			webview.setPadding(0, 24, 0, 0);
			webview.getSettings().setJavaScriptEnabled(true);
			webview.setWebViewClient(new WebViewCustomClient());

			webview.clearCache(true);
			webview.clearFormData();
			webview.clearHistory();
			webview.clearMatches();
			webview.clearSslPreferences();
			webview.getSettings().setUseWideViewPort(false);
			webview.setBackgroundColor(getResources().getColor(R.color.gray_github_light));
			getContent();
		}
	}

	protected void loadArguments() {
		if (getArguments() != null) {
			repoInfo = getArguments().getParcelable(REPO_INFO);
		}
	}

	private void getContent() {

		if (progressBar != null) {
			progressBar.setVisibility(View.VISIBLE);
			progressBar.progressiveStart();
		}
		
		GetReadmeContentsClient repoMarkdownClient = new GetReadmeContentsClient(getActivity(), repoInfo);
		repoMarkdownClient.setCallback(this);
		repoMarkdownClient.execute();
	}

	@Override
	public void onResponseOk(final String s, Response r) {

		if (progressBar != null) {
			progressBar.progressiveStop();
			progressBar.setVisibility(View.INVISIBLE);
		}

		Uri.Builder builder = Uri.parse("https://github.com/").buildUpon();

		builder.appendPath(repoInfo.owner);
		builder.appendPath(repoInfo.name);
		builder.appendPath("raw");
		builder.appendPath(repoInfo.branch);

		webview.loadDataWithBaseURL(builder.build().toString() + "/", s, "text/html", "UTF-8", null);
	}

	@Override
	public void onFail(RetrofitError error) {
		if (progressBar != null) {
			progressBar.progressiveStop();
			progressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void setCurrentBranch(Branch branch) {
		if (getActivity() != null) {
			GetReadmeContentsClient repoMarkdownClient = new GetReadmeContentsClient(getActivity(), repoInfo);
			repoMarkdownClient.setCallback(this);
			repoMarkdownClient.execute();
		}
	}

	private void onError(String tag, RetrofitError error) {
		ErrorHandler.onRetrofitError(getActivity(), "MarkdownFragment: " + tag, error);
	}

	@Override
	public CharSequence getTitle() {
		return getString(R.string.markdown_fragment_title);
	}

	private class WebViewCustomClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(i);
			return true;
		}
	}

	public void reload() {
		getContent();
	}

	@Override
	public void onStart() {
		super.onStart();
		updateReceiver = new UpdateReceiver();
		IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(updateReceiver, intentFilter);
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().unregisterReceiver(updateReceiver);
	}

	public class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (isOnline(context)) {
				reload();
			}
		}

		public boolean isOnline(Context context) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfoMob = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo netInfoWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return (netInfoMob != null && netInfoMob.isConnectedOrConnecting()) || (netInfoWifi != null && netInfoWifi.isConnectedOrConnecting());
		}
	}

}
