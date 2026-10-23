package com.frozendevs.periodictable.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.SharedElementCallback;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frozendevs.periodictable.PeriodicTableApplication;
import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.fragment.IsotopesFragment;
import com.frozendevs.periodictable.fragment.PropertiesFragment;
import com.frozendevs.periodictable.content.Database;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.adapter.PropertiesAdapter;
import com.frozendevs.periodictable.model.adapter.TableAdapter;
import com.frozendevs.periodictable.model.adapter.ViewPagerAdapter;
import com.frozendevs.periodictable.view.RecyclerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class PropertiesActivity extends AppCompatActivity {

    public static final String EXTRA_ATOMIC_NUMBER = "com.frozendevs.periodictable.AtomicNumber";

    public static final String ARGUMENT_PROPERTIES = "properties";

    private static final String STATE_ELEMENT_PROPERTIES = "elementProperties";

    private ElementProperties mElementProperties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.properties_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final PeriodicTableApplication application = (PeriodicTableApplication) getApplication();

            final SharedElementCallback callback = application.getSharedElementCallback();

            if (callback != null) {
                setEnterSharedElementCallback(callback);
            }

            /*
             * Work around shared view alpha state not being restored on exit transition finished.
             */
            final View.OnAttachStateChangeListener listener =
                    application.getOnAttachStateChangeListener();

            if (listener != null) {
                getWindow().getDecorView().addOnAttachStateChangeListener(listener);
            }
        }

        if (savedInstanceState == null || (mElementProperties = savedInstanceState.getParcelable(
                STATE_ELEMENT_PROPERTIES)) == null) {
            mElementProperties = Database.getElement(this, ElementProperties.class,
                    getIntent().getIntExtra(EXTRA_ATOMIC_NUMBER, 1));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // The element name is already shown inside the tile, so keep the collapsing
        // toolbar title hidden (it otherwise draws large text over the tile).
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGUMENT_PROPERTIES, mElementProperties);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this);
        pagerAdapter.addPage(R.string.fragment_title_properties, PropertiesFragment.class, bundle);
        pagerAdapter.addPage(R.string.fragment_title_isotopes, IsotopesFragment.class, bundle);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf");

        TableAdapter tableAdapter = new TableAdapter();

        View tileView = findViewById(R.id.tile_view);
        tableAdapter.getView(mElementProperties, tileView, (ViewGroup) tileView.getParent());
        tileView.setClickable(false);

        TextView configuration = (TextView) findViewById(R.id.element_electron_configuration);
        configuration.setText(PropertiesAdapter.formatProperty(this,
                mElementProperties.getElectronConfiguration()));
        configuration.setTypeface(typeface);

        TextView shells = (TextView) findViewById(R.id.element_electrons_per_shell);
        shells.setText(PropertiesAdapter.formatProperty(this,
                mElementProperties.getElectronsPerShell()));
        shells.setTypeface(typeface);

        TextView electronegativity = (TextView) findViewById(R.id.element_electronegativity);
        electronegativity.setText(PropertiesAdapter.formatProperty(this,
                mElementProperties.getElectronegativity()));
        electronegativity.setTypeface(typeface);

        TextView oxidationStates = (TextView) findViewById(R.id.element_oxidation_states);
        oxidationStates.setText(PropertiesAdapter.formatProperty(this,
                mElementProperties.getOxidationStates()));
        oxidationStates.setTypeface(typeface);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.properties_action_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_wiki) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    mElementProperties.getWikipediaLink())));
            return true;
        } else if (itemId == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.properties_context_menu, menu);
        menu.setHeaderTitle(R.string.context_title_options);

        super.onCreateContextMenu(menu, view, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String propertyName, propertyValue;

        View view = ((RecyclerView.RecyclerContextMenuInfo) item.getMenuInfo()).targetView;

        TextView symbol = (TextView) view.findViewById(R.id.property_symbol);

        if (symbol != null) {
            propertyName = getString(R.string.property_symbol);
            propertyValue = (String) symbol.getText();
        } else {
            propertyName = (String) ((TextView) view.findViewById(R.id.property_name)).getText();
            propertyValue = (String) ((TextView) view.findViewById(R.id.property_value)).getText();
        }

        if (item.getItemId() == R.id.context_copy) {
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).
                    setPrimaryClip(ClipData.newPlainText(propertyName, propertyValue));
            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_ELEMENT_PROPERTIES, mElementProperties);
    }
}
