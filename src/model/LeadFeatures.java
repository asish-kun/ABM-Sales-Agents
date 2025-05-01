// model/LeadFeatures.java  (NEW)
package model;

/**
 * Central index map for the 8 NN input features and the single output.
 * KEEP THIS CLASS IN SYNC with your training spreadsheet header.
 */
public final class LeadFeatures {
    private LeadFeatures() {}            // utility class – no instances

    public static final int WEEKS_ELAPSED         = 0;
    public static final int WEEKS_DIFF_PRIOR      = 1;
    public static final int ENTRY_BY_WEEK         = 2;
    public static final int AMOUNT                = 3;
    public static final int SECTOR                = 4;
    public static final int MKT_GEN               = 5;
    public static final int PRODUCT_SERVICE       = 6;
    // slot 7 is *reserved* for anything new you might add later
//    public static final int INPUT_COUNT           = 7;

    /** single-row softmax label index */
    public static final int LABEL_CONV_PROB_WEEK  = 0;
}
