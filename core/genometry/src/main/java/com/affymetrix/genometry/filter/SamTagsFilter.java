package com.affymetrix.genometry.filter;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.general.BoundedParameter;
import com.affymetrix.genometry.general.Parameter;
import com.affymetrix.genometry.operator.comparator.MathComparisonOperator;
import com.affymetrix.genometry.operator.comparator.NotEqualMathComparisonOperator;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SamTagsFilter extends SymmetryFilter{
    private final static List<MathComparisonOperator> COMPARATOR_VALUES = new LinkedList<>();
    private final static String TAG = "tag";
    private final static String TAG_VALUE = "value";
    private final static List<String> TAG_VALUES = new LinkedList<>();
    private final static String COMPARATOR = "comparator";
    public final static String DEFAULT_PROPERTY_VALUE = "";
    private Parameter<MathComparisonOperator> comparator = new BoundedParameter<>(COMPARATOR_VALUES);
    protected Parameter<String> tags = new BoundedParameter<>(TAG_VALUES);
    static {
        COMPARATOR_VALUES.add(new com.affymetrix.genometry.operator.comparator.EqualMathComparisonOperator());
        COMPARATOR_VALUES.add(new com.affymetrix.genometry.operator.comparator.NotEqualMathComparisonOperator());
    }
    static {
        TAG_VALUES.add("CR");
        TAG_VALUES.add("CB");
    }
    private Object given_property_value;
    protected Parameter<String> tag = new BoundedParameter<>(TAG_VALUES);
    protected Parameter<String> tag_value = new Parameter<String>(DEFAULT_PROPERTY_VALUE) {
        @Override
        public boolean set(Object e) {
            if (e != null) {
                try {
                    given_property_value = e;
                } catch (Exception ex) {
                }
                return super.set(e.toString().toLowerCase());
            }
            return super.set(e);
        }
    };

    public SamTagsFilter() {
        parameters.addParameter(TAG, String.class, tags);
        parameters.addParameter(TAG_VALUE, String.class, tag_value);
        parameters.addParameter(COMPARATOR,MathComparisonOperator.class,comparator);
    }

    @Override
    public boolean filterSymmetry(BioSeq seq, SeqSymmetry sym) {
        if(sym instanceof SymWithProps symWithProps){
            Object value = symWithProps.getProperty(tag.get());
            if(value==null){
                return false;
            } else if (value instanceof String str) {
                List<String> value_List = Arrays.asList(((String) given_property_value).split(";"));
                boolean bool = value_List.contains((String)value);
                if(comparator.get() instanceof NotEqualMathComparisonOperator){
                    return !bool;
                }
                return bool;
            }
        }
        return false;
    }

    @Override
    public String getName() {
       return "samtags";
    }
}
