package com.example.xyzreader.ui;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.textrecyclerview.TextAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ImageView mPhotoView;
    private boolean fullBleed;
    private boolean visible;

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
            Log.d("AYMAN", "It is now visible");
            animate();
        }
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
    private boolean changeStatusColor;
    private boolean hasColor;
    private String[] lines;
    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;
    private FloatingActionButton mFab;
    private TextAdapter mTextAdapter;
    private int animate;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        mRecyclerView.clearOnScrollListeners();
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail_new, container, false);
        mPhotoView = mRootView.findViewById(R.id.photo);
        mFab = mRootView.findViewById(R.id.share_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        bindViews();
        mRootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                mRootView.getViewTreeObserver().removeOnPreDrawListener(this);
                Log.d("AYMAN", "Layout is ready!");
                animate();
                return true;
            }
        });
        return mRootView;
    }

    private void updateStatusBar() {
        if (changeStatusColor && hasColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivityCast().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(mMutedColor);
            changeStatusColor = false;
        }
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }
        fullBleed = mRootView.findViewById(R.id.full_bleed_layout) != null;
        if (!fullBleed) {
            mAppBarLayout = mRootView.findViewById(R.id.appbar);
            mToolbar = mRootView.findViewById(R.id.toolbar);
            AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
            if (appCompatActivity != null) {
                appCompatActivity.setSupportActionBar(mToolbar);
            }
        }
        mRecyclerView = mRootView.findViewById(R.id.text_block);

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            Date publishedDate = parsePublishedDate();
            Spanned by;
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                by = Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>");
            } else {
                // If date is before 1902, just show the string
                by = Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>");

            }
            lines = mCursor.getString(ArticleLoader.Query.BODY).split("\r\n\r\n");
            for (int i = 0; i < lines.length; i++) {
                lines[i] = lines[i].replace("\r\n", " ");
            }
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                mPhotoView.setImageBitmap(bitmap);
                                Log.d("AYMAN", "Image loaded successfully");
                                animate();
                                createPaletteAsync(bitmap);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
            if (fullBleed) {
                mTextAdapter = new TextAdapter(getContext(), title, by);
            } else {
                mTextAdapter = new TextAdapter(getContext());
                TextView titleView = mRootView.findViewById(R.id.article_title);
                TextView bylineView = mRootView.findViewById(R.id.article_byline);
                bylineView.setMovementMethod(new LinkMovementMethod());
                titleView.setText(title);
                bylineView.setText(by);
            }
            mTextAdapter.setData(lines);
            mRecyclerView.setAdapter(mTextAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            mRootView.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        if (cursor != null && !cursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            cursor.close();
        } else {
            mCursor = cursor;
            bindViews();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    public void createPaletteAsync(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                hasColor = true;
                mMutedColor = p.getDarkMutedColor(0xFF333333);
                if (fullBleed) {
                    mTextAdapter.setColor(mMutedColor);
                } else {
                    mRootView.findViewById(R.id.meta_bar).setBackgroundColor(mMutedColor);
                }
                setImageViewPadding();
                updateStatusBar();
            }
        });
    }

    public void setChangeStatusColor(boolean state) {
        changeStatusColor = state;
        updateStatusBar();
    }

    public void setImageViewPadding() {
        if (fullBleed)
            return;
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(mPhotoView.getLayoutParams());
        int height = mToolbar.getMeasuredHeight();
        marginParams.setMargins(0, 0, 0, height);
        CollapsingToolbarLayout.LayoutParams layoutParams = new CollapsingToolbarLayout.LayoutParams(marginParams);
        layoutParams.setCollapseMode(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX);
        mPhotoView.setLayoutParams(layoutParams);
    }

    public void animate() {
        animate++;
        Log.d("AYMAN", "animate = " + animate);
        if (animate != 3) {
            return;
        }
        if (fullBleed)
            return;
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior != null) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt();
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    behavior.setTopAndBottomOffset((Integer) animation.getAnimatedValue());
                    mAppBarLayout.requestLayout();
                }
            });
            valueAnimator.setIntValues(behavior.getTopAndBottomOffset(), -(int) (mPhotoView.getHeight() * 0.5f));
            valueAnimator.setDuration(300);
            valueAnimator.start();
            animate = 2;
        }
    }

}
