package seaplus.seefood.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.appyvet.rangebar.RangeBar;

import java.util.ArrayList;

import seaplus.seefood.R;

import static seaplus.seefood.R.id.FilterSortBtn;

public class FilterSortActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayAdapter<CharSequence> sortAdapter;
    private Spinner sortDropdownlist;

    private Button priceRangeStartBtn;
    private Button priceRangeEndBtn;
    private Button ratingRangeStartBtn;
    private Button ratingRangeEndBtn;

    private EditText ratingRangeStartValue;
    private EditText ratingRangeEndValue;
    private RangeBar priceRangeBarObj;
    private RangeBar ratingRangeBarObj;

    private ArrayList<Object> filterSortList = new ArrayList<Object>();

    Button filterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_sort);

        // my_child_toolbar is defined in the layout file
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("Filter & Sort");
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        //Assign sort array to sort dropdownlist
        sortDropdownlist = (Spinner) findViewById(R.id.sortSpinner);
        sortAdapter = ArrayAdapter.createFromResource(this, R.array.sort_array, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortDropdownlist.setAdapter(sortAdapter);

        //Dynamically change range values

        //Instantiate obj
        priceRangeStartBtn = (Button) findViewById(R.id.priceRangeStart);
        priceRangeEndBtn = (Button) findViewById(R.id.priceRangeEnd);
        ratingRangeStartBtn = (Button) findViewById(R.id.ratingRangeStart);
        ratingRangeEndBtn = (Button) findViewById(R.id.ratingRangeEnd);

        priceRangeBarObj = (RangeBar) findViewById(R.id.priceRangeBar);
        ratingRangeBarObj = (RangeBar) findViewById(R.id.ratingRangeBar);

        //set display values
        priceRangeBarObj.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {

                priceRangeStartBtn.setText(setDollarFormat(leftPinIndex));
                priceRangeEndBtn.setText(setDollarFormat(rightPinIndex));
            }
        });

        ratingRangeBarObj.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {

                ratingRangeStartBtn.setText("" + ((float)leftPinIndex) / 2);
                ratingRangeEndBtn.setText("" + ((float)rightPinIndex) / 2);
            }
        });

        // Filter Button
        filterBtn = (Button) this.findViewById(FilterSortBtn);
        filterBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String startPrice = priceRangeStartBtn.getText().toString();
        String endPrice = priceRangeEndBtn.getText().toString();
        double startRating = Double.parseDouble(ratingRangeStartBtn.getText().toString());
        double endRating = Double.parseDouble(ratingRangeEndBtn.getText().toString());
        String sortValue = sortDropdownlist.getSelectedItem().toString();

        filterSortList.add(startPrice);
        filterSortList.add(endPrice);
        filterSortList.add(startRating);
        filterSortList.add(endRating);
        filterSortList.add(sortValue);

        Bundle extra = new Bundle();
        extra.putSerializable("FilterSortList", filterSortList);

        Intent mainNavigationPage = new Intent(this, MainNavigationActivity.class);
        mainNavigationPage.putExtra("extra", extra);
        mainNavigationPage.putExtra("CLocationObj", getIntent().getParcelableExtra("CLocationObj"));
        startActivity(mainNavigationPage);
    }

    //Note: pin index starts from 0
    public String setDollarFormat(int pinIndex){
        String newPinValue = "";

        switch (pinIndex)
        {
            case 0: newPinValue = "$";
                break;
            case 1: newPinValue = "$$";
                break;
            case 2: newPinValue = "$$$";
                break;
            case 3: newPinValue = "$$$$";
                break;
            case 4: newPinValue = "$$$$$";
                break;
        }
        return newPinValue;
    }
}