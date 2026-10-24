package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.model.DecayTarget;
import com.frozendevs.periodictable.model.Isotope;
import com.frozendevs.periodictable.view.ExpandableIndicatorView;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

public class IsotopesAdapter extends AbstractExpandableItemAdapter<IsotopesAdapter.GroupViewHolder,
        IsotopesAdapter.ChildViewHolder> {

    private Typeface mTypeface;
    private int mLinkColor;
    private IsotopeProperties[] mProperties = new IsotopeProperties[0];

    private class Property {
        String mName = "", mValue = "", mValueRaw = "";
        List<DecayTarget> mTargets;

        Property(Context context, String value) {
            if (value != null) {
                mValueRaw = value;
                if (!value.equals("")) mValue = value;
                else mValue = context.getString(R.string.property_value_unknown);
            }
        }

        Property(Context context, int name, String value) {
            this(context, value);

            mName = context.getString(name);
        }

        Property(Context context, int name, String value, int noneValue) {
            this(context, name, value);

            if (value != null && value.equals("")) {
                mValue = context.getString(noneValue);
            }
        }

        Property(Context context, int name, String value, int noneValue, int specialValue) {
            this(context, name, value, noneValue);

            if (value != null && value.equals("-")) {
                mValue = context.getString(specialValue);
            }
        }

        String getName() {
            return mName;
        }

        String getValue() {
            return mValue;
        }

        String getValueRaw() {
            return mValueRaw;
        }

        void setTargets(List<DecayTarget> targets) {
            mTargets = targets;
        }

        List<DecayTarget> getTargets() {
            return mTargets;
        }
    }

    private class IsotopeProperties {

        Property symbol, halfLife, abundance;
        private final List<Property> children = new ArrayList<>();

        public IsotopeProperties(Context context, Isotope isotope) {
            symbol = new Property(context, isotope.getSymbol());
            halfLife = new Property(context, R.string.property_half_life, isotope.getHalfLife(),
                    R.string.property_value_unknown, R.string.property_value_stable);
            abundance = new Property(context, R.string.property_abundance, isotope.getAbundance(),
                    R.string.property_value_none, R.string.property_value_trace);
            Property spin = new Property(context, R.string.property_spin, isotope.getSpin(),
                    R.string.property_value_unknown);
            Property decayModes = new Property(context,
                    R.string.property_decay_modes, isotope.getHalfLife().equals("-") ?
                    context.getString(R.string.property_value_none) : isotope.getDecayModes());
            decayModes.setTargets(DecayTarget.parse(isotope.getDecayModes()));

            // The original four rows are always shown (half-life, decay modes,
            // spin, abundance). The IAEA-sourced rows below are added only when
            // present, so isomers and unmeasured nuclides don't show empty rows.
            children.add(halfLife);
            children.add(decayModes);
            children.add(spin);
            children.add(abundance);
            addIfPresent(context, R.string.property_atomic_mass, isotope.getAtomicMass());
            addIfPresent(context, R.string.property_mass_excess, isotope.getMassExcess());
            addIfPresent(context, R.string.property_binding_energy, isotope.getBindingEnergy());
            addIfPresent(context, R.string.property_decay_energy, isotope.getDecayEnergy());
            addIfPresent(context, R.string.property_magnetic_moment, isotope.getMagneticMoment());
            addIfPresent(context, R.string.property_discovery, isotope.getDiscovery());
        }

        private void addIfPresent(Context context, int name, String value) {
            if (value != null && !value.equals("")) {
                children.add(new Property(context, name, value));
            }
        }

        public Property getSymbol() {
            return symbol;
        }

        public Property getHalfLife() {
            return halfLife;
        }

        public Property getAbundance() {
            return abundance;
        }

        public List<Property> getChildren() {
            return children;
        }
    }

    public class GroupViewHolder extends AbstractExpandableItemViewHolder implements
            View.OnClickListener, View.OnCreateContextMenuListener {
        private TextView mSymbol, mHalfLife, mAbundance;
        private ExpandableIndicatorView mIndicator;

        public GroupViewHolder(View view) {
            super(view);

            mIndicator = (ExpandableIndicatorView) view.findViewById(R.id.group_indicator);
            mSymbol = (TextView) view.findViewById(R.id.property_symbol);
            mHalfLife = (TextView) view.findViewById(R.id.property_half_life);
            mAbundance = (TextView) view.findViewById(R.id.property_abundance);

            view.setOnClickListener(this);
            view.setOnCreateContextMenuListener(this);
        }

        public void setIndicatorState(boolean expanded) {
            mIndicator.setStateExpanded(expanded);
        }

        public void setSymbol(String symbol) {
            mSymbol.setText(symbol);
        }

        public void setHalfLife(String halfLife) {
            mHalfLife.setText(halfLife);
        }

        public void setAbundance(String abundance) {
            mAbundance.setText(abundance);
        }

        public void setTypeface(Typeface typeface) {
            mSymbol.setTypeface(typeface);
            mHalfLife.setTypeface(typeface);
            mAbundance.setTypeface(typeface);
        }

        @Override
        public void onClick(View view) {
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view,
                                        ContextMenu.ContextMenuInfo menuInfo) {
        }
    }

    public class ChildViewHolder extends AbstractExpandableItemViewHolder implements
            View.OnClickListener, View.OnCreateContextMenuListener {
        private TextView mName, mValue;
        private final ColorStateList mDefaultValueColors;
        private List<DecayTarget> mTargets;

        public ChildViewHolder(View view) {
            super(view);

            mName = (TextView) view.findViewById(R.id.property_name);
            mValue = (TextView) view.findViewById(R.id.property_value);
            mDefaultValueColors = mValue.getTextColors();

            view.setOnClickListener(this);
            view.setOnCreateContextMenuListener(this);
        }

        public void setName(String name) {
            mName.setText(name);
        }

        public void setValue(String value) {
            mValue.setText(value);
        }

        // Highlight the decay-modes value when it links to a daughter nuclide;
        // reset to the normal colour for every other (recycled) row.
        public void setTargets(List<DecayTarget> targets) {
            mTargets = targets;

            if (targets != null && !targets.isEmpty()) {
                mValue.setTextColor(mLinkColor);
            } else {
                mValue.setTextColor(mDefaultValueColors);
            }
        }

        public void setTypeface(Typeface typeface) {
            mName.setTypeface(typeface);
            mValue.setTypeface(typeface);
        }

        @Override
        public void onClick(View view) {
            if (mTargets == null || mTargets.isEmpty()) {
                return;
            }

            if (mTargets.size() == 1) {
                navigate(view.getContext(), mTargets.get(0));
            } else {
                final Context context = view.getContext();
                final List<DecayTarget> targets = mTargets;

                CharSequence[] labels = new CharSequence[targets.size()];
                for (int i = 0; i < targets.size(); i++) {
                    labels[i] = targets.get(i).label;
                }

                new AlertDialog.Builder(context)
                        .setTitle(R.string.decay_choose_path)
                        .setItems(labels, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                navigate(context, targets.get(which));
                            }
                        })
                        .show();
            }
        }

        private void navigate(Context context, DecayTarget target) {
            Intent intent = new Intent(context, PropertiesActivity.class);
            intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER, target.atomicNumber);
            intent.putExtra(PropertiesActivity.EXTRA_FOCUS_ISOTOPE, target.isotopeSymbol);
            context.startActivity(intent);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view,
                                        ContextMenu.ContextMenuInfo menuInfo) {
        }
    }

    public IsotopesAdapter(Context context, Isotope[] isotopes) {
        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");

        TypedValue accent = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, accent, true);
        mLinkColor = accent.resourceId != 0 ?
                ContextCompat.getColor(context, accent.resourceId) : accent.data;

        if (isotopes != null) {
            mProperties = new IsotopeProperties[isotopes.length];

            for (int i = 0; i < isotopes.length; i++) {
                mProperties[i] = new IsotopeProperties(context, isotopes[i]);
            }
        }

        setHasStableIds(true);
    }

    @Override
    public int getGroupCount() {
        return mProperties.length;
    }

    @Override
    public int getChildCount(int groupPosition) {
        return mProperties[groupPosition].getChildren().size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        // advrecyclerview packs group/child ids into a limited bit range, so a full
        // 32-bit hashCode overflows it. Positions are stable (data never reorders).
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        return 0;
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup viewGroup, int viewType) {
        GroupViewHolder viewHolder = new GroupViewHolder(LayoutInflater.from(viewGroup.
                getContext()).inflate(R.layout.isotope_list_item, viewGroup, false));

        viewHolder.setTypeface(mTypeface);

        return viewHolder;
    }

    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup, int viewType) {
        ChildViewHolder viewHolder = new ChildViewHolder(LayoutInflater.from(viewGroup.
                getContext()).inflate(R.layout.properties_list_item, viewGroup, false));

        viewHolder.setTypeface(mTypeface);

        return viewHolder;
    }

    @Override
    public void onBindGroupViewHolder(GroupViewHolder groupViewHolder, int groupPosition,
                                      int viewType) {
        IsotopeProperties properties = mProperties[groupPosition];

        groupViewHolder.setSymbol(properties.getSymbol().getValue());
        groupViewHolder.setHalfLife("");
        groupViewHolder.setAbundance("");
        if (!properties.getHalfLife().getValueRaw().equals("")) {
            groupViewHolder.setHalfLife(properties.getHalfLife().getValue());

            if (!properties.getAbundance().getValueRaw().equals("")) {
                groupViewHolder.setAbundance(properties.getAbundance().getValue());
            }
        }

        final int expandState = groupViewHolder.getExpandStateFlags();

        if ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_UPDATED) != 0) {
            groupViewHolder.setIndicatorState((expandState & RecyclerViewExpandableItemManager.
                    STATE_FLAG_IS_EXPANDED) != 0);
        }
    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder childViewHolder, int groupPosition,
                                      int childPosition, int viewType) {
        Property property = getChild(groupPosition, childPosition);

        childViewHolder.setName(property.getName());
        childViewHolder.setValue(property.getValue());
        childViewHolder.setTargets(property.getTargets());
    }

    /** Group position of the isotope with the given symbol, or -1 if absent. */
    public int positionOf(String isotopeSymbol) {
        for (int i = 0; i < mProperties.length; i++) {
            if (mProperties[i].getSymbol().getValue().equals(isotopeSymbol)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(GroupViewHolder groupViewHolder,
                                                   int groupPosition, int x, int y,
                                                   boolean expand) {
        return true;
    }

    private Property getChild(int groupPosition, int childPosition) {
        return mProperties[groupPosition].getChildren().get(childPosition);
    }
}
