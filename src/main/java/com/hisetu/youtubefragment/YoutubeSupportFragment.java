package com.hisetu.youtubefragment;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public final class YoutubeSupportFragment extends YouTubePlayerSupportFragment
        implements YouTubePlayer.OnInitializedListener {

    public static final String EXTRA_VIDEO_ID = "extra_video_id";
    private YouTubePlayer player;
    private String videoId;
    private View playerControllerBackground;
    private ViewGroup playerControllerContainer;
    private YouTubePlayer.OnFullscreenListener onFullscreenListener;

    public static void initialize(String developKey) {
        YoutubeSupportFragment.developKey = developKey;
    }

    private static String developKey;

    public static YoutubeSupportFragment newInstance() {
        return new YoutubeSupportFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize(developKey, this);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        YouTubePlayerView youtubeView = (YouTubePlayerView) super.onCreateView(layoutInflater, viewGroup, bundle);

        ViewGroup layout = (ViewGroup) layoutInflater.inflate(R.layout.player_controller, viewGroup, false);
        layout.addView(youtubeView, 0, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        setUpPlayerView(layout);

        return layout;
    }

    private void setUpPlayerView(View layout) {
        playerControllerBackground = layout.findViewById(R.id.player_controller_background);
        playerControllerBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerControllerContainer.getChildCount() > 0)
                    return;
                if (player.isPlaying())
                    player.pause();
                else
                    player.play();
            }
        });
        playerControllerContainer = (ViewGroup) layout.findViewById(R.id.player_controller_container);
    }

    @Override
    public void onResume() {
        if (player != null) {
            player.play();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (player != null) {
            player.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    public void setVideoId(String videoId) {
        if (videoId != null && !videoId.equals(this.videoId)) {
            this.videoId = videoId;
            if (player != null) {
                player.cueVideo(videoId);
            }
        }
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        if (args == null)
            return;
        String videoId = args.getString(EXTRA_VIDEO_ID);
        if (videoId != null)
            setVideoId(videoId);
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean restored) {
        this.player = player;
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        player.setOnFullscreenListener(onFullscreenListener);
        if (!restored && videoId != null) {
            player.cueVideo(videoId);
        }

        player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
        player.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {
                playerControllerBackground.setVisibility(View.INVISIBLE);
                playerControllerContainer.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPaused() {
                playerControllerBackground.setVisibility(View.VISIBLE);
                playerControllerContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopped() {
                playerControllerBackground.setVisibility(View.VISIBLE);
                playerControllerContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBuffering(boolean b) {

            }

            @Override
            public void onSeekTo(int i) {

            }
        });
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        this.player = null;
    }

    public void setControllerBackground(@ColorRes int colorRes) {
        playerControllerBackground.setBackgroundColor(getResources().getColor(colorRes));
    }

    public void addItemView(TextView itemView) {
        playerControllerContainer.addView(itemView);
    }

    public void addItem(@DrawableRes int icon, String text, @Nullable final OnItemClickListener clickListener) {
        TextView textView = new TextView(getActivity());
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener == null)
                    return;
                clickListener.onClick(view, player);
            }
        });

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.topMargin = (int) getPxFromDp(10, displayMetrics);
        textView.setLayoutParams(params);

        textView.setCompoundDrawablesWithIntrinsicBounds(
                ResourcesCompat.getDrawable(getResources(), icon, null), null, null, null);
        textView.setCompoundDrawablePadding((int) getPxFromDp(5, displayMetrics));
        textView.setText(text);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        textView.setTextSize((int) getPxFromDp(9, displayMetrics));

        playerControllerContainer.addView(textView);
    }

    public void setOnFullscreenListener(YouTubePlayer.OnFullscreenListener onFullscreenListener) {
        this.onFullscreenListener = onFullscreenListener;
    }

    private float getPxFromDp(int value, DisplayMetrics displayMetrics) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics);
    }

    public interface OnItemClickListener {
        void onClick(View view, YouTubePlayer player);
    }
}
