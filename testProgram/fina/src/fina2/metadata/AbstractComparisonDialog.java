/*
 * AbstractComparisonDialog.java
 *
 * Created on 14 ������ 2002 �., 22:32
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
