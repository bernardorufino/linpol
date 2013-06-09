package br.com.bernardorufino.linpol;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import br.com.bernardorufino.linpol.model.InterpolationSet;

public class MainActivity extends Activity {
	static final String INTERPOLATION_SET_KEY = "br.com.bernardorufino.linpol.InterpolationSet"; 
	static final String CONTROL_PROPERTY_KEY = "br.com.bernardorufino.linpol.ControlProperty";
	
	private InterpolationSet set;
	private TableLayout table;
	private View root;
	private LayoutInflater inflater;
	
	private int controlProperty;
	private boolean chooseControlPropertyMode;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        if (state == null) {
            set = new InterpolationSet();
        } else {
            set = new InterpolationSet(state.getSerializable(INTERPOLATION_SET_KEY));
            controlProperty = state.getInt(CONTROL_PROPERTY_KEY);
        }
        table = (TableLayout) findViewById(R.id.table);
        root = table.getRootView();
        inflater = getLayoutInflater();
        
        if (state != null) {
            drawSetRows();
    		highlightSelectedRow();
        }
        
        ((TextView) findViewById(R.id.add_property)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int i = set.addProperty();
				addPropertyRow(i);
				setRowDefaults(i);
			}
		});
        ((Button) findViewById(R.id.interpolate_button)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				 try {interpolate();} catch (Exception e) {}
			}
		});
        ((Button) findViewById(R.id.choose_control_button)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				toggleActionButtons();
			}
		});
        
    }
    
    public void onSaveInstanceState(Bundle state) {
    	state.putSerializable(INTERPOLATION_SET_KEY, set.toSerializable());
    	state.putInt(CONTROL_PROPERTY_KEY, controlProperty);
    	super.onSaveInstanceState(state);
    }
    
    public voidrawSetRows() {
		for (int i = 0; i < set.size(); i++) {
			addPropertyRow(i);		    		
		}	
		setRowsDefaults();
    }
    
    public void setRowsDefaults() {
    	// See http://stackoverflow.com/questions/7274553
    	(new Handler()).postDelayed(new Runnable() {
			public void run() {
				for (int i = 0; i < set.size(); i++) {
					setRowDefaults(i);
				}
			}
    	}, 100);
	}	
    
    
    public void setRowDefaults(int i) {
    	TableRow row = getPropertyRow(i);
    	((EditText) row.findViewById(R.id.left_value)).setText(String.valueOf(set.getLeftValue(i)));
    	((EditText) row.findViewById(R.id.interpol_value)).setText(String.valueOf(set.getValue(i)));
    	((EditText) row.findViewById(R.id.right_value)).setText(String.valueOf(set.getRightValue(i)));
    }
    
    private void addPropertyRow(int i) {
    	// Unify leaving focus and pressing ok! 
    	TableRow row = (TableRow) inflater.inflate(R.layout.row, null); 
    	table.addView(row, table.getChildCount() - 1);
    	if (i == 0 && set.size() == 1) {
    		setControlPropertyRow(row);
    	}
    	setRowListeners(row);
    }
    
    
    private void setValue(int n, TextView v) {
    	int i = propertyIndex((TableRow) v.getParent());
    	float value;
    	try {
        	value = Float.parseFloat(v.getText().toString());
		} catch (Exception e) {return;}
    	if (n == -1) { // Coefficient!
    		set.setControlValue(i, value);
    	} else {
    		set.setValue(i, n, value);
    	}
    }
    
    private void toggleActionButtons () {
    	for (int i = 0; i < set.size(); i++) {
    		TableRow row = getPropertyRow(i);
    		toggleVisibility(row.findViewById(R.id.control));
    		toggleVisibility(row.findViewById(R.id.delete));
    	}
    	
    }
    
    private void highlightSelectedRow() {
    	for (int i = 0; i < set.size(); i++) {
    		TextView propertyLabel = (TextView) getPropertyRow(i).findViewById(R.id.property);
    		propertyLabel.setBackgroundColor(getResources().getColor(
    			(i == controlProperty) ? R.color.blue : R.color.dark
    		));
    	}
    }
    
    private void interpolate() {
    	setControlValue();
    	((TextView) findViewById(R.id.coefficient)).setText(String.valueOf(set.getCoefficient()));
    	for (int i = 0; i < set.size(); i++) {
    		EditText interpolValue = (EditText) getPropertyRow(i).findViewById(R.id.interpol_value);
    		interpolValue.setText(String.valueOf(set.getValue(i)));
    	}
    }
    
    private void setControlValue() {
    	EditText interpolValue = (EditText) getPropertyRow(controlProperty).findViewById(R.id.interpol_value);
    	set.setControlValue(controlProperty, Float.parseFloat(interpolValue.getText().toString()));    	
    }
    
    private int propertyIndex(View row) {
    	// - 1 because the first is the coefficient row
    	return table.indexOfChild((TableRow) row) - 1;
    }
    
    private TableRow getPropertyRow(int i) {
    	return (TableRow) table.getChildAt(i + 1);
    }
    
    private void setControlPropertyRow(TableRow row) {
		controlProperty = propertyIndex(row);
		highlightSelectedRow();
    }
    
    private void setRowListeners(TableRow row) {
    	((TextView) row.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				TableRow row = (TableRow) v.getParent();
				int i = propertyIndex(row);
				set.removeProperty(i);
				table.removeView(row);	
				if (i == controlProperty && set.size() > 0) {
					setControlPropertyRow(getPropertyRow(0));
				}
				root.invalidate();
			}
		});

    	((TextView) row.findViewById(R.id.control)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setControlPropertyRow((TableRow) v.getParent());
				toggleActionButtons();
			}
		});
    	
    	
    	
    	// Left Value
    	EditText leftView = (EditText) row.findViewById(R.id.left_value);
    	leftView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				setValue(0, v);
				return true;
			}
		});
    	leftView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) return;
				setValue(0, (TextView) v);						
			}
		});
    	// Right Value
    	EditText rightView = (EditText) row.findViewById(R.id.right_value);
    	rightView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				setValue(1, v);
				return true;
			}
		});
    	rightView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) return;
				setValue(1, (TextView) v);						
			}
		});
    }
    
    private void toggleVisibility(View v) {
    	v.setVisibility((v.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private void alert(String message) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Linpol").setMessage(message).setNeutralButton("Ok", null).show();
    }
    
}
