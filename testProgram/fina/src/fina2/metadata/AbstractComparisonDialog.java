/*
 * AbstractComparisonDialog.java
 *
 * Created on 14 Август 2002 г., 22:32
 */

package fina2.metadata;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public interface AbstractComparisonDialog {

    public void createComparison(String condition, String equation);

    public void updateComparison(String condition, String equation);

}
