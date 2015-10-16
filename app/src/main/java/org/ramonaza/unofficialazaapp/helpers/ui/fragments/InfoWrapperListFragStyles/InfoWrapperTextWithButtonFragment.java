package org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles;

import android.app.Fragment;
import android.widget.ArrayAdapter;

import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragment;
import org.ramonaza.unofficialazaapp.helpers.ui.other.InfoWrapperTextWithButtonAdapter;
import org.ramonazaapi.interfaces.InfoWrapper;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class InfoWrapperTextWithButtonFragment extends InfoWrapperListFragment {


    public InfoWrapperTextWithButtonFragment() {
        // Required empty public constructor
    }

    @Override
    public ArrayAdapter getAdapter() {
        return new InfoWrapperTextWithButtonAdapter(getActivity()) {
            @Override
            public String getButtonText() {
                return buttonName();
            }

            @Override
            public void onButton(InfoWrapper info) {
                onButtonClick(info);
            }

            @Override
            public void onText(InfoWrapper info) {
                onTextClick(info);
            }
        };
    }


    /**
     * Method to get each button name.
     *
     * @return the name of the button.
     */
    public abstract String buttonName();

    /**
     * The action each button performs.
     *
     * @param mWrapper the button's InfoWrapper; can be cast as necessary
     */
    public abstract void onButtonClick(InfoWrapper mWrapper);


    /**
     * The action pressing on each entry's name performs.
     *
     * @param mWrapper the button's InfoWrapper; can be cast as necessary
     */
    public abstract void onTextClick(InfoWrapper mWrapper);


}
