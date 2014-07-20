package com.alorma.github.ui.fragment.detail.repo;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RepositoryInfo;
import android.widget.RepositoryInfoField;
import android.widget.Toast;
import android.widget.bean.RepositoryUiInfo;

import com.alorma.github.BuildConfig;
import com.alorma.github.R;
import com.alorma.github.sdk.bean.dto.response.ListBranches;
import com.alorma.github.sdk.bean.dto.response.ListContributors;
import com.alorma.github.sdk.bean.dto.response.ListIssues;
import com.alorma.github.sdk.bean.dto.response.ListReleases;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.client.BaseClient;
import com.alorma.github.sdk.services.repo.BaseRepoClient;
import com.alorma.github.sdk.services.repo.GetRepoBranchesClient;
import com.alorma.github.sdk.services.repo.GetRepoClient;
import com.alorma.github.sdk.services.repo.GetRepoContributorsClient;
import com.alorma.github.sdk.services.repo.GetRepoIssuesClient;
import com.alorma.github.sdk.services.repo.GetRepoReleasesClient;
import com.joanzapata.android.iconify.Iconify;

import java.util.concurrent.LinkedBlockingQueue;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Bernat on 17/07/2014.
 */
public class RepoDetailFragment extends Fragment implements BaseClient.OnResultCallback<Repo>,RepositoryInfo.OnRepoInfoListener {
    public static final String OWNER = "OWNER";
    public static final String REPO = "REPO";
    private static final int INFO_CONTRIBUTORS = 0;
    private static final int INFO_BRANCHES = 1;
    private static final int INFO_RELEASES = 2;
    private static final int INFO_ISSUES = 3;
    private RepositoryInfo infoFields;
    private String owner;
    private String repo;
    private LinkedBlockingQueue<BaseRepoClient> clients;
    private boolean reIntent = false;

    public static RepoDetailFragment newInstance(String owner, String repo) {
        Bundle bundle = new Bundle();
        bundle.putString(OWNER, owner);
        bundle.putString(REPO, repo);

        RepoDetailFragment f = new RepoDetailFragment();
        f.setArguments(bundle);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.repo_detail_fragment, null, false);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            owner = getArguments().getString(OWNER);
            repo = getArguments().getString(REPO);

            if (getActivity() != null && getActivity().getActionBar() != null) {
                getActivity().getActionBar().setTitle(owner + "/" + repo);
            }

            configureClients();

            executeNextClient();
        }

        infoFields = (RepositoryInfo) view.findViewById(R.id.repoInfoFields);
        infoFields.setOnRepoInfoListener(this);

    }

    @Override
    public void onResponseOk(Repo repo, Response r) {

        executeNextClient();

    }

    @Override
    public void onFail(RetrofitError error) {
        onError("REPO", error, false);
        if (!reIntent) {
            reIntent = true;
            configureClients();
            executeNextClient();
        } else {
            Toast.makeText(getActivity(), "error retrieving repository info, please contact app developer", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    private void configureClients(){
        clients = new LinkedBlockingQueue<BaseRepoClient>();

        GetRepoClient repoClient = new GetRepoClient(getActivity(), this.owner, this.repo);
        repoClient.setOnResultCallback(this);

        clients.add(repoClient);

        GetRepoContributorsClient repoContributorsClient = new GetRepoContributorsClient(getActivity(), this.owner, this.repo);
        repoContributorsClient.setOnResultCallback(new ContributorsCallback());

        clients.add(repoContributorsClient);

        GetRepoBranchesClient repoBranchesClient = new GetRepoBranchesClient(getActivity(), this.owner, this.repo);
        repoBranchesClient.setOnResultCallback(new BranchesCallback());

        clients.add(repoBranchesClient);

        GetRepoReleasesClient repoReleasesClient = new GetRepoReleasesClient(getActivity(), this.owner, this.repo);
        repoReleasesClient.setOnResultCallback(new ReleasesCallback());

        clients.add(repoReleasesClient);

        GetRepoIssuesClient repoIssuesClient = new GetRepoIssuesClient(getActivity(), this.owner, this.repo);
        repoIssuesClient.setOnResultCallback(new IssuesCallback());

        clients.add(repoIssuesClient);

    }

    private void executeNextClient() {
        if (clients != null) {
            BaseRepoClient poll = clients.poll();
            if (poll != null) {
                poll.execute();
            }
        }
    }

    @Override
    public void onRepoInfoFieldClick(int id, RepositoryInfoField view, RepositoryUiInfo info) {

    }

    private class ContributorsCallback implements BaseClient.OnResultCallback<ListContributors> {
        @Override
        public void onResponseOk(ListContributors contributors, Response r) {
            if (contributors != null) {
                Iconify.IconValue iconValue = Iconify.IconValue.fa_group;
                if (contributors.size() == 1) {
                    iconValue = Iconify.IconValue.fa_user;
                }
                infoFields.addRepoInfoField(new RepositoryUiInfo(INFO_CONTRIBUTORS, iconValue, contributors.size(), R.plurals.contributors));
            }
            executeNextClient();
        }

        @Override
        public void onFail(RetrofitError error) {
            onError("CONTRIBUTORS", error, true);
        }
    }

    private class BranchesCallback implements BaseClient.OnResultCallback<ListBranches> {
        @Override
        public void onResponseOk(ListBranches branches, Response r) {
            if (branches != null) {
                Iconify.IconValue iconValue = Iconify.IconValue.fa_code_fork;
                infoFields.addRepoInfoField(new RepositoryUiInfo(INFO_BRANCHES, iconValue, branches.size(), R.plurals.branches));
            }
            executeNextClient();
        }

        @Override
        public void onFail(RetrofitError error) {
            onError("BRANCHES", error, true);
        }
    }

    private class ReleasesCallback implements BaseClient.OnResultCallback<ListReleases> {
        @Override
        public void onResponseOk(ListReleases releases, Response r) {
            if (releases != null) {
                Iconify.IconValue iconValue = Iconify.IconValue.fa_download;
                infoFields.addRepoInfoField(new RepositoryUiInfo(INFO_RELEASES, iconValue, releases.size(), R.plurals.releases));
            }
            executeNextClient();
        }

        @Override
        public void onFail(RetrofitError error) {
            onError("RELEASES", error, true);
        }
    }

    private class IssuesCallback implements BaseClient.OnResultCallback<ListIssues> {
        @Override
        public void onResponseOk(ListIssues issues, Response r) {
            if (issues != null) {
                Iconify.IconValue iconValue = Iconify.IconValue.fa_info_circle;
                infoFields.addRepoInfoField(new RepositoryUiInfo(INFO_ISSUES, iconValue, issues.size(), R.plurals.issues));
            }
            executeNextClient();
        }

        @Override
        public void onFail(RetrofitError error) {
            onError("ISSUES", error, true);
        }
    }

    private void onError(String tag, RetrofitError error, boolean executeNext) {
        Log.e(tag, "Error", error);
        if (BuildConfig.DEBUG) {
            Toast.makeText(getActivity(), "Error: " + tag, Toast.LENGTH_SHORT).show();
        }
        if (executeNext) {
            executeNextClient();
        }
    }
}
